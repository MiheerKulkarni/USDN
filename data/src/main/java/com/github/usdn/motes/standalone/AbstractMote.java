package com.github.usdn.motes.standalone;

import com.github.usdn.function.SimpleFormatter;
import com.github.usdn.motes.core.AbstractCore;
import com.github.usdn.motes.core.Pair;
import com.github.usdn.motes.logger.MoteFormatter;
import com.github.usdn.packet.USDNnetworkPacket;
import com.github.usdn.util.NodeAddress;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.usdn.packet.USDNnetworkPacket.DATA;

public abstract class AbstractMote implements Runnable {

    private static final int SECOND_IN_MILLIS = 1000;
    private static final int MAX_RSSI = 150;

    private final byte[] buf = new byte[USDNnetworkPacket.MAX_PACKET_LENGTH];
    private final Level level;

    private Logger logger, measureLogger;

    private final String neighborFilePath;

    private ConcurrentMap<Object, Object> neighbourList;

    private final int port;
    private int receivedBytes, receivedDataBytes, sentBytes, sentDataBytes;

    private DatagramSocket socket;

    private AbstractCore core;

    public AbstractMote(
            final int p,
            final String nfp,
            final String lvl) {
        this.neighborFilePath = nfp;
        this.neighbourList = (ConcurrentMap<Object, Object>) new HashMap<Object, Object>();
        this.port = p;
        this.level = Level.parse(lvl);
    }
    public final void logger() {
        measureLogger.log(Level.FINEST,
                "{0};{1};{2};{3};{4};{5};{6};{7};",
                new Object[]{core.getMyAddress(),
                        String.valueOf(core.getBattery().getLevel()),
                        String.valueOf(core.getBattery().getByteLevel()),
                        core.getFlowTableSize(),
                        sentBytes, receivedBytes,
                        sentDataBytes, receivedDataBytes});
    }
    public final void radioTX(final USDNnetworkPacket np) {

        if (np.isUSdn()) {
            sentBytes += np.getLen();
            if (DATA == np.getTyp()) {
                sentDataBytes += np.getPayloadSize();
            }
        }

        core.getBattery().transmitRadio(np.getLen());

        logger.log(Level.FINE, "RTX {0}", np);

        NodeAddress tmpNxHop = np.getNxh();
        NodeAddress tmpDst = np.getDst();

        if (tmpDst.isBroadcast() || tmpNxHop.isBroadcast()) {

            neighbourList.entrySet().stream()
                    .map((isa) -> new DatagramPacket(np.toByteArray(),
                            np.getLen(),2)).forEach((pck) -> {
                try {
                    socket.send(pck);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            });
        } else {
            FakeInfo isa = (FakeInfo) neighbourList.get(tmpNxHop);
            if (isa != null) {
                try {
                    DatagramPacket pck = new DatagramPacket(np.toByteArray(),
                            np.getLen(), isa.inetAddress);
                    socket.send(pck);

                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public final void run() {
        try {

            measureLogger = initLogger(Level.FINEST, "M_" + core.getMyAddress()
                    + ".log", new MoteFormatter());

            logger = initLogger(level, core.getMyAddress()
                    + ".log", new SimpleFormatter(core.getMyAddress().toString()));

            Path path = Paths.get(neighborFilePath);
            BufferedReader reader;

            if (!Files.exists(path)) {
                logger.log(Level.INFO, "External Config file not found. "
                        + "Loading default values.");
                InputStream in = getClass()
                        .getResourceAsStream("/" + neighborFilePath);
                reader = new BufferedReader(new InputStreamReader(in));
            } else {
                reader = new BufferedReader(new FileReader(neighborFilePath));
            }

            try (Stream<String> lines = reader.lines()) {
                neighbourList = lines.parallel()
                        .map(line -> line.trim())
                        .filter(line -> !line.isEmpty())
                        .map(line -> line.split(","))
                        .map(e -> new Object() {
                                    private final NodeAddress addr =
                                            new NodeAddress(e[0]);
                                    private final FakeInfo fk = new FakeInfo(
                                            new InetSocketAddress(e[1],
                                                    Integer.parseInt(e[2])
                                            ), Integer.parseInt(e[3])
                                    );
                                }
                        ).collect(Collectors
                                .toConcurrentMap(e -> e.addr, e -> e.fk));
            }

            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket = new DatagramSocket(port);

            new Timer().schedule(new TaskTimer(), SECOND_IN_MILLIS,
                    SECOND_IN_MILLIS);
            startThreads();

            while (core.getBattery().getByteLevel() > 0) {
                socket.receive(packet);
                USDNnetworkPacket np = new USDNnetworkPacket(packet.getData());
                int rssi = MAX_RSSI;
                if (np.isUSdn()) {
                    logger.log(Level.FINE, "RRX {0}", np);
                    FakeInfo fk = (FakeInfo) neighbourList.get(np.getSrc());
                    if (fk != null) {
                        rssi = fk.rssi;
                    } else {
                        rssi = MAX_RSSI;
                    }

                    if (DATA == np.getTyp()) {
                        receivedDataBytes += np.getPayloadSize();
                    }
                }
                core.rxRadioPacket(np, rssi);
            }
        } catch (IOException | RuntimeException ex) {
            logger.log(Level.SEVERE, ex.toString());
        }
    }

    private Logger initLogger(
            final Level lvl,
            final String file,
            final Formatter formatter) {
        Logger log = Logger.getLogger(file);
        log.setLevel(lvl);
        try {
            FileHandler fh;
            File dir = new File("logs");
            dir.mkdir();
            fh = new FileHandler("logs/" + file);
            fh.setFormatter(formatter);
            log.addHandler(fh);
            log.setUseParentHandlers(false);
        } catch (IOException | SecurityException ex) {
            log.log(Level.SEVERE, null, ex);
        }
        return log;
    }
    protected void startThreads() {
        new Thread(new SenderRunnable()).start();
        new Thread(new LoggerRunnable()).start();
    }
    private class FakeInfo {

        public InetSocketAddress inetAddress;

        private int rssi;

        FakeInfo(final InetSocketAddress ia, final int fakeRssi) {
            this.inetAddress = ia;
            this.rssi = fakeRssi;
        }
    }
    private class LoggerRunnable implements Runnable {

        @Override
        public void run() {
            try {
                while (true) {
                    Pair<Level, String> tmp = core.getLogToBePrinted();
                    logger.log(tmp.getKey(), tmp.getValue());
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.toString());
            }
        }
    }

    private class SenderRunnable implements Runnable {

        @Override
        public void run() {
            try {
                while (true) {
                    radioTX(core.getNetworkPacketToBeSend());
                }
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, ex.toString());
            }
        }
    }
    private class TaskTimer extends TimerTask {

        @Override
        public void run() {
            if (core.getBattery().getByteLevel() > 0) {
                core.timer();
                core.getBattery().keepAlive(1);
            }
            logger();
        }
    }
    public final AbstractCore getCore() {
        return core;
    }
    public final AbstractCore setCore(final AbstractCore cr) {
        core = cr;
        return core;
    }

}
