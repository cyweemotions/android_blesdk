package com.fitpolo.support.task.setTask;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.entity.UserInfo;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.DigitalConver;

/**
 * 目标设置
 */
public class TargetTask extends OrderTask {
    private byte[] orderData;
    public TargetTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.setTarget, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[]{
            (byte) MokoConstants.HEADER_READ_SEND,
            (byte) 0x09,
            (byte) MokoConstants.Setting,
            (byte) order.getOrderHeader(),
            (byte) 0x04,
            (byte) 0x08,
            (byte) 0x03,
            (byte) 0x06,
            (byte) 0x04,
            (byte) 0xFF,
            (byte) 0xFF,
        };
        //FF 09 00 02 04 03 03 06 04 FF FF
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
