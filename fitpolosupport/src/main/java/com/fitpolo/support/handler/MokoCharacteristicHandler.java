package com.fitpolo.support.handler;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.text.TextUtils;

import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.entity.MokoCharacteristic;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;

import java.util.HashMap;
import java.util.List;

public class MokoCharacteristicHandler {
    private static MokoCharacteristicHandler INSTANCE;

    public static final String SERVICE_UUID_SET = "e49a23c0";
    public static final String SERVICE_UUID_XOFF = "00009527";
    public static final String SERVICE_UUID_DATA_PUSH = "e49a25c0";
    public static final String SERVICE_UUID_XON_FRAME = "00009527";

    public HashMap<OrderType, MokoCharacteristic> mokoCharacteristicMap;

    private MokoCharacteristicHandler() {
        mokoCharacteristicMap = new HashMap<>();
    }

    public static MokoCharacteristicHandler getInstance() {
        if (INSTANCE == null) {
            synchronized (MokoCharacteristicHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MokoCharacteristicHandler();
                }
            }
        }
        return INSTANCE;
    }

    public HashMap<OrderType, MokoCharacteristic> getCharacteristics(final BluetoothGatt gatt) {
        if (mokoCharacteristicMap != null && !mokoCharacteristicMap.isEmpty()) {
            mokoCharacteristicMap.clear();
        }
        List<BluetoothGattService> services = gatt.getServices();
        for (final BluetoothGattService service : services) {
            String serviceUuid = service.getUuid().toString();
            if (TextUtils.isEmpty(serviceUuid)) {
                continue;
            }
            if (serviceUuid.startsWith("00001800") || serviceUuid.startsWith("00001801")) {
                continue;
            }
            final List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            if (service.getUuid().toString().startsWith(SERVICE_UUID_SET)) {
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    String characteristicUuid = characteristic.getUuid().toString();
                    if (TextUtils.isEmpty(characteristicUuid)) {
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.NOTIFY.getUuid())) {
                        boolean success = gatt.setCharacteristicNotification(characteristic, true);
                        LogModule.i("DATA_SET setCharacteristicNotification"+ success);
                        mokoCharacteristicMap.put(OrderType.NOTIFY, new MokoCharacteristic(characteristic, OrderType.NOTIFY));
                        mokoCharacteristicMap.put(OrderType.WRITE, new MokoCharacteristic(characteristic, OrderType.WRITE));
                        MokoSupport.getInstance().setNotifyDesc(characteristic);
                        continue;
                    }

                    if (characteristicUuid.equals(OrderType.WRITE.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.WRITE, new MokoCharacteristic(characteristic, OrderType.WRITE));
                        continue;
                    }
                }
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 这里是你想要延迟1秒后执行的代码
                    if(service.getUuid().toString().startsWith(SERVICE_UUID_DATA_PUSH)){
                        for (BluetoothGattCharacteristic characteristic : characteristics) {
                            String characteristicUuid = characteristic.getUuid().toString();
                            if (TextUtils.isEmpty(characteristicUuid)) {
                                continue;
                            }
                            if (characteristicUuid.equals(OrderType.DataPushNOTIFY.getUuid())) {
                                boolean success = gatt.setCharacteristicNotification(characteristic, true);
                                LogModule.i("DATA_PUSH setCharacteristicNotification"+ success);
                                MokoSupport.getInstance().setNotifyDesc(characteristic);
                                mokoCharacteristicMap.put(OrderType.DataPushNOTIFY, new MokoCharacteristic(characteristic, OrderType.DataPushNOTIFY));
                                mokoCharacteristicMap.put(OrderType.DataPushWRITE, new MokoCharacteristic(characteristic, OrderType.DataPushWRITE));
                                continue;
                            }
                            if (characteristicUuid.equals(OrderType.DataPushWRITE.getUuid())) {
                                mokoCharacteristicMap.put(OrderType.DataPushWRITE, new MokoCharacteristic(characteristic, OrderType.DataPushWRITE));
                                continue;
                            }
                        }
                    }
                }
            }, 1000);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 这里是你想要延迟1秒后执行的代码
                    if(service.getUuid().toString().startsWith(SERVICE_UUID_XON_FRAME)){
                        for (BluetoothGattCharacteristic characteristic : characteristics) {
                            String characteristicUuid = characteristic.getUuid().toString();
                            if (TextUtils.isEmpty(characteristicUuid)) {
                                continue;
                            }
                            if (characteristicUuid.equals(OrderType.XONFRAMENOTIFY.getUuid())) {
                                boolean success = gatt.setCharacteristicNotification(characteristic, true);
                                LogModule.i("XON_FRAME setCharacteristicNotification"+ success);
                                MokoSupport.getInstance().setNotifyDesc(characteristic);
                                mokoCharacteristicMap.put(OrderType.XONFRAMENOTIFY, new MokoCharacteristic(characteristic, OrderType.XONFRAMENOTIFY));
                                continue;
                            }
                            if (characteristicUuid.equals(OrderType.XONFRAMEWRITE.getUuid())) {
                                mokoCharacteristicMap.put(OrderType.XONFRAMEWRITE, new MokoCharacteristic(characteristic, OrderType.XONFRAMEWRITE));
                                continue;
                            }
                        }
                    }
                }
            }, 1500);

        }
        return mokoCharacteristicMap;
    }
}
