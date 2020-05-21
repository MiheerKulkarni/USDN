package com.github.usdn.motes.core;

import com.github.usdn.motes.battery.Dischargeable;
import com.github.usdn.packet.*;
import com.github.usdn.util.Neighbor;
import com.github.usdn.util.NodeAddress;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;

public class SinkCore extends AbstractCore {

    private static final int CTRL_RSSI = 255;

    private final String switchDPid, switchMac;

    private final long switchPort;
    private final InetSocketAddress addrController;

    private final ArrayBlockingQueue<USDNnetworkPacket> txControllerQueue
            = new ArrayBlockingQueue<>(QUEUE_SIZE);

    public SinkCore(
            final byte net,
            final NodeAddress address,
            final Dischargeable battery,
            final String dPid,
            final String mac,
            final long port,
            final InetSocketAddress ctrl) {
        super(net, address, battery);
        this.switchDPid = dPid;
        this.switchMac = mac;
        this.switchPort = port;
        this.addrController = ctrl;
    }

    @Override
    public final void controllerTX(final USDNnetworkPacket pck) {
        try {
            txControllerQueue.put(pck);
            log(Level.FINE, "Usdn controller " + pck);
        } catch (InterruptedException ex) {
            log(Level.SEVERE, ex.toString());
        }
    }
    public final USDNnetworkPacket getControllerPacketTobeSend()
            throws InterruptedException {
        return txControllerQueue.take();
    }

    @Override
    public final void dataCallback(final DataPacket packet) {
        controllerTX(packet);
    }

    @Override
    public final void rxConfig(final ConfigPacket packet) {
        NodeAddress dest = packet.getDst();
        NodeAddress src = packet.getSrc();

        if (!dest.equals(getMyAddress())) {
            runFlowMatch(packet);
        } else if (!src.equals(getMyAddress())) {
            controllerTX(packet);
        } else if (execConfigPacket(packet)) {
            controllerTX(packet);
        }
    }

    @Override
    public final NodeAddress getActualSinkAddress() {
        return getMyAddress();
    }

    @Override
    protected final void initUSDNSpecific() {
        setSinkDistance(0);
        setSinkRssi(CTRL_RSSI);
        setActive(true);
        RegProxyPacket rpp = new RegProxyPacket(1, getMyAddress(), switchDPid,
                switchMac, switchPort, addrController);
        controllerTX(rpp);
    }

    @Override
    protected final void reset() {
        // Nothing to do here
    }

    @Override
    protected final void rxRPL(final RPLPacket bp, final int rssi) {
        Neighbor nb = new Neighbor(bp.getSrc(), rssi, bp.getBattery());
        getNeighborTable().add(nb);
    }

}
