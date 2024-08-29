package com.fitpolo.support.task.getTask;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.entity.SitAlert;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.ByteType;
import com.fitpolo.support.utils.DigitalConver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GetSitAlertSettingTask extends OrderTask {
    private byte[] orderData;
    SitAlert sitAlert;

    public GetSitAlertSettingTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.setSitLongTimeAlert, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
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
        int sitAlertSwitch = Integer.parseInt(Integer.toHexString(subArray[0]), 16);
        int timeValue = Integer.parseInt(Integer.toHexString(subArray[1]), 16);
        byte[] start = new byte[]{subArray[2], subArray[3]};
        byte[] end = new byte[]{subArray[4], subArray[5]};
        int startTime = Integer.parseInt(DigitalConver.bytesToHex(start), 16);
        int endTime = Integer.parseInt(DigitalConver.bytesToHex(end), 16);
        LogModule.i("开关" + sitAlertSwitch);
        LogModule.i("间隔" + timeValue);
        LogModule.i("开始时间" + startTime);
        LogModule.i("结束时间" + endTime);
        result.add(sitAlertSwitch);
        result.add(timeValue);
        result.add(startTime);
        result.add(endTime);
        response.responseObject =  result;
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;

        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
