package com.fitpolo.support.task.funcTask;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.entity.funcEntity.MotionControl;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.DigitalConver;

import java.util.Arrays;

/**
 * 定位GPS
 */
public class MotionControlTask extends OrderTask {

    private static final int ORDERDATA_LENGTH = 6;

    private byte[] orderData;

    public MotionControlTask(MokoOrderTaskCallback callback, MotionControl motionControl) {
        super(OrderType.WRITE, OrderEnum.motionControl, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[]{
                (byte) MokoConstants.HEADER_READ_SEND,
                (byte) 0x07,
                (byte) 0x02,
                (byte) order.getOrderHeader(),
                (byte) 0x02,
                (byte) motionControl.type,
                (byte) motionControl.action, // 05
                (byte) 0xFF,
                (byte) 0xFF,
        };
//      FF 07 02 0c 02 01 01 FF FF
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
