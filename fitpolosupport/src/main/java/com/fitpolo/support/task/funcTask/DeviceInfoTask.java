package com.fitpolo.support.task.funcTask;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
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
public class DeviceInfoTask extends OrderTask {
    private byte[] orderData;

    public DeviceInfoTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.deviceInfo, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[]{
            (byte) MokoConstants.HEADER_READ_SEND,
            (byte) 0x04,
            (byte) MokoConstants.Function,
            (byte) order.getOrderHeader(),
            (byte) 0xFF,
            (byte) 0xFF
        };
//      FF 4 2 8 FF FF
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[3])) return;
        if (MokoConstants.Function != DigitalConver.byte2Int(value[2])) return;
        int dataLength = (value[4] & 0xFF);
        byte[] subArray = Arrays.copyOfRange(value, 5, dataLength + 5);

        String hexString = DigitalConver.bytesToHex(subArray);
        String result = DigitalConver.hex2String(hexString);
        LogModule.i("获取设备信息");
        LogModule.i(result);
// 创建 Gson 实例
        Gson gson = new Gson();

        // 定义 Map 类型
        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();

        // 将 JSON 字符串转换为 Map
        Map<String, Object> map = gson.fromJson(result, mapType);

        response.responseObject = map;
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;

        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
