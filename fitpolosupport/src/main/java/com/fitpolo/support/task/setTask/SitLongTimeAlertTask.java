package com.fitpolo.support.task.setTask;

import android.text.TextUtils;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.entity.SitAlert;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.ByteType;
import com.fitpolo.support.utils.DigitalConver;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置久坐提醒
 */
public class SitLongTimeAlertTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 17;
    // 久坐提醒
    private static final int HEADER_SET_SIT_LONG_TIME_ALERT = 0x2A;

    private byte[] orderData;
    SitAlert sitAlert;

    public SitLongTimeAlertTask(MokoOrderTaskCallback callback, SitAlert sitAlert) {
        super(OrderType.WRITE, OrderEnum.setSitLongTimeAlert, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        List<Byte> byteList = new ArrayList<>();

        byte onOff = sitAlert.interval == 0 ? (byte)0x00 : (byte)0x01;//开关 00 01
        byteList.add(onOff);

        byte[] timeU8 = DigitalConver.hex2bytes(Integer.toHexString(sitAlert.interval));
        for (byte b : timeU8) { byteList.add(b); }

        List<Byte> startU8 = DigitalConver.convert(sitAlert.startTime*60,ByteType.WORD);
        byteList.addAll(startU8);

        List<Byte> endU8 = DigitalConver.convert(sitAlert.endTime*60,ByteType.WORD);
        byteList.addAll(endU8);
        
        byteList.add((byte) 0x00);
        // List<Byte> 转换为 byte[]
        byte[] dataBytes = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            dataBytes[i] = byteList.get(i);
        }
        byte[] array1 = new byte[]{
            (byte) MokoConstants.HEADER_READ_SEND,
            (byte) (5 + dataBytes.length),
            (byte) MokoConstants.Setting,
            (byte) order.getOrderHeader(),
            (byte) dataBytes.length,
        };
        byte[] array2 = DigitalConver.mergeBitArrays(array1, dataBytes);
        byte[] array3 = new byte[]{
            (byte) 0xFF,
            (byte) 0xFF,
        };
        orderData = DigitalConver.mergeBitArrays(array2, array3);
        //[255, 12, 0, 5, 7, 0, 60, 1, 224, 5, 100, 0, 255, 255]
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[1])) {
            return;
        }
        LogModule.i(order.getOrderName() + "成功");
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
