package com.github.usdn.util;

import java.io.Serializable;

import static com.github.usdn.util.Utils.mergeBytes;

public final class NodeAddress implements Comparable<NodeAddress>, Serializable {
    public static final NodeAddress BROADCAST_ADDR = new NodeAddress("255.255");

    private static final long serialVersionUID = 1L;

    private final byte[] addr = new byte[2];

    public NodeAddress(final int a) {
        addr[0] = (byte) (a >>> Byte.SIZE);
        addr[1] = (byte) a;
    }
    public NodeAddress(final byte[] a) {
        if (a.length == 2) {
            addr[0] = a[0];
            addr[1] = a[1];
        }
    }
    public NodeAddress(final String a) {
        String[] add = a.split("\\s*\\.\\s*");
        if (add.length == 2) {
            addr[0] = (byte) Integer.parseInt(add[0]);
            addr[1] = (byte) Integer.parseInt(add[1]);
        } else {
            int adr = Integer.parseInt(a);
            addr[0] = (byte) (adr >>> Byte.SIZE);
            addr[1] = (byte) adr;
        }
    }
    public NodeAddress(final int addr0, final int addr1) {
        addr[0] = (byte) addr0;
        addr[1] = (byte) addr1;
    }

    @Override
    public int compareTo(final NodeAddress other) {
        return Integer.valueOf(intValue()).compareTo(other.intValue());
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof NodeAddress
                && ((NodeAddress) obj).intValue() == intValue();
    }

    public byte[] getArray() {
        return new byte[]{addr[0], addr[1]};
    }

    public byte getHigh() {
        return addr[0];
    }
    public byte getLow() {
        return addr[1];
    }
    @Override
    public int hashCode() {
        return Integer.valueOf(intValue()).hashCode();
    }

    public int intValue() {
        return mergeBytes(addr[0], addr[1]);
    }
    public boolean isBroadcast() {
        return equals(BROADCAST_ADDR);
    }
    public Byte[] toByteArray() {
        return new Byte[]{addr[0], addr[1]};
    }
    public String toString() {
        return Byte.toUnsignedInt(addr[0]) + "." + Byte.toUnsignedInt(addr[1]);
    }

}
