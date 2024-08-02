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
 * 定位GPS
 */
public class PositionGPSTask extends OrderTask {

    private static final int ORDERDATA_LENGTH = 6;

    private byte[] orderData;

    public PositionGPSTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.positionGPS, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[]{
                (byte) MokoConstants.HEADER_READ_SEND,
                (byte) 0x0e,
                (byte) MokoConstants.Function,
                (byte) order.getOrderHeader(),
                (byte) 0x09,
                (byte) 0x00,
                (byte) 0x06,
                (byte) 0xca,
                (byte) 0x70,
                (byte) 0xaa,
                (byte) 0x01,
                (byte) 0x57,
                (byte) 0xd4,
                (byte) 0xc8,
                (byte) 0xFF,
                (byte) 0xFF,
        };
//     [255, 14, 2, 11, 9, 0, 6, 202, 112, 170, 1, 87, 212, 200, 255, 255]
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
