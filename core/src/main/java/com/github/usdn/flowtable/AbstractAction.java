package com.github.usdn.flowtable;

import java.nio.ByteBuffer;
import java.util.Arrays;

public abstract class  AbstractAction implements FlowTableInterface {
    public enum Action
    {
        SDN_FT_ACTION_ACCEPT(0),                      /**< accept and pass to upper layers */
        SDN_FT_ACTION_DROP(1),                        /**< drop the packet */
        SDN_FT_ACTION_QUERY(2),                       /**< query the controller */
        SDN_FT_ACTION_FORWARD(3),                     /**< forward to neighbor */
        SDN_FT_ACTION_MODIFY(4),                      /**< modify the packet */
        SDN_FT_ACTION_FALLBACK(5),                    /**< send to the fallback interface */
        SDN_FT_ACTION_SRH(6),
        SDN_FT_ACTION_CALLBACK(7);

        private final byte action_code;

        private static final Action[] A_VALUES = Action.values();
        public static Action fromByte(final byte value) {
            return A_VALUES[value];
        }
        Action(final int v) {
            action_code = (byte) v;
        }

    }

    protected static final int TYPE_INDEX = 0;
    protected static final int VALUE_INDEX = 1;

    private byte[] action;

    public AbstractAction(final Action actionType, final int size) {
        action = new byte[size + 1];
        setType(actionType);
    }
    public AbstractAction(final byte[] value) {
        action = value;
    }
    public final Action getType() {
        return Action.fromByte(action[TYPE_INDEX]);
    }
    @Override
    public final byte[] toByteArray() {
        return Arrays.copyOf(action, action.length);
    }
    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractAction other = (AbstractAction) obj;
        return Arrays.equals(other.action, action);
    }

    @Override
    public final int hashCode() {
        return Arrays.hashCode(action);
    }

    protected final AbstractAction setType(final Action value) {
        action[TYPE_INDEX] = value.action_code;
        return this;
    }
    protected final AbstractAction setValue(final int index, final int value) {
        if (index < 0 || index >= action.length) {
            throw new ArrayIndexOutOfBoundsException("Index out of bound");
        } else {
            action[index + 1] = (byte) value;
        }
        return this;
    }
    protected final AbstractAction setValue(final byte[] value) {
        Action type = getType();
        action = ByteBuffer.allocate(value.length + 1)
                .put(type.action_code)
                .put(value).array();
        return this;
    }

    protected final byte[] getValue() {
        return Arrays.copyOfRange(action, VALUE_INDEX, action.length);
    }

    protected final int getValue(final int index) {
        if (index < 0 || index >= action.length) {
            throw new ArrayIndexOutOfBoundsException("Index out of bound");
        } else {
            return action[index + 1];
        }
    }
    @Override
    public String toString() {
        return getType().name();
    }
    protected final int getActionLength() {
        return action.length;
    }
    protected final int getValueLength() {
        return action.length - 1;
    }
    

}
