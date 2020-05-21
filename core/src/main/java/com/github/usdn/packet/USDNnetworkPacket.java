package com.github.usdn.packet;

import com.github.usdn.util.NodeAddress;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

public class USDNnetworkPacket implements Cloneable {

    public static final byte MAX_PACKET_LENGTH = 116;


    public static final byte NET_INDEX = 0,
            LEN_INDEX = 1,
            DST_INDEX = 2,
            SRC_INDEX = 4,
            TYP_INDEX = 6,
            TTL_INDEX = 7,
            NXH_INDEX = 8,
            PLD_INDEX = 10;
    public static final byte DFLT_HDR_LEN = 10;
    public static final byte DATA = 0,
            NODE_STATUS_UPDATE = 1,
            FLOWTABLE_QUERY = 2,
            FLOWTABLE_SET = 3,
            CONFIG = 4,
            RPL = 5,
            REG_PROXY=6;
    public static final byte DFLT_TTL_MAX = 100;

    private final byte[] data;
    public static final byte THRES = 53;

    public static int getNetworkPacketByteFromName(final String b) {
        switch (b) {
            case "LEN":
                return LEN_INDEX;
            case "NET":
                return NET_INDEX;
            case "SRC":
                return SRC_INDEX;
            case "DST":
                return DST_INDEX;
            case "TYP":
                return TYP_INDEX;
            case "TTL":
                return TTL_INDEX;
            case "NXH":
                return NXH_INDEX;
            default:
                return Integer.parseInt(b);
        }
    }
    public static boolean isUSdn(final byte[] data) {
        return (Byte.toUnsignedInt(data[NET_INDEX]) < THRES);
    }
    public static String getNetworkPacketByteName(final int b) {
        switch (b) {
            case (NET_INDEX):
                return "NET";
            case (LEN_INDEX):
                return "LEN";
            case (DST_INDEX):
                return "DST";
            case (SRC_INDEX):
                return "SRC";
            case (TYP_INDEX):
                return "TYP";
            case (TTL_INDEX):
                return "TTL";
            case (NXH_INDEX):
                return "NXH";
            default:
                return String.valueOf(b);
        }
    }
    public USDNnetworkPacket(final byte[] d) {
        data = new byte[MAX_PACKET_LENGTH];
        setArray(d);
    }
    public USDNnetworkPacket(final int net, final NodeAddress src,
                             final NodeAddress dst) {
        data = new byte[MAX_PACKET_LENGTH];
        setNet((byte) net);
        setSrc(src);
        setDst(dst);
        setTtl(DFLT_TTL_MAX);
        setLen(DFLT_HDR_LEN);
    }
    public USDNnetworkPacket(final int[] d) {
        data = new byte[MAX_PACKET_LENGTH];
        setArray(fromIntArrayToByteArray(d));
    }
    public USDNnetworkPacket(final DataInputStream dis) throws IOException {
        data = new byte[MAX_PACKET_LENGTH];
        byte[] tmpData = new byte[MAX_PACKET_LENGTH];
        int net = Byte.toUnsignedInt(dis.readByte());
        int len = Byte.toUnsignedInt(dis.readByte());
        if (len > 0) {
            tmpData[NET_INDEX] = (byte) net;
            tmpData[LEN_INDEX] = (byte) len;
            dis.readFully(tmpData, LEN_INDEX + 1, len - 2);

        }
        setArray(tmpData);
    }
    public final void setArray(final int[] array) {
        setArray(fromIntArrayToByteArray(array));
    }

    public final void setArray(final byte[] array) {
        if (isUSdn(array)) {
            if (array.length <= MAX_PACKET_LENGTH && array.length
                    >= DFLT_HDR_LEN) {

                setLen(array[LEN_INDEX]);
                setNet(array[NET_INDEX]);
                setSrc(array[SRC_INDEX], array[SRC_INDEX + 1]);
                setDst(array[DST_INDEX], array[DST_INDEX + 1]);
                setTyp(array[TYP_INDEX]);
                setTtl(array[TTL_INDEX]);
                setNxh(array[NXH_INDEX], array[NXH_INDEX + 1]);
                setPayload(Arrays.copyOfRange(array, DFLT_HDR_LEN,
                        getLen()));
            } else {
                throw new IllegalArgumentException("Invalid array size: "
                        + array.length);
            }
        } else {
            System.arraycopy(array, 0, data, 0, array.length);
        }
    }
    public final int getLen() {
        if (isUSdn()) {
            return Byte.toUnsignedInt(data[LEN_INDEX]);
        } else {
            return data.length;
        }
    }
    public final USDNnetworkPacket setLen(final byte value) {
        int v = Byte.toUnsignedInt(value);
        if (v <= MAX_PACKET_LENGTH && v > 0) {
            data[LEN_INDEX] = value;
        } else {
            throw new IllegalArgumentException("Invalid length: " + v);
        }
        return this;
    }
    public final int getNet() {
        return Byte.toUnsignedInt(data[NET_INDEX]);
    }
    public final USDNnetworkPacket setNet(final byte value) {
        data[NET_INDEX] = value;
        return this;
    }

    public final NodeAddress getSrc() {
        return new NodeAddress(data[SRC_INDEX], data[SRC_INDEX + 1]);
    }

    public final USDNnetworkPacket setSrc(final byte valueH, final byte valueL) {
        data[SRC_INDEX] = valueH;
        data[SRC_INDEX + 1] = valueL;
        return this;
    }

    public final USDNnetworkPacket setSrc(final NodeAddress address) {
        setSrc(address.getHigh(), address.getLow());
        return this;
    }
    public final NodeAddress getDst() {
        return new NodeAddress(data[DST_INDEX], data[DST_INDEX + 1]);
    }
    public final USDNnetworkPacket setDst(final byte valueH, final byte valueL) {
        data[DST_INDEX] = valueH;
        data[DST_INDEX + 1] = valueL;
        return this;
    }
    public final USDNnetworkPacket setDst(final NodeAddress address) {
        setDst(address.getHigh(), address.getLow());
        return this;
    }

    public final int getTyp() {
        return data[TYP_INDEX];
    }
    public final USDNnetworkPacket setTyp(final byte value) {
        data[TYP_INDEX] = value;
        return this;
    }
    public final int getTtl() {
        return Byte.toUnsignedInt(data[TTL_INDEX]);
    }
    public final USDNnetworkPacket setTtl(final byte value) {
        data[TTL_INDEX] = value;
        return this;
    }
    public final USDNnetworkPacket decrementTtl() {
        if (data[TTL_INDEX] > 0) {
            data[TTL_INDEX]--;
        }
        return this;
    }
    public final NodeAddress getNxh() {
        return new NodeAddress(data[NXH_INDEX], data[NXH_INDEX + 1]);
    }
    public final USDNnetworkPacket setNxh(final byte valueH, final byte valueL) {
        data[NXH_INDEX] = valueH;
        data[NXH_INDEX + 1] = valueL;
        return this;
    }
    public final USDNnetworkPacket setNxh(final NodeAddress address) {
        setNxh(address.getHigh(), address.getLow());
        return this;
    }

    public final USDNnetworkPacket setNxh(final String address) {
        setNxh(new NodeAddress(address));
        return this;
    }

    public final int getPayloadSize() {
        return (getLen() - DFLT_HDR_LEN);
    }
    @Override
    public final String toString() {
        return Arrays.toString(toIntArray());
    }
    public final byte[] toByteArray() {
        return Arrays.copyOf(data, getLen());
    }

    public final int[] toIntArray() {
        int[] tmp = new int[getLen()];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = Byte.toUnsignedInt(data[i]);
        }
        return tmp;
    }
    @Override
    public final USDNnetworkPacket clone() throws CloneNotSupportedException {
        super.clone();
        return new USDNnetworkPacket(data.clone());
    }
    public final boolean isUSdn() {
        return (Byte.toUnsignedInt(data[NET_INDEX]) < THRES);
    }
    private byte[] fromIntArrayToByteArray(final int[] array) {
        byte[] dataToByte = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            dataToByte[i] = (byte) array[i];
        }
        return dataToByte;
    }
    protected final byte[] getPayload() {
        return Arrays.copyOfRange(data, DFLT_HDR_LEN,
                getLen());
    }
    protected final USDNnetworkPacket setPayload(final byte[] p) {
        if (p.length + DFLT_HDR_LEN <= MAX_PACKET_LENGTH) {
            System.arraycopy(p, 0, data, DFLT_HDR_LEN, p.length);
            setLen((byte) (p.length + DFLT_HDR_LEN));
        } else {
            throw new IllegalArgumentException("Payload exceeds packet size");
        }
        return this;
    }

    protected final USDNnetworkPacket setPayloadSize(final int size) {
        if (DFLT_HDR_LEN + size <= MAX_PACKET_LENGTH) {
            setLen((byte) (DFLT_HDR_LEN + size));
        } else {
            throw new IllegalArgumentException("Index cannot be greater than "
                    + "the maximum payload size: " + size);
        }
        return this;
    }
    protected final USDNnetworkPacket setPayloadAt(final byte d, final int i) {
        if (DFLT_HDR_LEN + i < MAX_PACKET_LENGTH) {
            data[DFLT_HDR_LEN + i] = d;
            if ((i + DFLT_HDR_LEN) >= getLen()) {
                setLen((byte) (DFLT_HDR_LEN + i + 1));
            }
        } else {
            throw new IllegalArgumentException("Index cannot be greater than "
                    + "the maximum payload size");
        }
        return this;
    }
    protected final USDNnetworkPacket setPayload(final byte[] src,
                                             final int srcPos,
                                             final int payloadPos,
                                             final int length) {

        if (srcPos < 0 || payloadPos < 0 || length < 0) {
            throw new IllegalArgumentException("Negative index");
        } else {
            copyPayload(src, srcPos, payloadPos, length);
            setPayloadSize(length + payloadPos);
        }
        return this;
    }
    protected final USDNnetworkPacket copyPayload(final byte[] src,
                                              final int srcPos,
                                              final int payloadPos,
                                              final int length) {
        for (int i = 0; i < length; i++) {
            setPayloadAt(src[i + srcPos], i + payloadPos);
        }
        return this;
    }
    protected final byte getPayloadAt(final int i) {
        if (i + DFLT_HDR_LEN < getLen()) {
            return data[DFLT_HDR_LEN + i];
        } else {
            throw new IllegalArgumentException("Index cannot be greater than "
                    + "the maximum payload size");
        }
    }
    protected final byte[] getPayloadFromTo(final int start, final int stop) {
        if (start > stop) {
            throw new IllegalArgumentException(
                    "Start must be equal or less than stop");
        }
        if (stop < 0) {
            throw new IllegalArgumentException(
                    "Stop must be greater than 0");
        }
        if (start + DFLT_HDR_LEN > getLen()) {
            throw new IllegalArgumentException(
                    "Start is greater than packet size");
        }
        int newStop = Math.min(stop + DFLT_HDR_LEN, getLen());
        return Arrays.copyOfRange(data, start + DFLT_HDR_LEN, newStop);
    }
    protected final byte[] copyPayloadOfRange(final int start, final int end) {
        return Arrays.copyOfRange(data, DFLT_HDR_LEN + start,
                DFLT_HDR_LEN + end);
    }


}
