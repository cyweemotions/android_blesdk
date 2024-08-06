package com.fitpolo.support.task;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.setEntity.AlarmClock;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.utils.DigitalConver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 读取闹钟
 * @ClassPath com.fitpolo.support.task.ReadAlarmsTask
 */
public class ReadAlarmsTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 2;

    private byte[] orderData;
    private ArrayList<AlarmClock> alarms;

    public ReadAlarmsTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.READ_ALARMS, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) MokoConstants.HEADER_READ_SEND;
        orderData[1] = (byte) order.getOrderHeader();
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
    }
}
