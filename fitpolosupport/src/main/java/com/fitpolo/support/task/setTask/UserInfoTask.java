package com.fitpolo.support.task.setTask;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.entity.UserInfo;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.DigitalConver;

import java.util.Arrays;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置个人信息
 * @ClassPath com.fitpolo.support.task.UserInfoTask
 */
public class UserInfoTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 6;
    private byte[] orderData;

    public UserInfoTask(MokoOrderTaskCallback callback, UserInfo userInfo) {
        super(OrderType.WRITE, OrderEnum.setUserInfo, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        String userInfoStr = userInfo.toJson();
        LogModule.i("userinfo数据"+userInfoStr);
        String hexString = DigitalConver.string2Hex(userInfoStr);
        byte[] dataBytes = DigitalConver.hex2bytes(hexString);
//        LogModule.i(hexString);
//        LogModule.i(Arrays.toString(dataBytes));
        byte[] array1 = new byte[]{
            (byte) MokoConstants.HEADER_READ_SEND,
            (byte) (5 + dataBytes.length),
            (byte) MokoConstants.Setting,
            (byte) order.getOrderHeader(),
            (byte) dataBytes.length,
        };
        byte[] array2 = DigitalConver.mergeBitArrays(array1, dataBytes);
        byte[] array3 = new byte[]{
            (byte) 0xFF,
            (byte) 0xFF,
        };
        orderData = DigitalConver.mergeBitArrays(array2, array3);
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[1])) {
            return;
        }
        LogModule.i(order.getOrderName() + "成功");
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
