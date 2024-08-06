package com.fitpolo.support.task.setTask;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.DigitalConver;

/**
 * 时间格式设置
 */
public class TimeTask extends OrderTask {
    private byte[] orderData;
    public TimeTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.setTimeFormat, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[]{
                (byte) MokoConstants.HEADER_READ_SEND,
                (byte) 0x08, // 5+data.length
                (byte) MokoConstants.Setting,
                (byte) order.getOrderHeader(),
                (byte) 0x03,
                (byte) 0x01,
                (byte) 0x02,
                (byte) 0x08,
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
