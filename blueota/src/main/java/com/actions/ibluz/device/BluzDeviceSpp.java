package com.actions.ibluz.device;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

import com.tencent.mars.xlog.Log;

import com.actions.ibluz.factory.BluzDeviceFactory.ConnectionState;
import com.actions.ibluz.factory.IBluzDevice;
import com.actions.ibluz.util.Utils;

public class BluzDeviceSpp extends BluzDeviceBase {
	private static final String TAG = "BluzDeviceSpp";

	private BluetoothSocket mBluetoothSocket = null;
	private DataOutputStream mOutputStream = null;
	private DataInputStream mInputStream = null;
	private boolean mAutoConnecting = false;
	private UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final int SPP_CONNECT_TIMEOUT = 10000;

	private Handler mHandler = new Handler();
	private ConnectionTask mConnectionTask = null;
	/* indicate whether any activity poped while data connect */
	private boolean mConnectPopActivity = false;

	private int reConnectTime = 0; // 限制socket重试连接次数
	private static final int RECONNECT_MAX_TIME = 10;

	public BluzDeviceSpp(Context context, boolean a2dp) {
		super(context, a2dp, false);
		Log.i(TAG, "Create");
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	public BluzDeviceSpp(Context context, boolean a2dp, UUID uuid) {
		this(context, a2dp);
		if (uuid != null) {
			mUUID = uuid;
			Log.d(TAG,"set uuid: " + mUUID.toString());
		}
	}

	@Override
	public void write(byte[] buffer) throws Exception {
		Utils.printHexBuffer(TAG, buffer);

		mOutputStream.write(buffer);
	}

	@Override
	public void flush() throws Exception {
		mOutputStream.flush();
	}

	@Override
	public int read(byte[] buffer, int byteOffset, int byteCount) throws Exception {
		return mInputStream.read(buffer, byteOffset, byteCount);
	}

	@Override
	public int readInt() throws Exception {
		return mInputStream.readInt();
	}

	@Override
	public short readShort() throws Exception {
		return mInputStream.readShort();
	}

	@Override
	public int read() throws Exception {
		return mInputStream.read();
	}

	@Override
	public void connect() {
		super.connect();

		if (mBluetoothDevice == null || mAutoConnecting) {
			return;
		}

		Log.v(TAG, "connectSPPAsync");
		mAutoConnecting = true;
		updateConnectionState(ConnectionState.SPP_CONNECTING);
		mConnectionTask = new ConnectionTask();
		mConnectionTask.execute(mBluetoothDevice);
	}

	private Runnable mSppCallback = new Runnable() {
		@Override
		public synchronized void run() {
			if (mDeviceConnected == null) {
				if (!Utils.isAppForeground(mContext)) {
					/* FIXME when data connect, it pops a confirm activity, like Lenovo S939 */
					mHandler.postDelayed(mSppCallback, SPP_CONNECT_TIMEOUT);
					return;
				} else if (mConnectPopActivity) {
					/* if activity pop, the connect() moment is only triggered by the user behavior, so the timeout mechanism should change to make sense */
					mConnectPopActivity = false;
					mHandler.postDelayed(mSppCallback, SPP_CONNECT_TIMEOUT);
					return;
				}

				mConnectionTask.cancel(true);
				connectSppFail();
			}
		}
	};

	private void connectSPP(BluetoothDevice device) {
		if (device == null) {
			return;
		}
//
//		//配对
//		try {
//			Method method = BluetoothDevice.class.getMethod("createBond");
//			method.invoke(device);
//			Log.e("nick", "invoke");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		try {
			mBluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(mUUID);
		} catch (IOException e) {
			Log.e(TAG, "socket建立失败：" + e);
			try {
				mBluetoothSocket.close();
			} catch (IOException ex) {
				ex.printStackTrace();
				return;
			}
		return ;
		}
		mHandler.removeCallbacks(mSppCallback);
		int timeout = SPP_CONNECT_TIMEOUT;
		if (Build.MODEL.contains("HTC T328w")) {
			/* FIXME new desire need 13s to connect */
			timeout = 15000;
		}

		mConnectPopActivity = false;
		if (Build.MODEL.contains("Lenovo S939") || Build.MODEL.contains("Lenovo S898t+")) {
			mConnectPopActivity = true;
		}

		mHandler.postDelayed(mSppCallback, timeout);
		Log.i(TAG, "SPP connecting");
        //In some device like ZTE U930, discovery and connect at the same time may cause IOException: Unable to start Service Discovery which lead to a connection fail.
		mBluetoothAdapter.cancelDiscovery();
		try {
			mBluetoothSocket.connect();
			reConnectTime = 0;
		} catch (IOException e) {
			Log.d(TAG + " error", "蓝牙连接异常" + e);
			SharedPreferences share = mContext.getSharedPreferences(Utils.ISDIALOG_SHOWING, Context.MODE_PRIVATE);
			boolean isDialogShowing = share.getBoolean(Utils.DIALOG_SHOWING, false);
			Log.e("nick_dialog","" + isDialogShowing);
			try {
				mBluetoothSocket.close();
				disconnect(device);
				if (!isDialogShowing) {
					if (reConnectTime < RECONNECT_MAX_TIME) {
						Thread.sleep(1000);
						reConnectTime++;
						connectSPP(device);
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (NullPointerException ex) {

			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}

	}

	private class ConnectionTask extends AsyncTask<Object, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(Object... arg) {
			final BluetoothDevice device = (BluetoothDevice) arg[0];
			if (device != null) {
				try {
					/* let the data disconnect intent goes first */
					Thread.sleep(500);
					Log.d(TAG, device + "");
					reConnectTime = 0;
					connectSPP(device);
					if (isCancelled()) {
						Log.i(TAG, "task cancelled");
						return false;
					} else {
						return true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mAutoConnecting = false;
			mConnecting = false;
			if (result) {
				connectSppSuccess();
			} else {
				connectSppFail();
			}
		}
	}

	@Override
	public void disconnect() {
		if (mBluetoothSocket != null) {
			try {
				Log.i(TAG, "close");
				if (mInputStream != null) {
					mInputStream.close();
				}

				if (mOutputStream != null) {
					mOutputStream.flush();
					mOutputStream.close();
				}

				mBluetoothSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				mBluetoothSocket = null;
			}

			if (mConnectionListener != null && mDeviceConnected != null) {
				mConnectionListener.onDisconnected(mDeviceConnected);
			}
		}

		mDeviceConnected = null;
		mConnecting = false;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		mHandler.removeCallbacks(mSppCallback);
	}

	private void connectSppFail() {
		Log.i(TAG, "SPP connect fail");

		mHandler.removeCallbacks(mSppCallback);
		disconnect();
		updateConnectionState(ConnectionState.SPP_FAILURE);
		mBluetoothDevice = null;
	}

	/* inform the remote device, it's a valid connection */
	private void knockDoor() {
		byte[] buffer = new byte[] { '0', '1', '2', '3', '4', '5', '6', '7' };
		try {
			write(buffer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private synchronized void connectSppSuccess() {
		try {
			mInputStream = new DataInputStream(mBluetoothSocket.getInputStream());
			mOutputStream = new DataOutputStream(mBluetoothSocket.getOutputStream());
			cancelDiscovery();
			Log.i(TAG, "SPP connected");
			mDeviceConnected = mBluetoothDevice;
		//	knockDoor();
			updateConnectionState(ConnectionState.SPP_CONNECTED);
			if (mConnectionListener != null) {
				mConnectionListener.onConnected(mBluetoothDevice);
			}
			mHandler.removeCallbacks(mSppCallback);
		} catch (Exception e) {
			handleException(e);
		}
	}

	public Set<BluetoothDevice> getBoundedDevices(){
		Set<BluetoothDevice> mDevicesSet = new HashSet<>();
		Set<BluetoothDevice> mBoundedSet = mBluetoothAdapter.getBondedDevices();
		for (BluetoothDevice device : mBoundedSet) {
			if (device.getType() == BluetoothDevice.DEVICE_TYPE_CLASSIC
					|| device.getType() == BluetoothDevice.DEVICE_TYPE_DUAL){
				mDevicesSet.add(device);
			}
		}
		return mDevicesSet;
	}
}
