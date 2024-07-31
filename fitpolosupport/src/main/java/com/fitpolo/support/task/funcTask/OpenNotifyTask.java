package com.fitpolo.support.task.funcTask;

import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.task.OrderTask;


public class OpenNotifyTask extends OrderTask {
    public byte[] data;

    public OpenNotifyTask(OrderType orderType, OrderEnum orderEnum, MokoOrderTaskCallback callback) {
        super(orderType, orderEnum, callback, OrderTask.RESPONSE_TYPE_NOTIFY);
        data = new byte[0];
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
