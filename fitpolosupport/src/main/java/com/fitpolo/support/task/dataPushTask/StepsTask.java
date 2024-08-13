package com.fitpolo.support.task.dataPushTask;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.entity.dataEntity.Steps;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.ByteType;
import com.fitpolo.support.utils.DigitalConver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StepsTask extends OrderTask {
    private byte[] orderData;
    private int typeData; // record—— 1  current—— 0
    private int index = 1; // record=1 —— package index
    private Map<String, byte[]> res = new HashMap<>();
    public StepsTask(MokoOrderTaskCallback callback, int type) {
        super(OrderType.DataPushWRITE, OrderEnum.syncSteps, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        typeData = type;
        List<Byte> dataList = new ArrayList<>();
        byte isRecordByte = type == 1 ? (byte) 0x01 : (byte) 0x00;
        dataList.add(isRecordByte);
        Calendar calendar = Calendar.getInstance();
        //年
        int year = calendar.get(Calendar.YEAR);
        List<Byte> yearData = DigitalConver.convert(year, ByteType.WORD);
        dataList.addAll(yearData);
        //月
        int month = calendar.get(Calendar.MONTH) + 1;
        dataList.add((byte) (month & 0xFF));
        //日
        int day = calendar.get(Calendar.DAY_OF_MONTH);
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
        if(dataLength < 1) { //获取数据错误
            int result = (value[5] & 0xFF);
            if(result == 0){
                backResult = 0;
            }else{
                backResult = 1;
            }
        } else {
            byte[] subArray = Arrays.copyOfRange(value, 5, dataLength + 5);
            int type = (subArray[0] & 0xFF);
            if(type != typeData) { // 1——记录 0——当天
                return;
            }
//            LogModule.i("获取步数数据类型======"+type);
            if(type == 0) {
                // 获取当天的步数数据
                parseCurrentData(subArray);
            } else {
                // 获取步数记录
                parseRecordData(subArray);
            }
        }
    }

    // 解析当天数据
    private void parseCurrentData (byte[] list) {
        byte[] resultArray = Arrays.copyOfRange(list, 1, list.length);
        String resultHexStr = DigitalConver.bytesToHexString(resultArray);
        String resultStr = DigitalConver.hex2String(resultHexStr);
        LogModule.i("获取步数数据成功");
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
            String key = String.valueOf(packIndex);
            res.put(key,resultArray);
//            LogModule.i("获取步数数据类型packType====="+ res);
            if(packType == 2) { //结束 后面没有数据接收了
                String lastObject = "";
                List<Steps> dataSource = new ArrayList<>();
                int resIndex = 0;
                for (Map.Entry<String, byte[]> entry : res.entrySet()) {
//                    1、byte[]数据转换为String数据
                    byte[] value = entry.getValue();
                    String resultHexStr = DigitalConver.bytesToHexString(value);
                    String heartStr = DigitalConver.hex2String(resultHexStr);
                    heartStr = lastObject + heartStr;
//                    2、格式化数据 [ST]0,0,6000,240812060000\n[ST]0,0,7000,240812080000\n[ST]327,235,17000,240812080000
                    List<String> contents = Arrays.asList(heartStr.split("\n"));
//                    3、保存最后一条不完整数据
                    lastObject = contents.get(contents.size() - 1);
//                    4、最后一项为完整数据，最后一条也要添加
                    int contentsLength = resIndex == contents.size()-1 ? contents.size() : contents.size()-1;
                    for(int i=0;i<contentsLength;i++){
                        String contentStr = contents.get(i).replace("[ST]", "");
                        dataSource.add(Steps.StringTurnModel(contentStr));
                    }
                    resIndex++;
                }
                for (Steps step : dataSource) {
                    System.out.println("这是最终的数据格式" + step.toString());
                }
                MokoSupport.getInstance().setStepsData(dataSource);
                orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
                MokoSupport.getInstance().pollTask();
                callback.onOrderResult(response);
                MokoSupport.getInstance().executeTask(callback);
                index = 1;
            }
            index++;
        }
    }
}
