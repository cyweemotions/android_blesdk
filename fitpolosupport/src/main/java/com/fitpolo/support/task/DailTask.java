package com.fitpolo.support.task;

import static com.fitpolo.support.task.OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE;

import android.util.Log;

import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.DailControl;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.entity.StreamResType;
import com.fitpolo.support.entity.StreamType;
import com.fitpolo.support.task.authTask.SendBindCodeTask;
import com.fitpolo.support.utils.ByteType;
import com.fitpolo.support.utils.CRC16;
import com.fitpolo.support.utils.DigitalConver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DailTask extends OrderTask {
    private byte[] orderData;

    /*
    * 回调
    * 发送类型
    * 是否为多包
    * 发送的数据
    * */
    public DailTask(MokoOrderTaskCallback callback, int sendType,boolean isFirst, Boolean isMulti,List<Byte> sendData,int fileCount) {
        super(OrderType.XONFRAMEWRITE, OrderEnum.SYNC_DAIL, callback, RESPONSE_TYPE_WRITE_NO_RESPONSE);
        sendData = dataByControlType(sendType,isFirst,sendData,fileCount);
        Log.d("DailTask", "sendData: "+sendData.toString());
        ////组装数据
        List<Byte> dataList = new ArrayList<>();
        //消息头部
        byte header01 = (byte) 0xA5;
        byte header02 = (byte) 0xA3;
        dataList.add(header01);
        dataList.add(header02);
        //type
        List<Byte> typelength = DigitalConver.convert(isMulti?StreamType.xon_frame_type_multi.typeValue:StreamType.xon_frame_type_once.typeValue, ByteType.WORD);
        Collections.reverse(typelength);
        dataList.addAll(typelength);


        //3.设置内容长度
        List<Byte> dataLength = DigitalConver.convert(sendData.size(), ByteType.WORD);
        Collections.reverse(dataLength);
        dataList.addAll(dataLength);
        ///4.内容
        dataList.addAll(sendData);
        Log.d("DailTask", "dataList: "+dataList.toString());
        ///5.CRC校验
        byte[] crcSource = DigitalConver.listToArray(dataList);
        dataList.addAll(sendCrcValue(crcSource));

        List<Byte> res = (List<Byte>) sendCrcValue(DigitalConver.listToArray(dataList));
        Log.d("DailTask", "DailTask res: "+res.toString());
        orderData = DigitalConver.listToArray(dataList);;
        Log.d("DailTask", "orderData: "+DigitalConver.bytes2ListByte(orderData));
    }

    public DailTask(MokoOrderTaskCallback callback, boolean onlyXOF,int sendType, Boolean isMulti,List<Byte> sendData){
        super(OrderType.XONFRAMEWRITE, OrderEnum.SYNC_DAIL, callback, RESPONSE_TYPE_WRITE_NO_RESPONSE);
        this.sequence = System.currentTimeMillis();
        ////组装数据
        ////组装数据
        List<Byte> dataList = new ArrayList<>();
        //消息头部
        byte header01 = (byte) 0xA5;
        byte header02 = (byte) 0xA3;
        dataList.add(header01);
        dataList.add(header02);
        //type
        List<Byte> typelength = DigitalConver.convert(sendType, ByteType.WORD);
        Collections.reverse(typelength);
        dataList.addAll(typelength);


        //3.设置内容长度
        List<Byte> dataLength = DigitalConver.convert(sendData.size(), ByteType.WORD);
        Collections.reverse(dataLength);
        dataList.addAll(dataLength);
        ///4.内容
        dataList.addAll(sendData);
        Log.d("DailTask", "DailTask dataList: "+dataList.toString());
        ///5.CRC校验
        byte[] crcSource = DigitalConver.listToArray(dataList);
        dataList.addAll(sendCrcValue(crcSource));

        orderData = DigitalConver.listToArray(dataList);;
        Log.d("DailTask", "DailTask onlyXOF orderData: "+DigitalConver.bytes2ListByte(orderData));
    }
    /*
    * 发送类型
    * 发送数据
    * */
    public List<Byte> dataByControlType(int sendType,boolean isFrist,List<Byte> sendData,int fileCount){

        List<Byte> dataList = new ArrayList<>();
        if(isFrist){
            byte[] firstHeader = {0x00,0x00,0x00,0x00};
            dataList.addAll(DigitalConver.bytes2ListByte(firstHeader));
        }
        //消息头部
        byte header01 = (byte) 0x02;
        byte header02 = (byte) 0x00;
        dataList.add(header01);
        dataList.add(header02);

        List<Byte> typelength = DigitalConver.convert(sendType, ByteType.WORD);
        Collections.reverse(typelength);
        dataList.addAll(typelength);
        byte version = (byte) 0x00;
        dataList.add(version);
        dataList.add(version);
        //3.设置内容长度
        List<Byte> dataLength;
        if(isFrist){
            dataLength = DigitalConver.convert(fileCount, ByteType.DWORD);
        }else{
            dataLength = DigitalConver.convert(sendData.size(), ByteType.DWORD);
        }
        Collections.reverse(dataLength);
        dataList.addAll(dataLength);

        ///4.内容
        dataList.addAll(sendData);

        return dataList;
    }


    public Collection<? extends Byte> sendCrcValue(byte[] data){
        byte[] CRC = DigitalConver.intTo2Byte(CRC16.crc16(data));
        List<Byte> res = DigitalConver.bytes2ListByte(CRC);
        Log.d("TAG", "sendCrcValue: "+res.toString());
        return res;
    }

    //单包交互
    //多包交互

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        Log.d("", "parseValue: dailTask:"+value);
        super.parseValue(value);
        ///1.判断长度 长度最少为6
        if(value.length >= 6){
            ///2.数据长度验证
            boolean verfy = CrcVerify(value);
            if(verfy){
                ///获取类型
                byte[] subArray = Arrays.copyOfRange(value, 2, 3);
                DigitalConver.reverseArrayManually(subArray);
                int type = DigitalConver.byteArr2Int(subArray);
                ///长度
                byte[] lengthArray = Arrays.copyOfRange(value, 4, 5);
                DigitalConver.reverseArrayManually(lengthArray);
                int length = DigitalConver.byteArr2Int(lengthArray);
                byte[] datas = Arrays.copyOfRange(value, 6, 6+length);
                Log.d("TAG", "parseValue resType: "+type+"-"+length+"-"+datas);
                switch (type){
                    case 0:{ //xon_frame_type_ack
                        if(datas.length != 2) return;
                        DigitalConver.reverseArrayManually(datas);
                        //返回两个长度
                        int res = DigitalConver.byteArr2Int(datas);
                        response.responseObject = res;
                        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
                        callback.onOrderResult(response);
                        MokoSupport.getInstance().pollTask();
                        MokoSupport.getInstance().executeTask(callback);
                        Log.d("TAG", "parseValue: 返回几次");
                    }
                        break;
                    case 1:{ //xon_frame_type_mtu
                        if(datas.length != 2) return;
                        DigitalConver.reverseArrayManually(datas);
                        int mtu = DigitalConver.byteArr2Int(datas);
                        Log.d("xon_frame_type_mtu", "xon_frame_type_mtu: "+mtu);
                        ///发送MTU ack
                        sendAck(StreamType.xon_frame_type_ack.typeValue,StreamResType.xon_frame_ack_type_ok);
                    }
                        break;
                    case 2: { //xon_frame_type_once
//                        sendAck(StreamType.xon_frame_type_ack.typeValue,StreamResType.xon_frame_ack_type_ok);
                        List<Byte> res = DigitalConver.bytes2ListByte(datas);
                        Log.d("xon_frame_type_once", "xon_frame_type_once: "+res);
//                        response.responseObject = datas;
//                        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
//                        MokoSupport.getInstance().pollTask();
//                        callback.onOrderResult(response);
//                        MokoSupport.getInstance().executeTask(callback);
                    }
                        break;
                    case 3:{
                        //xon_frame_type_multi_info
                        if(datas.length == 8){

                        }else{

                        }
                    }
                        break;
                    case 4:{
                        //xon_frame_type_multi_crc

                    }
                        break;
                    case 5:{
                        //xon_frame_type_multi_ack
                        Log.d(String.valueOf(1), "xon_frame_type_multi_ack: ");
                        response.responseObject = datas;
                        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
                        callback.onOrderResult(response);
                        MokoSupport.getInstance().pollTask();
                        MokoSupport.getInstance().executeTask(callback);
                        Log.d("", "mQueue.size: "+ MokoSupport.getInstance().mQueue.size());

                    }
                        break;
                    case 6:{
                        //xon_frame_type_multi
                    }
                    break;
                    default:
                        break;
                }
            }else{
                Log.d("DailTask", "parseValue: crc verfy error");
            }
        }else{
            Log.d("DailTask", "parseValue: XON_Frame数据为空");
        }

    }

    ///解析CRC
    ///获取解析的crc
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


    ///发送ACK
    public void sendAck(int type, StreamResType ackType){
        List<Byte> dataList = new ArrayList<>();
        dataList.add((byte) ackType.typeValue);
        DailTask sendAckTask = new DailTask(callback, type,false,false,dataList,0);
        MokoSupport.getInstance().sendOrder(sendAckTask);
    }
}
