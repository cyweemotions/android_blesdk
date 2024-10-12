package com.actions.ibluz.device;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import com.tencent.mars.xlog.Log;

import com.actions.ibluz.device.DataBuffer.ReadDataBuffer;
import com.actions.ibluz.device.DataBuffer.WriteCallback;
import com.actions.ibluz.device.DataBuffer.WriteDataBuffer;
import com.actions.ibluz.device.blescan.BleScanState;
import com.actions.ibluz.device.blescan.BleScanner;
import com.actions.ibluz.device.blescan.SimpleScanCallback;
import com.actions.ibluz.factory.BluzDeviceFactory;
import com.actions.ibluz.factory.BluzDeviceFactory.ConnectionState;
import com.actions.ibluz.factory.IBluzDevice;
import com.actions.ibluz.util.Utils;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluzDeviceBle extends BluzDeviceBase {
	private static final String TAG = "BluzDeviceBle";

	private int mGattReConnectTime = 0;

	private static final int MSG_CONNECTED = 1;
	private static final int MSG_DISCONNECTED = 2;
	private static final int MSG_FOUND = 3;
	private static final int MSG_SHOW_MESSAGE = 4;

	private static final int CONNECT_TIMEOUT = 10000;
    private static final int SCAN_TIMEOUT = 10000;

	private static  UUID CLIENT_CHARACTERISTIC_CONFIGURATION = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
//	private static  UUID SERVICE_READ = UUID.fromString("00006666-0000-1000-8000-00805f9b34fb");
//	private static  UUID SERVICE_WRITE = UUID.fromString("00007777-0000-1000-8000-00805f9b34fb");
//	private static  UUID CHARACTERISTIC_READ_FIFO = UUID.fromString("00008888-0000-1000-8000-00805f9b34fb");
//	private static  UUID CHARACTERISTIC_WRITE_FIFO = UUID.fromString("00008877-0000-1000-8000-00805f9b34fb");

	private static  UUID SERVICE = UUID.fromString("e49a25f8-f69a-11e8-8eb2-f2801f1b9fd1");
	private static  UUID CHARACTERISTIC_WRITE_FIFO = UUID.fromString("e49a25e0-f69a-11e8-8eb2-f2801f1b9fd1");
	private static  UUID CHARACTERISTIC_READ_FIFO = UUID.fromString("e49a28e1-f69a-11e8-8eb2-f2801f1b9fd1");

	private BluetoothGatt mBluetoothGatt;
	private BluetoothGattCharacteristic mCharacteristicReadFifo;
	private BluetoothGattCharacteristic mCharacteristicWriteFifo;

	private WriteDataBuffer mWriteBuffer = null;
	private ReadDataBuffer mReadBuffer = null;

	private BluetoothDevice mDeviceBle = null;
	private BleScanner mBleScanner;

	private Timer mTimer;

	// nick 20230522新增，用于重连
//	private TimerTask mTask = new TimerTask() {
//		@Override
//		public void run() {
//			Log.w(TAG, "gatt null or device mismatch");
//			mBluetoothGatt = null;
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//				mBluetoothGatt = mDeviceBle.connectGatt(mContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE, BluetoothDevice.PHY_LE_1M_MASK);
//			} else
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
//				mBluetoothGatt = mDeviceBle.connectGatt(mContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
//			} else {
//				mBluetoothGatt = mDeviceBle.connectGatt(mContext, false, mGattCallback);
//			}
//		}
//	};

	public BluzDeviceBle(Context context) {
        super(context,false, false);
		Log.i(TAG, "Create");

		final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		mReadBuffer = new ReadDataBuffer();
		mWriteBuffer = new WriteDataBuffer(new WriteCallback() {

			@Override
			void onStart() {
				writeCharacteristic();
			}
		});
	}

	public BluzDeviceBle(Context context, boolean a2dp, boolean disBound) {
		super(context,a2dp, disBound);
		Log.i(TAG, "Create");

		final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		mReadBuffer = new ReadDataBuffer();
		mWriteBuffer = new WriteDataBuffer(new WriteCallback() {

			@Override
			void onStart() {
				writeCharacteristic();
			}
		});
	}

	public BluzDeviceBle(Context context, Map<String,UUID> uuids) {
		this(context);
		if (uuids != null) {
			setUUID(uuids);
		}
	}

	public BluzDeviceBle(Context context, Map<String,UUID> uuids, boolean a2dp, boolean removeBound) {
		this(context, a2dp, removeBound);
		if (uuids != null) {
			setUUID(uuids);
		}
	}

	public void closeNotify() {
		if (mBluetoothGatt != null) {
			if (mCharacteristicReadFifo != null) {
				Log.d(TAG, "read");
				mBluetoothGatt.setCharacteristicNotification(mCharacteristicReadFifo, false);
			}
			if (mCharacteristicWriteFifo != null) {
				Log.d(TAG, "write");
				mBluetoothGatt.setCharacteristicNotification(mCharacteristicWriteFifo, false);
			}
		}
	}

	@Override
	public void write(byte[] buffer) throws Exception {
		boolean success = mWriteBuffer.add(buffer);
		if (!success) {
			Log.i(TAG, "too much command, dump:" + buffer.toString());
		}
	}

	@Override
	public void flush() throws Exception {

	}

	@Override
	public int read(byte[] buffer, int byteOffset, int byteCount) throws Exception {
		Log.e(TAG, "buffer len:" + buffer.length + ";byteoffset:" + byteOffset + ";byteCount:" + byteCount);
		return mReadBuffer.read(buffer, byteOffset, byteCount);
	}

	@Override
	public int readInt() throws Exception {
		byte[] buffer = new byte[4];
		read(buffer, 0, 4);
		ByteBuffer wrapped = ByteBuffer.wrap(buffer);
		return wrapped.getInt();
	}

	@Override
	public short readShort() throws Exception {
		byte[] buffer = new byte[2];
		read(buffer, 0, 2);
		ByteBuffer wrapped = ByteBuffer.wrap(buffer);
		return wrapped.getShort();
	}

	@Override
	public int read() throws Exception {
		byte[] buffer = new byte[1];
		read(buffer, 0, 1);
		return buffer[0] & 0xff;
	}

	/**
	 * in order to callback in main thread
	 */
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_CONNECTED:
				connectBleSuccess();
				break;

			case MSG_DISCONNECTED:
                Log.i(TAG, "handleMessage: MSG_DISCONNECTED");
				if (mConnectionListener != null && mDeviceConnected!=null ) {
					mConnectionListener.onDisconnected(mDeviceConnected);
					mDeviceConnected = null;
					updateConnectionState(ConnectionState.SPP_DISCONNECTED);
				}
				if (mBluetoothGatt != null) {
					mBluetoothGatt.close();
					mBluetoothGatt = null;
				}
                break;

			case MSG_FOUND:
                BluetoothDevice device = (BluetoothDevice) msg.obj;

                if (mDiscoveryListener != null) {
                    mDiscoveryListener.onFound(device, BluzDeviceFactory.DiscoveryType.DISCOVERY_NORMAL);
                } else {
					Log.e("Ble_msg_found", "mDiscoveryListener null");
				}
                break;

			case MSG_SHOW_MESSAGE:
				// Toast.makeText(mContext, "onCharacteristicChanged",
				// Toast.LENGTH_SHORT).show();
				break;

			default:
				break;
			}
		}
	};

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "onConnectionStateChange() called with: gatt = [" + gatt + "], status = [" + status + "], newState = [" + newState + "]");
            BluetoothDevice device = gatt.getDevice();
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				if (gatt != mBluetoothGatt || !device.equals(mBluetoothDevice)) {
					Log.w(TAG, "gatt null or device mismatch");
					return;
				}

				Log.i(TAG, "Attempting to start service discovery");
				mBluetoothGatt.discoverServices();

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (mGattReConnectTime < 10) {
					mGattReConnectTime = mGattReConnectTime + 1;
					Timer timer = new Timer();
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							Log.w(TAG, "gatt null or device mismatch");
							if (mBluetoothGatt != null) {
								mBluetoothGatt.close();
								mBluetoothGatt = null;
							}
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
								mBluetoothGatt = mDeviceBle.connectGatt(mContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE, BluetoothDevice.PHY_LE_1M_MASK);
							} else
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
								mBluetoothGatt = mDeviceBle.connectGatt(mContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
							} else {
								mBluetoothGatt = mDeviceBle.connectGatt(mContext, false, mGattCallback);
							}
						}
					}, 100);
                } else {
					//use mGatt.close won't invoke this
					Log.i(TAG, "Disconnected from GATT server.");
					mHandler.sendEmptyMessage(MSG_DISCONNECTED);
				}
			}

			mConnecting = false;
		}

		@Override
		public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
			Log.d("mtu", "onMtuChanged() called with: gatt = [" + gatt + "], mtu = [" + mtu + "], status = [" + status + "]");
			//20 to ensure that shorter command can succeed when mtu change failed
			int finalMtu = 20;
			if (status == BluetoothGatt.GATT_SUCCESS) {
				finalMtu = mtu-5;
			}
			Log.v(TAG, "onMtuChanged: " + finalMtu);
			mWriteBuffer.setWriteMaxLength(finalMtu);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				//mBluetoothGatt.setPreferredPhy(BluetoothDevice.PHY_LE_2M, BluetoothDevice.PHY_LE_2M, BluetoothDevice.PHY_OPTION_NO_PREFERRED);
			}
		}

		@Override
		public void onPhyUpdate (BluetoothGatt gatt,
								 int txPhy,
								 int rxPhy,
								 int status){
			Log.d(TAG,"onPhyUpdate txPhy: " + txPhy +" rxPhy: " + rxPhy + " status: " + status);
		}

		public void onConnectionUpdated(BluetoothGatt gatt, int interval, int latency, int timeout,
										int status) {
			Log.d(TAG,"onConnectionUpdated interval: " + interval +" rxPhy: " + latency + " latency: " + latency
					+ " timeout: " + timeout + " status: " + status);
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (gatt != mBluetoothGatt) {
				return;
			}

			if (status == BluetoothGatt.GATT_SUCCESS) {
				findServiceAndCharacteristic();
			} else {
				Log.w(TAG, "onServicesDiscovered received: " + status);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            // not used http://stackoverflow.com/questions/25865587/android-4-3-bluetooth-ble-dont-called-oncharacteristicread
            Log.d(TAG, "onCharacteristicRead() : characteristic = [" + characteristic.getUuid() + "], status = [" + status + "]");
            if (status == BluetoothGatt.GATT_SUCCESS) {
				readCharacteristicSuccess(characteristic);
			} else {
				Log.w(TAG, "onCharacteristicRead received: " + status);
			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			Log.i(TAG, "onCharacteristicWrite: characteristic = [" + characteristic.getUuid()+"], status = ["  + status);
			if (status == BluetoothGatt.GATT_SUCCESS || status == BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
                writeCharacteristicSuccess(characteristic);
            else {
				Log.w(TAG, "onCharacteristicWrite received: " + status);
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.i(TAG, "onCharacteristicChanged: characteristic = [" + characteristic.getUuid()+"]");
            readIndicator(characteristic);
		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			Log.i(TAG, "onDescriptorRead:" + descriptor);
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(TAG, "onDescriptorWrite() called with: gatt = [" + gatt + "], descriptor = [" + descriptor + "], status = [" + status + "]");
			if (status == BluetoothGatt.GATT_SUCCESS) {

			//	knockDoor();
                if (Build.VERSION.SDK_INT >= 21) {
                    //for device don't have requestMtu method but has MTU larger than 20 like MX5

					Log.v(TAG, "android.os.Build.MANUFACTURER: " + android.os.Build.MANUFACTURER + " " + Build.MODEL);
                    if ("Meizu".equals(android.os.Build.MANUFACTURER)) {
                        // 魅族手机,其蓝牙模组不遵循MTU协议，手机与设备无法协商MTU长度，可能导致发包长度超出设备处理能力�?
						// 因此MTU设置�?0byte的有效负载，以兼容更多的设备�?
                        mWriteBuffer.setWriteMaxLength(20);
                    } else if ("Xiaomi".equals(android.os.Build.MANUFACTURER) && "Mi-4c".equals(Build.MODEL)) {
						//小米4C设置大MTU，会导致包头被丢掉几十个byte的问�?
						mBluetoothGatt.requestMtu(20);
					} else if ("QiKU".equals(android.os.Build.MANUFACTURER) && "8692-M02".equals(Build.MODEL)) {
						// 该手机蓝牙拆包传输会出错
						mBluetoothGatt.requestMtu(20);
					} else {
						mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
                        mBluetoothGatt.requestMtu(512);
                    }
                    // delay to prevent 133 problem
                    // delay here but not in the mtu callback because
                    // some device don't have requestMtu Method like Meizu Mx5
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                mHandler.sendEmptyMessage(MSG_CONNECTED);

			} else {
				Log.w(TAG, "onDescriptorWrite received:" + status);
			}
		}
	};

	private void writeCharacteristic() {
		Log.v(TAG + "nickk", "writeCharacteristic()");
        if (mBluetoothGatt != null) {
			byte[] buffer = mWriteBuffer.getBuffer();
			Utils.printHexBuffer(TAG, buffer);
			Log.e(TAG+"bufferlen", buffer.length + "");
			mCharacteristicWriteFifo.setValue(buffer);
            mBluetoothGatt.writeCharacteristic(mCharacteristicWriteFifo);
        } else {
			Log.v(TAG, "gatt == null");
		}
	}

	private void writeCharacteristicSuccess(BluetoothGattCharacteristic characteristic) {
		if (characteristic == mCharacteristicWriteFifo) {
			if (mWriteBuffer.isEnd()) {
				/* current item finish write next */
				mWriteBuffer.next();
			} else {
				writeCharacteristic();
			}
		}
	}

	private void enableCCC(BluetoothGattCharacteristic characteristic) {
		Log.i(TAG, "enableCCC" );
		//setWriteType is necessary so that notification is enable
		characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
		mBluetoothGatt.setCharacteristicNotification(characteristic, true);
		BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION);
		descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		mBluetoothGatt.writeDescriptor(descriptor);
	}

	private void knockDoor() {
		byte[] buffer = new byte[] { '0', '1', '2', '3', '4', '5', '6', '7' };
		try {
			write(buffer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
/*
	private void findServiceAndCharacteristic() {
		List<BluetoothGattService> services = mBluetoothGatt.getServices();
		for (BluetoothGattService service : services) {
			Log.i(TAG, "service uuid:" + service.getUuid().toString());
			List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();

			if (service.getUuid().equals(SERVICE_READ)) {
				for (BluetoothGattCharacteristic characteristic : characteristics) {
					Log.i(TAG, "read characteristic uuid:" + characteristic.getUuid().toString());
					UUID uuid = characteristic.getUuid();
					if (uuid.equals(CHARACTERISTIC_READ_FIFO)) {
						mCharacteristicReadFifo = characteristic;
					}
				}
			} else if (service.getUuid().equals(SERVICE_WRITE)) {
				for (BluetoothGattCharacteristic characteristic : characteristics) {
					Log.i(TAG, "write characteristic uuid:" + characteristic.getUuid().toString());
					UUID uuid = characteristic.getUuid();
					if (uuid.equals(CHARACTERISTIC_WRITE_FIFO)) {
						characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
						mCharacteristicWriteFifo = characteristic;
					}
				}
			}
		}
*/

	private void findServiceAndCharacteristic() {
		List<BluetoothGattService> services = mBluetoothGatt.getServices();
		for (BluetoothGattService service : services) {
			Log.i(TAG, "service uuid:" + service.getUuid().toString());
			List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();

			if (service.getUuid().equals(SERVICE)) {
				for (BluetoothGattCharacteristic characteristic : characteristics) {
					Log.i(TAG, "read characteristic uuid:" + characteristic.getUuid().toString());
					UUID uuid = characteristic.getUuid();
					if (uuid.equals(CHARACTERISTIC_READ_FIFO)) {
						mCharacteristicReadFifo = characteristic;
					} else if (uuid.equals(CHARACTERISTIC_WRITE_FIFO)) {
						characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
						mCharacteristicWriteFifo = characteristic;
					}
				}
			}
		}

		if (mCharacteristicReadFifo != null && mCharacteristicWriteFifo != null) {
			Log.i(TAG, "write fifo type:" + mCharacteristicWriteFifo.getWriteType());
			Log.i(TAG, "read fifo type:" + mCharacteristicReadFifo.getWriteType());
			Log.i(TAG, "write fifi property:" + mCharacteristicWriteFifo.getProperties());
			Log.i(TAG, "read fifo property:" + mCharacteristicReadFifo.getProperties());

			enableCCC(mCharacteristicReadFifo);
		}
	}

	private void readCharacteristicSuccess(BluetoothGattCharacteristic characteristic) {
		if (characteristic == mCharacteristicReadFifo) {
			Log.i(TAG, "readCharacteristicSuccess");
			byte[] value = characteristic.getValue();

			mReadBuffer.write(value);
		}
	}

	private void readIndicator(BluetoothGattCharacteristic characteristic) {
		if (characteristic == mCharacteristicReadFifo) {
			byte[] buffer = characteristic.getValue();
			mReadBuffer.add(buffer.length);
			mReadBuffer.write(buffer);
		}
	}

    private Runnable mScanCallback = new Runnable() {
        @Override
        public synchronized void run() {
            Log.i(TAG, "mScanCallback timeout");
            cancelDiscovery();
        }
    };

	private Runnable mConnectCallback = new Runnable() {
		@Override
		public synchronized void run() {
			if (mDeviceConnected == null) {
				Log.i(TAG, "mConnectCallback null");
				connectBleFail();
			}
		}
	};

	private SimpleScanCallback simpleScanCallback = new SimpleScanCallback() {
		@Override
		public void onBleScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			if (device.getName() != null)
				Log.d(TAG, "onBleScan() called with: device = [" + device.getName() + "], rssi = [" + rssi + "], scanRecord = [" + scanRecord + "]");
			mHandler.obtainMessage(MSG_FOUND, device).sendToTarget();
		}

		@Override
		public void onBleScanFailed(BleScanState scanState) {
			Log.d(TAG, "onBleScanFailed() called with: scanState = [" + scanState + "]");
		}
	};

    @Override
    public void startDiscovery() {
//        super.startDiscovery();
        if (android.os.Build.VERSION.SDK_INT >= 23 && !checkLocationPermission()) {
            Log.d(TAG, "startDiscovery fail: need Permission ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION");
        }
        cancelDiscovery();
		mTimer = new Timer();
		MyTimerTask task = new MyTimerTask();
		mTimer.schedule(task, SCAN_TIMEOUT);
//        mHandler.postDelayed(mScanCallback, SCAN_TIMEOUT);
		if(mBleScanner == null) {
			mBleScanner = new BleScanner(mContext, simpleScanCallback);
		}
		mBleScanner.startBleScan();

        if (mDiscoveryListener != null) {
            mDiscoveryListener.onDiscoveryStarted();
        }
    }

	class MyTimerTask extends TimerTask {
		@Override
		public void run() {
			Log.i(TAG, "mScanCallback timeout");
			cancelDiscovery();
		}
	}

    @Override
    protected void cancelDiscovery() {
//        super.cancelDiscovery();
        if (mDiscoveryListener != null) {
            mDiscoveryListener.onDiscoveryFinished();
        }
        mHandler.removeCallbacks(mScanCallback);
		if(mBleScanner != null) {
			mBleScanner.stopBleScan();
			mBleScanner = null;
		}

    }

    @Override
    public void retry(BluetoothDevice device) {
        connect(device);
    }

	@Override
	public void connect() {
		super.connect();
		if (mBluetoothDevice == null) {
			Log.e("ble", "ble == null");
			return;
		}
        mDeviceBle = mBluetoothDevice;
        connectBle();
	}

	private void connectBle() {
		Log.v(TAG, "connectBle");
        updateConnectionState(ConnectionState.SPP_CONNECTING);
		if (mTimer != null) {
			mTimer.cancel();
		}
		mHandler.removeCallbacks(mScanCallback);
		mHandler.postDelayed(mConnectCallback, CONNECT_TIMEOUT);
		String buildBound = android.os.Build.BRAND;
		mGattReConnectTime = 0;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			mBluetoothGatt = mDeviceBle.connectGatt(mContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE, BluetoothDevice.PHY_LE_1M_MASK);
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
			mBluetoothGatt = mDeviceBle.connectGatt(mContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
		} else {
			mBluetoothGatt = mDeviceBle.connectGatt(mContext, false, mGattCallback);
		}
	}

	private void connectBleFail() {
		Log.i(TAG, "BLE connect fail");

		mHandler.removeCallbacks(mConnectCallback);
        updateConnectionState(ConnectionState.SPP_FAILURE);
		disconnect();
        mBluetoothDevice = null;
        mConnecting = false;
	}

	private synchronized void connectBleSuccess() {
		try {
			Log.i(TAG, "BLE connected");
			mDeviceConnected = mBluetoothDevice;
            mHandler.removeCallbacks(mConnectCallback);
			updateConnectionState(ConnectionState.SPP_CONNECTED);
			if (mConnectionListener != null) {
				mConnectionListener.onConnected(mBluetoothDevice);
			}
		} catch (Exception e) {
			handleException(e);
		}
	}

	@Override
	public void disconnect() {
        if (mBluetoothGatt != null) {
            if (mConnectionListener != null && mDeviceConnected!=null ) {
                Log.i(TAG, "handleMessage: mConnectionListener");
                mConnectionListener.onDisconnected(mDeviceConnected);
                mDeviceConnected = null;
                updateConnectionState(ConnectionState.SPP_DISCONNECTED);
            }
			closeNotify();
            mBluetoothGatt.disconnect();
			Log.i(TAG, "mBluetoothGatt.disconnect()");
            mBluetoothGatt.close();
			Log.i(TAG, "mBluetoothGatt.close()");

            mBluetoothGatt = null;
            mReadBuffer = new ReadDataBuffer();
            mWriteBuffer = new WriteDataBuffer(new WriteCallback() {

                @Override
                void onStart() {
                    writeCharacteristic();
                }
            });

        }
		if (mDeviceA2dp != null) {
			mDeviceA2dp.disconnect(mDeviceConnected == null ? mDeviceA2dp.getConnectedDevice() : mDeviceConnected);
		}
	}

	@Override
	public BluetoothDevice getConnectedDevice() {
		/* always show the connected a2dp device after connected */
		return mDeviceConnected != null ? mBluetoothDevice : null;
	}

	@Override
	public Set<BluetoothDevice> getBoundedDevices(){
		Set<BluetoothDevice> mDevicesSet = new HashSet<>();
		Set<BluetoothDevice> mBoundedSet = mBluetoothAdapter.getBondedDevices();
		for (BluetoothDevice device : mBoundedSet) {
			if (device.getType() == BluetoothDevice.DEVICE_TYPE_LE
					|| device.getType() == BluetoothDevice.DEVICE_TYPE_DUAL){
				mDevicesSet.add(device);
			}
		}
		return mDevicesSet;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
//		mHandler.removeCallbacks(mScanCallback);
//		mHandler.removeCallbacks(mConnectCallback);
	}

	public void setUUID( Map<String,UUID> uuids){
		int mapSize = uuids.size();
		Iterator it = uuids.entrySet().iterator();
		for (int i=0; i < mapSize; i++){
			Map.Entry<String,UUID> entry = (Map.Entry<String,UUID>) it.next();
			String key = entry.getKey();
			UUID value = entry.getValue();
			if (key.equals(BluzDeviceFactory.BleKeyUUIDS.keyConfigurationUUID)){
				CLIENT_CHARACTERISTIC_CONFIGURATION = value;
				Log.d(TAG,"setUUID keyConfigurationUUID: " + value.toString());
			}
			else if (key.equals(BluzDeviceFactory.BleKeyUUIDS.keyServiceUUID)){
				SERVICE = value;
				Log.d(TAG,"setUUID keyServiceUUID: " + value.toString());
			}
		//	else if (key.equals(BluzDeviceFactory.BleKeyUUIDS.keyWriteServiceUUID)){
		//		SERVICE_WRITE = value;
		//		Log.d(TAG,"setUUID keyWriteServiceUUID: " + value.toString());
		//	}
			else if (key.equals(BluzDeviceFactory.BleKeyUUIDS.keyReadCharacteristicUUID)){
				CHARACTERISTIC_READ_FIFO = value;
				Log.d(TAG,"setUUID keyReadCharacteristicUUID: " + value.toString());
			}
			else if (key.equals(BluzDeviceFactory.BleKeyUUIDS.keyWriteCharacteristicUUID)){
				CHARACTERISTIC_WRITE_FIFO = value;
				Log.d(TAG,"setUUID keyWriteCharacteristicUUID: " + value.toString());
			}
		}
	}
}