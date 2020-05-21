package com.github.usdn.packet;

import com.github.usdn.flowtable.FlowtableEntry;
import com.github.usdn.util.NodeAddress;

import java.util.Arrays;

public class FlowTableSetPacket extends USDNnetworkPacket {

    public FlowTableSetPacket(final byte[] data) {
        super(data);
    }

    public FlowTableSetPacket(final USDNnetworkPacket data) {
        super(data.toByteArray());
    }

    public FlowTableSetPacket(final int net, final NodeAddress src,
                              final NodeAddress dst,
                              final FlowtableEntry entry) {
        super(net, src, dst);
        setTyp(FLOWTABLE_SET);
        setRule(entry);
    }
    public FlowTableSetPacket(final int[] data) {
        super(data);
    }

    public final FlowTableSetPacket setRule(final FlowtableEntry rule) {
        byte[] tmp = rule.toByteArray();
        // the last byte is for stats so it is useless to send it in a response
        setPayload(Arrays.copyOf(tmp, tmp.length - 1));
        return this;
    }
    public final FlowtableEntry getRule() {
        FlowtableEntry rule = new FlowtableEntry(getPayload());
        return rule;
    }
}
