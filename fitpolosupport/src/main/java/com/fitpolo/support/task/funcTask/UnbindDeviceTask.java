package com.fitpolo.support.task.funcTask;


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
 * 解绑设备
 */
public class UnbindDeviceTask extends OrderTask {
    private byte[] orderData;

    public UnbindDeviceTask (MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.unbindDevice, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[]{
            (byte) MokoConstants.HEADER_READ_SEND,
            (byte) 0x06,
            (byte) MokoConstants.Function,
            (byte) order.getOrderHeader(),
            (byte) 0x01,
            (byte) 0x01,
            (byte) 0xFF,
            (byte) 0xFF,
        };
//         FF 6 2 6 1 1 ff ff
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        LogModule.i("返回的"+ order.getOrderName() + Arrays.toString(value));
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[3])) return;
        if (MokoConstants.Function != DigitalConver.byte2Int(value[2])) return;
        int result = (value[5] & 0xFF);

        response.responseObject =  result;
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;

        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
