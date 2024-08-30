package com.fitpolo.support.task.getTask;

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

public class GetTargetTask extends OrderTask {
    private byte[] orderData;
    public GetTargetTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.setTarget, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        List<Byte> byteList = new ArrayList<>();
        byteList.add((byte) MokoConstants.HEADER_READ_SEND);
        byteList.add((byte) 4);
        byteList.add((byte) MokoConstants.GetSetting);
        byteList.add((byte) order.getOrderHeader());
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
        LogModule.i("返回的"+ order.getOrderName() + Arrays.toString(value));
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[3])) return;
        if (MokoConstants.GetSetting != DigitalConver.byte2Int(value[2])) return;
        int dataLength = (value[4] & 0xFF);
        byte[] subArray = Arrays.copyOfRange(value, 5, dataLength + 5);

        List<Integer> result = new ArrayList<>();
        LogModule.i("获取目标" + subArray);
        int stepValue = DigitalConver.byte2Int(subArray[0]);
        int distanceValue = DigitalConver.byte2Int(subArray[1]);
        int calorieValue = DigitalConver.byte2Int(subArray[2]);
        int sportValue = DigitalConver.byte2Int(subArray[3]);
        LogModule.i("步数" + stepValue);
        LogModule.i("距离" + distanceValue);
        LogModule.i("卡路里" + calorieValue);
        LogModule.i("运动时长" + sportValue);
        result.add(stepValue);
        result.add(distanceValue);
        result.add(calorieValue);
        result.add(sportValue);
        response.responseObject =  result;
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;

        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
