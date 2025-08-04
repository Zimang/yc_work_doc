package com.desaysv.aisound;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.desaysv.sceneengine.util.PxUtil;
import com.desaysv.sceneengine.socket.SocketAnalysis;
import com.desaysv.sceneengine.socket.SocketClient;

public class BaseApplication extends Application {

    private static Context context;
    public static final String BASE_TAG = "AiSound-";
    public static final String TAG = BASE_TAG + "BaseApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        context = this;

        initUtil();
        SocketAnalysis.init();
        SocketClient.getInstance().open(true);//TODO:测试代码为false，开启socket为true
        SocketClient.getInstance().initClient();
    }

    private void initUtil(){
        Resources resources = this.getResources();
        PxUtil.DensityDpi = resources.getDisplayMetrics().densityDpi;
        PxUtil.ScaledDensity = resources.getDisplayMetrics().scaledDensity;

        Log.i(TAG,"DensityDpi=" + PxUtil.DensityDpi);
    }

    public static Context getContext() {
//        Log.d(TAG, "getContext: ");
        return context;
    }
}
