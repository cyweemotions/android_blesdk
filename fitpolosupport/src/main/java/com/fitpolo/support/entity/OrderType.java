package com.fitpolo.support.entity;

import java.io.Serializable;

public enum OrderType implements Serializable {
    NOTIFY("NOTIFY", "e49a24c1-f69a-11e8-8eb2-f2801f1b9fd1"),
    WRITE("WRITE", "e49a24c1-f69a-11e8-8eb2-f2801f1b9fd1"),
    DataPushNOTIFY("DataPushNOTIFY", "e49a26c1-f69a-11e8-8eb2-f2801f1b9fd1"),
    DataPushWRITE("DataPushWRITE", "e49a26c1-f69a-11e8-8eb2-f2801f1b9fd1"),
    XONFRAMENOTIFY("XONFRAMENOTIFY", "00009529-0000-1000-8000-00805f9b34fb"),
    XONFRAMEWRITE("XONFRAMEWRITE", "00009528-0000-1000-8000-00805f9b34fb"),
    ;


    private String uuid;
    private String name;

    OrderType(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }
}
