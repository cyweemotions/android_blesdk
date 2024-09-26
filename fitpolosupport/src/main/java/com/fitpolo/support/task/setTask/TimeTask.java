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
    public TimeTask(MokoOrderTaskCallback callback, int timeFormat, int dateFormat, int timeZone) {
        super(OrderType.WRITE, OrderEnum.setTimeFormat, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[]{
                (byte) MokoConstants.HEADER_READ_SEND,
                (byte) 0x08, // 5+data.length
                (byte) MokoConstants.Setting,
                (byte) order.getOrderHeader(),
                (byte) 0x03,
                (byte) timeFormat,
                (byte) dateFormat,
                (byte) timeZone,
                (byte) 0xFF,
                (byte) 0xFF,
        };
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        LogModule.i(order.getOrderName() + "成功" );
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[3])) return;
        if (MokoConstants.Setting != DigitalConver.byte2Int(value[2])) return;
        int result = (value[5] & 0xFF);
        LogModule.i(order.getOrderName()+ "成功：" + result);

        response.responseObject = result == 0 ? 0 : 1; // 0-成功 1-失败
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }

}
