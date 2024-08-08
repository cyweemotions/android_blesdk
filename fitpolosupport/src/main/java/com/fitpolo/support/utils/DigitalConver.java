package com.fitpolo.support.utils;

import com.fitpolo.support.log.LogModule;
import com.google.gson.Gson;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Date 2017/5/15
 * @Author wenzheng.liu
 * @Description 数字转换类
 * @ClassPath com.fitpolo.support.utils.DigitalConver
 */
public class DigitalConver {

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description byte转16进制
     */
    public static String byte2HexString(byte b) {
        return String.format("%02X", b);
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * @Date 2017/5/15
     * @Author wenzheng.liu
     * @Description 16进制转10进制
     */
    public static String decodeToString(String data) {
        String string = Integer.toString(Integer.parseInt(data, 16));
        return string;
    }

    /**
     * @Date 2017/5/16
     * @Author wenzheng.liu
     * @Description 16进制转2进制
     */
    public static String hexString2binaryString(String hexString) {
        if (hexString == null || hexString.length() % 2 != 0)
            return null;
        String bString = "", tmp;
        for (int i = 0; i < hexString.length(); i++) {
            tmp = "0000"
                    + Integer.toBinaryString(Integer.parseInt(
                    hexString.substring(i, i + 1), 16));
            bString += tmp.substring(tmp.length() - 4);
        }
        return bString;
    }

    /**
     * @Date 2017/5/16
     * @Author wenzheng.liu
     * @Description byte转2进制
     */
    public static String byte2binaryString(byte b) {
        return hexString2binaryString(byte2HexString(b));
    }

    /**
     * @Date 2017/6/9
     * @Author wenzheng.liu
     * @Description 2进制转16进制
     */
    public static String binaryString2hexString(String bString) {
        if (bString == null || bString.equals("") || bString.length() % 8 != 0)
            return null;
        StringBuffer tmp = new StringBuffer();
        int iTmp = 0;
        for (int i = 0; i < bString.length(); i += 4) {
            iTmp = 0;
            for (int j = 0; j < 4; j++) {
                iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << (4 - j - 1);
            }
            tmp.append(Integer.toHexString(iTmp));
        }
        return tmp.toString();
    }

    /**
     * @Date 2017/8/15
     * @Author wenzheng.liu
     * @Description 将byte数组bRefArr转为一个整数, 字节数组的低位是整型的低字节位
     */
    public static int byteArr2Int(byte[] bRefArr) {
        return Integer.parseInt(DigitalConver.bytesToHexString(bRefArr), 16);
    }

    public static String byteArr2Str(byte[] bRefArr) {
        return Integer.toString(byteArr2Int(bRefArr));
    }

    public static int byte2Int(byte b) {
        return b & 0xFF;
    }

    /**
     * @Date 2017/8/14 0014
     * @Author wenzheng.liu
     * @Description 整数转换成byte数组
     */
    public static byte[] int2ByteArr(int iSource, int iArrayLen) {
        byte[] bLocalArr = new byte[iArrayLen];
        for (int i = 0; (i < 4) && (i < iArrayLen); i++) {
            bLocalArr[i] = (byte) (iSource >> 8 * i & 0xFF);
        }
        // 数据反了,需要做个翻转
        byte[] bytes = new byte[iArrayLen];
        for (int i = 0; i < bLocalArr.length; i++) {
            bytes[bLocalArr.length - 1 - i] = bLocalArr[i];
        }
        return bytes;
    }

    // 字符串转换为16进制
    public static String string2Hex(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }

    // 16进制转换为字符串
    public static String hex2String(String s) {
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "utf-8");//UTF-16le:Not
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

    public static byte[] hex2bytes(String hex) {
        if (hex.length() % 2 == 1) {
            hex = "0" + hex;
        }
        byte[] data = new byte[hex.length() / 2];
        for (int i = 0; i < data.length; i++) {
            try {
                data[i] = (byte) (0xff & Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return data;
    }
    public static byte[] mergeBitArrays(byte[] array1, byte[] array2) {
        // 计算合并后数组的长度
        int totalLength = array1.length + array2.length;
        // 创建一个新的数组来存储合并后的结果
        byte[] mergedArray = new byte[totalLength];
        // 复制第一个数组
        System.arraycopy(array1, 0, mergedArray, 0, array1.length);
        // 复制第二个数组到合并数组的末尾
        System.arraycopy(array2, 0, mergedArray, array1.length, array2.length);
        return mergedArray;
    }

    public static List<Byte> convert(int source, ByteType type) {
        // 1、将整数转换为十六进制字符串，并确保长度为偶数
        String hexStr = Integer.toHexString(source).toUpperCase();
        if (hexStr.length() % 2 != 0) {
            hexStr = "0" + hexStr;
        }
        // 2、使用ByteBuffer将十六进制字符串转换为byte数组
        ByteBuffer buffer = ByteBuffer.allocate(hexStr.length() / 2);
        for (int i = 0; i < hexStr.length(); i += 2) {
            String byteString = hexStr.substring(i, i + 2);
            buffer.put((byte) Integer.parseInt(byteString, 16));
        }
        byte[] byteArray = buffer.array();
        List<Integer> list = new ArrayList<>();
        switch (type) {
            case BYTE:
                break;
            case WORD:
                if (byteArray.length == 1) {
                    list.add(0);
                }
                break;
            case DWORD:
                for (int i = 0; i < 4 - byteArray.length; i++) {
                    list.add(0);
                }
                break;
        }
        //3、将byteArray中的每个byte添加到list中
        for (byte b : byteArray) {
            list.add(b & 0xFF); // 确保是正数
        }
        //4、将byte[]转换为List<Byte>
        List<Byte> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            int value = list.get(i);
            if (value < 0 || value > 255) {
                throw new IllegalArgumentException("List contains values outside the byte range");
            }
            result.add((byte) value);
        }
        return result;
    }
    public static List<Byte> bytes2ListByte(byte[] bytes) {
        List<Byte> byteList = new ArrayList<>();
        for (byte b : bytes) {
            byteList.add(b); // 自动装箱，将 byte 转为 Byte
        }
        return byteList;
    }
    public static byte[] listByte2bytes(List<Byte> list) {
        byte[] byteArray = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            byteArray[i] = list.get(i); // 自动拆箱，将 Byte 转为 byte
        }
        return byteArray;
    }
    public static List<Byte> removeZero(List<Byte> list) {
        List<Byte> newList = new ArrayList<>();
        for (Byte b : list) {
            if (b != (byte) 0x00) {
                newList.add(b);
            }
        }
        return newList;
    }

}
