package com.fitpolo.support.entity.dataEntity;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.log.LogModule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class HeartRateModel {
    public int heartRate;// 心率
    public long datetime;// 时间
    public HeartRateModel(int heartRate, int datetime) {
        this.heartRate = heartRate;
        this.datetime = datetime;
    }

    @Override
    public String toString() {
        return "HeartRate{" +
                "heartRate=" + heartRate +
                ", datetime=" + datetime +
                '}';
    }

    public static HeartRateModel StringTurnModel(String content){
        HeartRateModel model = new HeartRateModel(0, 0);
        if(content.contains(",")){
            List<String> array = Arrays.asList(content.split(","));
            String dateStr = MokoConstants.century + array.get(array.size()-1).toString();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // 设置时区为UTC
            long timestamp = 0;
            try {
                Date date = sdf.parse(dateStr);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.HOUR, -8); // 减去8小时
                Date newDate = calendar.getTime();
                timestamp = newDate.getTime() / 1000; // 转换为秒
//                System.out.println("Timestamp: " + timestamp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            model.datetime = timestamp;
            model.heartRate = Integer.parseInt(array.get(0));
        }
        return model;
    }
}
