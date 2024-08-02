package com.fitpolo.support.task.funcTask;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.DigitalConver;

/**
 * 获取电量
 */
public class GetBatteryTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 8;
    // 获取数据

    private byte[] orderData;

    public GetBatteryTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.getBattery, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[]{
            (byte) MokoConstants.HEADER_READ_SEND,
            (byte) 0x06,
            (byte) MokoConstants.Function,
            (byte) order.getOrderHeader(),
            (byte) 0x01,
            (byte) 0x00,
            (byte) 0xFF,
            (byte) 0xFF
        };
//        [255, 6, 2, 3, 1, 0, 255, 255]
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        LogModule.i(order.getOrderName() + "成功");
        LogModule.i("数据返回"+DigitalConver.bytesToHexString(value));
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        int batteryQuantity = DigitalConver.byte2Int(value[5]);
        MokoSupport.getInstance().setBatteryQuantity(batteryQuantity);
        LogModule.i("电池电量：" + batteryQuantity);
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[3])) {
            return;
        }
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
