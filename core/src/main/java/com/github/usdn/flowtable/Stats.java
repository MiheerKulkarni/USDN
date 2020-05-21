package com.github.usdn.flowtable;

import java.util.Arrays;

public final class Stats implements FlowTableInterface {
    public static final byte SIZE = 2;

    public static final int ENTRY_TTL_PERMANENT = 255;

    public static final int USDN_TTL_MAX = 150;

    private static final byte TTL_INDEX = 0, COUNT_INDEX = 1;

    private final byte[] stats = new byte[SIZE];

    public Stats() {
        stats[TTL_INDEX] = (byte) USDN_TTL_MAX;
        stats[COUNT_INDEX] = 0;
    }
    public Stats(final byte[] value) {
        switch (value.length) {
            case 2:
                stats[TTL_INDEX] = value[TTL_INDEX];
                stats[COUNT_INDEX] = value[COUNT_INDEX];
                break;
            case 1:
                stats[TTL_INDEX] = value[TTL_INDEX];
                stats[COUNT_INDEX] = 0;
                break;
            default:
                stats[TTL_INDEX] = (byte) USDN_TTL_MAX;
                stats[COUNT_INDEX] = 0;
                break;
        }
    }

    public int getTtl() {
        return Byte.toUnsignedInt(stats[TTL_INDEX]);
    }
    public int getCounter() {
        return Byte.toUnsignedInt(stats[COUNT_INDEX]);
    }
    public Stats setCounter(final int count) {
        stats[COUNT_INDEX] = (byte) count;
        return this;
    }
    public Stats increaseCounter() {
        stats[COUNT_INDEX]++;
        return this;
    }
    @Override
    public String toString() {
        if (getTtl() == ENTRY_TTL_PERMANENT) {
            return "TTL: PERM, U: " + getCounter();
        } else {
            return "TTL: " + getTtl() + ", U: " + getCounter();
        }
    }

    @Override
    public byte[] toByteArray() {
        return Arrays.copyOf(stats, SIZE);
    }

    public Stats setPermanent() {
        setTtl(ENTRY_TTL_PERMANENT);
        return this;
    }
    public Stats restoreTtl() {
        setTtl(USDN_TTL_MAX);
        return this;
    }
    public Stats decrementTtl(final int value) {
        setTtl(getTtl() - value);
        return this;
    }

    private Stats setTtl(final int ttl) {
        stats[TTL_INDEX] = (byte) ttl;
        return this;
    }
}
