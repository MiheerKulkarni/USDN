package com.github.usdn.flowtable;

import com.github.usdn.util.NodeAddress;

import static com.github.usdn.flowtable.AbstractAction.Action.SDN_FT_ACTION_FORWARD;

public class ForwardAction extends AbstractForwardAction {

    public ForwardAction(final String str)
    {
        super(SDN_FT_ACTION_FORWARD);
        if(SDN_FT_ACTION_FORWARD.name().equals(str.split(" ")[0].trim()))
        {
            setNextHop(new NodeAddress(str.split(" ")[1].trim()));
        }
        else
        {
            throw new IllegalArgumentException();
        }

    }

    public ForwardAction(final byte[] value)
    {
        super(value);
    }

    public ForwardAction(final NodeAddress nextHop) {
        super(SDN_FT_ACTION_FORWARD);
        setNextHop(nextHop);
    }

    @Override
    public String toString()
    {
        return SDN_FT_ACTION_FORWARD.name() + " " + getNextHop().intValue();
    }
}
