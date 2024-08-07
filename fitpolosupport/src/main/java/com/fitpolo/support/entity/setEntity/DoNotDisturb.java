package com.fitpolo.support.entity.setEntity;

public class DoNotDisturb {
    public int allToggle;// 全天勿扰开关 0–开 1–关
    public int partToggle; // 时段勿扰开关 0-开 1-关
    public int startTime;// 勿扰模式时间段起始，单位分钟 23*60
    public int endTime;// 勿扰模式时间段结束，单位分钟 8*60

    @Override
    public String toString() {
        return "DoNotDisturb{" +
            "allToggle=" + allToggle +
            ", partToggle=" + partToggle +
            ", startTime=" + startTime +
            ", endTime=" + endTime +
            '}';
    }
}
