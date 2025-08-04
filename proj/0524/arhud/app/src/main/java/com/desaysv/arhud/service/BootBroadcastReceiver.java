package com.desaysv.arhud.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.desaysv.arhud.MainActivity;

import java.util.Objects;

import ch.qos.logback.core.android.SystemPropertiesProxy;

/**
 * 描 述：
 * 修改描述：
 * 修 改 人：
 * 修改版本：
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    public String getSystemProperty(String key){
        String value = null;
        try{
            value = SystemPropertiesProxy.getInstance().get(key,"1");
        } catch (Exception e){
            e.printStackTrace();
        }

        return value;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
//        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
        /*Log.i("ARHUD==", "ARHUD BOOT");
        Intent it = new Intent(context, MainActivity.class);
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(it);*/
//        }
        String vm = getSystemProperty("ro.boot.vm");
        Log.i("ARHUD", "BootBroadcastReceiver VM=" + vm);
        //X86 3-2:2,X86 3-1:X86 2-1:4
        if (vm.equals("1")){
            Log.i("ARHUD", "X86-3 VM1 entry");

            Intent it = new Intent(context, MainActivity.class);
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(it);
        }
    }

}
