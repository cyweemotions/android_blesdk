package com.fitpolo.support.task.setTask;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.ByteType;
import com.fitpolo.support.utils.DigitalConver;

import java.util.ArrayList;
import java.util.List;

public class SleepTask extends OrderTask {
    private byte[] orderData;
    public SleepTask(MokoOrderTaskCallback callback, int startTime, int endTime) {
        super(OrderType.WRITE, OrderEnum.setSleep, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        List<Byte> startHexList = DigitalConver.convert(startTime * 60, ByteType.WORD);
        List<Byte> endHexList = DigitalConver.convert(endTime * 60, ByteType.WORD);
        List<Byte> byteList = new ArrayList<>();
        byteList.add((byte) 0x00);//睡眠监控开关：默认是开的，不能关闭
        byteList.addAll(startHexList);
        byteList.addAll(endHexList);
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
        //[255, 0a, 00, 04, 05, 00, 01, 224, 05, 100, 255, 255]
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        LogModule.i(order.getOrderName() + "成功");
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
