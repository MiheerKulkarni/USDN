package com.github.usdn.util;

public class usdnconf {
    byte SDN_CONF_FORCE_UPDATE = 0;
    byte SDN_CONF_REFRESH_LIFETIME_ON_HIT = 1;
    byte SDN_CONF_QUERY_FULL_PACKET = 0;
    byte SDN_CONF_RETRY_AFTER_QUERY=0;

    byte SDN_CONF_DEFAULT_NET=0;

    int SDN_FT_LIFETIME = 0xFFFF;
    int SDN_CONF_QUERY_INDEX = 0;
    int SDN_CONF_QUERY_LENGTH = 16;

    int  SDN_CONF_CONTROLLER_IP = 0xaaa;
    int SDN_CONF_CONTROLLER_UPDATE_PERIOD = 600;

    String SDN_CONF_CONTROLLER_INIT_STATE = "CTRL_CONNECTING";
    String SDN_CONF_CONTROLLER_CONN_TYPE = "SDN_CONN_TYPE_USDN";

    int SDN_CONF_CONTROLLER_CONN_DATA = 1234>>8;
    int SDN_CONF_CONTROLLER_CONN_LENGTH = 4;

    int SDN_CONF_CONTROLLER_CONN_LENGTH_MAX = 10;

    public int sdn_net = 0;
    public int cfg_id = 0;
    public int hops = 0;

    public int query_full = 0;
    public int query_idx = 0;
    public int query_len = 0;

    public int rpl_dio_interval = 0;
    public int rpl_dptr_interval=0;


}
