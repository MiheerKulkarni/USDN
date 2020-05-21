package com.github.usdn.util;

import java.nio.ByteBuffer;

public class Utils {

    private static final String DIGITS = "0123456789abcdef";

    private static final int MASK = 0xFF, MASK_1 = 4, MASK_2 = 0xf;

    public static byte[] concatByteArray(final byte[] a, final byte[] b) {
        return ByteBuffer.allocate(a.length + b.length).put(a).put(b).array();
    }

    public static int getBitRange(final int b, final int s, final int n) {
        return (((b & MASK) >> (s & MASK))
                & ((1 << (n & MASK)) - 1)) & MASK;
    }

    public static int mergeBytes(final int high, final int low) {
        int h = Byte.toUnsignedInt((byte) high);
        int l = Byte.toUnsignedInt((byte) low);
        return (h << Byte.SIZE) | l;
    }

    public static int setBitRange(final int val,
                                  final int start, final int len, final int newVal) {
        int mask = ((1 << len) - 1) << start;
        return (val & ~mask) | ((newVal << start) & mask);
    }

    public static byte[] splitInteger(final int value) {
        ByteBuffer b = ByteBuffer.allocate(2);
        b.putShort((short) value);
        return b.array();
    }

    public static String toHex(final byte[] data) {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < data.length; i++) {
            int v = Byte.toUnsignedInt(data[i]);
            buf.append(DIGITS.charAt(v >> MASK_1));
            buf.append(DIGITS.charAt(v & MASK_2));
        }

        return buf.toString();
    }

    private Utils() {
    }
}
