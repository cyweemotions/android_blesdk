package com.fitpolo.support.task.setTask;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.DigitalConver;

import java.util.ArrayList;
import java.util.List;
// 语种
// 0x00 -English
// 0x01 -Chinese SM
// 0x02 -Chinese TR
// 0x03 -Spanish
// 0x04 -Portuguese
// 0x05 -French
// 0x06 -Germen
// 0x07 -Italian
// 0x08 -Polski
// 0x09 -Russian
// 0x0a -Indonesia
// 0x0b -Thai
// 0x0c -Hebrew
// 0x0d -Arabic
// 0x0e -Japanese
// 0x0f -Korean
// 0x10 -Turkish
// 0x11 -Myanmar
// 0x12 -Vietnamese
// 0x13 -Persian
// 0x14 -Czech
// 0x15 -Greek
// 0x16 -Dutch
// 0x17 -Filipino
// 0x18 -Hindi
// 0x19 -Malay
public class LanguageTask extends OrderTask {
    private byte[] orderData;
    public LanguageTask(MokoOrderTaskCallback callback, int language) {
        super(OrderType.WRITE, OrderEnum.setLanguage, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        List<Byte> dataList = new ArrayList<>();
        dataList.add((byte) (language & 0xFF));// 语言 0-25
        int dataLength = dataList.size();

        List<Byte> byteList = new ArrayList<>();
        byteList.add((byte) MokoConstants.HEADER_READ_SEND);
        byteList.add((byte) (5 + dataLength));
        byteList.add((byte) MokoConstants.Setting);
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
        LogModule.i(order.getOrderName() + "成功" );
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[3])) return;
        if (MokoConstants.Setting != DigitalConver.byte2Int(value[2])) return;
        int result = (value[5] & 0xFF);
        LogModule.i(order.getOrderName()+ "成功：" + result);

        response.responseObject = result == 0 ? 0 : 1; // 0-成功 1-失败
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
