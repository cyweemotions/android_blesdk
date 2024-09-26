package com.fitpolo.support.task.funcTask;

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
 * 定位GPS
 */
public class PositionGPSTask extends OrderTask {

    private static final int ORDERDATA_LENGTH = 6;

    private byte[] orderData;

    public PositionGPSTask(MokoOrderTaskCallback callback,int state, int lat, int lng) {
        super(OrderType.WRITE, OrderEnum.positionGPS, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        List<Byte> dataList = new ArrayList<>();
        // 转换为十六进制字符串
        String latHex = Long.toHexString((lat * 1000000L) & 0xFFFFFFFFL).toUpperCase();
        String lngHex = Long.toHexString((lng * 1000000L) & 0xFFFFFFFFL).toUpperCase();

        List<Byte> latU8 = DigitalConver.bytes2ListByte(DigitalConver.hex2bytes(latHex));
        List<Byte> lngU8 = DigitalConver.bytes2ListByte(DigitalConver.hex2bytes(lngHex));
        dataList.add((byte) (state & 0xFF));
        dataList.addAll(latU8);
        dataList.addAll(lngU8);
        int dataLength = dataList.size();

        List<Byte> byteList = new ArrayList<>();
        byteList.add((byte) MokoConstants.HEADER_READ_SEND);
        byteList.add((byte) (5 + dataLength));
        byteList.add((byte) MokoConstants.Function);
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
//     [255, 14, 2, 11, 9, 0, 6, 202, 112, 170, 1, 87, 212, 200, 255, 255]
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
        int result = (value[5] & 0xFF);
        LogModule.i("设置用户信息：" + result);

        response.responseObject = result == 0 ? 0 : 1; // 0-成功 1-失败
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
