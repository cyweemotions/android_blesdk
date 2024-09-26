package com.fitpolo.support.task.setTask;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.entity.setEntity.AddressBook;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.DigitalConver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 通讯录设置
 */
public class AddressBookTask extends OrderTask {
    private byte[] orderData;
    public AddressBookTask (MokoOrderTaskCallback callback, AddressBook addressBook) {
        super(OrderType.WRITE, OrderEnum.setAddressBook, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        List<Byte> dataList = new ArrayList<>();
        dataList.add((byte) (addressBook.action & 0xFF));// 0-添加 1-删除

        List<Byte> nameByteList = string2bytes(addressBook.name, 20);
        dataList.addAll(nameByteList);//名字

        List<Byte> phoneByteList = string2bytes(addressBook.phoneNumber, 15);
        dataList.addAll(phoneByteList);//电话号码

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
    private List<Byte> string2bytes(String valueStr,int limit) {
        byte[] byteArray = valueStr.getBytes();
        List<Byte> byteList = new ArrayList<>();
        // 将字节数组复制到 List<Byte> 中，并在不足 20 的索引位置补充 0
        for (int i = 0; i < limit; i++) {
            if (i < byteArray.length) {
                byteList.add(byteArray[i]);
            } else {
                byteList.add((byte) 0); // 补充 0
            }
        }
        return byteList;
    }
    @Override
    public byte[] assemble() {
        return orderData;
    }
    @Override
    public void parseValue(byte[] value) {
        LogModule.i("返回的"+ order.getOrderName() + Arrays.toString(value));
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
