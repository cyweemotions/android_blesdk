package com.fitpolo.support.task;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;

public class FindDevice extends OrderTask{

    private static final int ORDERDATA_LENGTH = 2;

    private byte[] orderData;


    public FindDevice(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.getFirmwareVersion, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) MokoConstants.HEADER_READ_SEND;
        orderData[1] = (byte) order.getOrderHeader();
    }


    @Override
    public byte[] assemble() {
        return orderData;
    }
}
