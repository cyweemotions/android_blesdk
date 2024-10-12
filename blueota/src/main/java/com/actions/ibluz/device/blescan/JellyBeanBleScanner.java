package com.actions.ibluz.device.blescan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import com.tencent.mars.xlog.Log;

public class JellyBeanBleScanner extends BaseBleScanner {
    private final static String TAG = JellyBeanBleScanner.class.getName();

    public BluetoothAdapter mBluetooth = null;
    private SimpleScanCallback mScanCallback = null;
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            mScanCallback.onBleScan(device, rssi, scanRecord);
        }
    };

    public JellyBeanBleScanner(Context context, SimpleScanCallback callback) {
        mScanCallback = callback;
        BluetoothManager bluetoothMgr = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetooth = bluetoothMgr.getAdapter();
    }

    @SuppressWarnings(value = {"deprecation"})
    @Override
    public void onStartBleScan() {//scan always
        if (mBluetooth != null) {
            try {
                isScanning = mBluetooth.startLeScan(leScanCallback);
                Log.i(TAG, "mBluetooth.startLeScan() " + isScanning);
            }catch (SecurityException e){
                e.printStackTrace();
            }
        } else {
            mScanCallback.onBleScanFailed(BleScanState.BLUETOOTH_OFF);//bluetooth is off
        }
    }

    @SuppressWarnings(value = {"deprecation"})
    @Override
    public void onStopBleScan() {
        isScanning = false;
        try {
            if (mBluetooth != null) {
                mBluetooth.stopLeScan(leScanCallback);
            }
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onBleScanFailed(BleScanState scanState) {
        mScanCallback.onBleScanFailed(scanState);//扫描设备失败~
    }
}