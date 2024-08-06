package com.fitpolo.support.entity;

/**
 * @Date 2017/5/14 0014
 * @Author wenzheng.liu
 * @Description 久坐提醒
 * @ClassPath com.fitpolo.support.entity.SitAlert
 */
public class SitAlert {
    public int alertSwitch; // 久坐提醒开关 开0，关1
    public int startTime;// 开始时间 hour
    public int endTime;// 结束时间 hour
    public int interval;// 间隔时间 min

    @Override
    public String toString() {
        return "SitAlert{" +
                "alertSwitch=" + alertSwitch +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", interval='" + interval + '\'' +
                '}';
    }
}
