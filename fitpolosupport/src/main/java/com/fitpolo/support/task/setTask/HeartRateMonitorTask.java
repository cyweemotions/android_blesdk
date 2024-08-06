package com.fitpolo.support.task.setTask;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.entity.setEntity.HeartRateMonitor;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.DigitalConver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 设置心率监测
 */
public class HeartRateMonitorTask extends OrderTask {

    private byte[] orderData;

    public HeartRateMonitorTask(MokoOrderTaskCallback callback, HeartRateMonitor heartRateMonitor) {
        super(OrderType.WRITE, OrderEnum.setHeartRateMonitor, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        if(heartRateMonitor.interval < 0 || heartRateMonitor.interval > 60) {
            LogModule.e("心率监控间隔值错误");
        }
        if(heartRateMonitor.minLimit < 0 || heartRateMonitor.minLimit > 100) {
            LogModule.e("低心率限制值错误");
        }
        if(heartRateMonitor.maxLimit < 100 || heartRateMonitor.maxLimit > 250) {
            LogModule.e("高心率限制值错误");
        }
        List<Byte> byteList = new ArrayList<>(Arrays.asList(
            (byte) (heartRateMonitor.monitorSwitch & 0xFF),
            (byte) (heartRateMonitor.interval & 0xFF),
            (byte) (heartRateMonitor.alarmSwitch & 0xFF),
            (byte) (heartRateMonitor.minLimit & 0xFF),
            (byte) (heartRateMonitor.maxLimit & 0xFF)
        ));

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
