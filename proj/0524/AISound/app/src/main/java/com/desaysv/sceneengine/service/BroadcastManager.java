package com.desaysv.sceneengine.service;

import static com.desaysv.aisound.BaseApplication.BASE_TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.desaysv.aisound.BaseApplication;

public class BroadcastManager {
    private static final String TAG = BASE_TAG + "BroadcastManager";
    public static final String ACTION_SCENE_BROADCAST = "com.desaysv.sceneengine.ACTION_SCENE_BROADCAST";
    public static final String ACTION_SCENE_BROADCAST_2 = "com.desaysv.sceneengine.ACTION_SCENE_CHANGE_TOAPP";

    public static final String ACTION_SCENE_BROADCAST_3 = "RECEIVER_VPA_TYPEATION";

    private static final String EXTRA_KEY_DATA = "EXTRA_KEY_DATA";
    private static final String EXTRA_KEY_IDENTIFIER = "EXTRA_KEY_IDENTIFIER";

    private static volatile BroadcastManager instance;
    private static final Object lock = new Object();

    private final Context mContext;
    private BroadcastReceiver mBroadcastReceiver;
    private OnSceneDataReceiveListener mListener;
    private boolean isReceiverRegistered = false;

    private BroadcastManager() {
        mContext = BaseApplication.getContext();
        initReceiver();
    }

    public static BroadcastManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new BroadcastManager();
                }
            }
        }
        return instance;
    }

    private void initReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ACTION_SCENE_BROADCAST.equals(intent.getAction()) && mListener != null) {
                    String key = intent.getStringExtra(EXTRA_KEY_IDENTIFIER);
                    byte[] data = intent.getByteArrayExtra(EXTRA_KEY_DATA);
                    if (data != null) {
                        mListener.onDataReceived(intent.getAction(), key, data);
                    }
                } else if (ACTION_SCENE_BROADCAST_2.equals(intent.getAction()) && mListener != null) {
                    int data = 0;
                    Bundle b = intent.getExtras();
                    if (null != b.get("data")) data = Integer.parseInt(b.get("data").toString());
                    //data = intent.getIntExtra("data", 0);
                    if (data >= 0) {
                        mListener.onDataReceived(intent.getAction(), "data", new byte[]{(byte) data});//因为取值范围是1位数，所以强制转化byte
                    }
                } else if (ACTION_SCENE_BROADCAST_3.equals(intent.getAction()) && mListener != null) {
                    int data = 0;
                    Bundle b = intent.getExtras();
                    if (null != b.get("VPA_TYPE"))
                        data = Integer.parseInt(b.get("VPA_TYPE").toString());
                    if (data >= 0) {
                        mListener.onDataReceived(intent.getAction(), "VPA_TYPE", new byte[]{(byte) data});//因为取值范围是1位数，所以强制转化byte
                    }
                }
            }
        };
    }

    public void setOnSceneDataReceiveListener(OnSceneDataReceiveListener listener) {
        this.mListener = listener;
    }

    /**
     * 发送带标识的广播
     *
     * @param key  广播的标识符，用于区分不同类型的广播
     * @param data 要发送的数据
     */
    public void sendBroadcast(String key, String data) {
        if (data == null) {
            Log.w(TAG, "sendBroadcast: data is null");
            return;
        }

        if (key == null || key.isEmpty()) {
            Log.w(TAG, "sendBroadcast: key is null or empty");
            return;
        }

        Intent intent = new Intent(ACTION_SCENE_BROADCAST);
        intent.putExtra(EXTRA_KEY_IDENTIFIER, key);
        intent.putExtra(EXTRA_KEY_DATA, data);
        mContext.sendBroadcast(intent);
        Log.d(TAG, String.format("sendBroadcast: key=%s, data=%s", key, data));
    }

    public void sendBroadcast(String key, byte[] data) {
        if (data == null) {
            Log.w(TAG, "sendBroadcast: data is null");
            return;
        }

        if (key == null || key.isEmpty()) {
            Log.w(TAG, "sendBroadcast: key is null or empty");
            return;
        }

        Intent intent = new Intent(ACTION_SCENE_BROADCAST);
        intent.putExtra(EXTRA_KEY_IDENTIFIER, key);
        intent.putExtra(EXTRA_KEY_DATA, data);
        mContext.sendBroadcast(intent);
        Log.d(TAG, String.format("sendBroadcast: key=%s, data=%s", key, bytesToHexString(data)));
    }

    public void sendBroadcast(String action, String key, String data) {
        if (action == null || action.isEmpty()) {
            Log.w(TAG, "sendBroadcast: action is null or empty");
            return;
        }

        Intent intent = new Intent(action);
        if (null != key) intent.putExtra(key, data);
        mContext.sendBroadcast(intent);
        Log.d(TAG, String.format("sendBroadcast: action=%s, key=%s, data=%s", action, key, data));
    }

    /**
     * 注册广播接收器
     */
    public void registerReceiver() {
        if (!isReceiverRegistered) {
            //IntentFilter filter = new IntentFilter(ACTION_SCENE_BROADCAST);
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_SCENE_BROADCAST);
            filter.addAction(ACTION_SCENE_BROADCAST_2);
            filter.addAction(ACTION_SCENE_BROADCAST_3);
            ContextCompat.registerReceiver(mContext, mBroadcastReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
            isReceiverRegistered = true;
            Log.d(TAG, "Broadcast receiver registered");
        }
    }

    /**
     * 注销广播接收器
     */
    public void unregisterReceiver() {
        if (isReceiverRegistered) {
            mContext.unregisterReceiver(mBroadcastReceiver);
            isReceiverRegistered = false;
            Log.d(TAG, "Broadcast receiver unregistered");
        }
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    public interface OnSceneDataReceiveListener {
        /**
         * 当接收到广播数据时回调
         *
         * @param key  广播的标识符
         * @param data 接收到的数据
         */
        void onDataReceived(String action, String key, byte[] data);
    }
}
