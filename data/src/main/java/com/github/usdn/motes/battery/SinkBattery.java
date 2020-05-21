package com.github.usdn.motes.battery;

public class SinkBattery extends Battery {

    @Override
    public Battery transmitRadio(final int nByte) {
        return this;
    }

    @Override
    public Battery receiveRadio(final int nByte) {
        return this;
    }

    @Override
    public Battery keepAlive(final int seconds) {
        return this;
    }
}
