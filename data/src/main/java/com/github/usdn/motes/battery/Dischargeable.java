package com.github.usdn.motes.battery;

public interface Dischargeable {
    int getByteLevel();

    double getLevel();

    void setLevel(double batteryLevel);

    Battery keepAlive(int n);

    Battery receiveRadio(int nBytes);

    Battery transmitRadio(int nBytes);
}
