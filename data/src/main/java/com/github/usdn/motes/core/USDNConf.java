package com.github.usdn.motes.core;

public class USDNConf {

    public static final int USDN_DFLT_RSSI = 180;

    public static final byte USDN_DFLT_CNT_DATA_MAX = 10,
            USDN_DFLT_RPL_MAX = 10,
            USDN_NODES_REPORT_MAX = 2 * USDN_DFLT_RPL_MAX,
            USDN_DFLT_CNT_UPDTABLE_MAX = 6;

    public static final int USDN_COM_START_BYTE = 0x7A,
            USDN_COM_STOP_BYTE = 0x7E;

    public static final boolean USDN_MAC_SEND_UNICAST = false,
            USDN_MAC_SEND_BROADCAST = true;

    private USDNConf() {
    }
}
