package com.github.usdn.flowtable;

import java.util.Arrays;

public final class FallBackAction  extends AbstractAction{

    private static final byte ARGS_INDEX = 1, ID_INDEX = 0;

    public FallBackAction(final byte[] value) {
        super(value);
    }

    public FallBackAction(final String str) {
        super(Action.SDN_FT_ACTION_FALLBACK, 0);
        String[] tmp = str.split(" ");
        if (tmp[0].equals(Action.SDN_FT_ACTION_FALLBACK.name())) {
            byte[] args = new byte[tmp.length - 1];
            for (int i = 0; i < args.length; i++) {
                args[i] = (byte) (Integer.parseInt(tmp[i + 1]));
            }
            setValue(args);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public byte[] getArgs() {
        byte[] value = getValue();
        return Arrays.copyOfRange(value, ARGS_INDEX, value.length);
    }

    public int getId() {
        return getValue(ID_INDEX);
    }
    public FallBackAction setArgs(final byte[] args) {
        int i = 0;
        for (byte b : args) {
            setValue(ARGS_INDEX + i, b);
            i++;
        }
        return this;
    }
    public FallBackAction setId(final int id) {
        setValue(ID_INDEX, id);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(Action.SDN_FT_ACTION_FALLBACK.name());
        sb.append(' ').append(getId()).append(' ');
        for (byte b : getArgs()) {
            sb.append(Byte.toUnsignedInt(b)).append(' ');
        }
        return sb.toString();
    }


}
