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
 * 查询绑定信息
 */
public class QueryInfoTask extends OrderTask {
    private byte[] orderData;

    public QueryInfoTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.queryInfo, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[]{
            (byte) MokoConstants.HEADER_READ_SEND,
            (byte) 0x06,
            (byte) MokoConstants.Function,
            (byte) order.getOrderHeader(),
            (byte) 0x01,
            (byte) 0x00,
            (byte) 0xFF,
            (byte) 0xFF,
        };
//        FF 06 02 FF 01 00 FF FF
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        LogModule.i(order.getOrderName() + "成功" );
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[3])) return;
        if (MokoConstants.Function != DigitalConver.byte2Int(value[2])) return;
        int dataLength = (value[4] & 0xFF);
        byte[] subArray = Arrays.copyOfRange(value, 5, dataLength + 5);

        int result = DigitalConver.byte2Int(subArray[1]);
        LogModule.i("查询绑定信息：" + result);

        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
