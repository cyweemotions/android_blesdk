package com.fitpolo.support.handler;

import android.util.Log;

import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MKDailCallBack;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.DailControl;
import com.fitpolo.support.entity.OrderTaskResponse;
import com.fitpolo.support.entity.StreamResType;
import com.fitpolo.support.entity.StreamType;
import com.fitpolo.support.task.DailTask;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.task.UpgradeBandTask;
import com.fitpolo.support.task.authTask.SendBindCodeTask;
import com.fitpolo.support.utils.ByteType;
import com.fitpolo.support.utils.CRC16;
import com.fitpolo.support.utils.DigitalConver;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DailUpdateHandler implements MokoOrderTaskCallback, MokoSupport.IUpgradeDataListener{

     public int packageSize = 230;
     public InputStream input;
     public int allFileSize;
     public int currentFileIndex = 0;
     public int currentFileSize = 0;
     public MKDailCallBack callBack;
     public List currentTrans = new ArrayList<>();
     public int currentOffset = 0;
     public int totalPackages = 0;
     public List filePathContents = new ArrayList<>();
//    late List<int> dataSource = [];
//    late successBlock finishCall;
//    late int currentFileIndex = 0;
//    late List<String> filePathContents;
//    static late int allFileSize = 0;
//    static late int currentFileSize = 0;
    ///1.发送表盘文件
    ///2.获取文件个数和大小

    public DailUpdateHandler(MKDailCallBack callback) {
        this.callBack = callback;
    }

    public void startDailFileTransMit(List<String> FilePaths,int dailAllFileSize){
        ///1.判断是否连接蓝牙
        ///2.赋值创举变量，文件总大小，当前index,当前路劲，当前文件大小
        if(FilePaths.size() <= 0){
            return;
        }
        allFileSize = dailAllFileSize;
        currentFileIndex = 0;
        filePathContents = FilePaths;
        ///3.读取文件到字典中
        String name = FilePaths.get(0);
        byte[] sendData = readFileContent(name);

        ///4.开始发送文件握手包
        List<Byte> datas = DigitalConver.bytes2ListByte(sendData);
        Log.d("startDailFileTransMit", "startDailFileTransMit: "+datas.toString());
        sendFileDailStart(currentFileIndex,name,datas);
    }

    ///3.读取文件内容方法 读取一个文件
    public byte[] readFileContent(String filePath){
        byte[] bytesArray = null;
        if(filePath.isEmpty()){
            return bytesArray;
        }
        FileInputStream fileInputStream = null;

        try {
            fileInputStream = new FileInputStream(filePath);
            bytesArray = new byte[fileInputStream.available()];
            fileInputStream.read(bytesArray);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return bytesArray;
    }

    ///4.发送文件开始数据

    public void sendFileDailStart(int index, final String name, final List<Byte> source){
        Log.d("TAG", "sendFileDailStart: ");
        ///4.1 发送调用
        DailTask dailTask = new DailTask((MokoOrderTaskCallback) this, DailControl.DIAL_FILE_START.value,false,false, new ArrayList<Byte>(),0);
        dailTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                ///完成文件发送
                Log.d("sendFileDailStart", "sendFileDailStart onOrderResult: "+response.responseObject);
                int value = (int) response.responseObject;
                if(value == StreamResType.xon_frame_ack_type_ok.typeValue){
                    Log.d("sendFileDailStart", "sendFileDailStart onOrderResult success: ");
                    String[] names = name.split("/");
                    String fileName = names[names.length - 1];
                    List<Byte> nameLength = DigitalConver.convert(fileName.length(), ByteType.WORD);
                    String hexString = DigitalConver.string2Hex(fileName);
                    byte[] namebytes = DigitalConver.hex2bytes(hexString);
                    List<Byte> nameList = DigitalConver.bytes2ListByte(namebytes);
                    Log.d("sendFileDailStart", "sendFileDailStart onOrderResult success: "+fileName);
                    sendFileInfo(nameLength,nameList,source);
                }else{
                    Log.d("TAG", "sendFileDailStart onOrderResult error: "+response.responseObject);
                }

            }

            @Override
            public void onOrderTimeout(OrderTaskResponse response) {

            }

            @Override
            public void onOrderFinish() {

            }
        };
        MokoSupport.getInstance().sendOrder(dailTask);
    }


    ///5.发送文件信息
    public void sendFileInfo(List<Byte> length, List<Byte> nameData, final List<Byte> fileData){

        List<Byte> sendData = new ArrayList<>();
        Collections.reverse(length);
        sendData.addAll(length);
        sendData.addAll(nameData);
        DailTask dailTask = new DailTask((MokoOrderTaskCallback) this, DailControl.DIAL_FILE_INFO.value,false,false, sendData,0);
        dailTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                Log.d("TAG", "sendFileInfo onOrderResult: ");
                int value = (int) response.responseObject;
                if(value == StreamResType.xon_frame_ack_type_ok.typeValue){
                    Log.d("sendFileDailStart", "sendFileInfo onOrderResult success: ");
                    sendFileMultiInfo(fileData);
                }else{
                    Log.d("TAG", "sendFileDailStart onOrderResult error: "+response.responseObject);
                }
            }

            @Override
            public void onOrderTimeout(OrderTaskResponse response) {

            }

            @Override
            public void onOrderFinish() {

            }
        };
        MokoSupport.getInstance().sendOrder(dailTask);
    }


    ///6.发送文件详情包
    public void sendFileMultiInfo(final List<Byte> sendata){
        ///文件切割
        int sendLength = MokoSupport.mtu - 12;
        int packages = (sendata.size() / sendLength);
        if((sendata.size() % sendLength) > 0){
            packages = packages +1;
        }
        final int packedCount = packages;
        Log.d("sendFileMultiInfo", "sendFileMultiInfo  send: "+packages);
        List<Byte> dataSource = new ArrayList<>();
        ///packInfo
        List<Byte> packageInfo = DigitalConver.convert(packages,ByteType.DWORD);
        Collections.reverse(packageInfo);
        List<Byte> packageCount = DigitalConver.convert(sendata.size()+10,ByteType.DWORD);
        Collections.reverse(packageCount);
        dataSource.addAll(packageInfo);
        dataSource.addAll(packageCount);
        Log.d("sendFileDailStart", "sendFileMultiInfo onOrderResult send: "+dataSource);
        DailTask dailTask = new DailTask((MokoOrderTaskCallback) this,true, StreamType.xon_frame_type_multi_info.typeValue,true, dataSource);
        dailTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                Log.d("sendFileDailStart", "sendFileMultiInfo onOrderResult: "+response.responseObject);
                int value = (int) response.responseObject;
                if(value == StreamResType.xon_frame_ack_type_ok.typeValue){
                    ///需要判断发送包类型
                    Log.d("sendFileDailStart", "sendFileMultiInfo onOrderResult success: ");
                    List<Byte> first = sendata.subList(0,MokoSupport.mtu-22);
                    List<Byte> second = sendata.subList(first.size(),sendata.size());
                    currentTrans = second;
                    sendFileMutilFirst(first,sendata,second);

                }else{
                    Log.d("TAG", "sendFileMultiInfo onOrderResult error: "+response.responseObject);
                }
            }

            @Override
            public void onOrderTimeout(OrderTaskResponse response) {

            }

            @Override
            public void onOrderFinish() {

            }
        };
        MokoSupport.getInstance().sendOrder(dailTask);
    }

    ///7.正式发送文件
    /*
    * 需要注意这里会一次性发送两包数据
    * */
    public void sendFileMutilFirst(final List<Byte> first, List<Byte> fileData, List<Byte> second){
        Log.d("", "sendFileMutilFirst: 走了几次"+first+"-"+fileData.size());
        ///先发送第一包
        DailTask dailTaskFirst = new DailTask((MokoOrderTaskCallback) this, DailControl.DIAL_FILE_SEND.value,true,true, first,fileData.size());
        dailTaskFirst.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                byte[] value = (byte[]) response.responseObject;
                List<Byte> res = DigitalConver.bytes2ListByte(value);
                ///判断是否是最后一包
//                int value = (int) response.responseObject;
//                if(value == StreamResType.xon_frame_ack_type_ok.typeValue){
//                }else{
                    Log.d("TAG", "sendFileMutilFirst onOrderResult : "+res);
//                }
            }

            @Override
            public void onOrderTimeout(OrderTaskResponse response) {

            }

            @Override
            public void onOrderFinish() {

            }
        };
        currentFileSize = currentFileSize + first.size();
        MokoSupport.getInstance().sendOrder(dailTaskFirst);
        ///不需要回调
        int sendLength = MokoSupport.mtu - 12;
        int packages = (second.size() / sendLength);
        if((second.size() % sendLength) > 0){
            packages++;
        }
        sendFileMutil(1,second,packages);
    }
    /*
     * 发送文件多包
     */
    public void sendFileMutil(int offset, final List<Byte> fileData, int packages){
        totalPackages = packages;
        currentOffset = offset-1;
        Log.d("TAG", "sendFileMutil onOrderResult start: "+currentOffset+"--"+totalPackages);
        int sendLength = MokoSupport.mtu -12;
        Log.d("TAG", "sendFileMutil onOrderResult length: "+MokoSupport.mtu+"--"+sendLength);
        List<Byte> totalData = new ArrayList<>();
        ///当前为文件的第二包
        List<Byte> sendData = new ArrayList<>();
        List<Byte> packageIndex = DigitalConver.convert(offset,ByteType.DWORD);
        Collections.reverse(packageIndex);
        ///判断是否是最后一包
        if((fileData.size() - currentOffset*sendLength) >= sendLength){
            sendData = fileData.subList(currentOffset*sendLength,(currentOffset+1)*sendLength);
        }else{
            sendData = fileData.subList(currentOffset*sendLength,fileData.size());
        }
        totalData.addAll(packageIndex);
        totalData.addAll(sendData);
        Log.d("TAG", "sendFileMutil data: "+totalData.toString());
        currentFileSize = currentFileSize + sendData.size();
        DailTask dailTaskSecond = new DailTask((MokoOrderTaskCallback) this,true, StreamType.xon_frame_type_multi.typeValue,true, totalData);
        dailTaskSecond.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                   byte[] value = (byte[]) response.responseObject;
                   List<Byte> res = DigitalConver.bytes2ListByte(value);
                   Log.d("TAG", "sendFileMutil onOrderResult success: "+res);
                   if(res.size() > 1){
                       int first = DigitalConver.byte2Int(res.get(0));
                       if(first == 1){
                           ///请求成功
                           currentOffset = currentOffset+2;
                           float precent = (float)currentFileSize/allFileSize;
                           Log.d("TAG", "sendFileMutil onOrderResult success: "+currentOffset+"-"+totalPackages);
                           Log.d("TAG", "onOrderResult: progress " +"-"+precent);
                           callBack.onResult(precent);
                           if(currentOffset > totalPackages){
                               ///发送crc
                               sendCrc();
                           }else{
                               sendFileMutil(currentOffset,currentTrans,totalPackages);
                           }
                       }

                   }else{
                       Log.d("TAG", "sendFileMutil onOrderResult error: "+response.responseObject);
                   }

            }

            @Override
            public void onOrderTimeout(OrderTaskResponse response) {

            }

            @Override
            public void onOrderFinish() {

            }
        };
        MokoSupport.getInstance().sendOrder(dailTaskSecond);
    }


    /*
    * 发送多包CRC
    * */
    public void sendCrc(){
        byte[] datas = DigitalConver.listToArray(currentTrans);
        Log.d("TAG", "sendCrc success: ");
        List<Byte> CRC = DigitalConver.convert(CRC16.crc16(datas), ByteType.WORD);
        DailTask dailTaskSecond = new DailTask((MokoOrderTaskCallback) this,true, StreamType.xon_frame_type_multi_crc.typeValue, true, CRC);
        dailTaskSecond.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                ///判断类型是否为成功
                byte[] value = (byte[]) response.responseObject;
                List<Byte> res = DigitalConver.bytes2ListByte(value);
                currentFileIndex++;
                if(currentFileIndex < filePathContents.size()){
                    Log.d("TAG", "onOrderResult: 发送"+currentFileIndex+"包");
                    String name = filePathContents.get(currentFileIndex).toString();
                    ///获取文件内容
                    byte[] sendData = readFileContent(name);
                    ///4.开始发送文件握手包
                    List<Byte> datas = DigitalConver.bytes2ListByte(sendData);
                    ///读取文件名称和长度
                    String[] names = name.split("/");
                    String fileName = names[names.length - 1];
                    List<Byte> nameLength = DigitalConver.convert(fileName.length(), ByteType.WORD);
                    String hexString = DigitalConver.string2Hex(fileName);
                    byte[] namebytes = DigitalConver.hex2bytes(hexString);
                    List<Byte> nameList = DigitalConver.bytes2ListByte(namebytes);
                    Log.d("sendFileDailStart", "sendCrc onOrderResult success: "+datas.size()+"+"+fileName);
                    sendFileInfo(nameLength,nameList,datas);
                }else{
                    Log.d("TAG", "onOrderResult: 文件停止");
                    DailFileStop();
                }
            }

            @Override
            public void onOrderTimeout(OrderTaskResponse response) {

            }

            @Override
            public void onOrderFinish() {

            }
        };
        MokoSupport.getInstance().sendOrder(dailTaskSecond);
    }


    /*
    * 文件停止
    * */
    public void DailFileStop(){
        List<Byte> send = new ArrayList<>();
        byte header01 = (byte) 0x04;
        send.add(header01);
        DailTask dailTaskend = new DailTask((MokoOrderTaskCallback) this, DailControl.DIAL_FILE_STOP.value, false,false, send,0);
        MokoSupport.getInstance().sendOrder(dailTaskend);
    }

    @Override
    public void onOrderResult(OrderTaskResponse response) {
        Log.d("", "onOrderResult: uphandle");
    }

    @Override
    public void onOrderTimeout(OrderTaskResponse response) {

    }

    @Override
    public void onOrderFinish() {

    }


    ///返回回调
    @Override
    public void onDataSendSuccess(byte[] values) {

        Log.d("TAG", "onDataSendSuccess: "+DigitalConver.bytes2ListByte(values));
    }

    boolean CrcVerify(byte[] data) {
        int value = CRC16.crc16(data);
        Log.d("TAG", "CrcVerify: "+value);
        if(value == 0){
            ///crc 校验正确
            return true;
        }else{
            return false;
        }
    }
}
