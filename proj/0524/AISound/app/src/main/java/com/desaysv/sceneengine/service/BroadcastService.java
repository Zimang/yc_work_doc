package com.desaysv.sceneengine.service;

import static com.desaysv.aisound.BaseApplication.BASE_TAG;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class BroadcastService extends Service {
    private static final String TAG = BASE_TAG + "BroadcastService";
    private final IBinder binder = new LocalBinder();
    private BroadcastManager broadcastManager;

    public class LocalBinder extends Binder {
        public BroadcastService getService() {
            return BroadcastService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "BroadcastService onCreate");
        broadcastManager = BroadcastManager.getInstance();
        broadcastManager.registerReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "BroadcastService onStartCommand");
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "BroadcastService onDestroy");
        if (broadcastManager != null) {
            broadcastManager.unregisterReceiver();
        }
    }

    public void sendBroadcast(String key, byte[] data) {
        if (broadcastManager != null) {
            broadcastManager.sendBroadcast(key, data);
        }
    }

    public void sendBroadcast(String key, String data) {
        if (broadcastManager != null) {
            broadcastManager.sendBroadcast(key, data);
        }
    }

    public void sendBroadcast(String action, String key, String data) {
        if (broadcastManager != null) {
            broadcastManager.sendBroadcast(action, key, data);
        }
    }

    public void setOnSceneDataReceiveListener(BroadcastManager.OnSceneDataReceiveListener listener) {
        if (broadcastManager != null) {
            broadcastManager.setOnSceneDataReceiveListener(listener);
        }
    }
}
