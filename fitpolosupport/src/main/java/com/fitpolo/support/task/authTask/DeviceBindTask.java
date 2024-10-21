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
 * 2、去绑定
 */
public class DeviceBindTask extends OrderTask {
    private byte[] orderData;
    public DeviceBindTask (MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.bindAuth, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        List<Byte> dataList = new ArrayList<>();
        dataList.add((byte) 1);
        dataList.add((byte) 0);
        dataList.add((byte) 0);
        dataList.add((byte) 0);
        dataList.add((byte) 1);
        int dataLength = dataList.size();

        List<Byte> byteList = new ArrayList<>();
        byteList.add((byte) MokoConstants.HEADER_READ_SEND);
        byteList.add((byte) (6 + dataLength));
        byteList.add((byte) MokoConstants.DeviceAuth);
        byteList.add((byte) 1);
        byteList.add((byte) (1 + dataLength));
        byteList.add((byte) 0);
        byteList.addAll(dataList);
        byteList.add((byte) 0xFF);
        byteList.add((byte) 0xFF);

        byte[] dataBytes = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            dataBytes[i] = byteList.get(i);
        }


        StringBuilder hexBuilder = new StringBuilder();
        for (byte b : dataBytes) {
            hexBuilder.append(String.format("%02X ", b));
        }
        LogModule.i("鉴权返回数据格式错误"+hexBuilder.toString().trim());

        orderData = dataBytes;
    }
    @Override
    public byte[] assemble() {
        return orderData;
    }
    @Override
    public void parseValue(byte[] value) {
        StringBuilder hexBuilder = new StringBuilder();
        for (byte b : value) {
            hexBuilder.append(String.format("%02X ", b));
        }
        LogModule.i("鉴权数据第一步"+hexBuilder.toString().trim());
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
        int result = resultData[1];
        response.responseObject = result;
        if(resType == 0 && result == 1) {
            LogModule.i("请在设备端点击确认");
        } else if(resType == 0 && result == 2) {
//            LogModule.i("设备已被绑定,请在手表端选择“恢复出厂”后重试");
        } else if(resType == 0 && result == 3) {
            LogModule.i("设备绑定中。。。");
            orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
            MokoSupport.getInstance().pollTask();
//            callback.onOrderResult(response);
            MokoSupport.getInstance().executeTask(callback);
            List<Byte> dataList = new ArrayList<>();
            dataList.add((byte) 2);
            dataList.add((byte) 0);
            dataList.add((byte) 0);
            dataList.add((byte) 0);
            dataList.add((byte) 0);
            SendBindCodeTask sendBindCodeTask = new SendBindCodeTask(callback, 1,0, dataList);
            MokoSupport.getInstance().sendOrder(sendBindCodeTask);
        } else {
            LogModule.i("错误");
        }
    }
}
