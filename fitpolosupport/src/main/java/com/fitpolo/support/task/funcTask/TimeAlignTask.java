package com.fitpolo.support.task.funcTask;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.DigitalConver;

import java.util.Arrays;

/**
 * 同步时间
 */
public class TimeAlignTask extends OrderTask {

    private static final int ORDERDATA_LENGTH = 14;

    private byte[] orderData;

    public TimeAlignTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.syncTime, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[]{
            (byte) MokoConstants.HEADER_READ_SEND,
            (byte) 0x0C,
            (byte) MokoConstants.Function,
            (byte) order.getOrderHeader(),
            (byte) 0x07,
            (byte) 0x18,
            (byte) 0x07,
            (byte) 0x1F,
            (byte) 0x12,
            (byte) 0x06,
            (byte) 0x05,
            (byte) 0x03,
            (byte) 0xFF,
            (byte) 0xFF,
        };
//        [255, 12, 2, 1, 7, 24, 7, 31, 18, 6, 5, 3, 255, 255]
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
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
