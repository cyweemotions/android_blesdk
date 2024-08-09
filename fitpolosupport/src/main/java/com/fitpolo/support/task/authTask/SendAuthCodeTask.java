package com.fitpolo.support.task.authTask;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.DigitalConver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 4、发送鉴权
 */
public class SendAuthCodeTask extends OrderTask {
    private byte[] orderData;
    public SendAuthCodeTask(MokoOrderTaskCallback callback, int sendType, int type, List<Byte> data) {
        super(OrderType.WRITE, OrderEnum.bindAuth, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        List<Byte> dataList = new ArrayList<>();
        for (Byte b : data) {
            dataList.add(b);
        }
        int dataLength = dataList.size();

        List<Byte> byteList = new ArrayList<>();
        byteList.add((byte) MokoConstants.HEADER_READ_SEND);
        byteList.add((byte) (6 + dataLength));
        byteList.add((byte) MokoConstants.DeviceAuth);
        byteList.add((byte) sendType);
        byteList.add((byte) (1 + dataLength));
        byteList.add((byte) type);
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
        LogModule.i(order.getOrderName() + "成功");
        if (MokoConstants.DeviceAuth != DigitalConver.byte2Int(value[2])) {
            return;
        }
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}