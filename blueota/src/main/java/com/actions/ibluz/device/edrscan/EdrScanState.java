package com.actions.ibluz.device.edrscan;

import com.actions.ibluz.device.blescan.BleScanState;

public enum EdrScanState {

    BLUETOOTH_OFF(-1, "BLUETOOTH_OFF"),
    SCAN_SUCCESS(0, "SCAN_SUCCESS"),
    SCAN_FAILED_ALREADY_STARTED(1, "SCAN_FAILED_ALREADY_STARTED"),
    SCAN_FAILED_APPLICATION_REGISTRATION_FAILED(2, "SCAN_FAILED_APPLICATION_REGISTRATION_FAILED"),
    SCAN_FAILED_INTERNAL_ERROR(3, "SCAN_FAILED_INTERNAL_ERROR"),
    SCAN_FAILED_FEATURE_UNSUPPORTED(4, "SCAN_FAILED_FEATURE_UNSUPPORTED"),
    SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES(5, "SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES");

    private int mCode;
    private String mMsg;

    EdrScanState(int code, String msg) {
        this.mCode = code;
        this.mMsg = msg;
    }

    public int getmCode() {
        return mCode;
    }

    public String getmMsg() {
        return mMsg;
    }

    public static EdrScanState newInstance(int code) {
        switch (code) {
            case -1:
                return EdrScanState.BLUETOOTH_OFF;
            case 1:
                return EdrScanState.SCAN_FAILED_ALREADY_STARTED;
            case 2:
                return EdrScanState.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED;
            case 3:
                return EdrScanState.SCAN_FAILED_INTERNAL_ERROR;
            case 4:
                return EdrScanState.SCAN_FAILED_FEATURE_UNSUPPORTED;
            case 5:
                return EdrScanState.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES;
            default:
                return EdrScanState.SCAN_SUCCESS;
        }
    }
}
