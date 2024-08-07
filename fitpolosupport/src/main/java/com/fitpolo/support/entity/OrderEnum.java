package com.fitpolo.support.entity;

import java.io.Serializable;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 命令枚举
 * @ClassPath com.fitpolo.support.entity.OrderEnum
 */
public enum OrderEnum implements Serializable {
    getInnerVersion("获取内部版本", 0x09),
    setSystemTime("设置手环时间", 0x11),
    setUnitType("设置单位类型", 0x23),
    setLastScreen("设置最后显示", 0x27),
    setFunctionDisplay("设置功能显示", 0x19),
    getFirmwareVersion("获取固件版本", 0x90),
    getSleepHeartCount("获取睡眠和心率总数", 0x12),
    getAllSteps("获取记步数据", 0x92),
    getAllSleepIndex("获取睡眠index", 0x93),
    getAllSleepRecord("获取睡眠record", 0x94),
    getAllHeartRate("获取心率数据", 0x18),
    getLastestSteps("获取未同步的记步数据", 0x92),
    getLastestSleepIndex("获取未同步的睡眠记录数据", 0x93),
    getLastestSleepRecord("获取未同步的睡眠详情数据", 0x94),
    getLastestHeartRate("获取未同步的心率数据", 0xA8),
    getCRCVerifyResult("CRC校验", 0x28),
    getFirmwareParam("获取硬件参数", 0xA5),
    setShakeBand("设置手环震动", 0),
    setPhoneComingShake("设置来电震动", 0),
    setSmsComingShake("设置短信震动", 0),
    setFacebookNotify("设置facebook震动", 0),
    setQQNotify("设置QQ震动", 0),
    setSkypeNotify("设置Skype震动", 0),
    setTwitterNotify("设置Twitter震动", 0),
    setWhatsAppNotify("设置WhatsApp震动", 0),
    setWechatNotify("设置微信震动", 0),
    setSnapchatNotify("设置Snapchat震动", 0),
    setLineNotify("设置Line震动", 0),
    openNotify("打开设备通知", 0),


    /********************* 功能类型 *********************/
    syncTime("时间校准", 0x01),
    setSync("设置同步", 0x02),
    getBattery("获取电量", 0x03),
    findDevice("查找设备", 0x04),
    findPhone("查找手机", 0x05),
    unbindDevice("解绑设备", 0x06),
    languageSupport("语言支持", 0x07),
    deviceInfo("设备信息", 0x08),
    remotePhoto("远程拍照", 0x09),
    messageNotify("消息通知", 0x0a),
    positionGPS("定位GPS", 0x0b),
    motionControl("运动控制", 0x0c),
    queryInfo("查询信息", 0xFF),

    /********************* 设置类型 *********************/
    setUserInfo("设置用户信息", 0x01),
    setTarget("目标设置", 0x02),
    setTimeFormat("时间设置", 0x03),
    setSleep("睡眠设置", 0x04),
    setSitLongTimeAlert("设置久坐提醒", 0x05),
    setAutoLigten("设置抬手亮屏", 0x06),
    setHeartRateMonitor("设置心率", 0x07),
    setAlarmClock("设置闹钟数据", 0x08),
    setCallReminder("来电提醒", 0x09),
    setMotionTarget("运动目标设置", 0x0a),
    setStandardAlert("达标提醒", 0x0b),
    setLanguage("语言设置", 0x0c),
    setNotify("通知设置", 0x0d),
    setOnScreenDuration("亮屏时长", 0x0e),
    setDoNotDisturb("勿扰设置", 0x0f),
    setPowerSaveMode("省电模式", 0x10),
    setAddressBook("通讯录设置", 0x11),
    setSleepMonitor("睡眠算法设置", 0x13),



    READ_ALARMS("读取闹钟", 0x01),
    READ_SIT_ALERT("读取久坐提醒", 0x02),
    READ_SETTING("读取设置参数", 0x04),
    ;


    private String orderName;
    private int orderHeader;

    OrderEnum(String orderName, int orderHeader) {
        this.orderName = orderName;
        this.orderHeader = orderHeader;
    }

    public int getOrderHeader() {
        return orderHeader;
    }

    public String getOrderName() {
        return orderName;
    }
}
