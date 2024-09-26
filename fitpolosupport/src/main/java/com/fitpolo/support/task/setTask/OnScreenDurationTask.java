package com.fitpolo.support.task.setTask;

import androidx.annotation.NonNull;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.entity.setEntity.NotifyType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.DigitalConver;

import java.util.ArrayList;
import java.util.List;

public class OnScreenDurationTask extends OrderTask {
    private byte[] orderData;
    public OnScreenDurationTask (MokoOrderTaskCallback callback, int duration) {
        super(OrderType.WRITE, OrderEnum.setOnScreenDuration, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);

        List<Byte> dataList = new ArrayList<>();
        dataList.add((byte) duration);// 亮屏时长
        int dataLength = dataList.size();

        List<Byte> byteList = new ArrayList<>();
        byteList.add((byte) MokoConstants.HEADER_READ_SEND);
        byteList.add((byte) (5 + dataLength));
        byteList.add((byte) MokoConstants.Setting);
        byteList.add((byte) order.getOrderHeader());
        byteList.add((byte) dataLength);
        byteList.addAll(dataList);
        byteList.add((byte) 0xFF);
        byteList.add((byte) 0xFF);

        byte[] dataBytes = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            dataBytes[i] = byteList.get(i);
        }
        orderData = dataBytes;
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
