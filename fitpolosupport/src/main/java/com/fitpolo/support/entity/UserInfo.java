package com.fitpolo.support.entity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @Date 2017/5/14 0014
 * @Author wenzheng.liu
 * @Description 个人信息
 * @ClassPath com.fitpolo.support.entity.UserInfo
 */
public class UserInfo {
    public String name;// 名字
    public int male;// 性别 男：0；女：1
    public int birth;// 出生年月日
    public int height;// 身高
    public int weight;// 体重
    public int hand;// 佩戴， 0–左手， 1–右手
    public int MHR;// 最大心率（次/分）

    @Override
    public String toString() {
        return "UserInfo{" +
                "name=" + name +
                ", male=" + male +
                ", birth=" + birth +
                ", height=" + height +
                ", weight=" + weight +
                ", hand=" + hand +
                ", MHR=" + MHR +
                '}';
    }
    public String toJson() {
//        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
