package com.desaysv.sceneengine.util.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.AudioAttributes;
import android.net.Uri;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class SystemMediaPlayer extends AbstractMediaPlayer {
    private MediaPlayer mediaPlayer;
    private static final int BUFFER_SIZE = 512 * 1024; // 512KB buffer size
    private WeakReference<Context> contextRef;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void playMedia(Context context, String file) {
        playMedia(context,file,0);
    }
    @Override
    public void playMedia(Context context, String file, int startTimeInSeconds) {
        contextRef = new WeakReference<>(context);
        releaseMedia(); // Release any existing player
        mediaPlayer = new MediaPlayer();

        try {
            // Configure audio attributes
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build();
            mediaPlayer.setAudioAttributes(audioAttributes);

            // Set buffer size
            mediaPlayer.setOnBufferingUpdateListener((mp, percent) -> {
                Log.d(TAG, "Buffer update: " + percent + "%");
            });

            Uri audioUri = Uri.parse("content://com.desaysv.assetcontentprovider/" + file);
            mediaPlayer.setDataSource(context, audioUri);

            // Error handling
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer Error: " + what + ", " + extra);
                releaseMedia();
                return true;
            });

            // Async preparation with progress monitoring
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mainHandler.post(() -> {
                    if (mediaPlayer != null) {
                        // 跳转到指定时间点
                        if (startTimeInSeconds > 0) mediaPlayer.seekTo(startTimeInSeconds);//单位是毫秒
                        mediaPlayer.start();
                    }
                });
            });

            // Completion listener
            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d(TAG, "Playback completed");
                releaseMedia();
            });

        } catch (IOException e) {
            Log.e(TAG, "playMedia error: " + e.getMessage());
            releaseMedia();
        } catch (IllegalStateException e) {
            Log.e(TAG, "Invalid state: " + e.getMessage());
            releaseMedia();
        }
    }

    @Override
    public void pauseMedia() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.pause();
            } catch (IllegalStateException e) {
                Log.e(TAG, "pauseMedia error: " + e.getMessage());
            }
        }
    }

    @Override
    public void startMedia() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.start();
            } catch (IllegalStateException e) {
                Log.e(TAG, "startMedia error: " + e.getMessage());
            }
        }
    }

    @Override
    public void stopMedia() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            } catch (IllegalStateException e) {
                Log.e(TAG, "stopMedia error: " + e.getMessage());
            }
        }
    }

    @Override
    public void releaseMedia() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException e) {
                // Ignore
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        contextRef = null;
    }
}
