package com.fitpolo.support.entity.dataEntity;

import com.fitpolo.support.utils.ByteType;
import com.fitpolo.support.utils.DigitalConver;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PressureModel {
    public long id;
    public int year;
    public int month;
    public int day;
    public int relax; //放松
    public int normal; //正常
    public int strain; //紧张
    public int anxiety; //焦虑
    public int highest; // 最高
    public int minimum; // 最低
    public int lately; //最近
//    public String pressTime;
//    public String rawData;

    @Override
    public String toString() {
        return "PressureModel{" +
                "id=" + id +
                ", year =" + year +
                ", month =" + month +
                ", day =" + day +
                ", relax =" + relax +
                ", normal =" + normal +
                ", strain =" + strain +
                ", anxiety =" + anxiety +
                ", highest =" + highest +
                ", minimum =" + minimum +
                ", lately =" + lately +
                '}';
    }

    private static int turnList(List<Byte> source) {
        ByteBuffer buffer = ByteBuffer.wrap(DigitalConver.listByte2bytes(source));
        buffer.order(ByteOrder.BIG_ENDIAN); // 设置字节顺序（大端），如果字节数组是小端格式，使用 ByteOrder.LITTLE_ENDIAN

        int number = buffer.getInt();
        return number;
    }
    public static PressureModel ListConvertPress (List<Byte> source, int year, int month, int day){
        if(source.isEmpty() || source.size() < 30){
            return null;
        }
        PressureModel model = new PressureModel();
//        Calendar calendar = Calendar.getInstance();
//        long timeStamp = System.currentTimeMillis()/1000;// 创建 Calendar 实例

        Calendar calendar = Calendar.getInstance();
        // 设置具体的年月日
        calendar.set(year, month-1, day, 0, 0, 0); // 月份从0开始
        calendar.set(Calendar.MILLISECOND, 0);
        // 获取时间戳（以秒为单位）
        long timestamp = calendar.getTimeInMillis() / 1000; // 转换为秒

//        int year = calendar.get(Calendar.YEAR);
//        int month = calendar.get(Calendar.MONTH) + 1;
//        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int relax = (int) source.get(0);
        int normal = (int) source.get(1);
        int strain = (int) source.get(2);
        int anxiety = (int) source.get(3);
        int highest = (int) source.get(4);
        int minimum = (int) source.get(5);
        int lately = (int) source.get(6);
        List<Byte> times = source.subList(7,31);
//        List<String> timeList = new ArrayList<>();
//        for (byte str : times) {
//            timeList.add(String.valueOf((int) str));
//        }
        List<String> sourceList = new ArrayList<>();
        for (byte str : source) {
            sourceList.add(String.valueOf((int) str));
        }
//        String pressTime = DigitalConver.join(",", timeList);
//        String rawData = pressTime + "," + DigitalConver.join(",", sourceList);

        model.id = timestamp;
        model.year = year;
        model.month = month;
        model.day = day;
        model.relax = relax;
        model.normal = normal;
        model.strain = strain;
        model.anxiety = anxiety;
        model.highest = highest;
        model.minimum = minimum;
        model.lately = lately;
//        model.pressTime = pressTime;
//        model.rawData = rawData;
        return model;
    }
}
