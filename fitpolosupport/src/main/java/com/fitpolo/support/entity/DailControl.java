package com.fitpolo.support.entity;

public enum DailControl {

    DIAL_FILE_START("发送文件首包", 1),
    DIAL_FILE_INFO("发送文件", 2),
    DIAL_FILE_SEND("发送文件结束", 3),
    DIAL_FILE_STOP("文件结束", 4),
    DIAL_REC_RESULT("接受结果", 5),
    DIAL_FORCE_STOP("强制停止",6),
    ;

     public String name;
     public  int value;

    DailControl(String dailSendFileInfo, int i) {
        this.name = dailSendFileInfo;
        this.value = i;
    }
}
