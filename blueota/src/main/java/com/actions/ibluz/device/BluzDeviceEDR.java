package com.actions.ibluz.device;

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

import com.actions.ibluz.device.blescan.BleScanState;
import com.actions.ibluz.device.blescan.SimpleScanCallback;
import com.tencent.mars.xlog.Log;

import com.actions.ibluz.factory.BluzDeviceFactory;
import com.actions.ibluz.util.Utils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BluzDeviceEDR extends BluzDeviceBase {
    private static final String TAG = "BluzDeiceEDR";

    private static UUID sClientCharacteristicConfiguration = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
//    private static UUID sService = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
    private static  UUID sService = UUID.fromString("e49a25f8-f69a-11e8-8eb2-f2801f1b9fd1");
    private static UUID sCharacteristicWriteFifo = UUID.fromString("e49a25e0-f69a-11e8-8eb2-f2801f1b9fd1");
    private static UUID sCharacteristicReadFifo = UUID.fromString("e49a28e1-f69a-11e8-8eb2-f2801f1b9fd1");

    private static final int MSG_CONNECTED = 1;
    private static final int MSG_DISCONNECTED = 2;

    private static final int CONNECT_TIMEOUT = 10000;

    private BluetoothDevice mDeviceEdr = null;

    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mCharacteristicReadFifo;
    private BluetoothGattCharacteristic mCharacteristicWriteFifo;

    private DataBuffer.WriteDataBuffer mWriteBuffer = null;
    private DataBuffer.ReadDataBuffer mReadBuffer = null;


    // --------------------------------- handler -------------------------------------- //
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_CONNECTED:
                    Log.i(TAG, "handleMsg: MSG_CONNECTED");
                    connectEdrSuccess();
                    break;
                case MSG_DISCONNECTED:
                    Log.i(TAG, "handleMsg: MSG_DISCONNECTED");
                    if (mConnectionListener != null && mDeviceConnected != null) {
                        mConnectionListener.onDisconnected(mDeviceConnected);
                        mDeviceConnected = null;
                        updateConnectionState(BluzDeviceFactory.ConnectionState.SPP_DISCONNECTED);
                    }
                    break;
            }

        }
    };

    // ------------------------- gatt callback ----------------------------- //

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            Log.d(TAG, "onPhyUpdate txPhy: " + txPhy + " rxPhy: " + rxPhy + " status: " + status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "onConnectionStateChange() called with: gatt = [" + gatt + "], status = [" + status + "], newState = [" + newState + "]");
            BluetoothDevice device = gatt.getDevice();
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (gatt != mBluetoothGatt || !device.equals(mBluetoothDevice)) {
                    com.tencent.mars.xlog.Log.w(TAG, "gatt null or device mismatch");
                    return;
                }

                Log.i(TAG, "Attempting to start service discovery");
                gatt.discoverServices();


            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (gatt != mBluetoothGatt || !device.equals(mBluetoothDevice)) {
                    Log.w(TAG, "gatt null or device mismatch");
                    return;
                }
                //use mGatt.close won't invoke this
                Log.i(TAG, "Disconnected from GATT server.");
                mHandler.sendEmptyMessage(MSG_DISCONNECTED);
            }

            mConnecting = false;
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(TAG,"onServicesDiscovered");
            if (gatt != mBluetoothGatt) {
                return ;
            }
            Log.i(TAG,"onServicesDiscovered:" + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, sService + "");
                BluetoothGattService service = gatt.getService(sService);
                if (service != null) {
                    List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                    for (BluetoothGattCharacteristic characteristic : characteristics) {
                        Log.i(TAG, "read characteristic uuid:" + characteristic.getUuid().toString());
                        UUID uuid = characteristic.getUuid();
                        if (uuid.equals(sCharacteristicReadFifo)) {
                            mCharacteristicReadFifo = characteristic;
                            Log.e(TAG, "mCharacteristicReadFifo");
                        } else if (uuid.equals(sCharacteristicWriteFifo)) {
                            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                            mCharacteristicWriteFifo = characteristic;
                            Log.e(TAG, "mCharacteristicWriteFifo");
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
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
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
                        Log.e("Xiaomi", "mtu:20");
                        mBluetoothGatt.requestMtu(20);
                    } else if ("QiKU".equals(android.os.Build.MANUFACTURER) && "8692-M02".equals(Build.MODEL)) {
                        // 该手机蓝牙拆包传输会出错
                        mBluetoothGatt.requestMtu(20);
                    } else {
                        Log.e("nick_mtu", "mtu:512");
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

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            Log.d("NICK_chanage", "onMtuChanged() called with: gatt = [" + gatt + "], mtu = [" + mtu + "], status = [" + status + "]");
            //20 to ensure that shorter command can succeed when mtu change failed
            int finalMtu = 20;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                finalMtu = mtu-5;
            }
            Log.v(TAG, "onMtuChanged: " + finalMtu);
            mWriteBuffer.setWriteMaxLength(finalMtu);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                //mBluetoothGatt.setPreferredPhy(BluetoothDevice.PHY_LE_2M, BluetoothDevice.PHY_LE_2M, BluetoothDevice.PHY_OPTION_NO_PREFERRED);
//            }
        }
    };

    // ---------------------------------- --------------------------------------------- //


    private void enableCCC(BluetoothGattCharacteristic characteristic) {
        Log.i(TAG, "enableCCC" );
        //setWriteType is necessary so that notification is enable
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        mBluetoothGatt.setCharacteristicNotification(characteristic, true);
        BluetoothGattDescriptor descriptor  = characteristic.getDescriptor(sClientCharacteristicConfiguration);

        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    private void readCharacteristicSuccess(BluetoothGattCharacteristic characteristic) {
        if (characteristic == mCharacteristicReadFifo) {
            Log.i(TAG, "readCharacteristicSuccess");
            byte[] value = characteristic.getValue();

            mReadBuffer.write(value);
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

    private void readIndicator(BluetoothGattCharacteristic characteristic) {
        if (characteristic == mCharacteristicReadFifo) {
            byte[] buffer = characteristic.getValue();
            mReadBuffer.add(buffer.length);
            mReadBuffer.write(buffer);
        }
    }

    // ------------------------------------------- init ------------------------------------------ //
    public BluzDeviceEDR(Context context, Map<String, UUID> uuids, boolean a2dp, boolean removeBound) {
        super(context, a2dp, removeBound);
        init(context);
        Log.e("nick", "BluzDeviceEDR 4");
        if (null != uuids) {
            setUUID(uuids);
        }
    }

    public BluzDeviceEDR(Context context, Map<String, UUID> uuids) {
        this(context, uuids, false, false);
    }

    private void init(Context context) {
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();

        mReadBuffer = new DataBuffer.ReadDataBuffer();
        mWriteBuffer = new DataBuffer.WriteDataBuffer(new DataBuffer.WriteCallback() {
            @Override
            void onStart() {
                writeCharacteristic();
            }
        });
    }

    // 关闭notify
    public void closeNotify() {
        if (mBluetoothGatt != null) {
            if (mCharacteristicReadFifo != null) {
                Log.d(TAG, "closeNotify: read close");
                mBluetoothGatt.setCharacteristicNotification(mCharacteristicReadFifo, false);
            }
            if (mCharacteristicWriteFifo != null) {
                Log.d(TAG, "closeNotify: write close");
                mBluetoothGatt.setCharacteristicNotification(mCharacteristicWriteFifo, false);
            }
        }
    }

    // ---------------------------------- base -----------------------------------------//

    @Override
    public void disconnect() {
        if (mBluetoothGatt != null) {
            if (mConnectionListener != null && mDeviceConnected!=null ) {
                Log.i(TAG, "handleMessage: mConnectionListener");
                mConnectionListener.onDisconnected(mDeviceConnected);
                mDeviceConnected = null;
                updateConnectionState(BluzDeviceFactory.ConnectionState.SPP_DISCONNECTED);
            }
            closeNotify();
            mBluetoothGatt.disconnect();
            Log.i(TAG, "mBluetoothGatt.disconnect()");
            mBluetoothGatt.close();
            Log.i(TAG, "mBluetoothGatt.close()");
            mBluetoothGatt = null;
            mReadBuffer = new DataBuffer.ReadDataBuffer();
            mWriteBuffer = new DataBuffer.WriteDataBuffer(new DataBuffer.WriteCallback() {

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
    public void write(byte[] buffer) throws Exception {
        boolean success = mWriteBuffer.add(buffer);
        if (!success) {
            Log.i(TAG, "write: too much command, dump:" + buffer.toString());
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

    @Override
    public void retry(BluetoothDevice device) {
        connect(device);
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
            if (device.getType() == BluetoothDevice.DEVICE_TYPE_CLASSIC){
                mDevicesSet.add(device);
            }
        }
        return mDevicesSet;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Log.e(TAG, "finalize");
        mHandler.removeCallbacks(mScanCallback);
        mHandler.removeCallbacks(mConnectCallback);
    }

    // ------------------------------------ base ----------------------------------------- //

    private void writeCharacteristic() {
        Log.v(TAG, "writeCharacteristic");

        if (mBluetoothGatt != null) {
            byte[] buffer = mWriteBuffer.getBuffer();
            Utils.printHexBuffer(TAG, buffer);

            mCharacteristicWriteFifo.setValue(buffer);
            Log.e(TAG + "bufferlen", buffer.length + "");
            mBluetoothGatt.writeCharacteristic(mCharacteristicWriteFifo);
        }
    }

    private void setUUID(Map<String, UUID> uuids) {

        int mapSize = uuids.size();
        Log.e(TAG, "setUUID：" + mapSize);
        Iterator it = uuids.entrySet().iterator();

        for (int i = 0; i < mapSize; i++) {
            Map.Entry<String, UUID> entry = (Map.Entry<String, UUID>) it.next();
            String key = entry.getKey();
            UUID value = entry.getValue();
            if (key.equals(BluzDeviceFactory.EdrKeyUUIDS.keyConfigurationUUID)) {
                sClientCharacteristicConfiguration = value;
                Log.e(TAG, "setUUID keyConfiurationUUID: " + value.toString());
            } else if (key.equals(BluzDeviceFactory.EdrKeyUUIDS.keyServiceUUID)) {
                sService = value;
                Log.e(TAG, "setUUID keyServiceUUID: " + value.toString());
            } else if (key.equals(BluzDeviceFactory.EdrKeyUUIDS.keyReadCharacteristicUUID)) {
                sCharacteristicReadFifo = value;
                Log.d(TAG, "setUUID keyReadCharacteristicUUID: " + value.toString());
            } else if (key.equals(BluzDeviceFactory.EdrKeyUUIDS.keyWriteCharacteristicUUID)) {
                sCharacteristicWriteFifo = value;
                Log.d(TAG, "setUUID keyWriteCharacteristicUUID: " + value.toString());
            }
        }
    }



    // ---------------------------------------- connect ------------------------------------------ //
    @Override
    public void connect() {
        super.connect();
        if (mBluetoothDevice == null) {
            Log.e("edr", "edr == null");
            return ;
        }

        mDeviceEdr = mBluetoothDevice;
        connectEdr();
    }

    private void connectEdr() {
        Log.v(TAG, "connectEDR");
        updateConnectionState(BluzDeviceFactory.ConnectionState.SPP_CONNECTING);
        mHandler.removeCallbacks(mScanCallback);
        mHandler.postDelayed(mConnectCallback, CONNECT_TIMEOUT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBluetoothGatt = mDeviceEdr.connectGatt(mContext, false, mGattCallback);
            Log.e(TAG, "mBluetoothGatt: " + mBluetoothGatt);
        } else {
            mBluetoothGatt = mDeviceEdr.connectGatt(mContext, false, mGattCallback);
        }
    }

    private synchronized void connectEdrSuccess() {
        try {

            Log.e(TAG, "EDR connected：" + mBluetoothDevice);
            mDeviceConnected = mBluetoothDevice;
            mHandler.removeCallbacks(mConnectCallback);
            updateConnectionState(BluzDeviceFactory.ConnectionState.SPP_CONNECTED);
            if (mConnectionListener != null) {
                mConnectionListener.onConnected(mBluetoothDevice);
            }
        } catch (Exception e) {
            Log.e(TAG, "connectEdr exception: " + e);
            handleException(e);
        }
    }

    private void connectEdrFail() {
        Log.e(TAG, "EDR connect fail");

        mHandler.removeCallbacks(mConnectCallback);
        updateConnectionState(BluzDeviceFactory.ConnectionState.SPP_FAILURE);
        disconnect();

        mBluetoothDevice = null;
        mConnecting = false;
    }

    // -------------------------------------------- callback ----------------------------------------- //
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
                connectEdrFail();
            }
        }
    };

}
