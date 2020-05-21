package com.github.usdn.packet;

import com.github.usdn.util.NodeAddress;

public class ConfigPacket extends USDNnetworkPacket {
    private static final byte CNF_WRITE = 1;

    private static final int MASK_POS = 7, MASK = 0x7F;

    public ConfigPacket(final byte[] data) {
        super(data);
    }

    public ConfigPacket(final int[] data) {
        super(data);
    }

    public ConfigPacket(final USDNnetworkPacket data) {
        super(data.toByteArray());
    }

    public ConfigPacket(final int net, final NodeAddress src,
                        final NodeAddress dst,
                        final ConfigProperty read) {
        super(net, src, dst);
        setConfigId(read).setTyp(CONFIG);
    }
    public ConfigPacket(final int net, final NodeAddress src,
                        final NodeAddress dst,
                        final ConfigProperty write,
                        final byte[] value) {
        super(net, src, dst);
        setConfigId(write).setWrite().setParams(value, write.size)
                .setTyp(CONFIG);
    }

    public final boolean isWrite() {
        int value = Byte.toUnsignedInt(getPayloadAt((byte) 0)) >> MASK_POS;
        return (value == CNF_WRITE);
    }
    public final ConfigProperty getConfigId() {
        return ConfigProperty.fromByte((byte) (getPayloadAt((byte) 0) & MASK));
    }
    public final ConfigPacket setParams(final byte[] bytes, final int size) {
        if (size != -1) {
            for (int i = 0; i < size; i++) {
                setPayloadAt(bytes[i], i + 1);
            }
        } else {
            for (int i = 0; i < bytes.length; i++) {
                setPayloadAt(bytes[i], i + 1);
            }
        }
        return this;
    }
    public final byte[] getParams() {
        return getPayloadFromTo(1, getPayloadSize());
    }

    private ConfigPacket setWrite() {
        setPayloadAt((byte) ((getPayloadAt(0)) | (CNF_WRITE << MASK_POS)), 0);
        return this;
    }
    private ConfigPacket setConfigId(final ConfigProperty id) {
        setPayloadAt(id.value, 0);
        return this;
    }
    public enum ConfigProperty {
        /**
         * Restarts the node.
         */
        RESET(0, 0),
        /**
         * Network ID. Can be read/written.
         */
        MY_NET(1, 1),
        /**
         * Address of the node. Can be read/written.
         */
        MY_ADDRESS(2, 2),
        /**
         * Default Packet TTL. Can be read/written.
         */
        PACKET_TTL(3, 1),
        /**
         * Filter packets depending on RSSI. Can be read/written.
         */
        RSSI_MIN(4, 1),
        /**
         * Seconds between RPL DIO/CONF. Can be read/written.
         */
        RPL_PERIOD(5, 2),
        /**
         * Seconds between reports. Can be read/written.
         */
        REPORT_PERIOD(6, 2),
        /**
         * Reports between resets. Can be read/written.
         */
        RESET_PERIOD(7, 2),
        /**
         * TTL of a FlowTableEntry. Can be read/written.
         */
        RULE_TTL(8, 1),
        /**
         * Adds an alias to the list of aliases of the node. write only.
         */
        ADD_ALIAS(9, 2),
        /**
         * Removes an alias from the list of aliases of the node. write only.
         */
        REM_ALIAS(10, 1),
        /**
         * Gets an alias from the list of aliases of the node. read only.
         */
        GET_ALIAS(11, 1),
        /**
         * Adds a rule to the FlowTable of the node. write only.
         */
        ADD_RULE(12, -1),
        /**
         * Removes a rule from the FlowTable of the node. write only.
         */
        REM_RULE(13, 1),
        /**
         * Gets a rule from the FlowTable of the node. read only.
         */
        GET_RULE(14, 1),
        /**
         * Adds a function to the node. write only.
         */
        ADD_FUNCTION(15, -1),
        /**
         * Removes a function from the node. write only.
         */
        REM_FUNCTION(16, 1),
        /**
         * Gets a function from the node. read only.
         */
        GET_FUNCTION(17, 1);


        private final byte value;


        private final int size;

        private static final ConfigProperty[] VALUES = ConfigProperty.values();


        public static ConfigProperty fromByte(final byte value) {
            return VALUES[value];
        }
        public int getSize() {
            return size;
        }

        ConfigProperty(final int v, final int s) {
            value = (byte) v;
            size = s;
        }
    }
}
