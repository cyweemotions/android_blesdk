package com.fitpolo.support.task.funcTask;

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
import java.util.Arrays;
import java.util.List;

/**
 * 语言支持
 */
public class LanguageSupportTask extends OrderTask {
    private byte[] orderData;

    public LanguageSupportTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.languageSupport, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[]{
            (byte) MokoConstants.HEADER_READ_SEND,
            (byte) 0x04,
            (byte) MokoConstants.Function,
            (byte) order.getOrderHeader(),
            (byte) 0xFF,
            (byte) 0xFF
        };
//      FF 4 2 7 FF FF
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        LogModule.i(order.getOrderName() + "成功" );
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[3])) return;
        if (MokoConstants.Function != DigitalConver.byte2Int(value[2])) return;
        int dataLength = (value[4] & 0xFF);
        byte[] subArray = Arrays.copyOfRange(value, 5, dataLength + 5);

//        int batteryQuantity = DigitalConver.byte2Int(subArray[0]);
//        MokoSupport.getInstance().setBatteryQuantity(batteryQuantity);
        LogModule.i("语言支持：" + Arrays.toString(subArray));

        response.responseObject =  bytes2Intlist(subArray);
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;

        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }

    private List<Integer> bytes2Intlist (byte[] byteArray) {
        List<Integer> binaryList = new ArrayList<>(); // 创建一个 List 来存储整数

        // 遍历 byte 数组并转换每个 byte 为二进制字符串
        for (byte number : byteArray) {
            String binaryString = String.format("%8s", Integer.toBinaryString(number & 0xFF)).replace(' ', '0');

            // 将每个字符转换为整数并添加到 List 中
            for (char c : binaryString.toCharArray()) {
                binaryList.add(Character.getNumericValue(c)); // 将字符转换为数字
            }
        }
        return binaryList;
    }
}
