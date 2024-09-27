package com.fitpolo.support.utils;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.entity.DailySleep;
import com.fitpolo.support.entity.DailyStep;
import com.fitpolo.support.entity.HeartRate;
import com.fitpolo.support.log.LogModule;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Date 2018/4/6
 * @Author wenzheng.liu
 * @Description 复杂数据解析类
 * @ClassPath com.fitpolo.support.utils.ComplexDataParse
 */
public class ComplexDataParse {
    //运动类型
    public static String sportType = "0";
    public static DailyStep parseDailyStep(byte[] value, int index) {
        // 日期
        Calendar calendar = Calendar.getInstance();
        calendar.set(2000 + DigitalConver.byte2Int(value[index]),
                DigitalConver.byte2Int(value[index + 1]) - 1,
                DigitalConver.byte2Int(value[index + 2]));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = calendar.getTime();
        String dateStr = sdf.format(date);
        // 步数
        byte[] step = new byte[4];
        System.arraycopy(value, index + 3, step, 0, 4);
        String stepStr = DigitalConver.byteArr2Str(step);

        // 时长
        byte[] duration = new byte[2];
        System.arraycopy(value, index + 7, duration, 0, 2);
        String durationStr = DigitalConver.byteArr2Str(duration);

        // 距离
        byte[] distance = new byte[2];
        System.arraycopy(value, index + 9, distance, 0, 2);
        String distanceStr = new DecimalFormat().format(DigitalConver.byteArr2Int(distance) * 0.1);
        // 卡路里
        byte[] calories = new byte[2];
        System.arraycopy(value, index + 11, calories, 0, 2);
        String caloriesStr = DigitalConver.byteArr2Str(calories);

        DailyStep dailyStep = new DailyStep();
        dailyStep.date = dateStr;
        dailyStep.count = stepStr;
        dailyStep.duration = durationStr;
        dailyStep.distance = distanceStr;
        dailyStep.calories = caloriesStr;
        LogModule.i(dailyStep.toString());
        return dailyStep;
    }


    public static DailyStep parseCurrentStep(byte[] value) {
        if (0xb5 != DigitalConver.byte2Int(value[0])
                || 0x04 != DigitalConver.byte2Int(value[1])
                || 0x0a != DigitalConver.byte2Int(value[2])) {
            return null;
        }
        // 日期
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = calendar.getTime();
        String dateStr = sdf.format(date);
        // 步数
        byte[] step = new byte[4];
        System.arraycopy(value, 3, step, 0, 4);
        String stepStr = DigitalConver.byteArr2Str(step);

        // 时长
        byte[] duration = new byte[2];
        System.arraycopy(value, 7, duration, 0, 2);
        String durationStr = DigitalConver.byteArr2Str(duration);

        // 距离
        byte[] distance = new byte[2];
        System.arraycopy(value, 9, distance, 0, 2);
        String distanceStr = new DecimalFormat().format(DigitalConver.byteArr2Int(distance) * 0.1);
        // 卡路里
        byte[] calories = new byte[2];
        System.arraycopy(value, 11, calories, 0, 2);
        String caloriesStr = DigitalConver.byteArr2Str(calories);

        DailyStep dailyStep = new DailyStep();
        dailyStep.date = dateStr;
        dailyStep.count = stepStr;
        dailyStep.duration = durationStr;
        dailyStep.distance = distanceStr;
        dailyStep.calories = caloriesStr;
        return dailyStep;
    }

    public static DailySleep parseDailySleepIndex(byte[] value, HashMap<Integer, DailySleep> sleepsMap, int index) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Calendar calendar = Calendar.getInstance();
        // 起始时间
        calendar.set(2000 + DigitalConver.byte2Int(value[index + 1]),
                DigitalConver.byte2Int(value[index + 2]) - 1,
                DigitalConver.byte2Int(value[index + 3]),
                DigitalConver.byte2Int(value[index + 4]),
                DigitalConver.byte2Int(value[index + 5]));
        Date startDate = calendar.getTime();
        String startDateStr = sdf.format(startDate);
        // 结束时间
        calendar.set(2000 + DigitalConver.byte2Int(value[index + 6]),
                DigitalConver.byte2Int(value[index + 7]) - 1,
                DigitalConver.byte2Int(value[index + 8]),
                DigitalConver.byte2Int(value[index + 9]),
                DigitalConver.byte2Int(value[index + 10]));
        Date endDate = calendar.getTime();
        String endDateStr = sdf.format(endDate);
        // 深睡
        byte[] deep = new byte[2];
        System.arraycopy(value, index + 11, deep, 0, 2);
        String deepStr = DigitalConver.byteArr2Str(deep);
        // 浅睡
        byte[] light = new byte[2];
        System.arraycopy(value, index + 13, light, 0, 2);
        String lightStr = DigitalConver.byteArr2Str(light);
        // 清醒
        byte[] awake = new byte[2];
        System.arraycopy(value, index + 15, awake, 0, 2);
        String awakeStr = DigitalConver.byteArr2Str(awake);

        // 记录睡眠日期
        String date = new SimpleDateFormat("yyy-MM-dd").format(endDate);

        // 构造睡眠数据
        DailySleep dailySleep = new DailySleep();
        dailySleep.date = date;
        dailySleep.startTime = startDateStr;
        dailySleep.endTime = endDateStr;
        dailySleep.deepDuration = deepStr;
        dailySleep.lightDuration = lightStr;
        dailySleep.awakeDuration = awakeStr;
        dailySleep.records = new ArrayList<>();
        LogModule.i(dailySleep.toString());
        // 暂存睡眠数据，以index为key，以实例为value，方便更新record;
        sleepsMap.put(DigitalConver.byte2Int(value[index]), dailySleep);
        return dailySleep;
    }

    public static void parseDailySleepRecord(byte[] value, HashMap<Integer, DailySleep> mSleepsMap, int index) {
        DailySleep dailySleep = mSleepsMap.get(DigitalConver.byte2Int(value[index]));
        if (dailySleep != null) {
            int len = DigitalConver.byte2Int(value[index + 2]);
            if (dailySleep.records == null) {
                dailySleep.records = new ArrayList<>();
            }
            for (int i = 0; i < len && index + 3 + i < value.length; i++) {
                String hex = DigitalConver.byte2HexString(value[index + 3 + i]);
                // 转换为二进制
                String binary = DigitalConver.hexString2binaryString(hex);
                for (int j = binary.length(); j > 0; ) {
                    j -= 2;
                    String status = binary.substring(j, j + 2);
                    dailySleep.records.add(status);
                }
            }
            LogModule.i(dailySleep.toString());
        }
    }

    public static void parseHeartRate(byte[] value, ArrayList<HeartRate> heartRates) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 3; i++) {
            byte year = value[i * 6 + 2];
            byte month = value[i * 6 + 3];
            byte day = value[i * 6 + 4];
            byte hour = value[i * 6 + 5];
            byte min = value[i * 6 + 6];
            byte heartRateValue = value[i * 6 + 7];
            if (DigitalConver.byte2Int(year) == 0) {
                continue;
            }
            calendar.set(2000 + DigitalConver.byte2Int(year),
                    DigitalConver.byte2Int(month) - 1,
                    DigitalConver.byte2Int(day),
                    DigitalConver.byte2Int(hour),
                    DigitalConver.byte2Int(min));
            Date time = calendar.getTime();
            String heartRateTime = sdf.format(time);
            String heartRateStr = DigitalConver.byte2Int(heartRateValue) + "";
            HeartRate heartRate = new HeartRate();
            heartRate.time = heartRateTime;
            heartRate.value = heartRateStr;
            LogModule.i(heartRate.toString());
            heartRates.add(heartRate);
        }
    }

    //运动中数据解析
    public static ArrayList<String> parseInMotionData(byte[] value) {
        ArrayList<String> sportData = new ArrayList<>();
        int dataLength = (value[4] & 0xFF);
        byte[] content = Arrays.copyOfRange(value, 5, dataLength + 5);
        byte[] result = Arrays.copyOfRange(content, 2, content.length);
        String resultHexStr = DigitalConver.bytesToHexString(result);
        assert resultHexStr != null;
        String resultStr = DigitalConver.hex2String(resultHexStr);
        System.out.println("运动中数据：------------"+sportType);
        System.out.println(resultStr);

        Pattern pattern = Pattern.compile("^\\d+");

        if(resultStr.startsWith(MokoConstants.SS)) {//运动开始
            resultStr = resultStr.replace(MokoConstants.SS, "");
            String[] fruits = resultStr.split(",");
            sportType = fruits[1].substring(0, 1);
            sportData.add("1"); // 状态——开始
            sportData.add(fruits[0]); // 时间 240927164631
            sportData.add(fruits[1].substring(0, 1)); // 运动类型 走跑类：0 1 2 3  骑行类： 5
        } else if(resultStr.startsWith(MokoConstants.SP)) {//运动进行
            resultStr = resultStr.replace(MokoConstants.SP, "");
            String[] fruits = resultStr.split(",");
            sportData.add("2"); // 状态——进行中
            if (Objects.equals(sportType, "5")) { //骑行
                System.out.println("运动中数据骑行：------------"+(sportType.equals("5")));
                sportData.add(fruits[0]); // 开始时间——171945 hhmmss
                sportData.add(fruits[3]); //实时心率 次/分
                sportData.add(fruits[5]); //实时速度 每公里用时
                sportData.add(fruits[7]); //平均心率 次/分
                sportData.add(fruits[9]); //距离 米
                sportData.add(fruits[10]); //卡路里 千卡
                sportData.add(fruits[12]); //平均速度 每公里用时
                String lastValue = fruits[13].replaceAll("[^\\d]", "");
                sportData.add(lastValue); //时间 秒
            } else {
                System.out.println("运动中数据跑步：------------"+(sportType.equals("5")));
                sportData.add(fruits[0]); // 开始时间——171945 hhmmss
                sportData.add(fruits[1]); //步频 步/分
                sportData.add(fruits[2]); //实时速度 秒 每公里用时
                sportData.add(fruits[3]); //实时心率 次/分
                sportData.add(fruits[7]); //平均心率 次/分
                sportData.add(fruits[8]); //平均速度 秒 每公里用时
                sportData.add(fruits[9]); //距离 米
                sportData.add(fruits[10]); //卡路里 千卡
                sportData.add(fruits[11]); //步数
                String lastValue = fruits[12].replaceAll("[^\\d]", "");
                sportData.add(lastValue); //时间 秒
            }
        } else if(resultStr.startsWith(MokoConstants.PAUSE)) { //运动暂停
            resultStr = resultStr.replace(MokoConstants.PAUSE, "");
            String[] fruits = resultStr.split(",");
            sportData.add("3"); // 状态——暂停
            String lastValue = fruits[0].replaceAll("[^\\d]", "");
            sportData.add(lastValue); // 开始时间——171945 hhmmss
        } else if(resultStr.startsWith(MokoConstants.RESUME)) { //运动继续
            resultStr = resultStr.replace(MokoConstants.RESUME, "");
            String[] fruits = resultStr.split(",");
            sportData.add("4"); // 状态——继续
            String lastValue = fruits[0].replaceAll("[^\\d]", "");
            sportData.add(lastValue); // 开始时间——171945 hhmmss
        } else if(resultStr.startsWith(MokoConstants.SE)) {  //运动结束
            resultStr = resultStr.replace(MokoConstants.SE, "");
            String[] fruits = resultStr.split(",");
            sportData.add("5"); // 状态——结束
            sportData.add(fruits[0]); // 开始时间— yyMMddHHmmss —240927164832
            sportData.add(fruits[1]); // 结束时间— YYMMddHHmmss —240927165058
            sportData.add(fruits[2]); // 运动类型
            if (Objects.equals(sportType, "5")) { //骑行
                sportData.add(fruits[4]); //距离
                sportData.add(fruits[5]); //卡路里
                sportData.add(fruits[14]); ////最低心率
                sportData.add(fruits[15]); //心率区间——热身 例：2, 1, 9, 0, 0, 17%
                sportData.add(fruits[16]); //心率区间——燃脂 8%
                sportData.add(fruits[17]); //心率区间——有氧 75%
                sportData.add(fruits[18]); //心率区间——无氧 0%
                sportData.add(fruits[19]); //心率区间——极限 0%
                sportData.add(fruits[20]); //平均心率
                sportData.add(fruits[21]); //最大心率
                sportData.add(fruits[22]); //最低心率
                sportData.add(fruits[24]); //时长——分钟
                sportData.add(fruits[33]); //
                sportData.add(fruits[34]); //平均配速
                sportData.add(fruits[35]); //
                sportData.add(fruits[36]); //
                sportData.add(fruits[37]); //
                sportData.add(fruits[48]); //
                sportData.add(fruits[49]); //
                sportData.add(fruits[50]); //
                sportData.add(fruits[51]); //
                String lastValue = fruits[52].replaceAll("[^\\d]", "");
                sportData.add(lastValue); //
            } else {
                sportData.add(fruits[3]); //
                sportData.add(fruits[4]); //距离
                sportData.add(fruits[5]); //卡路里
                sportData.add(fruits[6]); //
                sportData.add(fruits[7]); //平均步频
                sportData.add(fruits[8]); //最大步频
                sportData.add(fruits[9]); //
                sportData.add(fruits[10]); //平均步长
                sportData.add(fruits[11]); //
                sportData.add(fruits[12]); //平均配速
                sportData.add(fruits[13]); //最大配速
                sportData.add(fruits[14]); //最低心率
                sportData.add(fruits[15]); //心率区间——热身 例：2, 1, 9, 0, 0, 17%
                sportData.add(fruits[16]); //心率区间——燃脂 8%
                sportData.add(fruits[17]); //心率区间——有氧 75%
                sportData.add(fruits[18]); //心率区间——无氧 0%
                sportData.add(fruits[19]); //心率区间——极限 0%
                sportData.add(fruits[20]); //平均心率
                sportData.add(fruits[21]); //最大心率
                sportData.add(fruits[22]); //最小心率
                sportData.add(fruits[23]); //最大摄氧量
                sportData.add(fruits[24]); //时长——分钟
                sportData.add(fruits[48]); //
                sportData.add(fruits[49]); //
                sportData.add(fruits[50]); //
                sportData.add(fruits[51]); //
                String lastValue = fruits[52].replaceAll("[^\\d]", "");
                sportData.add(lastValue); //
            }
        } else {//运动无数据结束
            resultStr = resultStr.replace(MokoConstants.NODATA, "");
            String[] fruits = resultStr.split(",");
            sportData.add("6"); // 状态——无数据结束
            sportData.add(fruits[0]); // 240927164832
            sportData.add(fruits[1]); // 240927165058
            String lastValue = fruits[2].replaceAll("[^\\d]", "");
            sportData.add(lastValue); // 运动类型
        }
        return sportData;
    }
}
