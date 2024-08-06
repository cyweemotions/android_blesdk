package com.fitpolo.support.entity.setEntity;

import com.google.gson.Gson;

public class HeartRateMonitor {
    public int monitorSwitch;// 监控开关
    public int interval;// 监控间隔时间/min
    public int alarmSwitch;// 报警开关
    public int minLimit;// 低心率限制
    public int maxLimit;// 高心率限制

    @Override
    public String toString() {
        return "HeartRateMonitor{" +
                "monitorSwitch=" + monitorSwitch +
                ", interval=" + interval +
                ", alarmSwitch=" + alarmSwitch +
                ", minLimit=" + minLimit +
                ", maxLimit=" + maxLimit +
                '}';
    }
    public String toJson() {
//        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
