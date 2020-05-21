package com.github.usdn.motes.standalone;

import com.github.usdn.motes.battery.Dischargeable;
import com.github.usdn.motes.battery.SinkBattery;
import com.github.usdn.motes.core.SinkCore;
import com.github.usdn.packet.USDNnetworkPacket;
import com.github.usdn.util.NodeAddress;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Sink extends AbstractMote{

    private final InetSocketAddress ctrl;

    private DataInputStream dis;
     private int MAX_RSSI=150;
    private DataOutputStream dos;

    private Socket tcpSocket;

    public Sink(final byte net, final NodeAddress myAddress, final int port,
                final InetSocketAddress controller,
                final String neighboursPath, final String logLevel,
                final String dpid, final String mac, final long sPort) {

        super(port, neighboursPath, logLevel);
        ctrl = controller;
        Dischargeable battery = new SinkBattery();
        setCore(new SinkCore(net, myAddress, battery, dpid, mac, sPort, ctrl))
                .start();
    }

    @Override
    protected final void startThreads() {
        super.startThreads();
        try {
            tcpSocket = new Socket(ctrl.getAddress(), ctrl.getPort());
            new Thread(new TcpListener()).start();
            new Thread(new TcpSender()).start();
        } catch (IOException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }

    }

    private class TcpListener implements Runnable {

        @Override
        public void run() {
            try {
                dis = new DataInputStream(tcpSocket.getInputStream());
                while (true) {
                    USDNnetworkPacket np = new USDNnetworkPacket(dis);
                    getCore().rxRadioPacket(np, MAX_RSSI);
                }
            } catch (IOException ex) {
                Logger.getGlobal().log(Level.SEVERE, null, ex);
            }
        }
    }
    private final class TcpSender implements Runnable {

        @Override
        public void run() {
            try {
                dos = new DataOutputStream(tcpSocket.getOutputStream());
                while (true) {
                    USDNnetworkPacket np = ((SinkCore) getCore())
                            .getControllerPacketTobeSend();
                    dos.write(np.toByteArray());
                }
            } catch (IOException | InterruptedException ex) {
                Logger.getGlobal().log(Level.SEVERE, null, ex);
            }
        }
    }
}
