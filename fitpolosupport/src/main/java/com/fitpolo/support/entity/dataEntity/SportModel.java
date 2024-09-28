package com.fitpolo.support.entity.dataEntity;

import com.fitpolo.support.log.LogModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SportModel {
    public long date; //运动结束时间
    public long start; //运动开始时间
    public int total_steps; //步数
    public float total_distance; //距离（米）
    public float total_calories; //卡路里
    public float avg_step_freq; //平均步频
    public float avg_step_len;//平均步长
    public float avg_pace; //平均速度
    public float max_step_freq; //最大步频
    public float max_pace; //最大速度
    public float avg_heart;//平均心率
    public int hr_zone1;//心率区间-热身
    public int hr_zone2;//心率区间-燃脂
    public int hr_zone3;//心率区间-有氧运动
    public int hr_zone4;//心率区间-无氧运动
    public int hr_zone5;//心率区间-极限
    public int max_heart;//最大心率
    public int min_heart;//最小心率
    public float vo2max; //最大摄氧量
    public int training_time; //训练时间
    public int sportType; //运动类型
    public int second;//运动时间(s)
    public List<Integer> step_freqs = new ArrayList<>();//步频数组
    public List<Integer> speeds = new ArrayList<>(); //速度数组
    public List<Integer> hr_values = new ArrayList<>();//心率数组
    public List<String> gpsData = new ArrayList<>();//GPS
    public List<Integer> jumpfreqs = new ArrayList<>();//跳频数组
    public int jumpfreq;//跳频
    public int jumpCount;//跳次
    public int strokeCount; //游泳-划水次数
    public int strokeLaps; //游泳-趟次
    public int strokeFreq; //游泳-划频
    public int strokeAvgFreq;//游泳-平均划频
    public int strokeAvgSwolf;//游泳-平均Swolf
    public int strokeMaxFreq;//游泳-最大划频
    public int strokeBestSwolf;//游泳-最佳Swolf
    public int strokeType;//游泳-泳姿
    public List<Integer> swimfreqs = new ArrayList<>(); //游泳频率
    public List<Integer> bikeSpeed = new ArrayList<>(); //骑行速度
    public String RawData = ""; //原始数据
    public String extentsion = ""; //扩展字段

    public SportModel() {}

    @Override
    public String toString() {
        return "SportModel{" +
                "date=" + date +
                ", start=" + start +
                ", total_steps=" + total_steps +
                ", total_distance=" + total_distance +
                ", total_calories=" + total_calories +
                ", avg_step_freq=" + avg_step_freq +
                ", avg_step_len=" + avg_step_len +
                ", avg_pace=" + avg_pace +
                ", max_step_freq=" + max_step_freq +
                ", max_pace=" + max_pace +
                ", avg_heart=" + avg_heart +
                ", hr_zone1=" + hr_zone1 +
                ", hr_zone2=" + hr_zone2 +
                ", hr_zone3=" + hr_zone3 +
                ", hr_zone4=" + hr_zone4 +
                ", hr_zone5=" + hr_zone5 +
                ", max_heart=" + max_heart +
                ", min_heart=" + min_heart +
                ", vo2max=" + vo2max +
                ", training_time=" + training_time +
                ", sportType=" + sportType +
                ", second=" + second +
                ", step_freqs=" + step_freqs +
                ", speeds=" + speeds +
                ", hr_values=" + hr_values +
                ", gpsData=" + gpsData +
                ", jumpfreqs=" + jumpfreqs +
                ", jumpfreq=" + jumpfreq +
                ", jumpCount=" + jumpCount +
                ", strokeCount=" + strokeCount +
                ", strokeLaps=" + strokeLaps +
                ", strokeFreq=" + strokeFreq +
                ", strokeAvgFreq=" + strokeAvgFreq +
                ", strokeAvgSwolf=" + strokeAvgSwolf +
                ", strokeMaxFreq=" + strokeMaxFreq +
                ", strokeBestSwolf=" + strokeBestSwolf +
                ", strokeType=" + strokeType +
                ", swimfreqs=" + swimfreqs +
                ", bikeSpeed=" + bikeSpeed +
                ", RawData=" + RawData +
                ", extentsion=" + extentsion +
                '}';
    }
    private static int formatNum(String value) {
        int result;
        if (value != null && !value.isEmpty()) {
            try {
                result = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                result = 0;
            }
        } else {
            result = 0;
        }
        return result;
    }

    private static Float formatFloatNum(String value) {
        Float result;
        if (value != null && !value.isEmpty()) {
            try {
                result = Float.parseFloat(value);
            } catch (NumberFormatException e) {
                result = 0.0F;
            }
        } else {
            result = 0.0F;
        }
        return result;
    }

    public static SportModel StringTurnModel(String content,long start,int type,List<String> sportContent,String rawData){
        SportModel model = new SportModel();
        List<Integer> skipType = Arrays.asList(9, 12, 13);
        if(content.contains(",")){
            List<String> array = Arrays.asList(content.split(","));
            model.start = Long.parseLong(array.get(0));
            model.date = Long.parseLong(array.get(1));
            model.sportType = formatNum(array.get(2));
            model.total_steps = formatNum(array.get(3));
            model.total_distance = formatFloatNum(array.get(4));
            model.total_calories = formatFloatNum(array.get(5));
            model.avg_step_freq = formatFloatNum(array.get(7));
            model.max_step_freq = formatFloatNum(array.get(8));
            model.avg_step_len = formatFloatNum(array.get(10));
            model.avg_pace = formatFloatNum(array.get(12));
            model.max_pace = formatFloatNum(array.get(13));
            model.hr_zone1 = formatNum(array.get(15));
            model.hr_zone2 = formatNum(array.get(16));
            model.hr_zone3 = formatNum(array.get(17));
            model.hr_zone4 = formatNum(array.get(18));
            model.hr_zone5 = formatNum(array.get(19));
            model.avg_heart = formatFloatNum(array.get(20));
            model.max_heart = formatNum(array.get(21));
            model.min_heart = formatNum(array.get(22));
            model.vo2max = formatFloatNum(array.get(23));
            model.training_time = formatNum(array.get(24));
            model.second = model.training_time*60;
            if(skipType.contains(type)){
                model.jumpCount = formatNum(array.get(38));
                model.jumpfreq = formatNum(array.get(39));
            }else if(type == 6){
                //室内游泳
                model.strokeCount = formatNum(array.get(25));
                model.strokeLaps = formatNum(array.get(26));
                model.strokeFreq = formatNum(array.get(27));
                model.strokeAvgFreq = formatNum(array.get(28));
                model.strokeAvgSwolf = formatNum(array.get(29));
                model.strokeMaxFreq = formatNum(array.get(30));
                model.strokeBestSwolf = formatNum(array.get(31));
                model.strokeType = formatNum(array.get(32));
            }
        }
        if(!sportContent.isEmpty()){
            for(int i=0; i<sportContent.size(); i++) {
                List<String> values = Arrays.asList(sportContent.get(i).split(","));

                // 运动内容数据:150314,,,0,,,119
                // 运动内容数据:110635,,,97,,0
                if(values.get(1).isEmpty()){
                    model.step_freqs.add(0);
                }else{
                    model.step_freqs.add(formatNum(values.get(1)));
                }
                if(values.get(2).isEmpty()){
                    model.speeds.add(0);
                }else{
                    model.speeds.add(formatNum(values.get(2)));
                }
                if(values.get(3).isEmpty()){
                    model.hr_values.add(0);
                }else{
                    model.hr_values.add(formatNum(values.get(3)));
                }
                if(skipType.contains(type)) {
                    if(values.get(6).isEmpty()){
                        model.jumpfreqs.add(0);
                    }else{
                        model.jumpfreqs.add(formatNum(values.get(6)));
                    }
                }else if(type==6){
                    ///室内游泳
                    if(values.get(4).isEmpty()){
                        model.swimfreqs.add(0);
                    }else{
                        model.swimfreqs.add(formatNum(values.get(4)));
                    }
                }else if(type== 5){
                    ///户外骑行
                    LogModule.i("户外骑行数据存在问题:${element.toString()}");
                    if(!values.get(5).isEmpty()){
                        model.bikeSpeed.add(formatNum(values.get(5)));
                    }
                }
            }
            if(model.sportType == 5){
                LogModule.i("运动内容不是空${model.bikeSpeed.toString()}");
            }
        }
        model.RawData = rawData;
        return model;
    }
}
