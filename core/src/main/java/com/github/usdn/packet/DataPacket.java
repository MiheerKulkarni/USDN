package com.github.usdn.packet;

import com.github.usdn.util.NodeAddress;

public class DataPacket extends USDNnetworkPacket {

    public DataPacket(final byte[] data) {
        super(data);
    }
    public DataPacket(final int[] data) {
        super(data);
    }

    public DataPacket(final USDNnetworkPacket data) {
        super(data.toByteArray());
    }

    public DataPacket(final int net, final NodeAddress src,
                      final NodeAddress dst,
                      final byte[] payload) {
        super(net, src, dst);
        setTyp(DATA);
        setPayload(payload);
    }
    public final byte[] getData() {
        return super.getPayload();
    }
}
