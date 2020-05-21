package com.github.usdn.flowtable;

import static com.github.usdn.flowtable.AbstractAction.Action.SDN_FT_ACTION_CALLBACK;
public final class CallbackAction extends AbstractAction {

    public CallbackAction()
    {
        super(SDN_FT_ACTION_CALLBACK,1);
        /*setNextHop(BROADCAST_ADDR);*/
    }

    public CallbackAction(final byte[] value) {
        super(value);
    }
}
