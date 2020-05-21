package com.github.usdn.util;

public final class Neighbor {

    private final NodeAddress addr;

    private final int memory,batt;

    private final int DEFAULT=0XFFFF;

    public Neighbor(final NodeAddress a, final int m, final int b){
        addr = a;
        memory = 1024;
        batt = DEFAULT;
    }

    public NodeAddress getAddr() {
        return addr;
    }

    public int getMemory(){
        return memory;
    }

    public int getBatt(){
        return batt;
    }

}
