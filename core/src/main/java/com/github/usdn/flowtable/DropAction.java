package com.github.usdn.flowtable;

public class DropAction extends AbstractAction {

    private static final byte SIZE = 0;

    public DropAction() {
        super(Action.SDN_FT_ACTION_DROP, SIZE);
    }

    public DropAction(final byte[] value) {
        super(value);
    }
}
