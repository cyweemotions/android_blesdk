package com.fitpolo.support.task.getTask;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.entity.setEntity.AddressBook;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.ByteType;
import com.fitpolo.support.utils.DigitalConver;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressBookDataTask extends OrderTask {
    private List<HashMap> addressBookDataList = new ArrayList<>();
    private byte[] orderData;
    private byte[] addressBookData = new byte[]{};
    public AddressBookDataTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.setAddressBook, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
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
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[3])) {
            return;
        }
        int dataLength = (value[4] & 0xFF) + 5;
        int header = DigitalConver.byte2Int(value[5]);
        byte[] subArray = Arrays.copyOfRange(value, 7, dataLength);
        LogModule.i(order.getOrderName() + "返回的header" + header);
        addressBookData = DigitalConver.mergeBitArrays(addressBookData, subArray);
        if(header == 0 || header == 2) { //等于0或2代表最后一包
            List<Byte> byteList = DigitalConver.bytes2ListByte(addressBookData);
            try {
                int totals = (byteList.size() / 35);
                for(int i=0;i<totals;i++){
                    List<Byte> albumInfo = byteList.subList(i*35,(i+1)*35);
                    List<Byte> name = albumInfo.subList(0,20);
                    List<Byte> phone = albumInfo.subList(20,35);
                    String albumName = new String(DigitalConver.listByte2bytes(DigitalConver.removeZero(name)), "UTF-8");
                    String albumPhone = new String(DigitalConver.listByte2bytes(DigitalConver.removeZero(phone)), "UTF-8");
                    HashMap<String, String> map = new HashMap<>();
                    map.put("name", albumName);
                    map.put("phone", albumPhone);
                    addressBookDataList.add(map);
                }
                response.responseObject = addressBookDataList;
                LogModule.i("返回的通讯录数据" + addressBookDataList.toString());
                orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
                LogModule.i(order.getOrderName() + "成功");
            } catch (UnsupportedEncodingException e) {
                LogModule.i(order.getOrderName() + "失败");
                e.printStackTrace();
            }
            MokoSupport.getInstance().pollTask();
            callback.onOrderResult(response);
            MokoSupport.getInstance().executeTask(callback);
        }
    }

}
