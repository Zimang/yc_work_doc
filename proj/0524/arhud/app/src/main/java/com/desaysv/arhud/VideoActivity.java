package com.desaysv.arhud;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.VideoView;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class VideoActivity extends AppCompatActivity {
    private String TAG = "ARHUD";
    // 声明 UI 组件和相关变量
    private FrameLayout contentContainer; // 用于显示内容的容器
    private VideoView videoView; // 用于播放视频的视图
    //1:一键复位 2:智能迎宾 3:开机动画 4:绿野森林 5:超感模式 6暖心护航 7舒适清扬 8常规模式 9自由体验 10离开空间
    private int[] videoResources = {R.raw.lysl, R.raw.cgms, R.raw.nxhh, R.raw.ssqy, R.raw.anmi}; // 视频资源数组
    private boolean isImagePlaying = false; // 标记是否正在播放图片
    private View imageLayout; // 图片布局
    private View videoLayout; // 视频布局

    /*private  void launchActivityOnDisplay(int displayid){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent, ActivityOptions.makeBasic().setLaunchDisplayId(displayid).toBundle());
    }*/
    private void executeCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            /*DataOutputStream os = new DataOutputStream(process.getOutputStream());
            BufferedReader input = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            os.close();

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = input.readLine()) != null) {
                output.append(line).append("\n");
            }*/
            process.waitFor();
            //return output.toString();
        } catch (IOException | InterruptedException e) {
            Log.e(TAG, "Execute command error: " + e.getMessage());
            //return null;
        }
    }

    // 广播接收器，用于接收播放图片和视频的广播
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); // 获取广播动作
            Log.d(TAG,"Broadcast:"+action);
            // 根据广播动作执行不同的逻辑
            if (BroadcastConstants.ACTION_PLAY_IMAGE.equals(action)) {
                playImage(); // 播放图片
            } else if (BroadcastConstants.ACTION_BOUNDLESS_NAVI.equals(action)) {
                // 获取视频索引
                String videoId = intent.getStringExtra(BroadcastConstants.EXTRA_VIDEO_ID);
                Log.d(TAG,"ACTION_BOUNDLESS_NAVI");//cgms
                if (videoId.equals("01")) {
                    playVideo(1); // 播放指定索引的视频
                }
            } else if (BroadcastConstants.ACTION_BOUNDLESS_SR.equals(action)) {
                // 获取视频索引
                String videoId = intent.getStringExtra(BroadcastConstants.EXTRA_VIDEO_ID);
                Log.d(TAG,"ACTION_BOUNDLESS_SR");//ssqy
                if (videoId.equals("01")) {
                    playVideo(3); // 播放指定索引的视频
                }
            } else if (BroadcastConstants.ACTION_SCENE_CHANGE_TOAPP.equals(action)) {
                // 获取视频索引
                String strvideoId = intent.getStringExtra(BroadcastConstants.EXTRA_VIDEO_ID);
                //int videoId = Integer.parseInt(strvideoId);
                Log.d(TAG, "onReceive: strvideoId");
                if (strvideoId.equals("04")){
                    playVideo(0); // 播放指定索引的视频
                } else if (strvideoId.equals("05")){
                    playVideo(1); // 播放指定索引的视频
                } else if (strvideoId.equals("06")){
                    playVideo(2); // 播放指定索引的视频
                } else if (strvideoId.equals("07")){
                    playVideo(3); // 播放指定索引的视频
                }
            }
            else if (BroadcastConstants.ACTION_SCENE_CHANGE_TOIPAD.equals(action)) {
                // 获取视频索引
                String strvideoId = intent.getStringExtra(BroadcastConstants.EXTRA_VIDEO_ID);
                //int videoId = Integer.parseInt(strvideoId);
                Log.d(TAG, "onReceive: strvideoId");
                if (strvideoId.equals("01")){
                    playVideo(0); // 播放指定索引的视频
                } else if (strvideoId.equals("02")){
                    playVideo(1); // 播放指定索引的视频
                } else if (strvideoId.equals("03")){
                    playVideo(2); // 播放指定索引的视频
                } else if (strvideoId.equals("04")){
                    playVideo(3); // 播放指定索引的视频
                }
            }else if (BroadcastConstants.ACTION_BOOT_ANIM.equals(action)) {
                // 获取视频索引
                Log.d(TAG, "onReceive: ACTION_BOOT_ANIM");
                playVideo(4); // 播放指定索引的视频
            } else {
                playImage(); // 播放图片
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置全屏模式
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main_video); // 加载布局文件
        //executeCommand("adb shell am start -n com.desaysv.arhud/.MainActivity --display 2");

        // 初始化 UI 组件
        contentContainer = findViewById(R.id.content_container);
        //playImage(); // 播放图片
        Log.d(TAG,"arhud entry");

        // 注册广播接收器
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastConstants.ACTION_PLAY_IMAGE); // 添加播放图片的广播动作
        intentFilter.addAction(BroadcastConstants.ACTION_BOOT_ANIM);
        intentFilter.addAction(BroadcastConstants.ACTION_SCENE_CHANGE_TOAPP);
        intentFilter.addAction(BroadcastConstants.ACTION_SCENE_CHANGE_TOIPAD);
        intentFilter.addAction(BroadcastConstants.ACTION_BOUNDLESS_NAVI);
        intentFilter.addAction(BroadcastConstants.ACTION_BOUNDLESS_SR);
        registerReceiver(mBroadcastReceiver, intentFilter); // 注册广播接收器, registerReceiver(mBroadcastReceiver, intentFilter)
    }

    // 播放图片的逻辑
    private void playImage() {
        // 如果视频正在播放，停止视频
        if (videoLayout != null && videoView.isPlaying()) {
            videoView.stopPlayback();
        }

        // 隐藏视频布局，显示图片布局
        if (videoLayout != null) {
            contentContainer.removeView(videoLayout);
            videoLayout = null;
        }

        // 加载图片布局
        if (imageLayout == null) {
            imageLayout = getLayoutInflater().inflate(R.layout.image_layout, contentContainer, false); // 加载图片布局
            contentContainer.addView(imageLayout); // 添加到内容容器
        } else {
            contentContainer.setVisibility(View.VISIBLE); // 显示内容容器
        }

        // 设置图片
        ((android.widget.ImageView) imageLayout.findViewById(R.id.image_view)).setImageResource(R.drawable.image); // 设置图片资源
        isImagePlaying = true; // 标记正在播放图片
        hideSystemUI(); // 隐藏系统 UI
    }

    // 播放开机动画的逻辑

    // 播放开机动画的逻辑
    private void playBootAnimation() {
        // 如果视频正在播放，停止视频
        if (videoLayout != null && videoView.isPlaying()) {
            videoView.stopPlayback();
        }

        // 隐藏视频布局，显示图片布局
        if (videoLayout != null) {
            contentContainer.removeView(videoLayout);
            videoLayout = null;
        }

        // 加载视频布局
        if (videoLayout == null) {
            videoLayout = getLayoutInflater().inflate(R.layout.video_layout, contentContainer, false); // 加载视频布局
            videoView = videoLayout.findViewById(R.id.video_view); // 获取开机动画
            contentContainer.addView(videoLayout); // 添加到内容容器
        } else {
            contentContainer.setVisibility(View.VISIBLE); // 显示内容容器
        }

        // 设置视频 URI 并开始播放
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.cgms);
        videoView.setVideoURI(videoUri);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start(); // 开始播放
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e("VideoView", "播放错误: " + what + ", " + extra);
                return true;
            }
        });
        hideSystemUI(); // 隐藏系统 UI

        // 设置视频播放完成的监听器
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playImage(); // 播放主界面图片
            }
        });
    }

    // 播放视频的逻辑
    private void playVideo(int index) {
        if (index >= 0 && index < videoResources.length) {
            int videoResId = videoResources[index]; // 获取视频资源 ID

            // 如果图片正在显示，隐藏图片
            if (isImagePlaying) {
                if (imageLayout != null) {
                    contentContainer.removeView(imageLayout); // 移除图片布局
                    imageLayout = null;
                }
                isImagePlaying = false; // 标记不再播放图片
            }

            // 如果视频正在播放，停止视频
            if (videoLayout != null && videoView.isPlaying()) {
                videoView.stopPlayback();
            }

            // 加载视频布局
            if (videoLayout == null) {
                videoLayout = getLayoutInflater().inflate(R.layout.video_layout, contentContainer, false); // 加载视频布局
                videoView = videoLayout.findViewById(R.id.video_view); // 获取视频视图
                contentContainer.addView(videoLayout); // 添加到内容容器
            } else {
                contentContainer.setVisibility(View.VISIBLE); // 显示内容容器
            }

            // 设置视频 URI 并开始播放
            Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + videoResId);
            videoView.setVideoURI(videoUri);
            videoView.start(); // 开始播放视频
            hideSystemUI(); // 隐藏系统 UI

            // 设置视频播放完成的监听器
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    hideSystemUI(); // 隐藏系统 UI
                    //playImage(); // 播放图片
                    videoView.seekTo(0);
                    videoView.start();
                }
            });
        }
    }

    // 隐藏系统 UI
    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消注册广播接收器,unregisterReceiver(mBroadcastReceiver);
        unregisterReceiver(mBroadcastReceiver);
    }
}