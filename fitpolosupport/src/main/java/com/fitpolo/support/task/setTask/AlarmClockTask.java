package com.fitpolo.support.task.setTask;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.entity.setEntity.AlarmClock;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.ByteType;
import com.fitpolo.support.utils.DigitalConver;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置闹钟
 */
public class AlarmClockTask extends OrderTask {
    private byte[] orderData;

    // 周一到周日掩码
    // 0x01 --  周日
    // 0x02 --  周一
    // 0x04 --  周二
    // 0x08 --  周三
    // 0x10 --  周四
    // 0x20 --  周五
    // 0x40 --  周六
    public AlarmClockTask(MokoOrderTaskCallback callback, AlarmClock alarmClock) {
        super(OrderType.WRITE, OrderEnum.setAlarmClock, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        List<Byte> byteList = new ArrayList<>();
        byteList.add((byte) (alarmClock.action & 0xFF)); // 添加\删除\修改
        byteList.add((byte) (alarmClock.index & 0xFF)); // 闹钟编号
        byteList.add((byte) (alarmClock.toggle & 0xFF)); // 闹钟状态
        byteList.add((byte) (alarmClock.mode & 0xFF)); // 铃声
        List<Byte> startU8 = DigitalConver.convert(alarmClock.time, ByteType.WORD);
        byteList.addAll(startU8); // 闹钟时间
        byteList.add((byte) (alarmClock.repeat & 0xFF)); // 重复
        byteList.add((byte) (alarmClock.activeDay & 0xFF)); // 周一到周日掩码
        // List<Byte> 转换为 byte[]
        byte[] dataBytes = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            dataBytes[i] = byteList.get(i);
        }
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
