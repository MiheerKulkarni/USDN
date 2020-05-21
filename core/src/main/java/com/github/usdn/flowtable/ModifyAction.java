package com.github.usdn.flowtable;

public final class ModifyAction extends AbstractAction{

    private static final byte SIZE = 0;

    private static  final byte Lport_status=2,Rport_status=1;

    public ModifyAction(String status) {

        super(Action.SDN_FT_ACTION_MODIFY,SIZE);

        String [] operands = status.split(" ");
        if (operands.length == Lport_status)
        {

        }


    }

    public ModifyAction(final byte[] value) {
        super(value);
    }
}
