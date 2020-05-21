package com.github.usdn.motes.standalone;

import com.github.usdn.motes.battery.Battery;
import com.github.usdn.motes.battery.Dischargeable;
import com.github.usdn.motes.core.MoteCore;
import com.github.usdn.util.NodeAddress;

public class Mote extends AbstractMote{

    public Mote(final byte net,
                final NodeAddress myAddress,
                final int port,
                final String neighboursPath,
                final String logLevel) {
        super(port, neighboursPath, logLevel);
        Dischargeable battery = new Battery();
        setCore(new MoteCore(net, myAddress, battery)).start();
    }
}
