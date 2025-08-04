package com.desaysv.sceneengine.util.player;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioPlayer extends AbstractMediaPlayer {
    private static final String TAG = "AudioPlayerHelper";
    private static final int SAMPLE_RATE = 48000;
    private static final int BIT_DEPTH = 16;

    // 播放控制相关
    private AudioTrack audioTrack;
    private Thread playbackThread;
    private volatile boolean isPlaying = false;
    private volatile boolean isPaused = false;

    @Override
    public void playMedia(Context context, String file) {
        playMedia(context, file, 0);
    }

    @Override
    public void playMedia(Context context, String file, int startTimeInSeconds) {
        File trackFile = new File("/vendor/etc/Ambient.wav");
        file = trackFile.getAbsolutePath();
        playAudioFile(file, startTimeInSeconds * 1000);
    }

    @Override
    public void pauseMedia() {
        if (isPlaying && !isPaused) {
            isPaused = true;
            if (audioTrack != null) {
                audioTrack.pause();
            }
        }
    }

    @Override
    public void startMedia() {
        if (isPlaying && isPaused) {
            isPaused = false;
            if (audioTrack != null) {
                audioTrack.play();
            }
        }
    }

    @Override
    public void stopMedia() {
        isPlaying = false;
        isPaused = false;

        if (playbackThread != null) {
            try {
                playbackThread.join(100);
            } catch (InterruptedException e) {
                Log.e(TAG, "播放线程中断", e);
            }
            playbackThread = null;
        }

        if (audioTrack != null) {
            try {
                audioTrack.pause();
                audioTrack.flush();
                Thread.sleep(50);
                audioTrack.stop();
                audioTrack.release();
            } catch (Exception e) {
                Log.e(TAG, "AudioTrack释放异常", e);
            }
            audioTrack = null;
        }
    }

    @Override
    public void releaseMedia() {
        stopMedia();
    }

    @Override
    public int getAudioSessionId() {
        return audioTrack != null ? audioTrack.getAudioSessionId() : 0;
    }

    /**
     * 核心播放逻辑
     */
    private void playAudioFile(String filePath, int startPositionMs) {
        stopMedia(); // 先停止之前的播放

        try {
            MediaExtractor extractor = new MediaExtractor();
            extractor.setDataSource(filePath);

            int audioTrackIndex = selectAudioTrack(extractor);
            if (audioTrackIndex == -1) {
                Log.e(TAG, "未找到音频轨道");
                return;
            }

            extractor.selectTrack(audioTrackIndex);
            MediaFormat format = extractor.getTrackFormat(audioTrackIndex);

            // 配置AudioFormat
            AudioFormat audioFormat = new AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_7POINT1_SURROUND)
                    .build();

            int bufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_7POINT1_SURROUND,
                    AudioFormat.ENCODING_PCM_16BIT
            );

            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            audioTrack = new AudioTrack(
                    attributes,
                    audioFormat,
                    bufferSize * 2,
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE
            );

            if (audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
                Log.e(TAG, "AudioTrack初始化失败");
                stopMedia();
                return;
            }

            if (startPositionMs > 0) {
                extractor.seekTo(startPositionMs * 1000L, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
            }

            startPlaybackThread(extractor);
        } catch (IOException e) {
            Log.e(TAG, "播放失败: " + filePath, e);
            stopMedia();
        }
    }

    /**
     * 播放线程
     */
    private void startPlaybackThread(MediaExtractor extractor) {
        isPlaying = true;
        isPaused = false;

        playbackThread = new Thread(() -> {
            ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024);
            audioTrack.play();

            do {
                while (isPlaying) {
                    if (isPaused) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            break;
                        }
                        continue;
                    }

                    int sampleSize = extractor.readSampleData(buffer, 0);
                    if (sampleSize < 0) {
                        // 如果是循环播放，重置到开始位置
                        if (isLooping && isPlaying) {
                            extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                            continue;
                        }
                        break;
                    }

                    int written = audioTrack.write(buffer, sampleSize, AudioTrack.WRITE_BLOCKING);
                    if (written < 0) {
                        Log.e(TAG, "写入AudioTrack失败: " + written);
                        break;
                    }

                    extractor.advance();
                }
            } while (isLooping && isPlaying);

            stopMedia();
        }, "AudioPlaybackThread");

        playbackThread.start();
    }

    /**
     * 选择音频轨道
     */
    private int selectAudioTrack(MediaExtractor extractor) {
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime != null && mime.startsWith("audio/")) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 设置是否循环播放
     *
     * @param looping true表示循环播放，false表示播放一次
     */
    public void setLooping(boolean looping) {
        this.isLooping = looping;
    }
}