package com.actions.ibluz.device.edrscan;

import com.actions.ibluz.device.blescan.BleScanState;

public abstract class BaseEdrScanner {
    protected boolean isScanning;

    public abstract void onStartEdrScan();

    public abstract void onStopEdrScan();

    public abstract void onEdrScanFailed(BleScanState scanState);

}
