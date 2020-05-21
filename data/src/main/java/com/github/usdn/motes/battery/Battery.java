package com.github.usdn.motes.battery;

public class Battery implements Dischargeable {

    private static final double MAX_LEVEL = 5000,
    // 9000000 mC = 2 AAA batteries = 15 Days
    // 5000 mC = 12 min
    KEEP_ALIVE = 6.8, // mC spent every 1 s
            RADIO_TX = 0.0027, // mC to send 1byte
            RADIO_RX = 0.00094;
    private static final int MAX_BATT = 255;

    private double level;

    public Battery() {
        level = Battery.MAX_LEVEL;
    }

    @Override
    public final double getLevel() {
        return level;
    }
    @Override
    public final void setLevel(final double batteryLevel) {
        if (batteryLevel >= 0) {
            level = batteryLevel;
        } else {
            level = 0;
        }
    }
    @Override
    public Battery transmitRadio(final int nBytes) {
        double newVal = level - Battery.RADIO_TX * nBytes;
        setLevel(newVal);
        return this;
    }
    @Override
    public Battery receiveRadio(final int nBytes) {
        double newVal = level - Battery.RADIO_RX * nBytes;
        setLevel(newVal);
        return this;
    }
    @Override
    public Battery keepAlive(final int n) {
        double newVal = level - Battery.KEEP_ALIVE * n;
        setLevel(newVal);
        return this;
    }
    @Override
    public final int getByteLevel() {
        if (Battery.MAX_LEVEL != 0) {
            return (int) ((level / Battery.MAX_LEVEL) * MAX_BATT);
        } else {
            return 0;
        }
    }


}
