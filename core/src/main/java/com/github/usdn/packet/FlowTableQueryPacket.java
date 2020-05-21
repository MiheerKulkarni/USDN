package com.github.usdn.packet;

import com.github.usdn.util.NodeAddress;

import static com.github.usdn.util.Utils.concatByteArray;

public class FlowTableQueryPacket extends USDNnetworkPacket{

    private static final byte ID_INDEX = 0, PART_INDEX = 1,
            REQUEST_HEADER_SIZE = 3,
            REQUEST_PAYLOAD_SIZE = USDNnetworkPacket.MAX_PACKET_LENGTH
                    - (DFLT_HDR_LEN + REQUEST_HEADER_SIZE), TOTAL_INDEX = 2;

    public static FlowTableQueryPacket[] createPackets(
            final int net,
            final NodeAddress src,
            final NodeAddress dst,
            final byte id,
            final byte[] buf) {

        int i;
        if (buf.length > REQUEST_PAYLOAD_SIZE) {
            i = 2;
        } else {
            i = 1;
        }

        int remaining = buf.length % REQUEST_PAYLOAD_SIZE;
        FlowTableQueryPacket[] ll = new FlowTableQueryPacket[i];

        byte[] payload;

        if (i == 1) {
            payload = new byte[remaining];
        } else {
            payload = new byte[REQUEST_PAYLOAD_SIZE];
        }

        System.arraycopy(buf, 0, payload, 0, payload.length);
        FlowTableQueryPacket np = new FlowTableQueryPacket(net, src, dst, id, 0, i, payload);
        ll[0] = np;

        if (i > 1) {
            payload = new byte[remaining];
            System.arraycopy(buf, REQUEST_PAYLOAD_SIZE, payload, 0, remaining);
            np = new FlowTableQueryPacket(net, src, dst, id, 1, i, payload);
            ll[1] = np;
        }

        return ll;
    }

    public static USDNnetworkPacket mergePackets(final FlowTableQueryPacket rp0,
                                             final FlowTableQueryPacket rp1) {
        if (rp0.getPart() == 0) {
            return new USDNnetworkPacket(
                    concatByteArray(rp0.getData(), rp1.getData()));
        } else {
            return new USDNnetworkPacket(
                    concatByteArray(rp1.getData(), rp0.getData()));
        }
    }
    public FlowTableQueryPacket(final byte[] data) {
        super(data);
    }
    public FlowTableQueryPacket(final USDNnetworkPacket data) {
        super(data.toByteArray());
    }
    public FlowTableQueryPacket(final int[] data) {
        super(data);
    }

    private FlowTableQueryPacket(final int net,
                          final NodeAddress src,
                          final NodeAddress dst,
                          final int id,
                          final int part,
                          final int total,
                          final byte[] data) {
        super(net, src, dst);
        setTyp(FLOWTABLE_QUERY);
        setId(id).setTotal(total).setPart(part).setData(data);
    }
    public final byte[] getData() {
        return getPayloadFromTo(TOTAL_INDEX + 1, getPayloadSize());
    }
    public final int getDataSize() {
        return getPayloadSize() - (TOTAL_INDEX + 1);
    }
    public final int getId() {
        return getPayloadAt(ID_INDEX);
    }
    public final int getPart() {
        return getPayloadAt(PART_INDEX);
    }
    public final int getTotal() {
        return getPayloadAt(TOTAL_INDEX);
    }
    private FlowTableQueryPacket setData(final byte[] data) {
        setPayload(data, 0, TOTAL_INDEX + 1, data.length);
        return this;
    }
    private FlowTableQueryPacket setId(final int id) {
        setPayloadAt((byte) id, ID_INDEX);
        return this;
    }
    private FlowTableQueryPacket setPart(final int part) {
        setPayloadAt((byte) part, PART_INDEX);
        return this;
    }
    private FlowTableQueryPacket setTotal(final int total) {
        setPayloadAt((byte) total, TOTAL_INDEX);
        return this;
    }

}
