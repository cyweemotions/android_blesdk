package com.fitpolo.support.task.setTask;

import androidx.annotation.NonNull;

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
import java.util.List;

/**
 * 通知设置
 */
public class NotifyTask extends OrderTask {
    private byte[] orderData;
    public NotifyTask (MokoOrderTaskCallback callback, NotifyType notifyType) {
        super(OrderType.WRITE, OrderEnum.setNotify, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        List<Byte> dataList = new ArrayList<>();
        dataList.add((byte) (notifyType.toggle & 0xFF));// 开关
        List<Byte> notifyTypeData = type2Bytes(notifyType); //通知APP
        dataList.addAll(notifyTypeData);
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
    private List<Byte> type2Bytes (NotifyType notifyType) {
        List<Byte> list = new ArrayList<>();
        List<Integer> typeList = getIntegerList(notifyType);
        byte byte1 = 0;
        byte byte2 = 0;
        for (int i = typeList.size()-1; i >= 0 ; i--) {
            int value = typeList.get(i);
            if (i < 8) {
                // 设置byte1的比特位
                byte1 = (byte) ((byte1 << 1) | value);
            } else {
                // 设置byte2的比特位
                byte2 = (byte) ((byte2 << 1) | value);
            }
        }
        list.add(byte2);
        list.add(byte1);
        System.out.println("Byte 2: " + String.format("%8s", Integer.toBinaryString(byte2 & 0xFF)).replace(' ', '0'));
        System.out.println("Byte 1: " + String.format("%8s", Integer.toBinaryString(byte1 & 0xFF)).replace(' ', '0'));
        return list;
    }
    @NonNull
    private static List<Integer> getIntegerList(NotifyType notifyType) {
        List<Integer> typeList = new ArrayList<>();
        typeList.add(notifyType.common);
        typeList.add(notifyType.facebook);
        typeList.add(notifyType.instagram);
        typeList.add(notifyType.kakaotalk);
        typeList.add(notifyType.line);
        typeList.add(notifyType.linkedin);
        typeList.add(notifyType.SMS);
        typeList.add(notifyType.QQ);
        typeList.add(notifyType.twitter);
        typeList.add(notifyType.viber);
        typeList.add(notifyType.vkontaket);
        typeList.add(notifyType.whatsapp);
        typeList.add(notifyType.wechat);
        typeList.add(notifyType.other1);
        typeList.add(notifyType.other2);
        typeList.add(notifyType.other3);
        return typeList;
    }
    @Override
    public byte[] assemble() {
        return orderData;
    }
    @Override
    public void parseValue(byte[] value) {
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[3])) {
            return;
        }
        if(DigitalConver.byte2Int(value[4]) == 0x01) {
            LogModule.i(order.getOrderName() + "成功");
            orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        } else {
            LogModule.i(order.getOrderName() + "失败");
        }
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
