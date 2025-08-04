package com.desaysv.sceneengine.util.player;

import static android.media.AudioFormat.CHANNEL_OUT_7POINT1;
import static android.media.AudioFormat.ENCODING_PCM_16BIT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioTrackMediaPlayer extends AbstractMediaPlayer {
    private AudioTrack audioTrack;
    private static final int SAMPLE_RATE = 48000; // 采样率
    private static final int BUFFER_SIZE = 1024*1024; // 缓冲区大小32KB
    private ExecutorService executor;
    private final AtomicBoolean isPlaying = new AtomicBoolean(false);
    private boolean isLooping = false;

    public AudioTrackMediaPlayer() {
        initExecutor();
    }

    private void initExecutor() {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }
    }

    @Override
    public int getAudioSessionId(){
        return audioTrack != null ? audioTrack.getAudioSessionId() : 0;
    }

    @Override
    public void playMedia(Context context, String file) {
        playMedia(context,file,0);
    }
    @SuppressLint("Range")
    @Override
    public void playMedia(Context context, String file, int startTimeInSeconds) {
        try {
            stopMedia(); // 确保之前的播放已经停止
            initExecutor(); // 确保executor可用

            // 配置AudioTrack
            int minBufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_7POINT1_SURROUND,
                    AudioFormat.ENCODING_PCM_16BIT
            );

            // 创建AudioAttributes
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            // 创建AudioFormat
            AudioFormat audioFormat = new AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_7POINT1_SURROUND)
                    .build();

            // 初始化AudioTrack，使用更大的缓冲区
            audioTrack = new AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    //.setBufferSizeInBytes(Math.max(minBufferSize * 2, BUFFER_SIZE))
                    .setBufferSizeInBytes(minBufferSize * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .setSessionId(AudioManager.AUDIO_SESSION_ID_GENERATE)
                    .build();

            isPlaying.set(true);
            audioTrack.play();

            // 异步处理音频数据
            executor.execute(() -> {
                Uri audioUri = Uri.parse("content://com.desaysv.assetcontentprovider/" + file);
                byte[] buffer = new byte[BUFFER_SIZE];
                
                do {
                    try (InputStream inputStream = context.getContentResolver().openInputStream(audioUri)) {
                        int bytesRead;
                        int writeResult;
                        while (isPlaying.get() && (bytesRead = inputStream.read(buffer)) != -1) {
                            if (audioTrack != null && audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                                // 使用阻塞模式写入，确保数据完全写入
                                int offset = startTimeInSeconds;
                                writeResult = audioTrack.write(buffer, offset, bytesRead, AudioTrack.WRITE_BLOCKING);
                                //writeResult = audioTrack.write(buffer, bytesRead, AudioTrack.WRITE_BLOCKING);
                                if (writeResult < 0) {
                                    Log.e(TAG, "Error writing to AudioTrack: " + writeResult);
                                    break;
                                }
                                Log.d(TAG, "Written bytes: " + bytesRead);
                            } else {
                                break;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error playing audio: " + e.getMessage());
                        break;
                    }
                } while (isLooping && isPlaying.get());

                // 等待音频播放完成
                if (audioTrack != null && audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                    audioTrack.stop();
                    Log.d(TAG, "Audio playback completed");
                }
                
                if (!isLooping) {
                    stopMedia();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initializing audio: " + e.getMessage());
            stopMedia();
        }
    }

    @Override
    public void pauseMedia() {
        if (audioTrack != null) {
            audioTrack.pause();
        }
    }

    @Override
    public void startMedia() {
        if (audioTrack != null) {
            isPlaying.set(true);
            audioTrack.play();
        }
    }

    @Override
    public void stopMedia() {
        isPlaying.set(false);
        if (audioTrack != null) {
            try {
                // 1. 先暂停播放
                audioTrack.pause();
                
                // 2. 清空缓冲区中的剩余数据
                audioTrack.flush();
                
                // 3. 等待一小段时间确保数据处理完成
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Sleep interrupted: " + e.getMessage());
                }
                
                // 4. 最后停止并释放资源
                audioTrack.stop();
                audioTrack.release();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping audio: " + e.getMessage());
            } finally {
                audioTrack = null;
            }
        }
    }

    @Override
    public void releaseMedia() {
        stopMedia();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    public void setLooping(boolean looping) {
        isLooping = looping;
    }
}
