package com.github.usdn.function;

import com.github.usdn.flowtable.FlowtableEntry;
import com.github.usdn.packet.USDNnetworkPacket;
import com.github.usdn.util.Neighbor;
import com.github.usdn.util.NodeAddress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

public interface FunctionInterface {

    void function(
            HashMap<String, Object> adcRegister,
            List<FlowtableEntry> flowTable,
            Set<Neighbor> neighborTable,
            ArrayList<Integer> statusRegister,
            List<NodeAddress> acceptedId,
            ArrayBlockingQueue<USDNnetworkPacket> flowTableQueue,
            ArrayBlockingQueue<USDNnetworkPacket> txQueue,
            byte[] args,
            USDNnetworkPacket np
    );
}
