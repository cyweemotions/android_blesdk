package com.fitpolo.support.task.funcTask;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.BandAlarm;
import com.fitpolo.support.entity.FindDevice;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.DigitalConver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/**
 * 查找设备
 */
public class FindDeviceTask extends OrderTask {

    private static final int ORDERDATA_LENGTH = 8;

    private byte[] orderData;

    public FindDeviceTask(MokoOrderTaskCallback callback, FindDevice findDevice) {
        super(OrderType.WRITE, OrderEnum.findDevice, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) MokoConstants.HEADER_READ_SEND;
        orderData[1] = (byte) 0x06;
        orderData[2] = (byte) 0x02;
        orderData[3] = (byte) order.getOrderHeader();
        orderData[4] = (byte) 0x01;
        orderData[5] = (byte) findDevice.action;
        orderData[6] = (byte) 0xFF;
        orderData[7] = (byte) 0xFF;
//        [255, 11, 4, 1, 6, 0, 2, 0, 0, 0, 0, 255, 255]
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        LogModule.i(order.getOrderName() + "成功");
        LogModule.i(Arrays.toString(value));
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[3])) {
            return;
        }

        int group = DigitalConver.byte2Int(value[5]);
        if (group != 0)
            return;
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;

        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
