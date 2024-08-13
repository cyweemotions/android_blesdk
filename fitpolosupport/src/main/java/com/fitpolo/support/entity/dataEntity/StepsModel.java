package com.fitpolo.support.entity.dataEntity;

import com.fitpolo.support.MokoConstants;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.text.ParseException;
import java.util.Date;
public class StepsModel {
    public int step;// 步数
    public int distance;// 距离
    public int calorie;// 卡路里
    public long datetime;// 时间

    public StepsModel(int step, int distance, int calorie, long datetime) {
        this.step = step;
        this.distance = distance;
        this.calorie = calorie;
        this.datetime = datetime;
    }

    @Override
    public String toString() {
        return "Steps{" +
            "step=" + step +
            ", distance=" + distance +
            ", calorie=" + calorie +
            ", datetime=" + datetime +
            '}';
    }

    public static StepsModel StringTurnModel(String content){
        StepsModel model = new StepsModel(0, 0,0,0);
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
            model.step = Integer.parseInt(array.get(0));
            model.distance = Integer.parseInt(array.get(1));
            model.calorie = Integer.parseInt(array.get(2));
        }
        return model;
    }
}
