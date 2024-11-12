package com.fitpolo.support.task.dataPushTask;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.entity.dataEntity.BloodOxygenModel;
import com.fitpolo.support.entity.dataEntity.HeartRateModel;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.ByteType;
import com.fitpolo.support.utils.DigitalConver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class SyncHeartRateTask extends OrderTask {
    private byte[] orderData;
    private int typeData; // record—— 1  current—— 0
    private int index = 1; // record=1 —— package index
    private List<byte[]> res = new ArrayList<>();
    public SyncHeartRateTask(MokoOrderTaskCallback callback, int year, int month, int day) {
        super(OrderType.DataPushWRITE, OrderEnum.syncHeartRate, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        typeData = 1;
        List<Byte> dataList = new ArrayList<>();
        byte isRecordByte = (byte) 0x01;
        dataList.add(isRecordByte);
        //年
        List<Byte> yearData = DigitalConver.convert(year, ByteType.WORD);
        dataList.addAll(yearData);
        //月
        dataList.add((byte) (month & 0xFF));
        //日
        dataList.add((byte) (day & 0xFF));
        dataList.add((byte) 0x00);
        dataList.add((byte) 0x00);
        dataList.add((byte) 0x00);
        dataList.add((byte) 0x00);
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
        int backResult = 0;
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[3])) return;
        if (MokoConstants.DataNotify != DigitalConver.byte2Int(value[2])) return;
        int dataLength = (value[4] & 0xFF);
        if(dataLength <= 8) { //获取数据错误
//            int result = (value[5] & 0xFF);
//            if(result == 0){
//                backResult = 0;
//            }else{
//                backResult = 1;
//            }
            List<HeartRateModel> dataSource = new ArrayList<>();
            response.responseObject = dataSource;
            MokoSupport.getInstance().setHeartRateData(dataSource);
            orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
            MokoSupport.getInstance().pollTask();
            callback.onOrderResult(response);
            MokoSupport.getInstance().executeTask(callback);
        } else {
            byte[] subArray = Arrays.copyOfRange(value, 5, dataLength + 5);
            int type = (subArray[0] & 0xFF);
            if(type != typeData) { // 1——记录 0——当天
                return;
            }
//            LogModule.i("获取数据类型======"+type);
            if(type == 0) {
                // 获取当天的心率数据
                parseCurrentData(subArray);
            } else {
                // 获取心率记录
                parseRecordData(subArray);
            }
        }
    }

    // 解析当天数据
    private void parseCurrentData (byte[] list) {
        byte[] resultArray = Arrays.copyOfRange(list, 1, list.length);
        String resultHexStr = DigitalConver.bytesToHexString(resultArray);
        String resultStr = DigitalConver.hex2String(resultHexStr);
        LogModule.i("获取心率数据成功");
        LogModule.i(resultStr);

        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
    // 解析记录数据
    private void parseRecordData (byte[] list) {
        int packType = (list[1] & 0xFF);
        int packIndex = (list[2] & 0xFF);
        byte[] resultArray = Arrays.copyOfRange(list, 8, list.length);
        if(packIndex == index) {
            res.add(resultArray);
//            LogModule.i("获取步数数据类型packType====="+ res);
            if(packType == 0 || packType == 2) { //结束 后面没有数据接收了
                StringBuilder resultStr = new StringBuilder(); // 最后的数据
                List<HeartRateModel> dataSource = new ArrayList<>();
                for (int i=0; i<res.size(); i++) {
                    // 1、byte[]数据转换为String数据
                    byte[] value = res.get(i);
                    String resultHexStr = DigitalConver.bytesToHexString(value);
                    String heartStr = DigitalConver.hex2String(resultHexStr);
                    resultStr.append(heartStr);
                }
                List<String> contents = Arrays.asList(resultStr.toString().split("\n"));
                for(int j=0;j<contents.size();j++){
                    String contentStr = contents.get(j).replace("[HR]", "");
                    dataSource.add(HeartRateModel.StringTurnModel(contentStr));
                }
                for (HeartRateModel heartRate : dataSource) {
                    System.out.println("这是最终的数据格式" + heartRate.toString());
                }
                LogModule.i("获取心率数据长度======="+dataSource.size());
                response.responseObject = dataSource;
                MokoSupport.getInstance().setHeartRateData(dataSource);
                orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
                MokoSupport.getInstance().pollTask();
                callback.onOrderResult(response);
                MokoSupport.getInstance().executeTask(callback);
                index = 1;
            } else {
                MokoSupport.getInstance().timeoutHandler(this);
            }
            index++;
        }
    }
}
