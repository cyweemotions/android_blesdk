package com.fitpolo.support.entity.setEntity;

import com.google.gson.Gson;

public class MotionTarget {
    public int setType;//类型：0- 目标设置 1-自动暂停设置
    public int sportType;//0-户外步行 1-户外跑步 2-户外骑行 3-室内步行
    public int distance = 0;// 距离 单位公里 区间0-99
    public int sportTime = 0;// 时间 单位分 区间0-23 实际值*10+5
    public int calorie = 0;// 卡路里 单位千卡 区间0-9 实际值*100
    public int targetType = 0;// 目标设置类型 0-none 1-distance 2-sportTime 3-calorie
    public int autoPauseSwitch = 0;// 自动暂停开关 当setType为1的情况下设置

    @Override
    public String toString() {
        return "MotionTarget{" +
            "setType=" + setType +
            ", sportType=" + sportType +
            ", distance=" + distance +
            ", sportTime=" + sportTime +
            ", calorie=" + calorie +
            ", targetType=" + targetType +
            ", autoPauseSwitch=" + autoPauseSwitch +
            '}';
    }
}
