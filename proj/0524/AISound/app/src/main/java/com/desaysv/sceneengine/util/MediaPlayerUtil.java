package com.desaysv.sceneengine.util;

import static com.desaysv.aisound.BaseApplication.BASE_TAG;

import android.content.Context;

import com.desaysv.sceneengine.util.player.AbstractMediaPlayer;
import com.desaysv.sceneengine.util.player.AudioTrackMediaPlayer;
import com.desaysv.sceneengine.util.player.SystemMediaPlayer;

public class MediaPlayerUtil {
    public static final String TAG = BASE_TAG + "MediaPlayer";
    //private static AbstractMediaPlayer player = new SystemMediaPlayer();
    private static AbstractMediaPlayer player = new AudioTrackMediaPlayer();

    public static void playMedia(Context context, String file) {
        player.playMedia(context,file);
    }
    public static void playMedia(Context context, String file,int startTimeInSeconds) {
        player.playMedia(context,file,startTimeInSeconds);
    }

    public static void pauseMedia() {
        player.pauseMedia();
    }

    public static void startMedia() {
        player.startMedia();
    }

    public static void stopMedia() {
        player.stopMedia();
    }

    public static void releaseMedia() {
        player.releaseMedia();
    }

    public static void setLooping(boolean looping) {
        player.setLooping(looping);
    }
}
