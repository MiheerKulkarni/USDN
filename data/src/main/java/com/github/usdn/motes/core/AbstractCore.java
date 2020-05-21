package com.github.usdn.motes.core;

import com.github.usdn.flowtable.*;
import com.github.usdn.function.FunctionInterface;
import com.github.usdn.motes.battery.Dischargeable;
import com.github.usdn.packet.*;
import com.github.usdn.util.Neighbor;
import com.github.usdn.util.NodeAddress;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.github.usdn.flowtable.FlowtableStructure.*;
import static com.github.usdn.motes.core.USDNConf.*;
import static com.github.usdn.packet.USDNnetworkPacket.*;
import static com.github.usdn.util.Utils.mergeBytes;
import static com.github.usdn.util.Utils.splitInteger;

public abstract class AbstractCore {
    private static final int FUNCTION_HEADER = 3;

    public static final int MAX_RSSI = 255;

    protected static final int QUEUE_SIZE = 100;
    private static final int ENTRY_TTL_PERMANENT = 150;
    private final Dischargeable battery;

    private int cntRPL, cntNodeStatus, cntUpdTable, cntRPLMax, cntNodeStatusMax,
            cntUpdtableMax;

    private byte requestId;
    private int sinkDistance, sinkRssi;

    private final List<NodeAddress> acceptedId = new LinkedList<>();

    private final List<FlowtableEntry> flowTable = new LinkedList<>();

    private final ArrayBlockingQueue<USDNnetworkPacket> ftQueue
            = new ArrayBlockingQueue<>(100);

    private final HashMap<Integer, LinkedList<byte[]>> functionBuffer
            = new HashMap<>();

    private final HashMap<Integer, FunctionInterface> functions
            = new HashMap<>();
    private boolean isActive;

    private final ArrayBlockingQueue<Pair<Level, String>> logQueue
            = new ArrayBlockingQueue<>(100);

    private NodeAddress myAddress;
    private int myNet;
    private final Set<Neighbor> neighborTable =
            Collections.synchronizedSet(new HashSet<>());;

    private int rssiMin;
    private int ruleTtl;
    private final ArrayBlockingQueue<Pair<USDNnetworkPacket, Integer>> rxQueue
            = new ArrayBlockingQueue<>(QUEUE_SIZE);
    private final HashMap<String, Object> sensors = new HashMap<>();
    private final ArrayList<Integer> statusRegister = new ArrayList<>();

    private final ArrayBlockingQueue<USDNnetworkPacket> txQueue
            = new ArrayBlockingQueue<>(QUEUE_SIZE);
    AbstractCore(final byte net, final NodeAddress address,
                 final Dischargeable bat) {
        myAddress = address;
        myNet = net;
        battery = bat;
    }

    public final Dischargeable getBattery() {
        return battery;
    }
    public final int getFlowTableSize() {
        return flowTable.size();
    }
    public final Pair<Level, String> getLogToBePrinted() throws
            InterruptedException {
        return logQueue.take();
    }
    public final NodeAddress getMyAddress() {
        return myAddress;
    }
    public final int getNet() {
        return myNet;
    }
    public final USDNnetworkPacket getNetworkPacketToBeSend() throws
            InterruptedException {
        return txQueue.take();
    }
    public final void rxRadioPacket(final USDNnetworkPacket np, final int rssi) {
        if (np.getDst().isBroadcast()
                || np.getNxh().equals(myAddress)
                || acceptedId.contains(np.getNxh())
                || !np.isUSdn()) {
            try {
                rxQueue.put(new Pair<>(np, rssi));
            } catch (InterruptedException ex) {
                log(Level.SEVERE, ex.toString());
            }
        }
    }

    public final void start() {
        initFlowTable();
        initStatusRegister();
        initUSDN();
        new Thread(new IncomingQueuePacketManager()).start();
        new Thread(new FlowTableQueuePacketManager()).start();
    }
    public final void timer() {
        if (isActive) {
            cntRPL++;
            cntNodeStatus++;
            cntUpdTable++;

            if ((cntRPL) >= cntRPLMax) {
                cntRPL = 0;
                radioTX(prepareBeacon());
            }

            if ((cntNodeStatus) >= cntNodeStatusMax) {
                cntNodeStatus = 0;
                controllerTX(prepareReport());
            }

            if ((cntUpdTable) >= cntUpdtableMax) {
                cntUpdTable = 0;
                updateTable();
            }
        }
    }

    private boolean compare(final int op, final int item1, final int item2) {
        if (item1 == -1 || item2 == -1) {
            return false;
        }
        switch (op) {
            case EQUAL:
                return item1 == item2;
            case NOT_EQUAL:
                return item1 != item2;
            case GREATER:
                return item1 > item2;
            case LESS:
                return item1 < item2;
            case GREATER_OR_EQUAL:
                return item1 >= item2;
            case LESS_OR_EQUAL:
                return item1 <= item2;
            default:
                return false;
        }
    }
    private FunctionInterface createServiceInterface(final byte[] classFile) {
        CustomClassLoader cl = new CustomClassLoader();
        FunctionInterface srvI = null;
        Class service = cl.defClass(classFile);
        try {
            srvI = (FunctionInterface) service.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            log(Level.SEVERE, ex.toString());
        }
        return srvI;
    }

    private int getOperand(final USDNnetworkPacket packet, final int size,
                           final int location, final int value) {
        switch (location) {
            case NULL:
                return 0;
            case CONST:
                return value;
            case PACKET:
                int[] intPacket = packet.toIntArray();
                if (size == 2) {
                    if (value >= intPacket.length) {
                        return -1;
                    }
                    return intPacket[value];
                }
                return -1;
            case STATUS:
                if (size == 2) {
                    if (value >= statusRegister.size()) {
                        return -1;
                    }
                    return statusRegister.get(value);
                }

                return -1;
            default:
                return -1;
        }
    }
    private void initFlowTable() {
        FlowtableEntry toSink = new FlowtableEntry();
        toSink.addWindow(new FlowtableStructure().setOperator(EQUAL).setSize(2)
                .setLhsLocation(PACKET).setLhs(DST_INDEX).setRhsLocation(CONST)
                .setRhs(this.myAddress.intValue()));
        toSink.addWindow(fromString("P.TYP == 3"));
        toSink.addAction(new ForwardAction(myAddress));
        toSink.getStats().setPermanent();
        flowTable.add(toSink);
    }

    private void initStatusRegister() {
        for (int i = 0; i < 1000; i++) {
            statusRegister.add(0);
        }
    }
    private boolean matchRule(final FlowtableEntry rule,
                              final USDNnetworkPacket packet) {
        if (rule.getEntry().isEmpty()) {
            return false;
        }

        int target = rule.getEntry().size();
        return (rule.getEntry().stream().filter(w -> matchRule(rule, packet))
                .count() == target);
    }

    private boolean matchEntry(final FlowtableStructure w, final USDNnetworkPacket packet) {
        int operator = w.getOperator();
        int size = w.getSize();
        int lhs = getOperand(packet, size, w.getLhsLocation(), w.getLhs());
        int rhs = getOperand(packet, size, w.getRhsLocation(), w.getRhs());
        return compare(operator, lhs, rhs);
    }
    private RPLPacket prepareBeacon() {
        return new RPLPacket(myNet, myAddress,
                getActualSinkAddress(), sinkDistance, battery.getByteLevel());
    }
    private NodeStatusUpdatePacket prepareReport() {

        NodeStatusUpdatePacket rp = new NodeStatusUpdatePacket(myNet, myAddress,
                getActualSinkAddress(), sinkDistance, battery.getByteLevel());

        rp.setNeighbors(this.neighborTable.size()).setNxh(getNextHopVsSink());

        int j = 0;
        synchronized (neighborTable) {
            for (Neighbor n : neighborTable) {
                rp.setNeighborAddressAt(n.getAddr(), j)
                        .setLinkQualityAt((byte) n.getMemory(), j);
                j++;
            }
            neighborTable.clear();
        }
        return rp;
    }

    private void runAction(final AbstractAction act, final USDNnetworkPacket np) {
        try {
            switch (act.getType()) {
                case SDN_FT_ACTION_FORWARD:
                    np.setNxh(((AbstractForwardAction) act).getNextHop());
                    radioTX(np);
                    break;
                case SDN_FT_ACTION_QUERY:
                    FlowTableQueryPacket[] rps = FlowTableQueryPacket.createPackets(
                            (byte) myNet, myAddress, getActualSinkAddress(),
                            requestId++, np.toByteArray());

                    for (FlowTableQueryPacket rp : rps) {
                        controllerTX(rp);
                    }
                    break;
                case SDN_FT_ACTION_ACCEPT:
                    ftQueue.put(np);
                    break;
                default:
                    break;
            } //switch
        } catch (InterruptedException ex) {
            log(Level.SEVERE, ex.toString());
        }
    }
    private int searchRule(final FlowtableEntry rule) {
        int i = 0;
        for (FlowtableEntry fte : flowTable) {
            if (fte.equalWindows(rule)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private void updateTable() {
        int i = 0;
        for (Iterator<FlowtableEntry> it = flowTable.iterator();
             it.hasNext();) {
            i++;
            FlowtableEntry fte = it.next();
            int ttl = fte.getStats().getTtl();
            if (ttl != ENTRY_TTL_PERMANENT) {
                if (ttl >= 10) {
                    fte.getStats().decrementTtl(10);
                } else {
                    it.remove();
                    log(Level.INFO, "Removing rule at position " + i);
                    if (i == 0) {
                        reset();
                    }
                }
            }
        }
    }
    protected abstract void controllerTX(USDNnetworkPacket pck);

    protected abstract void dataCallback(DataPacket packet);

    protected final void setActive(final boolean active) {
        this.isActive = active;
    }
    protected abstract NodeAddress getActualSinkAddress();
    protected final NodeAddress getNextHopVsSink() {
        return ((AbstractForwardAction) (flowTable.get(0).getActions().get(0)))
                .getNextHop();
    }
    protected final int getSinkDistance() {
        return sinkDistance;
    }
    protected final void setSinkDistance(final int distance) {
        this.sinkDistance = distance;
    }
    protected final int getSinkRssi() {
        return sinkRssi;
    }
    protected final void setSinkRssi(final int rssi) {
        this.sinkRssi = rssi;
    }

    protected final void initUSDN() {
        cntRPLMax = USDN_DFLT_RPL_MAX ;
        cntNodeStatusMax = USDN_NODES_REPORT_MAX;
        cntUpdtableMax = USDN_DFLT_CNT_UPDTABLE_MAX;
        rssiMin = 1000;
        ruleTtl = 150;
        initUSDNSpecific();
    }
    protected abstract void initUSDNSpecific();

    protected final void insertRule(final FlowtableEntry rule) {
        int i = searchRule(rule);
        if (i != -1) {
            flowTable.set(i, rule);
            log(Level.INFO, "Replacing rule " + rule
                    + " at position " + i);
        } else {
            flowTable.add(rule);
            log(Level.INFO, "Inserting rule " + rule
                    + " at position " + (flowTable.size() - 1));
        }

    }
    private boolean isAcceptedIdAddress(final NodeAddress address) {
        return (address.equals(myAddress)
                || address.isBroadcast()
                || acceptedId.contains(address));
    }
    private boolean isAcceptedIdPacket(final USDNnetworkPacket packet) {
        return isAcceptedIdAddress(packet.getDst());
    }
    protected final void log(final Level level, final String logMessage) {
        try {
            logQueue.put(new Pair<>(level, logMessage));
        } catch (InterruptedException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }
    }
    private void execWriteConfigPacket(final ConfigPacket packet) {
        byte[] value = packet.getParams();
        int idValue = Byte.toUnsignedInt(value[0]);
        switch (packet.getConfigId()) {
            case MY_ADDRESS:
                myAddress = new NodeAddress(value);
                break;
            case MY_NET:
                myNet = idValue;
                break;
            case RPL_PERIOD:
                cntRPL = mergeBytes(value[0], value[1]);
                break;
            case REPORT_PERIOD:
                cntNodeStatusMax = mergeBytes(value[0], value[1]);
                break;
            case RULE_TTL:
                cntUpdtableMax = idValue;
                break;
            case PACKET_TTL:
                ruleTtl = idValue;
                break;
            case RSSI_MIN:
                rssiMin = idValue;
                break;
            case ADD_ALIAS:
                acceptedId.add(new NodeAddress(value));
                break;
            case REM_ALIAS:
                acceptedId.remove(idValue);
                break;
            case REM_RULE:
                if (idValue != 0) {
                    flowTable.remove(idValue);
                }
                break;
            case ADD_RULE:
                break;
            case RESET:
                reset();
                break;
            case ADD_FUNCTION:
                if (functionBuffer.get(idValue) == null) {
                    functionBuffer.put(idValue, new LinkedList<>());
                }
                byte[] function = Arrays.copyOfRange(value, FUNCTION_HEADER,
                        value.length);
                int totalParts = Byte.toUnsignedInt(value[2]);
                functionBuffer.get(idValue).add(function);
                if (functionBuffer.get(idValue).size() == totalParts) {
                    int total = 0;
                    total = functionBuffer.get(idValue).stream().map((n)
                            -> (n.length)).reduce(total, Integer::sum);
                    int pointer = 0;
                    byte[] func = new byte[total];
                    for (byte[] n : functionBuffer.get(idValue)) {
                        System.arraycopy(n, 0, func, pointer, n.length);
                        pointer += n.length;
                    }
                    functions.put(idValue, createServiceInterface(func));
                    log(Level.INFO, "New Function Added at pos.: " + idValue);
                    functionBuffer.remove(idValue);
                }
                break;
            case REM_FUNCTION:
                functions.remove(idValue);
                break;
            default:
                break;
        }
    }
    private boolean execReadConfigPacket(final ConfigPacket packet) {
        ConfigPacket.ConfigProperty id = packet.getConfigId();
        byte[] value = packet.getParams();
        int size = id.getSize();
        switch (id) {
            case MY_ADDRESS:
                packet.setParams(myAddress.getArray(), size);
                break;
            case MY_NET:
                packet.setParams(new byte[]{(byte) myNet}, size);
                break;
            case RPL_PERIOD:
                packet.setParams(splitInteger(cntRPLMax), size);
                break;
            case REPORT_PERIOD:
                packet.setParams(splitInteger(cntNodeStatus), size);
                break;
            case RULE_TTL:
                packet.setParams(new byte[]{(byte) cntUpdtableMax}, size);
                break;
            case PACKET_TTL:
                packet.setParams(new byte[]{(byte) ruleTtl}, size);
                break;
            case RSSI_MIN:
                packet.setParams(new byte[]{(byte) rssiMin}, size);
                break;
            case GET_ALIAS:
                int aIndex = Byte.toUnsignedInt(value[0]);
                if (aIndex < acceptedId.size()) {
                    byte[] tmp = acceptedId.get(aIndex).getArray();
                    packet.setParams(ByteBuffer.allocate(tmp.length + 1)
                            .put((byte) aIndex).put(tmp).array(), -1);
                } else {
                    return false;
                }
                break;
            case GET_RULE:
                int i = Byte.toUnsignedInt(value[0]);
                if (i < flowTable.size()) {
                    FlowtableEntry fte = flowTable.get(i);
                    byte[] tmp = fte.toByteArray();
                    packet.setParams(ByteBuffer.allocate(tmp.length + 1)
                            .put((byte) i).put(tmp).array(), -1);
                } else {
                    return false;
                }
                break;

            default:
                break;
        }
        return true;
    }

    protected final boolean execConfigPacket(final ConfigPacket packet) {
        boolean toBeSent = false;
        try {
            if (packet.isWrite()) {
                execWriteConfigPacket(packet);
            } else {
                toBeSent = execReadConfigPacket(packet);
            }
        } catch (Exception ex) {
            log(Level.SEVERE, ex.toString());
        }
        return toBeSent;
    }

    protected final void radioTX(final USDNnetworkPacket np) {
        np.decrementTtl();
        txQueue.add(np);
    }
    protected abstract void reset();

    protected final void runFlowMatch(final USDNnetworkPacket packet) {
        int i = 0;
        boolean matched = false;
        for (FlowtableEntry fte : flowTable) {
            i++;
            if (matchRule(fte, packet)) {
                log(Level.FINE, "Matched Rule #" + i + " " + fte.toString());
                matched = true;
                fte.getActions().stream().forEach((a) -> {
                    runAction(a, packet);
                });
                fte.getStats().increaseCounter();
                break;
            }
        }

        if (!matched) {
            // send a rule request
            FlowTableQueryPacket[] rps = FlowTableQueryPacket.createPackets((byte) myNet,
                    myAddress, getActualSinkAddress(), requestId++,
                    packet.toByteArray());

            for (FlowTableQueryPacket rp : rps) {
                controllerTX(rp);
            }
        }
    }
    protected abstract void rxRPL(final RPLPacket bp, final int rssi);
    protected abstract void rxConfig(ConfigPacket packet);
    protected final void rxData(final DataPacket packet) {
        if (isAcceptedIdPacket(packet)) {
            dataCallback(packet);
        } else if (isAcceptedIdAddress(packet.getNxh())) {
            runFlowMatch(packet);
        }
    }
    protected final void rxHandler(final USDNnetworkPacket packet, final int rssi) {

        if (!packet.isUSdn()) {
            runFlowMatch(packet);
        } else if (packet.getLen() > DFLT_HDR_LEN && packet.getNet() == myNet
                && packet.getTtl() != 0) {

            switch (packet.getTyp()) {
                case DATA:
                    rxData(new DataPacket(packet));
                    break;

                case RPL:
                    rxRPL(new RPLPacket(packet), rssi);
                    break;

                case NODE_STATUS_UPDATE:
                    rxNodestatus(new NodeStatusUpdatePacket(packet));
                    break;

                case FLOWTABLE_QUERY:
                    rxQuery(new FlowTableQueryPacket(packet));
                    break;

                case FLOWTABLE_SET:
                    rxSet(new FlowTableSetPacket(packet));
                    break;

                case CONFIG:
                    rxConfig(new ConfigPacket(packet));
                    break;

                default:
                    runFlowMatch(packet);
                    break;
            }

        }
    }

    protected final void rxNodestatus(final NodeStatusUpdatePacket packet) {
        controllerTX(packet);
    }

    protected final void rxQuery(final FlowTableQueryPacket packet) {
        controllerTX(packet);
    }
    protected final void rxSet(final FlowTableSetPacket packet) {
        if (isAcceptedIdPacket(packet)) {
            packet.getRule().setStats(new Stats());
            insertRule(packet.getRule());
        } else {
            runFlowMatch(packet);
        }
    }
    private class CustomClassLoader extends ClassLoader {

        public Class defClass(final byte[] data) {
            return defineClass(null, data, 0, data.length);
        }
    }
    private class FlowTableQueuePacketManager implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    USDNnetworkPacket np = ftQueue.take();
                    rxQueue.put(new Pair<>(np, MAX_RSSI));
                }
            } catch (InterruptedException ex) {
                log(Level.SEVERE, ex.toString());
            }
        }
    }

    private class IncomingQueuePacketManager implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    Pair<USDNnetworkPacket, Integer> p = rxQueue.take();
                    rxHandler(p.getKey(), p.getValue());
                }
            } catch (InterruptedException ex) {
                log(Level.SEVERE, ex.toString());
            }
        }
    }

    public final List<NodeAddress> getAcceptedId() {
        return acceptedId;
    }
    public final List<FlowtableEntry> getFlowTable() {
        return flowTable;
    }

    public final ArrayBlockingQueue<USDNnetworkPacket> getFtQueue() {
        return ftQueue;
    }
    public final Set<Neighbor> getNeighborTable() {
        return neighborTable;
    }

    public final int getRssiMin() {
        return rssiMin;
    }

    public final int getRuleTtl() {
        return ruleTtl;
    }
    public final HashMap<String, Object> getSensors() {
        return sensors;
    }

    public final ArrayBlockingQueue<USDNnetworkPacket> getTxQueue() {
        return txQueue;
    }
    public final ArrayList<Integer> getStatusRegister() {
        return statusRegister;
    }




}
