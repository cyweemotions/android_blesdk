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

public class SleepModel {
    public int type;//睡眠类型 类型 0 睡眠,1 小睡
    public int state;//睡眠状态    SLEEP_SHOLLOW = 1,SLEEP_DEEP = 2,SLEEP_AWAKE = 3, SLEEP_REM = 12, 睡眠开始——0， 夜间睡眠结束——4，小睡结束——14
    public long datetime;// 时间
    public int slice;// 小睡时间
    public SleepModel(int type, int state, long datetime, int slice) {
        this.type = type;
        this.state = state;
        this.datetime = datetime;
        this.slice = slice;
    }

    @Override
    public String toString() {
        return "SleepModel{" +
                "type=" + type +
                ", state=" + state +
                ", datetime=" + datetime +
                ", slice=" + slice +
                '}';
    }

    public static SleepModel StringTurnModel(String content, int type){
        SleepModel model = new SleepModel(0, 0, 0, 0);
        if(content.contains(",")){
            List<String> array = Arrays.asList(content.split(","));
            model.type = type;
            model.state = Integer.parseInt(array.get(0));
            if(array.size() >=3){
                model.slice = Integer.parseInt(array.get(2));
            }
            Calendar calendar = Calendar.getInstance();
            String dateTemp = array.get(1);
            if(array.get(1).length() == 8) {
                dateTemp = array.get(1) + "00";
            }
            String dateStr = calendar.get(Calendar.YEAR) + dateTemp;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // 设置时区为UTC
            long timestamp = 0;
            try {
                Date date = sdf.parse(dateStr);
                calendar.setTime(date);
                calendar.add(Calendar.HOUR, -8); // 减去8小时
                Date newDate = calendar.getTime();
                timestamp = newDate.getTime() / 1000; // 转换为秒
//                System.out.println("Timestamp: " + timestamp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            model.datetime = timestamp;
        }
        return model;
    }
}
