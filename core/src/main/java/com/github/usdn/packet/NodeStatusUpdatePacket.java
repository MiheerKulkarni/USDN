package com.github.usdn.packet;

import com.github.usdn.util.NodeAddress;

public class NodeStatusUpdatePacket extends USDNnetworkPacket {
    private static final byte MAX_NEIG = 35,
            NEIGH_INDEX = 2,
            NEIGH_SIZE = 3;

    public NodeStatusUpdatePacket(final byte[] data) {
        super(data);
    }

    public NodeStatusUpdatePacket(final int net, final NodeAddress src,
                        final NodeAddress dst,
                        final int distance,
                        final int battery) {
        super(net, src, dst);
        setDst(dst);
        setTyp(NODE_STATUS_UPDATE);
    }

    public NodeStatusUpdatePacket(final int[] data) {
        super(data);
    }

    public NodeStatusUpdatePacket(final USDNnetworkPacket data) {
        super(data.toByteArray());
    }

    public final int getNeigborsSize() {
        return Byte.toUnsignedInt(getPayloadAt(NEIGH_INDEX));
    }
    public final NodeStatusUpdatePacket setNeighbors(final int value) {
        if (value <= MAX_NEIG) {
            setPayloadAt((byte) value, NEIGH_INDEX);
            setPayloadSize((byte) (NEIGH_SIZE + value * NEIGH_SIZE));
        } else {
            throw new IllegalArgumentException("Too many neighbors");
        }
        return this;
    }
    public final NodeAddress getNeighborAddress(final int i) {
        if (i <= MAX_NEIG) {
            return new NodeAddress(
                    getPayloadAt(NEIGH_INDEX + 1 + (i * NEIGH_SIZE)),
                    getPayloadAt(NEIGH_INDEX + 2 + (i * NEIGH_SIZE)));
        } else {
            throw new IllegalArgumentException(
                    "Index exceeds max number of neighbors");
        }
    }
    public final NodeStatusUpdatePacket setNeighborAddressAt(final NodeAddress addr,
                                                   final int i) {
        if (i <= MAX_NEIG) {
            setPayloadAt(addr.getHigh(), (NEIGH_INDEX + 1 + (i * NEIGH_SIZE)));
            setPayloadAt(addr.getLow(), (NEIGH_INDEX + 2 + (i * NEIGH_SIZE)));
            if (getNeigborsSize() < i) {
                setNeighbors(i);
            }
            return this;
        } else {
            throw new IllegalArgumentException(
                    "Index exceeds max number of neighbors");
        }
    }

    public final int getLinkQuality(final int i) {
        if (i <= MAX_NEIG) {
            return getPayloadAt(NEIGH_INDEX + ((i + 1) * NEIGH_SIZE));
        } else {
            throw new IllegalArgumentException(
                    "Index exceeds max number of neighbors");
        }
    }
    public final NodeStatusUpdatePacket setLinkQualityAt(final byte value, final int i) {
        if (i <= MAX_NEIG) {
            setPayloadAt(value, NEIGH_INDEX + ((i + 1) * NEIGH_SIZE));
            if (getNeigborsSize() < i) {
                setNeighbors(i);
            }
            return this;
        } else {
            throw new IllegalArgumentException(
                    "Index exceeds max number of neighbors");
        }
    }



}
