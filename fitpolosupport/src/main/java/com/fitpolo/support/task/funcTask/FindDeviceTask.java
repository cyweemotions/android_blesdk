package com.fitpolo.support.task.funcTask;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.FindDevice;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.DigitalConver;

import java.util.Arrays;

/**
 * 查找设备
 */
public class FindDeviceTask extends OrderTask {

    private static final int ORDERDATA_LENGTH = 8;

    private byte[] orderData;

    public FindDeviceTask(MokoOrderTaskCallback callback, FindDevice findDevice) {
        super(OrderType.WRITE, OrderEnum.findDevice, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[]{
            (byte) MokoConstants.HEADER_READ_SEND,
            (byte) 0x06,
            (byte) MokoConstants.Function,
            (byte) order.getOrderHeader(),
            (byte) 0x01,
            (byte) findDevice.action,
            (byte) 0xFF,
            (byte) 0xFF
        };
//        [255, 06, 02, 04, 01, 01, 255, 255]
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        LogModule.i(order.getOrderName() + "成功");
        LogModule.i(Arrays.toString(value));
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[3])) return;
        if (MokoConstants.Function != DigitalConver.byte2Int(value[2])) return;
        int dataLength = (value[4] & 0xFF);
        int result = (value[5] & 0xFF); //设置结果
        byte[] subArray = Arrays.copyOfRange(value, 5, dataLength + 5);
        LogModule.i(Arrays.toString(subArray));

        response.responseObject =  result == 0 ? 0 : 1;
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
