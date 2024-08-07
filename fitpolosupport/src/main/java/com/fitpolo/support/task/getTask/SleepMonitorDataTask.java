package com.fitpolo.support.task.getTask;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.DigitalConver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 获取睡眠监测算法设置
 */
public class SleepMonitorDataTask extends OrderTask {
    private byte[] orderData;
    public SleepMonitorDataTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.setSleepMonitor, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        List<Byte> byteList = new ArrayList<>();
        byteList.add((byte) MokoConstants.HEADER_READ_SEND);
        byteList.add((byte) 4);
        byteList.add((byte) MokoConstants.GetSetting);
        byteList.add((byte) order.getOrderHeader());
        byteList.add((byte) 0xFF);
        byteList.add((byte) 0xFF);

        byte[] dataBytes = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            dataBytes[i] = byteList.get(i);
        }
        orderData = dataBytes;
    }
    @Override
    public byte[] assemble() {
        return orderData;
    }
    @Override
    public void parseValue(byte[] value) {
        LogModule.i("返回的"+ order.getOrderName() + Arrays.toString(value));
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
