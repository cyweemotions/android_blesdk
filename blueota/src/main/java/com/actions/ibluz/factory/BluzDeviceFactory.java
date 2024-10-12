package com.actions.ibluz.factory;

import java.util.Map;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.util.Log;

import com.actions.ibluz.device.BluzDeviceBle;
import com.actions.ibluz.device.BluzDeviceEDR;
import com.actions.ibluz.device.BluzDeviceSpp;

public class BluzDeviceFactory {
	/** 连接设备类型 */
	public static class ConnectionType {
		/** SPP */
		public static final String SPP = "SPP";
		/** BLE */
		public static final String BLE = "BLE";
		/** SPP_ONLY */
		public static final String SPP_ONLY = "SPP_ONLY";
		/** BLE and BR */
		public static final String BLE_BR = "BLE_BR";
		/** EDR **/
		public static final String EDR = "EDR";
	}

	/** 连接状态 */
	public static class ConnectionState {
		public static final int A2DP_CONNECTED = 1;
		public static final int A2DP_CONNECTING = 2;
		public static final int A2DP_DISCONNECTED = 3;
		public static final int A2DP_FAILURE = 4;
		public static final int A2DP_PAIRING = 5;

		public static final int SPP_CONNECTED = 11;
		public static final int SPP_CONNECTING = 12;
		public static final int SPP_DISCONNECTED = 13;
		public static final int SPP_FAILURE = 14;
	}

	public static class BleKeyUUIDS {
		public static final String keyConfigurationUUID = "keyConfigurationUUID";
	//	public static final String keyReadServiceUUID = "keyReadServiceUUID";
	//	public static final String keyWriteServiceUUID = "keyWriteServiceUUID";
		public static final String keyServiceUUID = "keyServiceUUID";
		public static final String keyReadCharacteristicUUID = "keyReadCharacteristicUUID";
		public static final String keyWriteCharacteristicUUID = "keyWriteCharacteristicUUID";
	}

	public static class EdrKeyUUIDS {
		public static final String keyConfigurationUUID = "keyConfigurationUUID";
		public static final String keyServiceUUID = "keyServiceUUID";
		public static final String keyReadCharacteristicUUID = "keyReadCharacteristicUUID";
		public static final String keyWriteCharacteristicUUID = "keyWriteCharacteristicUUID";
	}

	public static class DiscoveryType{
		public static final int DISCOVERY_NORMAL = 1;
		public static final int DISCOVERY_A2DP = 2;
	}

	public static UUID sUUID = null;
	public static Map<String,UUID> bleUUIDs = null;
	public static Map<String, UUID> mEdrUUIDs = null;

	/**
	 * 获取默认协议蓝牙连接设备
	 *
	 * @param context
	 * @return
	 */
	public static IBluzDevice getDevice(Context context) {
		return getDevice(context, ConnectionType.SPP);
	}

	/**
	 * 设置自定义UUID
	 *
	 * @param uuid
	 *            SPP协议的UUID
	 */
	public static void setUUID(UUID uuid) {
		sUUID = uuid;
	}

	/**
	 * 设置自定义UUID
	 *
	 * @param uuids
	 *            BLE协议的UUIDs
	 */
	public static void setUUID(Map<String,UUID> uuids) {
		bleUUIDs = uuids;
	}

	/**
	 * 设置自定义 edr UUID
	 * @param uuids
	 * 	         EDR协议的UUIDs
	 * @param isEDR
	 * 			 是否为EDR
	 */
	public static void setUUID(Map<String, UUID> uuids, boolean isEDR) {
		Log.e("setUUID", isEDR + "");
		if (isEDR) {
			mEdrUUIDs = uuids;
		} else {
			setUUID(uuids);
		}
	}

	/**
	 * 获取蓝牙连接设备
	 *
	 * @param context
	 * @param type
	 *            设备协议类型
	 * @return
	 */
	public static IBluzDevice getDevice(Context context, String type) {
		if (BluetoothAdapter.getDefaultAdapter() == null) {
			return null;
		}

		if (type.equals(ConnectionType.SPP)) {
			return new BluzDeviceSpp(context, true, sUUID);
		} else if (type.equals(ConnectionType.BLE)) {
			return new BluzDeviceBle(context,bleUUIDs);
		} else if (type.equals(ConnectionType.SPP_ONLY)) {
			return new BluzDeviceSpp(context, false, sUUID);
		} else if (type.equals(ConnectionType.BLE_BR)) {
			return new BluzDeviceBle(context,bleUUIDs,true, false);
		} else if (type.equals(ConnectionType.EDR)) {
			Log.e("edr", "EDR");
			return new BluzDeviceEDR(context, mEdrUUIDs);
		} else {
			return null;
		}
	}
}
