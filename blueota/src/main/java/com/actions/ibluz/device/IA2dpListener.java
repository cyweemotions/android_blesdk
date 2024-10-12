package com.actions.ibluz.device;

import android.bluetooth.BluetoothDevice;

public interface IA2dpListener {
	void connect();

	void disconnect();

	void updateConnectionState(BluetoothDevice device, int state);
}
