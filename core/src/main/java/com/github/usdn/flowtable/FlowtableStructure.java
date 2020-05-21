package com.github.usdn.flowtable;

import com.github.usdn.packet.USDNnetworkPacket;
import com.github.usdn.util.Utils;

import java.util.Arrays;

import static com.github.usdn.util.Utils.getBitRange;
import static com.github.usdn.util.Utils.setBitRange;

public class FlowtableStructure implements FlowTableInterface {

    public static final byte EQUAL = 0,
            GREATER = 2,
            GREATER_OR_EQUAL = 4,
            LESS = 3,
            LESS_OR_EQUAL = 5,
            NOT_EQUAL = 1;
    public static final byte SIZE = 5;

    private static final byte LEFT_BIT = 3, LEFT_INDEX_H = 1, LEFT_INDEX_L = 2,
            LEFT_LEN = 2, OP_BIT = 5, OP_INDEX = 0, OP_LEN = 3,
            RIGHT_BIT = 1, RIGHT_INDEX_H = 3, RIGHT_INDEX_L = 4,
            RIGHT_LEN = LEFT_LEN,
            SIZE_BIT = 0,
            SIZE_LEN = 1, WIN_LEN = 3;

    private final byte[] entry = new byte[SIZE];

    public static FlowtableStructure fromString(final String val) {
        FlowtableStructure w = new FlowtableStructure();
        String[] operands = val.split(" ");
        if (operands.length == WIN_LEN) {
            String lhs = operands[0];
            int[] tmpLhs = FlowtableStructure.getOperandFromString(lhs);
            w.setLhsLocation(tmpLhs[0]);
            w.setLhs(tmpLhs[1]);
            w.setOperator(w.getOperatorFromString(operands[1]));

            String rhs = operands[2];
            int[] tmpRhs = FlowtableStructure.getOperandFromString(rhs);
            w.setRhsLocation(tmpRhs[0]);
            w.setRhs(tmpRhs[1]);

            if ("P.SRC".equals(lhs)
                    || "P.DST".equals(lhs)
                    || "P.NXH".equals(lhs)
                    || "P.SRC".equals(rhs)
                    || "P.DST".equals(rhs)
                    || "P.NXH".equals(rhs)) {
                w.setSize(1);
            }
        }
        return w;
    }

    public FlowtableStructure() {
        Arrays.fill(entry, (byte) 0);
    }
    public FlowtableStructure(final byte[] value) {
        if (value.length == SIZE) {
            System.arraycopy(value, 0, entry, 0, value.length);
        } else {
            Arrays.fill(entry, (byte) 0);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FlowtableStructure other = (FlowtableStructure) obj;
        return Arrays.equals(other.entry, entry);
    }

    public int getLhs() {
        return Utils.mergeBytes(entry[LEFT_INDEX_H], entry[LEFT_INDEX_L]);
    }

    public int getLhsLocation() {
        return getBitRange(entry[OP_INDEX], LEFT_BIT, LEFT_LEN);
    }
    public String getLhsToString() {
        switch (getLhsLocation()) {
            case CONST:
                return String.valueOf(getLhs());
            case PACKET:
                return "Packet sent" + USDNnetworkPacket.getNetworkPacketByteName(getLhs());
            case STATUS:
                return "Recevied status" + getLhs();
            default:
                return "";
        }
    }

    public static int[] getOperandFromString(final String val) {
        int[] tmp = new int[2];
        String[] strVal = val.split("\\.");
        switch (strVal[0]) {
            case "P":
                tmp[0] = PACKET;
                break;
            case "R":
                tmp[0] = STATUS;
                break;
            default:
                tmp[0] = CONST;
                break;
        }

        switch (tmp[0]) {
            case PACKET:
                tmp[1] = USDNnetworkPacket.getNetworkPacketByteFromName(strVal[1]);
                break;
            case CONST:
                tmp[1] = Integer.parseInt(strVal[0]);
                break;
            default:
                tmp[1] = Integer.parseInt(strVal[1]);
                break;
        }
        return tmp;
    }
    public int getOperator() {
        return getBitRange(entry[OP_INDEX], OP_BIT, OP_LEN);
    }
    private int getOperatorFromString(final String val) {
        switch (val) {
            case ("=="):
                return EQUAL;
            case ("!="):
                return NOT_EQUAL;
            case (">"):
                return GREATER;
            case ("<"):
                return LESS;
            case (">="):
                return GREATER_OR_EQUAL;
            case ("<="):
                return LESS_OR_EQUAL;
            default:
                throw new IllegalArgumentException();
        }
    }
    public String getOperatorToString() {
        switch (getOperator()) {
            case (EQUAL):
                return " == ";
            case (NOT_EQUAL):
                return " != ";
            case (GREATER):
                return " > ";
            case (LESS):
                return " < ";
            case (GREATER_OR_EQUAL):
                return " >= ";
            case (LESS_OR_EQUAL):
                return " <= ";
            default:
                return "";
        }
    }
    public int getRhs() {
        return Utils.mergeBytes(entry[RIGHT_INDEX_H], entry[RIGHT_INDEX_L]);
    }
    public int getRhsLocation() {
        return getBitRange(entry[OP_INDEX], RIGHT_BIT, RIGHT_LEN);
    }
    public String getRhsToString() {
        switch (getRhsLocation()) {
            case CONST:
                return String.valueOf(getRhs());
            case PACKET:
                return "Packet Received" + USDNnetworkPacket.getNetworkPacketByteName(getRhs());
            case STATUS:
                return "Received Status." + getRhs();
            default:
                return "";
        }
    }
    public int getSize() {
        return getBitRange(entry[OP_INDEX], SIZE_BIT, SIZE_LEN);
    }

    public String getSizeToString() {
        return String.valueOf(getSize() + 1);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(entry);
    }
    public FlowtableStructure setLhs(final int val) {
        entry[LEFT_INDEX_H] = (byte) (val >>> Byte.SIZE);
        entry[LEFT_INDEX_L] = (byte) val;
        return this;
    }
    public FlowtableStructure setLhsLocation(final int value) {
        entry[OP_INDEX] = (byte) setBitRange(
                entry[OP_INDEX], LEFT_BIT, LEFT_LEN, value);
        return this;
    }
    public FlowtableStructure setOperator(final int value) {
        entry[OP_INDEX] = (byte) setBitRange(
                entry[OP_INDEX], OP_BIT, OP_LEN, value);
        return this;
    }
    public FlowtableStructure setRhs(final int val) {
        entry[RIGHT_INDEX_H] = (byte) (val >>> Byte.SIZE);
        entry[RIGHT_INDEX_L] = (byte) val;
        return this;
    }

    public FlowtableStructure setRhsLocation(final int value) {
        entry[OP_INDEX] = (byte) setBitRange(
                entry[OP_INDEX], RIGHT_BIT, RIGHT_LEN, value);
        return this;
    }
    public FlowtableStructure setSize(final int value) {
        entry[OP_INDEX] = (byte) setBitRange(
                entry[OP_INDEX], SIZE_BIT, SIZE_LEN, value);
        return this;
    }

    @Override
    public byte[] toByteArray() {
        return Arrays.copyOf(entry, SIZE);
    }

    @Override
    public String toString() {
        return getLhsToString() + getOperatorToString() + getRhsToString();
    }
}
