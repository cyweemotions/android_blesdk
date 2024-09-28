package com.fitpolo.support.task.dataPushTask;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.DigitalConver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class SyncWeatherTask extends OrderTask {
    private byte[] orderData;
    public SyncWeatherTask(MokoOrderTaskCallback callback, String weather) {
        super(OrderType.DataPushWRITE, OrderEnum.syncWeather, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
////        String weather = "4:ShenZhen:20240809,13,26,Clear|20240810,15,28,Clouds|20240811,17,30,Clear|20240812,17,30,Clear|";


//        Calendar calendar = Calendar.getInstance();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//        String oneDay = sdf.format(calendar.getTime());
//        calendar.add(Calendar.DAY_OF_MONTH, 1);
//        String twoDay = sdf.format(calendar.getTime());
//        calendar.add(Calendar.DAY_OF_MONTH, 2);
//        String threeDay = sdf.format(calendar.getTime());
//        calendar.add(Calendar.DAY_OF_MONTH, 3);
//        String fourDay = sdf.format(calendar.getTime());

//        String weather = "4:深圳:"+oneDay+",26222,33571,802|"+twoDay+",30184,30468,500|"+threeDay+",30191,30503,500|"+fourDay+",30235,30537,500|";
//        String weather = "4:深圳:20240809,30222,30571,802|20240810,30184,30468,500|20240811,30191,30503,500|20240812,30235,30537,500|";

        String weatherBytesStr = DigitalConver.string2Hex(weather);
        byte[] weatherBytes = DigitalConver.hex2bytes(weatherBytesStr);
        List<Byte> dataList = DigitalConver.bytes2ListByte(weatherBytes);
        int dataLength = dataList.size();

        List<Byte> byteList = new ArrayList<>();
        byteList.add((byte) MokoConstants.HEADER_READ_SEND);
        byteList.add((byte) (5 + dataLength));
        byteList.add((byte) MokoConstants.DataNotify);
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
        LogModule.i("返回的"+ order.getOrderName() + Arrays.toString(value));
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[3])) return;
        if (MokoConstants.DataNotify != DigitalConver.byte2Int(value[2])) return;
        int result = (value[5] & 0xFF);
        LogModule.i(order.getOrderName()+ "成功：" + result);

        response.responseObject = result == 0 ? 0 : 1; // 0-成功 1-失败
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;

        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
