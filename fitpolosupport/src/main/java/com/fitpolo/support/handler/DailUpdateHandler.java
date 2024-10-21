package com.fitpolo.support.handler;

import android.util.Log;

import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MKDailCallBack;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.DailControl;
import com.fitpolo.support.entity.OrderTaskResponse;
import com.fitpolo.support.task.DailTask;
import com.fitpolo.support.task.authTask.SendBindCodeTask;
import com.fitpolo.support.utils.ByteType;
import com.fitpolo.support.utils.CRC16;
import com.fitpolo.support.utils.DigitalConver;

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

public class DailUpdateHandler implements MokoOrderTaskCallback{

     public int packageSize = 230;
     public InputStream input;
     public int allFileSize;
     public int cuttentFileIndex = 0;
     public MKDailCallBack callBack;
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
        cuttentFileIndex = 0;
        ///3.读取文件到字典中
        String name = FilePaths.get(0);
        byte[] sendData = readFileContent(name);

        ///4.开始发送文件握手包
        List<Byte> datas = DigitalConver.bytes2ListByte(sendData);
        Log.d("startDailFileTransMit", "startDailFileTransMit: "+datas.toString());
        sendFileDailStart(cuttentFileIndex,name,datas);
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
        DailTask dailTask = new DailTask((MokoOrderTaskCallback) this, DailControl.DIAL_FILE_START.value,false, source);
        dailTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                ///完成文件发送
                Log.d("sendFileDailStart", "onOrderResult: "+response.toString());
                List<Byte> nameLength = DigitalConver.convert(name.length(), ByteType.WORD);
                String hexString = DigitalConver.string2Hex(name);
                byte[] namebytes = DigitalConver.hex2bytes(hexString);
                List<Byte> nameList = DigitalConver.bytes2ListByte(namebytes);
                sendFileInfo(nameLength,nameList,source);
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
        DailTask dailTask = new DailTask((MokoOrderTaskCallback) this, DailControl.DIAL_FILE_INFO.value,false, sendData);
        dailTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                
                sendFileMultiInfo(fileData);
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
    public void sendFileMultiInfo(List<Byte> sendata){

        DailTask dailTask = new DailTask((MokoOrderTaskCallback) this, DailControl.DIAL_FILE_SEND.value,true, sendata);
        dailTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {

                ///需要判断发送包类型
                ///更具MTU

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
    public void sendFileMutilFirst(List<Byte> first,List<Byte> fileData,List<Byte> second){

        DailTask dailTaskFirst = new DailTask((MokoOrderTaskCallback) this, DailControl.DIAL_FILE_SEND.value,true, first);
        ///不需要回调
        MokoSupport.getInstance().sendOrder(dailTaskFirst);
        sendFileMutil(0,fileData,1);
    }
    /*
     * 发送文件多包
     */
    public void sendFileMutil(int offset, final List<Byte> fileData, int packages){
        DailTask dailTaskSecond = new DailTask((MokoOrderTaskCallback) this, DailControl.DIAL_FILE_SEND.value,true, fileData);
        dailTaskSecond.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                ///判断是否是最后一包
                if(true){
                    ///发送crc
                    sendCrc(fileData);
                }else{
                    ///继续发送下一包
                    sendFileMutil(1,fileData,2);
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
    public void sendCrc(List<Byte> sendData){
        byte[] datas = DigitalConver.listToArray(sendData);
        List<Byte> CRC = DigitalConver.convert(CRC16.crc16(datas), ByteType.WORD);
        DailTask dailTaskSecond = new DailTask((MokoOrderTaskCallback) this, DailControl.DAIL_SEND_FILE.value,true, CRC);
        dailTaskSecond.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                ///判断类型是否为成功
                if(true){
                    Log.d("sendCrc", "sendCrc: 表盘上传成功");
                }else{
                    ///抛出异常
                    Log.d("sendCrc", "sendCrc: crc失败");
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
}
