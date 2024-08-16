package com.fitpolo.support.entity.dataEntity;

import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.utils.DigitalConver;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class PaiModel {
    public long id;// 时间戳
    public int year;// 年
    public int month;// 月
    public int day;// 天
    public int pai;// 值
    public int totals;// 总数
    public int low;// 最低强度
    public int lowMins;// 最低强度持续时间
    public int medium;// 中强度
    public int mediumMins;// 中强度持续时间
    public int high; // 高强度
    public int highMins;// 最高强度持续时间

    @Override
    public String toString() {
        return "PaiModel{" +
                "id=" + id +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", pai=" + pai +
                ", totals=" + totals +
                ", low=" + low +
                ", lowMins=" + lowMins +
                ", medium=" + medium +
                ", mediumMins=" + mediumMins +
                ", high=" + high +
                ", highMins=" + highMins +
                '}';
    }
    private static int turnList(List<Byte> source) {
        ByteBuffer buffer = ByteBuffer.wrap(DigitalConver.listByte2bytes(source));
        buffer.order(ByteOrder.BIG_ENDIAN); // 设置字节顺序（大端），如果字节数组是小端格式，使用 ByteOrder.LITTLE_ENDIAN

        int number = buffer.getInt();
        return number;
    }

    public static PaiModel ListCurrentPAi(List<Byte> source){
        LogModule.i("这是===ListCurrentPAi.size()===="+source.size());
        if(source.isEmpty() || source.size() < 28){
            return null;
        }
        PaiModel model = new PaiModel();

        long timeStamp = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // 月份从0开始
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int totals = (int) Math.floor((double) turnList(source.subList(0, 4)) / 1000) > 200 ? 0 : (int) Math.floor((double) turnList(source.subList(0, 4)) / 1000);
        int pai = (int) Math.floor((double) turnList(source.subList(4, 8)) / 1000) > 100 ? 0 : (int) Math.floor((double) turnList(source.subList(4, 8)) / 1000);
        int low = (int) Math.floor((double) turnList(source.subList(8,12)) / 1000) > 100 ? 0 : (int) Math.floor((double) turnList(source.subList(8,12)) / 1000);
        int medium = (int) Math.floor((double) turnList(source.subList(12,16)) / 1000) > 100 ? 0 : (int) Math.floor((double) turnList(source.subList(12,16)) / 1000);
        int high = (int) Math.floor((double) turnList(source.subList(16, 20)) / 1000) > 100 ? 0 : (int) Math.floor((double) turnList(source.subList(16, 20)) / 1000);
        int lowMins = turnList(source.subList(20,24));
        int mediumMins = turnList(source.subList(24,28));
        int highMins = turnList(source.subList(28,32));
        model.id = timeStamp;
        model.year = year;
        model.month = month;
        model.day = day;
        model.totals = totals;
        model.pai = pai;
        model.low = low;
        model.medium = medium;
        model.high = high;
        model.lowMins = lowMins;
        model.mediumMins = mediumMins;
        model.highMins = highMins;

        return model;
    }
    public static PaiModel ListConvertPAi(List<Byte> source){
        LogModule.i("这是===source.size()===="+source.size());
        if(source.isEmpty() || source.size() < 36){
            return null;
        }
        PaiModel model = new PaiModel();

        int date = turnList(source.subList(0,4));
        long timeStamp = DigitalConver.formatTimeStamp(String.valueOf(date),"yyyyMMdd");
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTimeInMillis(timeStamp * 1000);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // 月份从0开始
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int totals = (int) Math.floor((double) turnList(source.subList(4, 8)) / 1000);
        int pai = (int) Math.floor((double) turnList(source.subList(8, 12)) / 1000);
        int low = (int) Math.floor((double) turnList(source.subList(12,16)) / 1000);
        int medium = (int) Math.floor((double) turnList(source.subList(16,20)) / 1000);
        int high = (int) Math.floor((double) turnList(source.subList(20, 24)) / 1000);
        int lowMins = turnList(source.subList(24,28));
        int mediumMins = turnList(source.subList(28,32));
        int highMins = turnList(source.subList(32,36));
        model.id = timeStamp;
        model.year = year;
        model.month = month;
        model.day = day;
        model.totals = totals;
        model.pai = pai;
        model.low = low;
        model.medium = medium;
        model.high = high;
        model.lowMins = lowMins;
        model.mediumMins = mediumMins;
        model.highMins = highMins;

        return model;
    }
}
