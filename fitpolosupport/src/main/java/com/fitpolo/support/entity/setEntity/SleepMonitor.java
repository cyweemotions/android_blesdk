package com.fitpolo.support.entity.setEntity;

public class SleepMonitor {
    public int heighPrecisionDetec = 0; // 睡眠高精度检测 0-开 1-关
    public int breatheQualityDetec = 0; // 睡眠呼吸质量检测 0-开 1-关

    @Override
    public String toString() {
        return "SleepMonitor{" +
                "heighPrecisionDetec=" + heighPrecisionDetec +
                ", breatheQualityDetec=" + breatheQualityDetec +
                '}';
    }
}
