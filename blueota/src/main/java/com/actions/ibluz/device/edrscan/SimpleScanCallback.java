package com.actions.ibluz.device.edrscan;

import android.bluetooth.BluetoothDevice;

public interface SimpleScanCallback {
    void onEdrScan(BluetoothDevice device, int rssi, byte[] scanRecord);

    void onEdrFailed(EdrScanState scanState);
}
