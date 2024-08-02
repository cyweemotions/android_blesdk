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
 * 消息通知
 */
public class MessageNotifyTask extends OrderTask {

    private static final int ORDERDATA_LENGTH = 28;

    private byte[] orderData;

    public MessageNotifyTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.messageNotify, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[]{
            (byte) MokoConstants.HEADER_READ_SEND,
            (byte) 0x1a,
            (byte) 0x02,
            (byte) order.getOrderHeader(),
            (byte) 0x15,
            (byte) 0x00,
            (byte) 0x18,
            (byte) 0x08,
            (byte) 0x01,
            (byte) 0x10,
            (byte) 0x39,
            (byte) 0x2b,
            (byte) 0x06,
            (byte) 0xe6,
            (byte) 0xa0,
            (byte) 0x87,
            (byte) 0xe9,
            (byte) 0xa2,
            (byte) 0x98,
            (byte) 0x06,
            (byte) 0xe5,
            (byte) 0x86,
            (byte) 0x85,
            (byte) 0xe5,
            (byte) 0xae,
            (byte) 0xb9,
            (byte) 0xFF,
            (byte) 0xFF,
        };
//      [255, 26, 2, 10, 21, 0, 24, 8, 1, 16, 57, 43, 6, 230, 160, 135, 233, 162, 152, 6, 229, 134, 133, 229, 174, 185, 255, 255]
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
