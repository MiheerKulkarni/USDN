package com.github.usdn.packet;

import com.github.usdn.util.NodeAddress;
import com.github.usdn.util.Utils;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class RegProxyPacket extends USDNnetworkPacket {

    private static final int DPID_INDEX = 0, DPID_LEN = 8,
            IP_LEN = 4,
            MAC_INDEX = DPID_INDEX + DPID_LEN, MAC_LEN = 6, MAC_STR_LEN = 18,
            PORT_INDEX = MAC_INDEX + MAC_LEN, PORT_LEN = 8,
            IP_INDEX = PORT_INDEX + PORT_LEN, RADIX = 16,
            TCP_INDEX = IP_INDEX + IP_LEN;

    public RegProxyPacket(final byte[] data) {
        super(data);
    }
    public RegProxyPacket(final USDNnetworkPacket data) {
        super(data.toByteArray());
    }

    public RegProxyPacket(final int net, final NodeAddress src,
                          final String dPid,
                          final String mac,
                          final long port,
                          final InetSocketAddress isa) {
        super(net, src, src);
        setTyp(REG_PROXY);
        setMac(mac);
        setDpid(dPid);
        setPort(port);
        setNxh(src);
        setInetSocketAddress(isa);
    }

    public RegProxyPacket(final int[] data) {
        super(data);
    }
    public final String getDpid() {
        return new String(getPayloadFromTo(DPID_INDEX, MAC_INDEX));
    }

    public final InetSocketAddress getInetSocketAddress() {
        try {
            byte[] ip = getPayloadFromTo(IP_INDEX, IP_INDEX + IP_LEN);
            return new InetSocketAddress(InetAddress.getByAddress(ip),
                    Utils.mergeBytes(getPayloadAt(TCP_INDEX),
                            getPayloadAt(TCP_INDEX + 1)));
        } catch (UnknownHostException ex) {
            return null;
        }
    }
    public final String getMac() {
        StringBuilder sb = new StringBuilder(MAC_STR_LEN);
        byte[] mac = getPayloadFromTo(MAC_INDEX, MAC_INDEX + MAC_LEN);
        for (byte b : mac) {
            if (sb.length() > 0) {
                sb.append(':');
            }
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    public final long getPort() {
        return new BigInteger(getPayloadFromTo(PORT_INDEX, PORT_INDEX
                + PORT_LEN)).longValue();
    }

    public final RegProxyPacket setDpid(final String dPid) {
        byte[] dpid = dPid.getBytes(Charset.forName("UTF-8"));
        int len = Math.min(DPID_LEN, dpid.length);
        setPayload(dpid, 0, DPID_INDEX, len);
        return this;
    }
    public final RegProxyPacket setInetSocketAddress(
            final InetSocketAddress isa) {
        byte[] ip = isa.getAddress().getAddress();
        int port = isa.getPort();
        return setInetSocketAddress(ip, port);
    }
    public final RegProxyPacket setMac(final String mac) {
        String[] elements = mac.split(":");
        if (elements.length != MAC_LEN) {
            throw new IllegalArgumentException("Invalid MAC address");
        }
        for (int i = 0; i < MAC_LEN; i++) {
            setPayloadAt((byte) Integer.parseInt(elements[i], RADIX),
                    MAC_INDEX + i);
        }
        return this;
    }
    public final RegProxyPacket setPort(final long port) {
        byte[] bytes = ByteBuffer
                .allocate(Long.SIZE / Byte.SIZE).putLong(port).array();
        setPayload(bytes, (byte) 0, PORT_INDEX, PORT_LEN);
        return this;
    }

    private RegProxyPacket setInetSocketAddress(final byte[] ip, final int p) {
        setPayload(ip, 0, IP_INDEX, IP_LEN);
        setPayloadAt((byte) (p), TCP_INDEX + 1);
        setPayloadAt((byte) (p >> Byte.SIZE), TCP_INDEX);
        return this;
    }
}
