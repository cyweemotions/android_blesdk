package com.fitpolo.support.entity.dataEntity;

import com.fitpolo.support.MokoConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class BloodOxygenModel {
    public int bloodOxygen;// 心率
    public long datetime;// 时间
    public BloodOxygenModel(int bloodOxygen, int datetime) {
        this.bloodOxygen = bloodOxygen;
        this.datetime = datetime;
    }

    @Override
    public String toString() {
        return "BloodOxygenModel{" +
                "bloodOxygen=" + bloodOxygen +
                ", datetime=" + datetime +
                '}';
    }

    public static BloodOxygenModel StringTurnModel(String content){
        BloodOxygenModel model = new BloodOxygenModel(0, 0);
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
            model.bloodOxygen = Integer.parseInt(array.get(0));
        }
        return model;
    }
}
