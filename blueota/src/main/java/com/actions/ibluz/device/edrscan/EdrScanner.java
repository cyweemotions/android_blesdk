package com.actions.ibluz.device.edrscan;

import android.content.Context;

public class EdrScanner {
    private final static String TAG = EdrScanner.class.getName();
    public BaseEdrScanner mEdrScanner;

    public EdrScanner(Context context, final SimpleScanCallback callback) {

    }

    public boolean isScanning() {
        return mEdrScanner.isScanning;
    }

    public void startEdrScan() {
        mEdrScanner.onStartEdrScan();
    }

    public void stopEdrScan() {
        mEdrScanner.onStopEdrScan();
    }
}
