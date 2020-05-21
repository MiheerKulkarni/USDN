package com.github.usdn.flowtable;

public final class AcceptAction extends AbstractAction {

    private static final byte SIZE = 0;

    public AcceptAction()
    {
        super(Action.SDN_FT_ACTION_ACCEPT,SIZE);
    }
    public AcceptAction(final byte[] array) {
        super(array);
    }
}
