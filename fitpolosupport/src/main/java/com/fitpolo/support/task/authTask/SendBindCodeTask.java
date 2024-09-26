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
 * 3、发送绑定确认
 */
public class SendBindCodeTask extends OrderTask {
    private byte[] orderData;
    public SendBindCodeTask(MokoOrderTaskCallback callback, int sendType, int type, List<Byte> data) {
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
        if (MokoConstants.DeviceAuth != DigitalConver.byte2Int(value[2])) {
            return;
        }
        int dataLength = (value[4] & 0xFF) + 5;
        byte[] resultData = Arrays.copyOfRange(value, 5, dataLength);
        if(resultData.length != 3) {
            LogModule.i("鉴权返回数据格式错误"+resultData);
            return;
        }
        int resType = resultData[0];
        int resResult = resultData[1];
        int reason = resultData[2];
        LogModule.i("鉴权返回数据正确resType======"+resType);
        LogModule.i("鉴权返回数据正确resResult===="+resResult);
        LogModule.i("鉴权返回数据正确reason======="+reason);
        if((resType == 0) && (resResult == 2) && (reason == 2)) {
            LogModule.i("设备绑定鉴权成功");
            orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
            response.responseObject = 1;
        } else {
            LogModule.i("设备绑定鉴权失败");
            response.responseObject = 0;
        }
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
