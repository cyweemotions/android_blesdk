package com.fitpolo.support.task.setTask;

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
    private byte[] orderData;
    SitAlert sitAlert;

    public SitLongTimeAlertTask(MokoOrderTaskCallback callback, SitAlert sitAlert) {
        super(OrderType.WRITE, OrderEnum.setSitLongTimeAlert, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        List<Byte> byteList = new ArrayList<>();

        byte onOff = sitAlert.interval == 0 ? (byte)0x00 : (byte)0x01;//开关 00 01
        byteList.add(onOff);

        byte[] timeU8 = DigitalConver.hex2bytes(Integer.toHexString(sitAlert.interval));
        for (byte b : timeU8) { byteList.add(b); } // 久坐提醒时间间隔，单位分钟

        List<Byte> startU8 = DigitalConver.convert(sitAlert.startTime*60,ByteType.WORD);
        byteList.addAll(startU8); // 开始时间

        List<Byte> endU8 = DigitalConver.convert(sitAlert.endTime*60,ByteType.WORD);
        byteList.addAll(endU8); // 结束时间
        // 0x01 --  周日
        // 0x02 --  周一
        // 0x04 --  周二
        // 0x08 --  周三
        // 0x10 --  周四
        // 0x20 --  周五
        // 0x40 --  周六
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
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[3])) {
            return;
        }
        if(DigitalConver.byte2Int(value[4]) == 0x01) {
            LogModule.i(order.getOrderName() + "成功");
            orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        } else {
            LogModule.i(order.getOrderName() + "失败");
        }
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
