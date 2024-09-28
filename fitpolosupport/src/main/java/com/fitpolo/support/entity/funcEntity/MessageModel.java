package com.fitpolo.support.entity.funcEntity;

public class MessageModel {
    public int appType;// 通知app类型
    public String title;// 标题
    public String content;// 消息内容
    public int year;//年 2024 => 24
    public int month;//月
    public int day;//日
    public int hour;//时
    public int minute;//分
    public int second;//秒

    @Override
    public String toString() {
        return "MotionControl{" +
                "appType=" + appType +
                "title=" + title +
                ", content=" + content +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", hour=" + hour +
                ", minute=" + minute +
                ", second=" + second +
                '}';
    }
}
