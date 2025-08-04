package com.desaysv.sceneengine.socket;

import static android.media.AudioManager.STREAM_MUSIC;
import static com.desaysv.aisound.BaseApplication.BASE_TAG;

import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.desaysv.sceneengine.util.ByteUtil;

import java.io.IOException;
import java.util.Arrays;

public final class SocketAnalysis {
    private static final String TAG = BASE_TAG + "SocketAnalysis";

    private static SocketAnalysis instance;

    private SocketAnalysis() {
    }

    public static void init() {
        Log.d(TAG, "init: ");
        instance = SocketAnalysisHolder.INSTANCE;
    }

    private static class SocketAnalysisHolder {
        private static final SocketAnalysis INSTANCE = new SocketAnalysis();
    }

    public static SocketAnalysis getInstance() {
        if (instance == null) {
            init();
        }
        return instance;
    }


    public synchronized void processMsgFromPC(byte[] msg) {
        if (msg[0] != SocketConstants.Response.HEAD) {
            Log.e(TAG, "AnalysisMsgFromPC: 数据头错误 " + msg[0]);
            return;
        }
        //if (msg[1] == SocketConstants.Response.STATUS_SUCCESS) { //服务下发 或请求成功
        if (msg[1] == SocketConstants.Response.SOURE.MCU.getKey()) { //判断是MCU消息
            switch (msg[2]) {//指令码
                case SocketConstants.Response.ORDER_LOGIN://处理登录响应
                    SocketClient.isLogined = true;
                    SocketClient.isLogining = false;
                    break;
                default://其他则为场景响应需求
                    break;
            }
        } else {
            Log.d(TAG, "AnalysisMsgFromPC 状态码错误: " + msg[1]);
        }
    }

    public void analysisMsgToPC(byte type) {
        this.analysisMsgToPC(type, new int[]{});
    }

    public void analysisMsgToPC(byte type, int[] msg) {
        Log.d(TAG, "AnalysisMsgToPC: type:" + "0x" + String.format("%02X", type) + " msg:" + Arrays.toString(msg));
        byte[] data = new byte[msg.length];
        for (int i = 0; i < msg.length; i++) {
            data[i] = (byte) msg[i];
        }
        switch (type) {
            case SocketConstants.Request.ORDER_LOGIN:
                data = SocketRequest.login();
                break;
            case SocketConstants.Request.ORDER_HEARTBEAT:
                data = SocketRequest.heartbeat();
                break;
            case SocketConstants.Request.ORDER_AR_HUD:
                data = SocketRequest.arHud(data);
                break;
            default:
                break;
        }

        SocketClient.getInstance().postMsgToPc(data);
    }

    public byte[] putSocketHead(byte[] msg) {
        msg[0] = SocketConstants.Request.HEAD;
        msg[1] = SocketConstants.Request.STATUS_TO_MCU;
        return msg;
    }

    public byte[] putSocketEnd(byte[] msg) {
        msg[msg.length - 2] = SocketConstants.Request.FINAL_1;
        msg[msg.length - 1] = SocketConstants.Request.FINAL_2;
        return msg;
    }

    public byte[] putSocketMode(byte[] msg) {
        msg = putSocketHead(msg);
        msg = putSocketEnd(msg);
        return msg;
    }

    public byte calcMsgLength(int length) {
        byte result = 0x00;
        switch (length) {
            case 1:
                result = 0x01;
                break;
            case 2:
                result = 0x02;
                break;
            case 3:
                result = 0x03;
                break;
            case 4:
                result = 0x04;
                break;
            case 5:
                result = 0x05;
                break;
            case 6:
                result = 0x06;
                break;
            case 7:
                result = 0x07;
                break;
            case 8:
                result = 0x08;
                break;
            case 9:
                result = 0x09;
                break;
            case 10:
                result = 0x0A;
                break;
            case 11:
                result = 0x0B;
                break;
            case 12:
                result = 0x0C;
                break;
            case 13:
                result = 0x0D;
                break;
            case 14:
                result = 0x0E;
                break;
            case 15:
                result = 0x0F;
                break;
            case 16:
                result = 0x10;
                break;
            case 17:
                result = 0x11;
                break;
            case 18:
                result = 0x12;
                break;
            case 19:
                result = 0x13;
                break;
            case 20:
                result = 0x14;
                break;
            case 21:
                result = 0x15;
                break;
            case 22:
                result = 0x16;
                break;
            case 23:
                result = 0x17;
                break;
            case 24:
                result = 0x18;
                break;
            default:
                break;
        }
        return result;
    }

    public static class SocketRequest {
        //登录
        public static byte[] login() {
            byte[] data = new byte[6 + SocketConstants.Request.MSG_LOGIN.length];
            data = getInstance().putSocketMode(data);
            data[2] = SocketConstants.Request.ORDER_LOGIN;
            data[3] = getInstance().calcMsgLength(SocketConstants.Request.MSG_LOGIN.length);
            for (int i = 0; i < SocketConstants.Request.MSG_LOGIN.length; i++) {
                data[i + 4] = SocketConstants.Request.MSG_LOGIN[i];
            }
            SocketClient.isLogining = true;
            Log.d(TAG, "login data " + Arrays.toString(data));
            return data;
        }

        //心跳包
        public static byte[] heartbeat() {
            Log.d(TAG, "heartbeat: ");
            byte[] data = new byte[7];
            data = getInstance().putSocketMode(data);
            data[2] = SocketConstants.Request.ORDER_HEARTBEAT;
            data[3] = getInstance().calcMsgLength(1);
            data[4] = 0x0A; //10s超时
            return data;
        }

        //AR HUD 选项
        public static byte[] arHud(byte[] msg) {
            byte[] data = new byte[msg.length + 6];
            data = getInstance().putSocketMode(data);
            data[2] = SocketConstants.Request.ORDER_AR_HUD;
            data[3] = getInstance().calcMsgLength(22);
            for (int i = 4, j = 0; j < msg.length; i++, j++) {
                data[i] = msg[j];
            }
            Log.d(TAG, "build arHud byte[]: " + ByteUtil.bytesToHexWithPrefix(data));
            return data;
        }
    }

    public static class SocketResponse {
        //
    }


}
