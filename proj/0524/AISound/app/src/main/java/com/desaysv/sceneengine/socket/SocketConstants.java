package com.desaysv.sceneengine.socket;

import java.util.HashMap;

public class SocketConstants {
    //客户端请求
    public static class Request {
        //数据头
        public static final byte HEAD = 0x21;  //数据头
        public static final byte STATUS_TO_MCU = (byte) 0x81;  //状态吗： To MCU

        //数据尾
        public static final byte FINAL_1 = 0x0D;  //数据尾1
        public static final byte FINAL_2 = 0x0A;  //数据尾2


        //指令码
        public static final byte ORDER_LOGIN = (byte) 0xFF;  //指令码-登录
        public static final byte ORDER_HEARTBEAT = (byte) 0xF1;  //心跳包
        public static final byte ORDER_AR_HUD = 0x04;  //AR HUD 选项

        //消息内容
        public static final byte ORDER_OFF = 0x00;  //关闭
        public static final byte ORDER_ON = 0x01;  //开启
        //登录
        public static final byte[] MSG_LOGIN = {0x4D, 0x61, 0x69, 0x6E, 0x43, 0x6F, 0x6E, 0x74, 0x72, 0x6F, 0x6C, 0x43, 0x61, 0x72, 0x64};  //消息内容-登录

    }


    //服务端响应
    public static class Response {
        //数据头
        public static final byte HEAD = 0x21; //数据头
        //状态吗
        public static final byte STATUS_SUCCESS = 0x00; //成功
        public static final byte STATUS_FAIL = (byte) 0xFF; //失败
        //指令码
        public static final byte ORDER_LOGIN = (byte) 0xFF; //登录
        public static final byte ORDER_AR_HUD = 0x04;  //AR HUD 选项

        //消息长度
        public static final byte MSG_LENGTH = 0x01;

        //消息内容
        public static final byte ORDER_OFF = 0x00; //关闭
        public static final byte ORDER_ON = 0x01; //开启
        //成功
        public static final byte MSG_SUCCESS = 0x00; //成功
        //错误码
        public static final byte MSG_ERROR_1 = 0x01; //登录ASCII名称 错误
        public static final byte MSG_ERROR_2 = 0x02; //登录超时
        public static final byte MSG_ERROR_3 = 0x03; //请求失败，未登录
        public static final byte MSG_ERROR_4 = 0x04; //状态码错误
        public static final byte MSG_ERROR_5 = 0x05; //指令错误
        public static final byte MSG_ERROR_6 = 0x06; //协议错误，不正确的协议数据

        //数据尾
        public static final byte FINAL_1 = 0x0D; //数据尾1
        public static final byte FINAL_2 = 0x0A; //数据尾2

        public static final HashMap<Byte, String> ERROR_MAP = new HashMap<Byte, String>() {
            {
                put(MSG_ERROR_1, "登录ASCII名称 错误");
                put(MSG_ERROR_2, "登录超时");
                put(MSG_ERROR_3, "请求失败，未登录");
                put(MSG_ERROR_4, "状态码错误");
                put(MSG_ERROR_5, "指令错误");
                put(MSG_ERROR_6, "协议错误，不正确的协议数据");
            }
        };

        public enum SOURE {
            IPAD((byte) 0x01), ZC((byte) 0x02), ZK((byte) 0x03),MCU((byte)0x81);
            private final byte key;

            SOURE(byte i) {
                this.key = i;
            }

            byte getKey(){
                return this.key;
            }
        }
    }
}
