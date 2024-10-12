package com.actions.ibluz.device;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;

import com.actions.ibluz.factory.BluzDeviceFactory;
import com.tencent.mars.xlog.Log;

import com.actions.ibluz.factory.BluzDeviceFactory.ConnectionState;
import com.actions.ibluz.factory.IBluzDevice;
import com.actions.ibluz.factory.IBluzIO;
import com.actions.ibluz.util.Utils;

import java.util.List;
import java.util.Set;

public abstract class BluzDeviceBase implements IBluzDevice, IBluzIO, IA2dpListener {
	private static final String TAG = "BluzDeviceBase";
	private static final String DATA_DISCONNECT_INTENT = "com.actions.ibluz.data.disconnect";
	private static final String INTENT_PACKAGE_NAME = "package-name";

	// compatability: some device's discovery time is longer than 1minute which will influent the auto-reconnect
	private static final int DISCOVERY_TIMEOUT = 13000;

	protected Context mContext = null;
	protected BluetoothAdapter mBluetoothAdapter;
	protected OnDiscoveryListener mDiscoveryListener = null;
	protected OnConnectionListener mConnectionListener = null;
	protected BluetoothDevice mBluetoothDevice = null;
	protected BluetoothDevice mDeviceConnected = null;
	protected boolean mConnecting = false;
	protected BluzDeviceA2dpBase mDeviceA2dp = null;
	private BluetoothDevice mDeviceRetry = null;
	private boolean mAtuoConnect = true;
	private boolean mRegisterReceiver = false;

	public BluzDeviceBase(Context context) {
		init(context, true, false);
	}

	public BluzDeviceBase(Context context, boolean a2dp, boolean removeBound) {
		init(context, a2dp, removeBound);
	}

	private void init(Context context, boolean a2dp, boolean removeBound) {
		Log.i(TAG, "Create with a2dp:" + a2dp);
		mRegisterReceiver = false;
		if (a2dp) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				mDeviceA2dp = new BluzDeviceA2dp(context, removeBound);
			} else {
				mDeviceA2dp = new BluzDeviceA2dpCompat(context);
			}

			mDeviceA2dp.setListener(this);
		}

		mContext = context;
		registerReceiver();
	}

	@Override
	public BluetoothDevice getConnectedA2dpDevice() {
		if (mDeviceA2dp != null) {
			//fix samsung galaxy s7 disconnect bluetooth manual then auto reconnect
			if (Build.MODEL.contains("SM-G9308")){
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return mDeviceA2dp.getConnectedDevice();
		} else {
			return null;
		}
	}

	@Override
	public void updateConnectionState(BluetoothDevice device, int state) {
		mBluetoothDevice = device;
		updateConnectionState(state);
	}

	@Override
	public IBluzIO getIO() {
		return this;
	}

	@Override
	public void connect() {
		if (mDeviceA2dp == null) {
			/** only data connect */
			sendDisconnectIntent();

			mDeviceConnected = null;
			//return from super method but will continue in subclass method
			//
			return;
		}

		//SPP_ONLY and BLE won't call below
		//set mBluetoothDevice to null in some case so that it won't be connected in subclass(BluzDeviceBle, BluzDeviceSpp )
		BluetoothDevice device = getConnectedA2dpDevice();
		if (device == null || (mDeviceConnected != null && device.getAddress().equals(mDeviceConnected.getAddress()))) {
			Log.v(TAG, "device null or already connected, device:" + device+ "device==null?:"+(device==null));
			//don't connect if no a2dpdevice is connected or spp is already connected
//			mDeviceA2dp.updateA2dpConnectionState(A)
			mBluetoothDevice = null;
		} else if (!Utils.isAppForeground(mContext)) {
			Log.v(TAG, "Deactivated");
			//app is not in foreground
			mBluetoothDevice = null;
		} else {
			// disconnect other app's spp connection
			sendDisconnectIntent();

			//reconnect spp to connected a2dp device
			mBluetoothDevice = device;
			mDeviceConnected = null;
		}
	}

	protected void registerReceiver() {
		Log.e("nick", "registerReceiver");
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(DATA_DISCONNECT_INTENT);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			mContext.registerReceiver(mBluetoothReceiver, filter,mContext.RECEIVER_EXPORTED);
		}else{
			mContext.registerReceiver(mBluetoothReceiver, filter);
		}
		mRegisterReceiver = true;
	}

	protected void unregisterReceiver() {
		if (mRegisterReceiver) {
			mContext.unregisterReceiver(mBluetoothReceiver);
			mRegisterReceiver = false;
		}
	}

	@Override
	public boolean isEnabled() {
		return mBluetoothAdapter.isEnabled();
	}

	@Override
	public boolean enable() {
		return mBluetoothAdapter.enable();
	}

	@Override
	public boolean disable() {
		return mBluetoothAdapter.disable();
	}

	public void startDiscovery() {
		if (android.os.Build.VERSION.SDK_INT >= 23 && !checkLocationPermission()) {
			Log.d(TAG, "startDiscovery fail: need Permission ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION");
		}
		cancelDiscovery();
		mBluetoothAdapter.startDiscovery();
	}

	// 增加配对设备
	private void retrievePairedDevice() {
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				Log.v(TAG, "retrievePairedDevice: " + device.getName() + ", " + device.getAddress());
				if (isValidType(device) && mDiscoveryListener != null) {
					mDiscoveryListener.onFound(device, BluzDeviceFactory.DiscoveryType.DISCOVERY_A2DP);
				}
			}
		}
	}

	private void retrieveConnectedDevice() {
		int a2dp = mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
		if (a2dp != BluetoothProfile.STATE_CONNECTED) {
			return;
		}

		Log.v(TAG, "retrieveConnectedDevice");
		mBluetoothAdapter.getProfileProxy(mContext, new BluetoothProfile.ServiceListener() {

			@Override
			public void onServiceDisconnected(int profile) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onServiceConnected(int profile, BluetoothProfile proxy) {
				// TODO Auto-generated method stub
				List<BluetoothDevice> mDevices = proxy.getConnectedDevices();
				if (mDevices != null && mDevices.size() > 0) {
					for (BluetoothDevice device : mDevices) {
						Log.i("W", "onServiceConnected: " + device.getName());
						if (isValidType(device) && mDiscoveryListener != null) {
							if (mDeviceA2dp != null)
								mDiscoveryListener.onFound(device, BluzDeviceFactory.DiscoveryType.DISCOVERY_A2DP);
						}
					}
				} else {
					Log.i("W", "onServiceConnected null");
				}
			}
		}, BluetoothProfile.A2DP);
	}

	protected boolean checkLocationPermission() {
		return checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION) || checkPermission(Manifest.permission.ACCESS_FINE_LOCATION);
	}

	private boolean checkPermission(final String permission) {
		return mContext.checkPermission(permission, android.os.Process.myPid(), android.os.Process.myUid()) == PackageManager.PERMISSION_GRANTED;
	}

	protected void cancelDiscovery() {
		if (mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}
	}

	private Handler mHandler = new Handler();
	private Runnable mRetryCallback = new Runnable() {
		@Override
		public void run() {
			/* while connect fail, the retry will be called right after connect(), if we put the assignment behind, there will be conflicted */
			BluetoothDevice device = mDeviceRetry;
			mDeviceRetry = null;
			connect(device);
		}
	};

	private Runnable mDiscoveryTimeoutCallback = new Runnable() {
		@Override
		public void run() {
			Log.v(TAG, "Bluetooth discovery timeout");
			cancelDiscovery();
			if (mDiscoveryListener != null) {
				mDiscoveryListener.onDiscoveryFinished();
			}
		}
	};

	@Override
	public void retry(BluetoothDevice device) {
		mDeviceRetry = device;
		/* after connect fail, restart may do some favor, very tricky(4.0.4) */
		startDiscovery();
	}

	private void sendDisconnectIntent() {
		Intent intent = new Intent(DATA_DISCONNECT_INTENT);
		intent.putExtra(INTENT_PACKAGE_NAME, mContext.getPackageName());
		mContext.sendBroadcast(intent);
	}

	@Override
	public void connect(BluetoothDevice device) {
		if (device == null) {
			return;
		} else if (mDeviceConnected != null) {
			if (device.equals(mDeviceConnected)) {
				Log.i(TAG, "already connected");
				return;
			} else {
				Log.i(TAG, "replace device");
				disconnect(null);
			}
		} else if (mBluetoothDevice != null && device.equals(mBluetoothDevice) && mConnecting) {
			Log.i(TAG, "in connecting");
			return;
		}

		mConnecting = true;
		mBluetoothDevice = device;

		cancelDiscovery();
		if (mDeviceA2dp != null) {
			mDeviceA2dp.connect(device);
		} else {
			connect();
		}
	}

	@Override
	public void disconnect(BluetoothDevice device) {
		Log.i(TAG, "disconnect all");
		// close data connection
		disconnect();
		if (mDeviceA2dp != null) {
			mDeviceA2dp.disconnect(device == null ? mDeviceA2dp.getConnectedDevice() : device);
		}
	}

	protected void updateConnectionState(int state) {
		if (state == ConnectionState.A2DP_FAILURE) {
			// if A2DP connect fail, then the next spp/ble will not procceed
			mConnecting = false;
		}

		if (mDiscoveryListener != null) {
			mDiscoveryListener.onConnectionStateChanged(mBluetoothDevice, state);
		}
	}

	@Override
	public BluetoothDevice getConnectedDevice() {
		return mDeviceConnected;
	}

	@Override
	public Set<BluetoothDevice> getBoundedDevices(){
		return mBluetoothAdapter.getBondedDevices();
	}

	@Override
	public void setOnConnectionListener(OnConnectionListener listener) {
		mConnectionListener = listener;
	}

	@Override
	public void setOnDiscoveryListener(OnDiscoveryListener listener) {
		mDiscoveryListener = listener;
	}

	@Override
	public void release() {
		Log.i(TAG, "release");
		unregisterReceiver();
		disconnect();
		if (mBluetoothAdapter != null) {
			cancelDiscovery();
		}

		/* make sure that when the activity is not alive, their will be no broadcast receiver */
		if (mDeviceA2dp != null) {
			mDeviceA2dp.release();
		}
	}

	protected void handleException(Exception e) {
		e.printStackTrace();
		disconnect();
	}

	@SuppressLint("NewApi")
	private boolean isValidType(BluetoothDevice device) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			int type = device.getType();
			if (type != BluetoothDevice.DEVICE_TYPE_LE) {
				return true;
			}
		}

		return true;
	}

	private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
		private boolean mIsScaning = false; // 用于解决部分机器搜索

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				if (mIsScaning == false) {
					mIsScaning = true;
					Log.v(TAG, "Bluetooth discovery started!");
					mHandler.removeCallbacks(mDiscoveryTimeoutCallback);
					mHandler.postDelayed(mDiscoveryTimeoutCallback, DISCOVERY_TIMEOUT);
					if (mDiscoveryListener != null) {
						mDiscoveryListener.onDiscoveryStarted();
						retrieveConnectedDevice();
					}

					if (mDeviceRetry != null) {
						mHandler.removeCallbacks(mRetryCallback);
						mHandler.post(mRetryCallback);
					} else if (mDeviceA2dp != null) {
						//auto reconnect spp at connected a2dp device
						if (mAtuoConnect)
							connect();
					}
				}
			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (isValidType(device) && mDiscoveryListener != null) {
					mDiscoveryListener.onFound(device, BluzDeviceFactory.DiscoveryType.DISCOVERY_NORMAL);
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				Log.v(TAG, "Bluetooth discovery finished!");
				mHandler.removeCallbacks(mDiscoveryTimeoutCallback);
				if (mDiscoveryListener != null) {
					mDiscoveryListener.onDiscoveryFinished();
				}
				if (mIsScaning == true) {
					mIsScaning = false;
				}
			} else if (DATA_DISCONNECT_INTENT.equals(action)) {
				Log.v(TAG, "data disconnect");
				String packageName = intent.getStringExtra(INTENT_PACKAGE_NAME);
				if (!packageName.equals(mContext.getPackageName())) {
					disconnect();
				}
			}
		}
	};

	@Override
	public void setAutoConnectDataChanel(boolean enable){
		Log.i(TAG, "setAutoConnectDataChanel " + enable);
		mAtuoConnect = enable;
		if (mDeviceA2dp != null){
			mDeviceA2dp.setAutoConnectDataChanel(enable);
		}
	}
}
