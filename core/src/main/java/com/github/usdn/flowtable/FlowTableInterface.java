package com.github.usdn.flowtable;

public interface FlowTableInterface {
    byte CONST=1,NULL=0, PACKET=2,STATUS = 3;

    byte[] toByteArray();
}
