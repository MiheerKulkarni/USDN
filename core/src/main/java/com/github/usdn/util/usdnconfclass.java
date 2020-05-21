package com.github.usdn.util;

import com.github.usdn.function.SimpleFormatter;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class usdnconfclass {
    usdnconf sdn_conf;
    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    // suppress the logging output to the console
    Handler[] handlers = logger.getHandlers();

    public void sdn_init()
    {
        sdn_conf.sdn_net = sdn_conf.SDN_CONF_DEFAULT_NET;
        sdn_conf.cfg_id = 0;
        sdn_conf.hops =   0;
        sdn_conf.SDN_FT_LIFETIME =((sdn_conf.SDN_FT_LIFETIME == 0xFFFF) ? -1 : sdn_conf.SDN_FT_LIFETIME);
        sdn_conf.query_full =   sdn_conf.SDN_CONF_QUERY_FULL_PACKET;
        sdn_conf.query_idx =           sdn_conf.SDN_CONF_QUERY_INDEX;
        sdn_conf.query_len =           sdn_conf.SDN_CONF_QUERY_LENGTH;
        sdn_conf.rpl_dio_interval =    sdn_conf.rpl_dio_interval;
        sdn_conf.rpl_dptr_interval=   sdn_conf.rpl_dptr_interval;

        sdn_print();
    }

    public void sdn_print()
    {
        logger.log(Level.INFO, "{0},{1},{2},{3},{4},{5}",new Object[]{sdn_conf.cfg_id,sdn_conf.hops,sdn_conf.SDN_FT_LIFETIME,sdn_conf.query_len,sdn_conf.rpl_dio_interval,sdn_conf.rpl_dptr_interval});
        for(int i=0;i<logger.getHandlers().length;i++)
        {
            logger.getHandlers()[i].setFormatter(new SimpleFormatter("n:"));
        }

    }
}
