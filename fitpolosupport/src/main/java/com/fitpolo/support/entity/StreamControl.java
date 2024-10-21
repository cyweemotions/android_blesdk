package com.fitpolo.support.entity;

public enum StreamControl {

    DIAL_FILE_START("DIAL_FILE_START","获取文件个数", 0),
    DIAL_FILE_INFO("DIAL_FILE_INFO","获取文件", 1),
    DIAL_FILE_SEND("DIAL_FILE_SEND","删除文件", 2),
    DIAL_FILE_STOP("DIAL_FILE_STOP","回应文件个数", 3),
    DIAL_REC_RESULT("DIAL_REC_RESULT","上传文件", 4),
    DIAL_FORCE_STOP("DIAL_FORCE_STOP","无法上传文件", 5),
    ;

    StreamControl(String dialForceStop, String 无法上传文件, int i) {
    }
}
