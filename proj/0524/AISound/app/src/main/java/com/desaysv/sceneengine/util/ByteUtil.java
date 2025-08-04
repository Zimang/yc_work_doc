package com.desaysv.sceneengine.util;

import java.util.stream.IntStream;

/**
 * @author lyy
 */
public class ByteUtil {
    public static byte[] strToBytes(String str) {
        return (null != str) ? str.getBytes() : null;
    }

    // 字符串转换为字节数组
    public static byte[] byteStrToBytes(String str) {
        String[] strArr = str.split(" ");
        byte[] b = new byte[strArr.length];
        IntStream.range(0, strArr.length).forEach(i -> {
            // 去掉前缀 "0x" 并解析为整数
            int intValue = Integer.parseInt(strArr[i].substring(2), 16);
            // 强制转换为 byte
            b[i] = (byte) intValue;
        });
        return b;
    }

    // 字节数组转换为十六进制字符串
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex).append(" ");
        }
        return hexString.toString();
    }

    public static char[] bytesToHexString(byte[] data) {
        char[] hexArray = new char[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            int high = (data[i] & 0xF0) >> 4;
            int low = data[i] & 0x0F;
            hexArray[i * 2] = (char) (high >= 10 ? high + 'A' - 10 : high + '0');
            hexArray[i * 2 + 1] = (char) (low >= 10 ? low + 'A' - 10 : low + '0');
        }
        return hexArray;
    }

    public static String bytesToString(byte[] data){
        return new String(data);
    }

    public static String bytesToHexWithPrefix(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append("0x");
            sb.append(String.format("%02X", b));
            sb.append(" ");
        }
        // 去掉最后一个多余的空格
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

}
