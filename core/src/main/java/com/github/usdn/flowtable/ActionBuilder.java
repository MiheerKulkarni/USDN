package com.github.usdn.flowtable;
import static com.github.usdn.flowtable.AbstractAction.*;
public final class ActionBuilder {

    public static AbstractAction build(final String val) {
        switch (Action.valueOf(val.split(" ")[0])) {
            case SDN_FT_ACTION_FORWARD :
                return new ForwardAction(val);
            case SDN_FT_ACTION_CALLBACK:
                return new CallbackAction();
            case SDN_FT_ACTION_MODIFY:
                return new ModifyAction(val);
            case SDN_FT_ACTION_ACCEPT:
                return new AcceptAction();
            case SDN_FT_ACTION_QUERY:
                return new QueryAction();
            case SDN_FT_ACTION_FALLBACK:
                return new FallBackAction(val);
            case SDN_FT_ACTION_DROP:
                return new DropAction();
            default:
                throw new IllegalArgumentException();
        }
    }

    public static AbstractAction build(final byte[] array) {
        switch (Action.fromByte(array[TYPE_INDEX])) {
            case SDN_FT_ACTION_FORWARD:
                return new ForwardAction(array);
            case SDN_FT_ACTION_CALLBACK:
                return new CallbackAction(array);
            case SDN_FT_ACTION_DROP:
                return new DropAction(array);
            case SDN_FT_ACTION_FALLBACK:
                return new FallBackAction(array);
            case SDN_FT_ACTION_QUERY:
                return new QueryAction(array);
            case SDN_FT_ACTION_MODIFY:
                return new ModifyAction(array);
            case SDN_FT_ACTION_ACCEPT:
                return new AcceptAction(array);
            default:
                throw new IllegalArgumentException();
        }
    }

    private ActionBuilder() {
    }
}
