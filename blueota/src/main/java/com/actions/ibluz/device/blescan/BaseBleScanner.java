package com.actions.ibluz.device.blescan;

public abstract class BaseBleScanner {
    protected boolean isScanning;

    public abstract void onStartBleScan();

    public abstract void onStopBleScan();

    public abstract void onBleScanFailed(BleScanState scanState);
}