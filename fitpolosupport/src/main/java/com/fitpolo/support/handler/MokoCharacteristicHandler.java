package com.fitpolo.support.handler;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.text.TextUtils;

import com.fitpolo.support.entity.MokoCharacteristic;
import com.fitpolo.support.entity.OrderType;

import java.util.HashMap;
import java.util.List;

public class MokoCharacteristicHandler {
    private static MokoCharacteristicHandler INSTANCE;

    public static final String SERVICE_UUID_SET = "e49a23c0";
    public static final String SERVICE_UUID_XOFF = "00009527";
    public static final String SERVICE_UUID_DATA_PUSH = "e49a25c0";

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

    public HashMap<OrderType, MokoCharacteristic> getCharacteristics(BluetoothGatt gatt) {
        if (mokoCharacteristicMap != null && !mokoCharacteristicMap.isEmpty()) {
            mokoCharacteristicMap.clear();
        }
        List<BluetoothGattService> services = gatt.getServices();
        for (BluetoothGattService service : services) {
            String serviceUuid = service.getUuid().toString();
            if (TextUtils.isEmpty(serviceUuid)) {
                continue;
            }
            if (serviceUuid.startsWith("00001800") || serviceUuid.startsWith("00001801")) {
                continue;
            }
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            if (service.getUuid().toString().startsWith(SERVICE_UUID_SET)) {
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    String characteristicUuid = characteristic.getUuid().toString();
                    if (TextUtils.isEmpty(characteristicUuid)) {
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.NOTIFY.getUuid())) {
                        gatt.setCharacteristicNotification(characteristic, true);
                        mokoCharacteristicMap.put(OrderType.NOTIFY, new MokoCharacteristic(characteristic, OrderType.NOTIFY));
                        mokoCharacteristicMap.put(OrderType.WRITE, new MokoCharacteristic(characteristic, OrderType.WRITE));
                        continue;
                    }

                    if (characteristicUuid.equals(OrderType.WRITE.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.WRITE, new MokoCharacteristic(characteristic, OrderType.WRITE));
                        continue;
                    }
                }
            }
            if(service.getUuid().toString().startsWith(SERVICE_UUID_DATA_PUSH)){
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    String characteristicUuid = characteristic.getUuid().toString();
                    if (TextUtils.isEmpty(characteristicUuid)) {
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.DataPushNOTIFY.getUuid())) {
                        gatt.setCharacteristicNotification(characteristic, true);
                        mokoCharacteristicMap.put(OrderType.DataPushNOTIFY, new MokoCharacteristic(characteristic, OrderType.DataPushNOTIFY));
                        continue;
                    }

                    if (characteristicUuid.equals(OrderType.DataPushWRITE.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.DataPushWRITE, new MokoCharacteristic(characteristic, OrderType.DataPushWRITE));
                        continue;
                    }
                }
            }
        }
        return mokoCharacteristicMap;
    }
}
