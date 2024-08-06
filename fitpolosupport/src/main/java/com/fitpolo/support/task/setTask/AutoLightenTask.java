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

/**
 * 抬手亮屏
 */
public class AutoLightenTask extends OrderTask {
    // 自动亮屏幕

    private byte[] orderData;

    public AutoLightenTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.setAutoLigten, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        List<Byte> byteList = new ArrayList<>();
        byteList.add((byte) 0x00);// 0 开 1 关
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
