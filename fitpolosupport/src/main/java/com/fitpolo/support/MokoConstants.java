package com.fitpolo.support;

/**
 * @Date 2017/5/10
 * @Author wenzheng.liu
 * @Description 蓝牙常量
 */
public class MokoConstants {
    // 读取发送头
    public static final int HEADER_READ_SEND = 0xFF;
    // 读取接收头
    public static final int HEADER_READ_GET = 0xB1;
    // 设置发送头
    public static final int HEADER_WRITE_SEND = 0xB2;
    // 设置接收头
    public static final int HEADER_WRITE_GET = 0xB3;
    // 记步发送头
    public static final int HEADER_SETP_SEND = 0xB4;
    // 记步接收头
    public static final int HEADER_STEP_GET = 0xB5;
    // 心率发送头
    public static final int HEADER_HEARTRATE_SEND = 0xB6;
    // 心率接收头
    public static final int HEADER_HEARTRATE_GET = 0xB7;

    ///蓝牙设置SETTING
     public static final int Setting = 0x00;
    ///蓝牙获取设置SETTING
     public static final int GetSetting = 0x01;
    ///蓝牙功能设置
     public static final int Function = 0x02;
    ///数据交互类型
     public static final int DataNotify = 0x03;
    ///设备鉴权
     public static final int DeviceAuth = 0x04;
    ///数据交互类型
     public static final int DailPush = 0x05;
     public static final String century = "20";

     public static final String SS = "[1]"; //运动开始
     public static final String SE = "[5]"; //运动结束
     public static final String SP = "[2]"; //运动进行
    // 发现状态
    public static final String ACTION_DISCOVER_SUCCESS = "com.moko.fitpolodemo.ACTION_DISCOVER_SUCCESS";
    public static final String ACTION_DISCOVER_TIMEOUT = "com.moko.fitpolodemo.ACTION_DISCOVER_TIMEOUT";
    // 断开连接
    public static final String ACTION_CONN_STATUS_DISCONNECTED = "com.moko.fitpolodemo.ACTION_CONN_STATUS_DISCONNECTED";
    // 命令结果
    public static final String ACTION_ORDER_RESULT = "com.moko.fitpolodemo.ACTION_ORDER_RESULT";
    public static final String ACTION_ORDER_TIMEOUT = "com.moko.fitpolodemo.ACTION_ORDER_TIMEOUT";
    public static final String ACTION_ORDER_FINISH = "com.moko.fitpolodemo.ACTION_ORDER_FINISH";
    public static final String ACTION_CURRENT_DATA = "com.moko.fitpolodemo.ACTION_CURRENT_DATA";

    // extra_key
    public static final String EXTRA_KEY_RESPONSE_ORDER_TASK = "EXTRA_KEY_RESPONSE_ORDER_TASK";
    public static final String EXTRA_KEY_CURRENT_DATA_TYPE = "EXTRA_KEY_CURRENT_DATA_TYPE";


    public static String commonPName = "";
    public static String facebookPName = "com.android.ftpeasy";
    public static String instagramPName = "com.instagram.android";
    public static String kakaotalkPName = "com.kakao.talk";
    public static String LinePName = "cjp.naver.line.android";
    public static String LINKEDINPName = "com.linkedin.android";
    public static String MESSAGERPName = "com.facebook.orca";
    public static String QQPName = "com.tencent.mobileqq";
    public static String TWITTERPName = "com.twitter.android";
    public static String VIBERPName = "com.viber.voip";
    public static String VKONTAKETPName = "com.vkontakte.android";
    public static String WHATSAPPPName = "com.whatsapp.w4b";
    public static String wechatPName = "com.tencent.mm";
    public static String mms = "com.android.mms";
}
