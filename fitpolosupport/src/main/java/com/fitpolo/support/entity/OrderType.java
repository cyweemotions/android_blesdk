package com.fitpolo.support.entity;

import java.io.Serializable;

public enum OrderType implements Serializable {
    NOTIFY("NOTIFY", "e49a24c1-f69a-11e8-8eb2-f2801f1b9fd1"),
    WRITE("WRITE", "e49a24c1-f69a-11e8-8eb2-f2801f1b9fd1"),
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
