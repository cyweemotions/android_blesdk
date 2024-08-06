package com.fitpolo.support.entity.setEntity;

/**
 * 手环闹钟
 */
public class AlarmClock {
    public int action;// 0–添加 1–删除 2–修改 如果action是1则不需要后续字段
    public int index; //闹钟编号 0-2
    public int toggle;// 闹钟状态 0-打开 1-关闭
    public int mode;// 0-震动 1-声音 2-声音加震动
    public int time;// 闹钟时间 单位分钟
    public int repeat;// 0-单次 1-active days
    public int activeDay;// 周一到周日的掩码
    // 0x01 --  周日
    // 0x02 --  周一
    // 0x04 --  周二
    // 0x08 --  周三
    // 0x10 --  周四
    // 0x20 --  周五
    // 0x40 --  周六

    @Override
    public String toString() {
        return "AlarmClock{" +
            "action=" + action +
            ", index=" + index +
            ", toggle=" + toggle +
            ", mode=" + mode +
            ", time=" + time +
            ", repeat=" + repeat +
            ", activeDay=" + activeDay +
            '}';
    }
}
