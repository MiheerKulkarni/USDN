package com.github.usdn.flowtable;

public class usdnft {
    int SDN_CONF_FT_MAX_WHITELIST = 10;
    int  SDN_FT_MAX_MATCHES = 10;
    int SDN_FT_MAX_ACTIONS = 10;
    int SDN_FT_MAX_ENTRIES = 10;
    int SDN_FT_DATA_MEMB_SIZE = 1024;
    int SDN_FT_INFINITE_LIFETIME = 0xFFFF;

    public enum flowtable_id
    {
        WHITELIST,
        FLOWTABLE
    }
    public enum match_op_id
    {
        LT_EQ(-2),
        LT(-1),
        EQ (0),
        GT (1),
        GT_EQ(2),
        NOT_EQ(3);

        private int op_code;

        match_op_id(int op_code)
        {
            this.op_code = op_code;
        }

        public int getOp_code() {
            return op_code;
        }
    }
    public enum action_type
    {
        SDN_FT_ACTION_ACCEPT(0),                      /**< accept and pass to upper layers */
        SDN_FT_ACTION_DROP(1),                        /**< drop the packet */
         SDN_FT_ACTION_QUERY(2),                       /**< query the controller */
         SDN_FT_ACTION_FORWARD(3),                     /**< forward to neighbor */
         SDN_FT_ACTION_MODIFY(4),                      /**< modify the packet */
        SDN_FT_ACTION_FALLBACK(5),                    /**< send to the fallback interface */
        SDN_FT_ACTION_SRH(6),
        SDN_FT_ACTION_CALLBACK(7);

        public int action_code;

        action_type(int action_code){this.action_code = action_code;}
        public int getAction_code(){return this.action_code;}
    }

    public match_op_id operator;
    int op_index,op_len,op_req_Ext;
    int action_index,action_len;
    int ttl,count;

    int ft_id;




}
