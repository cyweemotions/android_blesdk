package com.fitpolo.support.task.getTask;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.entity.UserInfo;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.DigitalConver;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

/**
 * 设备信息
 */
public class GetUserInfoTask extends OrderTask {
    private byte[] orderData;

    public GetUserInfoTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.setUserInfo, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[]{
            (byte) MokoConstants.HEADER_READ_SEND,
            (byte) 0x04,
            (byte) MokoConstants.GetSetting,
            (byte) order.getOrderHeader(),
            (byte) 0xFF,
            (byte) 0xFF
        };
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[3])) return;
        if (MokoConstants.GetSetting != DigitalConver.byte2Int(value[2])) return;
        int dataLength = (value[4] & 0xFF);
        byte[] subArray = Arrays.copyOfRange(value, 5, dataLength + 5);

        String hexString = DigitalConver.bytesToHex(subArray);
        String result = DigitalConver.hex2String(hexString);
//        LogModule.i("获取用户信息");
        LogModule.i(result);
        Gson gson = new Gson();
        UserInfo userFromJson = gson.fromJson(result, UserInfo.class);
        System.out.println("UserInfo Object: " + userFromJson);
        response.responseObject = userFromJson;
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;

        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
