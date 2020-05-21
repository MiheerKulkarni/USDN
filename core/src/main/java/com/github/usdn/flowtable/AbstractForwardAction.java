package com.github.usdn.flowtable;

import com.github.usdn.util.NodeAddress;

public abstract class AbstractForwardAction extends AbstractAction {

    protected static final byte NXH_INDEX =0;

    public AbstractForwardAction(final Action actiontype)
    {
        super(actiontype,2);
    }

    public AbstractForwardAction(final byte[] value) {
        super(value);
    }

    public final AbstractForwardAction setNextHop(final NodeAddress addr) {
        setValue(NXH_INDEX, addr.getHigh());
        setValue(NXH_INDEX + 1, addr.getLow());
        return this;
    }

    public final NodeAddress getNextHop() {
        return new NodeAddress(getValue(NXH_INDEX), getValue(NXH_INDEX + 1));
    }
}
