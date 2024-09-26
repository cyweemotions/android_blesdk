package com.fitpolo.support.task.setTask;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.entity.setEntity.MotionTarget;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.DigitalConver;

import java.util.ArrayList;
import java.util.List;

public class MotionTargetTask extends OrderTask {
    private byte[] orderData;
    public MotionTargetTask(MokoOrderTaskCallback callback, MotionTarget motionTarget) {
        super(OrderType.WRITE, OrderEnum.setMotionTarget, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        if(motionTarget.distance < 0 || motionTarget.distance > 99) {
            LogModule.e("距离值错误");
            return;
        }
        if(motionTarget.sportTime < 0 || motionTarget.sportTime > 23) {
            LogModule.e("时间值错误");
            return;
        }
        if(motionTarget.calorie < 0 || motionTarget.calorie > 9) {
            LogModule.e("卡路里值错误");
            return;
        }

        List<Byte> dataList = new ArrayList<>();
        dataList.add((byte) (motionTarget.setType & 0xFF));// 类型：0-目标设置 1-自动暂停设置
        dataList.add((byte) (motionTarget.sportType & 0xFF));//0-户外步行 1-户外跑步 2-户外骑行 3-室内步行
        if(motionTarget.setType == 0) { //设置目标
            dataList.add((byte) (motionTarget.distance & 0xFF));// 距离 单位公里 区间0-99
            dataList.add((byte) (motionTarget.sportTime & 0xFF));// 时间 单位分 区间0-23 实际值*10+5
            dataList.add((byte) (motionTarget.calorie & 0xFF));// 卡路里 单位千卡 区间0-9 实际值*100
            dataList.add((byte) (motionTarget.targetType & 0xFF));// 目标设置类型 0-none 1-distance 2-sportTime 3-calorie
        } else { // 设置自动暂停
            dataList.add((byte) (motionTarget.autoPauseSwitch & 0xFF));// 自动暂停开关 当setType为1的情况下设置
        }
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
