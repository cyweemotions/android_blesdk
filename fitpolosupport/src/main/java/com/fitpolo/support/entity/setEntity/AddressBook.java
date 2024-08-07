package com.fitpolo.support.entity.setEntity;

public class AddressBook {
    public int action;// 0-添加 1-删除
    public String name; // 名字
    public String phoneNumber;// 电话号

    @Override
    public String toString() {
        return "AddressBook{" +
                "action=" + action +
                ", name=" + name +
                ", phoneNumber=" + phoneNumber +
                '}';
    }
}
