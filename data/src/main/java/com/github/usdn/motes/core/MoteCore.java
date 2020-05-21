package com.github.usdn.motes.core;

import com.github.usdn.flowtable.FlowtableEntry;
import com.github.usdn.flowtable.FlowtableStructure;
import com.github.usdn.flowtable.ForwardAction;
import com.github.usdn.motes.battery.Dischargeable;
import com.github.usdn.packet.ConfigPacket;
import com.github.usdn.packet.DataPacket;
import com.github.usdn.packet.RPLPacket;
import com.github.usdn.packet.USDNnetworkPacket;
import com.github.usdn.util.Neighbor;
import com.github.usdn.util.NodeAddress;

import java.nio.charset.Charset;
import java.util.logging.Level;

import static com.github.usdn.flowtable.FlowTableInterface.CONST;
import static com.github.usdn.flowtable.FlowTableInterface.PACKET;
import static com.github.usdn.flowtable.FlowtableStructure.EQUAL;
import static com.github.usdn.flowtable.FlowtableStructure.fromString;
import static com.github.usdn.packet.USDNnetworkPacket.DFLT_TTL_MAX;
import static com.github.usdn.packet.USDNnetworkPacket.DST_INDEX;

public class MoteCore extends AbstractCore {

    private static final boolean cond = true;

    public MoteCore(final byte net, final NodeAddress na,
                    final Dischargeable battery) {
        super(net, na, battery);
    }

    @Override
    public final void controllerTX(final USDNnetworkPacket np) {
        np.setNxh(getNextHopVsSink());
        radioTX(np);
    }

    @Override
    public final void dataCallback(final DataPacket dp) {
        if (cond == true) {
            log(Level.INFO, new String(dp.getData(),
                    Charset.forName("UTF-8")));
            dp.setSrc(getMyAddress())
                    .setDst(getActualSinkAddress())
                    .setTtl((byte) getRuleTtl());
            runFlowMatch(dp);
        }
    }



    @Override
    protected final void rxRPL(final RPLPacket bp, final int rssi) {
        if (rssi > getRssiMin()) {
            if (bp.getDistance() < getSinkDistance()
                    && (rssi > getSinkRssi())) {
                setActive(true);
                FlowtableEntry toSink = new FlowtableEntry();
                toSink.addWindow(new FlowtableStructure()
                        .setOperator(EQUAL)
                        .setSize(2)
                        .setLhsLocation(PACKET)
                        .setLhs(DST_INDEX)
                        .setRhsLocation(CONST)
                        .setRhs(bp.getSinkAddress().intValue()));
                toSink.addWindow(fromString("P.TYP == 3"));
                toSink.addAction(new ForwardAction(bp.getSrc()));
                getFlowTable().set(0, toSink);

                setSinkDistance(bp.getDistance() + 1);
                setSinkRssi(rssi);
            } else if ((bp.getDistance() + 1) == getSinkDistance()
                    && getNextHopVsSink().equals(bp.getSrc())) {
                getFlowTable().get(0).getStats().restoreTtl();
                getFlowTable().get(0).getEntry().get(0)
                        .setRhs(bp.getSinkAddress().intValue());
            }
            Neighbor nb = new Neighbor(bp.getSrc(), rssi, bp.getBattery());
            getNeighborTable().add(nb);
        }
    }

    @Override
    protected final void rxConfig(final ConfigPacket cp) {
        NodeAddress dest = cp.getDst();
        if (!dest.equals(getMyAddress())) {
            runFlowMatch(cp);
        } else if (execConfigPacket(cp)) {
            cp.setSrc(getMyAddress());
            cp.setDst(getActualSinkAddress());
            cp.setTtl((byte) getRuleTtl());
            runFlowMatch(cp);
        }
    }
    @Override
    protected final NodeAddress getActualSinkAddress() {
        return new NodeAddress(getFlowTable().get(0).getEntry()
                .get(0).getRhs());
    }

    @Override
    protected final void initUSDNSpecific() {
        reset();
    }

    @Override
    protected final void reset() {
        setSinkDistance(DFLT_TTL_MAX + 1);
        setSinkRssi(0);
        setActive(false);
    }

}
