package com.actions.ibluz.factory;

import android.bluetooth.BluetoothDevice;

import com.actions.ibluz.factory.BluzDeviceFactory.ConnectionState;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** 设备连接接口 */
public interface IBluzDevice {
	/**
	 * 获取设备使能信息
	 * 
	 * @return true 表示已打开
	 */
	public boolean isEnabled();

	/**
	 * 打开蓝牙
	 * 
	 * @return true 表示正在打开
	 */
	public boolean enable();

	/**
	 * 关闭蓝牙
	 * 
	 * @return true 表示正在关闭
	 */
	public boolean disable();

	/**
	 * 开始扫描蓝牙设备
	 */
	public void startDiscovery();

	/**
	 * 连接失败后重连
	 * 
	 * @param device
	 *            蓝牙设备
	 */
	public void retry(BluetoothDevice device);

	/**
	 * 连接蓝牙设备，当前应用所需的Profile
	 * 
	 * @param device
	 *            蓝牙设备
	 */
	public void connect(BluetoothDevice device);

	/**
	 * 断开全部连接
	 * 
	 * @param device
	 *            蓝牙设备
	 */
	public void disconnect(BluetoothDevice device);

	/**
	 * 设置是否自动连接数据通道(默认是自动连接)
	 *
	 * @param enable
	 *            是否使能
	 */
	public void setAutoConnectDataChanel(boolean enable);

	/**
	 * 获取当前已连接的设备，含音频和数据
	 * 
	 * @return 蓝牙设备
	 */
	public BluetoothDevice getConnectedDevice();

	/**
	 * 获取当前已连接的音频设备
	 * 
	 * @return 蓝牙设备
	 */
	public BluetoothDevice getConnectedA2dpDevice();

	/**
	 * 获取当前已配对设备
	 *
	 * @return 蓝牙设备集合
	 */
	public Set<BluetoothDevice> getBoundedDevices();

	/**
	 * 设置连接回调监听
	 * 
	 * @param listener
	 *            监听
	 */
	public void setOnConnectionListener(OnConnectionListener listener);

	/**
	 * 设置搜索设备回调监听
	 * 
	 * @param listener
	 *            监听
	 */
	public void setOnDiscoveryListener(OnDiscoveryListener listener);

	/**
	 * 断开数据连接，释放资源
	 */
	public void release();

	/** 搜索设备回调 */
	public static interface OnDiscoveryListener {
		/**
		 * 连接状态改变
		 * 
		 * @param device
		 *            蓝牙设备
		 * @param state
		 *            连接状态，{@link ConnectionState}
		 */
		public void onConnectionStateChanged(BluetoothDevice device, int state);

		/**
		 * 搜索开始
		 */
		public void onDiscoveryStarted();

		/**
		 * 搜索结束
		 */
		public void onDiscoveryFinished();

		/**
		 * 找到设备
		 * 
		 * @param device
		 *            蓝牙设备
		 */
		public void onFound(BluetoothDevice device, int type);
	}

	/** 设备连接状态回调 */
	public static interface OnConnectionListener {
		/**
		 * 连接
		 * 
		 * @param device
		 *            蓝牙设备
		 */
		public void onConnected(BluetoothDevice device);

		/**
		 * 断开连接
		 * 
		 * @param device
		 *            蓝牙设备
		 */
		public void onDisconnected(BluetoothDevice device);
	}

	/**
	 * 获取数据发送接收器
	 * 
	 * @return 接收器
	 */
	public IBluzIO getIO();

}
