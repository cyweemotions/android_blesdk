package com.fitpolo.support.task.dataPushTask;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.entity.dataEntity.SportModel;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.ByteType;
import com.fitpolo.support.utils.DigitalConver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class SyncSportTask extends OrderTask {
    private byte[] orderData;
    private int typeData; // record—— 1  current—— 0
    private int index = 1; // record=1 —— package index
    private int location = 0;
    private List<String> sportHistoryDatas = new ArrayList<>();
    private List<byte[]> res = new ArrayList<>();
    public SyncSportTask(MokoOrderTaskCallback callback, int fileIndex, int year, int month, int day) {
        super(OrderType.DataPushWRITE, OrderEnum.syncSport, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        location = fileIndex;
        typeData = 1;
        List<Byte> dataList = new ArrayList<>();
        byte isRecordByte = (byte) 0x01;
        dataList.add(isRecordByte);
//        Calendar calendar = Calendar.getInstance();
        //年
        List<Byte> yearData = DigitalConver.convert(year, ByteType.WORD);
        dataList.addAll(yearData);
        //月
        dataList.add((byte) (month & 0xFF));
        //日
        dataList.add((byte) (day & 0xFF));
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
//            int result = (value[5] & 0xFF);
//            if(result == 0){
//                backResult = 0;
//            }else{
//                backResult = 1;
//            }
//            List<SportModel> dataSource = new ArrayList<>();
//            response.responseObject = dataSource;
//            MokoSupport.getInstance().setSportData(dataSource);
//            orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
//            MokoSupport.getInstance().pollTask();
//            callback.onOrderResult(response);
//            MokoSupport.getInstance().executeTask(callback);
        } else {
            byte[] subArray = Arrays.copyOfRange(value, 5, dataLength + 5);
            int type = (subArray[0] & 0xFF);
            if(type != typeData) { // 1——记录 0——当天
                return;
            }
//            LogModule.i("获取数据类型======"+type);
            if(type == 0) {
                // 获取当天的运动数据
                parseCurrentData(subArray);
            } else {
                // 获取运动记录
                parseRecordData(subArray);
            }
        }
    }

    // 解析当天数据
    private void parseCurrentData (byte[] list) {
        byte[] resultArray = Arrays.copyOfRange(list, 1, list.length);
        String resultHexStr = DigitalConver.bytesToHexString(resultArray);
        String resultStr = DigitalConver.hex2String(resultHexStr);
        LogModule.i("获取运动数据成功");
        LogModule.i(resultStr);

        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
    // 解析记录数据
    private void parseRecordData (byte[] list) {
        int packType = (list[1] & 0xFF);
        int fileLocation = (list[2] & 0xFF);
        if(packType == 3 && (list.length == 3)){
            LogModule.i("拿到文件的个数===="+ fileLocation);
            return;
        }
        int packIndex = (list[3] & 0xFF);
        byte[] resultArray = Arrays.copyOfRange(list, 5, list.length);
        if(packIndex == index && fileLocation == location) {
            res.add(resultArray);
            LogModule.i("获取运动数据类型packType====="+ res);
            if(packType == 0 || packType == 2) {
                StringBuilder resultStr = new StringBuilder(); // 最后的数据
                for (int i=0; i<res.size(); i++) {
                    // 1、byte[]数据转换为String数据
                    byte[] value = res.get(i);
                    String resultHexStr = DigitalConver.bytesToHexString(value);
                    String heartStr = DigitalConver.hex2String(resultHexStr);
//                    System.out.println("这是运动数据" + heartStr.toString());
                    resultStr.append(heartStr);
                }
                sportHistoryDatas.add(String.valueOf(resultStr));

                res.clear();
                index = 1;
                location++;
            }
            index++;
        }
        if(packType == 3) {
            List<SportModel> dataSource = new ArrayList<>();
            long start = 0;
            int sportType = 0;
            for(int j=0;j<sportHistoryDatas.size();j++){
                String sportSubStr = sportHistoryDatas.get(j);
                System.out.println("这是运动数据sportSubStr" + sportSubStr);
                List<String> contents = Arrays.asList(sportSubStr.split("\n"));
                List<String> sportDetails = new ArrayList<>();
                for(int i=0; i<contents.size(); i++) {
                    if(contents.get(i).startsWith(MokoConstants.SS)) {
                        String contentStr = contents.get(i).replace(MokoConstants.SS, "");
                        if(contentStr.contains(",")) {
                            List<String> contentList = Arrays.asList(contentStr.split(","));
                            start = Long.parseLong(contentList.get(0));
                            sportType = Integer.parseInt(contentList.get(contentList.size()-1));
                        }
                    } else if (contents.get(i).startsWith(MokoConstants.SE)) {
                        String contentStr = contents.get(i).replace(MokoConstants.SE, "");
                        dataSource.add(SportModel.StringTurnModel(contentStr, start, sportType,sportDetails,sportSubStr));
                    } else if (contents.get(i).startsWith(MokoConstants.SP)) {
                        String contentStr = contents.get(i).replace(MokoConstants.SP, "");
//                        System.out.println("这是contentStr" + contentStr);
                        sportDetails.add(contentStr);
                    }
                }
            }
            sportHistoryDatas.clear();
            for (SportModel sportdata : dataSource) {
                System.out.println("这是最终的运动数据" + sportdata.toString());
            }
            LogModule.i("获取运动数据长度======="+dataSource.size());
            response.responseObject = dataSource;
            MokoSupport.getInstance().setSportData(dataSource);
            orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
            MokoSupport.getInstance().pollTask();
            callback.onOrderResult(response);
            MokoSupport.getInstance().executeTask(callback);
        } else {
            MokoSupport.getInstance().timeoutHandler(this);
        }
    }
}
