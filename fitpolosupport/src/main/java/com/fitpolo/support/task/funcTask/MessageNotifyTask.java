package com.fitpolo.support.task.funcTask;

import android.os.Build;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.entity.funcEntity.MessageModel;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.ByteType;
import com.fitpolo.support.utils.DigitalConver;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * 消息通知
 */
public class MessageNotifyTask extends OrderTask {
    private byte[] orderData;
    public MessageNotifyTask(MokoOrderTaskCallback callback, MessageModel messageModel) {
        super(OrderType.WRITE, OrderEnum.messageNotify, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        List<Byte> dataList = new ArrayList<>();
        dataList.add((byte) (messageModel.appType & 0xFF));
        int mtu = 220;
        dataList.add((byte) (messageModel.year & 0xFF));
        dataList.add((byte) (messageModel.month & 0xFF));
        dataList.add((byte) (messageModel.day & 0xFF));
        dataList.add((byte) (messageModel.hour & 0xFF));
        dataList.add((byte) (messageModel.minute & 0xFF));
        dataList.add((byte) (messageModel.second & 0xFF));
        // 标题
        byte[] titleBytes = messageModel.title.getBytes();
        List<Byte> titleByteList;
        if(titleBytes.length > 20) {
            titleByteList = DigitalConver.bytes2ListByte(Arrays.copyOfRange(titleBytes, 0,20));
        } else {
            titleByteList = DigitalConver.bytes2ListByte(titleBytes);
        }
        dataList.add((byte) titleByteList.size());
        dataList.addAll(titleByteList);
        // 内容
        byte[] contentBytes = messageModel.content.getBytes();
        List<Byte> contentByteList;
        int notifyLength = mtu - 30;
        if(contentBytes.length >= notifyLength) {
            contentByteList = DigitalConver.bytes2ListByte(Arrays.copyOfRange(contentBytes, 0,notifyLength));
        } else {
            contentByteList = DigitalConver.bytes2ListByte(contentBytes);
        }
        dataList.add((byte) contentByteList.size());
        dataList.addAll(contentByteList);
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
//      [255, 26, 2, 10, 21, 0, 24, 8, 1, 16, 57, 43, 6, 230, 160, 135, 233, 162, 152, 6, 229, 134, 133, 229, 174, 185, 255, 255]
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        LogModule.i("返回的"+ order.getOrderName() + Arrays.toString(value));
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[3])) return;
        if (MokoConstants.Function != DigitalConver.byte2Int(value[2])) return;
        int result = (value[5] & 0xFF);
        LogModule.i("返回的"+ result);

        response.responseObject =  result;
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;

        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
