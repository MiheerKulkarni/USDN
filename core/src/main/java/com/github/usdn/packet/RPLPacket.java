package com.github.usdn.packet;

import com.github.usdn.util.NodeAddress;

import static com.github.usdn.util.NodeAddress.BROADCAST_ADDR;

public class RPLPacket extends USDNnetworkPacket {

    private static final byte DIST_INDEX = 0,
            BATT_INDEX = 1;
    public RPLPacket(final byte[] data) {
        super(data);
    }

    public RPLPacket(final int[] data) {
        super(data);
    }

    public RPLPacket(final USDNnetworkPacket data) {
        super(data.toByteArray());
    }

    public RPLPacket(final int net, final NodeAddress src,
                        final NodeAddress sink,
                        final int distance, final int battery) {
        super(net, src, BROADCAST_ADDR);
        setTyp(RPL);
        setSinkAddress(sink);
        setDistance((byte) distance);
        setBattery((byte) battery);
    }

    public final int getDistance() {
        return Byte.toUnsignedInt(getPayloadAt(DIST_INDEX));
    }

    public final RPLPacket setDistance(final byte value) {
        setPayloadAt(value, DIST_INDEX);
        return this;
    }
    public final int getBattery() {
        return Byte.toUnsignedInt(getPayloadAt(BATT_INDEX));
    }

    public final RPLPacket setBattery(final byte value) {
        setPayloadAt(value, BATT_INDEX);
        return this;
    }
    public final RPLPacket setSinkAddress(final NodeAddress addr) {
        setNxh(addr);
        return this;
    }
    public final NodeAddress getSinkAddress() {
        return getNxh();
    }

}
