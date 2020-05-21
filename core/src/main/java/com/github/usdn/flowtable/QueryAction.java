package com.github.usdn.flowtable;

public class QueryAction extends AbstractAction {

    private static final byte SIZE=0;
    public QueryAction()
    {
        super(Action.SDN_FT_ACTION_QUERY,SIZE);

    }
    public QueryAction(final byte[] value) {
        super(value);
    }
}
