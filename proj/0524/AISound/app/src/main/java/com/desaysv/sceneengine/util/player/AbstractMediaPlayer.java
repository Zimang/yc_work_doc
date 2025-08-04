package com.desaysv.sceneengine.util.player;

import android.content.Context;

public abstract class AbstractMediaPlayer {
    protected static final String TAG = "AbstractMediaPlayer";
    protected boolean isLooping = false;
    public int getAudioSessionId(){
        return 0;
    }
    /**
     * 播放媒体文件
     * @param context 上下文
     * @param file 文件路径
     */
    public abstract void playMedia(Context context, String file);
    public abstract void playMedia(Context context, String file,int startTimeInSeconds);

    /**
     * 暂停播放
     */
    public abstract void pauseMedia();

    /**
     * 继续播放
     */
    public abstract void startMedia();

    /**
     * 停止播放
     */
    public abstract void stopMedia();

    /**
     * 释放资源
     */
    public abstract void releaseMedia();

    /**
     * 设置是否循环播放
     * @param looping true表示循环播放，false表示不循环
     */
    public void setLooping(boolean looping) {
        this.isLooping = looping;
    }

    /**
     * 获取是否循环播放
     * @return true表示循环播放，false表示不循环
     */
    public boolean isLooping() {
        return isLooping;
    }
}
