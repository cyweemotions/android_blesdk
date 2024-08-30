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
 * 1、查询鉴权状态
 */
public class QueryAuthStateTask extends OrderTask {
    private byte[] orderData;
    public QueryAuthStateTask (MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.queryAuthState, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        List<Byte> dataList = new ArrayList<>();
        dataList.add((byte) 0);

        int dataLength = dataList.size();

        List<Byte> byteList = new ArrayList<>();
        byteList.add((byte) MokoConstants.HEADER_READ_SEND);
        byteList.add((byte) (5 + dataLength));
        byteList.add((byte) MokoConstants.DeviceAuth);
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
        if (MokoConstants.DeviceAuth != DigitalConver.byte2Int(value[2])) {
            return;
        }
        int dataLength = (value[4] & 0xFF) + 5;
        byte[] subArray = Arrays.copyOfRange(value, 5, dataLength);
        if((subArray[1] & 0xFF) == 1) { // 1
            LogModule.i("设备已被绑定,请在手表端选择“恢复出厂”后重试"+(subArray[1] & 0xFF) );
        } else { // 0
            LogModule.i("未绑定"+(subArray[1] & 0xFF) );
        }
        response.responseObject = (subArray[1] & 0xFF);
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
