package com.desaysv.aisound;

import static com.desaysv.aisound.BaseApplication.BASE_TAG;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.desaysv.aisound.adapter.GalleryAdapter;
import com.desaysv.aisound.adapter.GalleryImg;
import com.desaysv.sceneengine.service.BroadcastManager;
import com.desaysv.sceneengine.service.BroadcastService;
import com.desaysv.sceneengine.socket.SocketAnalysis;
import com.desaysv.sceneengine.socket.SocketConstants;
import com.desaysv.sceneengine.util.PxUtil;
import com.desaysv.sceneengine.util.player.AbstractMediaPlayer;
import com.desaysv.sceneengine.util.player.AudioPlayer;
import com.desaysv.sceneengine.util.player.AudioTrackMediaPlayer;
import com.desaysv.sceneengine.util.player.SystemMediaPlayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = BASE_TAG + "MainActivity";
    private BroadcastService broadcastService;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BroadcastService.LocalBinder binder = (BroadcastService.LocalBinder) service;
            broadcastService = binder.getService();
            // 设置广播接收监听
            broadcastService.setOnSceneDataReceiveListener((action, key, data) -> {
                Log.d(TAG, "收到广播数据: action=" + action + " key=" + key + ", data=" + Arrays.toString(data));
                if (action.equals(BroadcastManager.ACTION_SCENE_BROADCAST_2)) {
                    if ((int) data[0] != 4) {
                        saveCjRecord(0);
                    }
                } else if (action.equals(BroadcastManager.ACTION_SCENE_BROADCAST_3)) {
                    if ((int) data[0] != 4) {
                        saveCjRecord(0);
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            broadcastService = null;
        }
    };

    private LinearLayout nav_yx, nav_cj, nav_bb, nav_cj_action_and_time, nav_cj_action;
    private ImageView nav_cj_action_iv1;
    private Button nav_cj_bt, nav_bb_bt;
    private TextView nav_cj_action_name, nav_cj_progress_time_tv, nav_cj_total_time_tv;

    private LinearLayout m_411;
    private LinearLayout m_411_kscj;
    private Button m_411_kscj_bt;

    private FrameLayout m_412;
    private RelativeLayout m_412_yp_title_1, m_412_yp_title_2, m_412_yp_title_3, m_412_yp_title_4, m_412_yp_title_5, m_412_yp_title_6, m_412_yp_title_7, m_412_yp_title_8;


    private LinearLayout m_413;

    private FrameLayout m_414;
    private LinearLayout m_414_pd_1, m_414_pd_2, m_414_pd_3;

    private FrameLayout m_415, m_415_tab;
    private Button m_415_ajy_bt, m_415_kjqjy_bt;
    private ViewPager2 m_415_viewPager;


    private FrameLayout m_416, m_416_detail;
    private LinearLayout m_416_sure, m_416_volumn, m_416_reset, m_416_detail_opt;
    private Button m_416_ajy_bt, m_416_kjqjy_bt;
    private ImageView m_416_play, m_416_play_process, m_416_sure_iv, m_416_volumn_iv;
    private ViewPager2 m_416_viewPager;
    private TextView m_416_title, m_416_sure_title, m_416_volumn_title;

    private FrameLayout m_417;
    private LinearLayout m_417_detail_opt, m_417_wrzj, m_417_chwq, m_417_zdywz, m_417_volumn, m_417_volumn_0;
    private Button m_417_ajy_bt, m_417_kjqjy_bt;
    private FrameLayout m_417_zc, m_417_zc_xyz, m_417_detail, m_417_viewPager_fl;
    private TextView m_417_title, m_417_wrzj_title, m_417_chwq_title, m_417_zdywz_title;
    private ImageView m_417_wrzj_iv, m_417_chwq_iv, m_417_zdywz_iv;
    private SeekBar m_417_volumn_process;
    private ViewPager2 m_417_viewPager;

    private int beibaoCount = 0;//背包里采集的数量//TODO: 需要设置为0

    private YxActionStatus status = YxActionStatus.Init;
    private List<GalleryImg> imageList = new ArrayList<>();

    ValueAnimator musicPlayAnimator;
    AnimatorSet waveformAnimatorSet;
    AnimatorSet segmentAnimatorSet = new AnimatorSet();

    private long totalElapsedTime = 0;
    private long startTime = 0;
    private boolean isAnimationPaused = false;
    private String playFile = "all.mp3";

    private static AbstractMediaPlayer mediaPlayer = new SystemMediaPlayer();
    private static AbstractMediaPlayer audioTrackMediaPlayer = new AudioPlayer();//new AudioTrackMediaPlayer();//

    private final Coordinate centerPoint = Coordinate.getZcCenter();

    // 背包信息存储记录
    private String fileDir = "/storage/emulated/10/Android/data/com.desaysv.aisound/files/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置全屏显示
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.main);

        initViews();
        initListeners();
        initImageList();

        switchNavTab("nav_cj");

        // 绑定广播服务
        Intent intent = new Intent(this, BroadcastService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    private void initViews() {
        // Find views
        nav_yx = findViewById(R.id.nav_yx);
        nav_cj = findViewById(R.id.nav_cj);
        nav_bb = findViewById(R.id.nav_bb);
        nav_cj_action_and_time = findViewById(R.id.nav_cj_action_and_time);
        nav_cj_action = findViewById(R.id.nav_cj_action);
        nav_cj_action_iv1 = findViewById(R.id.nav_cj_action_iv1);
        nav_cj_action_name = findViewById(R.id.nav_cj_action_name);
        nav_cj_bt = findViewById(R.id.nav_cj_bt);
        nav_cj_progress_time_tv = findViewById(R.id.nav_cj_progress_time_tv);
        nav_cj_total_time_tv = findViewById(R.id.nav_cj_total_time_tv);
        nav_bb_bt = findViewById(R.id.nav_bb_bt);

        m_411 = findViewById(R.id.m_411);
        m_411_kscj = findViewById(R.id.m_411_kscj);
        m_411_kscj_bt = findViewById(R.id.m_411_kscj_bt);

        m_412 = findViewById(R.id.m_412);
        m_412_yp_title_1 = findViewById(R.id.m_412_yp_title_1);
        m_412_yp_title_2 = findViewById(R.id.m_412_yp_title_2);
        m_412_yp_title_3 = findViewById(R.id.m_412_yp_title_3);
        m_412_yp_title_4 = findViewById(R.id.m_412_yp_title_4);
        m_412_yp_title_5 = findViewById(R.id.m_412_yp_title_5);
        m_412_yp_title_6 = findViewById(R.id.m_412_yp_title_6);
        m_412_yp_title_7 = findViewById(R.id.m_412_yp_title_7);
        m_412_yp_title_8 = findViewById(R.id.m_412_yp_title_8);

        //m_413 = findViewById(R.id.m_413);

        m_414 = findViewById(R.id.m_414);
        m_414_pd_1 = findViewById(R.id.m_414_pd_1);
        m_414_pd_2 = findViewById(R.id.m_414_pd_2);
        m_414_pd_3 = findViewById(R.id.m_414_pd_3);

        m_415 = findViewById(R.id.m_415);
        m_415_tab = findViewById(R.id.m_415_tab);
        m_415_ajy_bt = findViewById(R.id.m_415_ajy_bt);
        m_415_kjqjy_bt = findViewById(R.id.m_415_kjqjy_bt);
        m_415_viewPager = findViewById(R.id.m_415_viewPager);


        m_416 = findViewById(R.id.m_416);
        m_416_ajy_bt = findViewById(R.id.m_416_ajy_bt);
        m_416_kjqjy_bt = findViewById(R.id.m_416_kjqjy_bt);
        m_416_play = findViewById(R.id.m_416_play);
        m_416_play_process = findViewById(R.id.m_416_play_process);
        m_416_sure = findViewById(R.id.m_416_sure);
        m_416_sure_iv = findViewById(R.id.m_416_sure_iv);
        m_416_sure_title = findViewById(R.id.m_416_sure_title);
        m_416_volumn = findViewById(R.id.m_416_volumn);
        m_416_volumn_iv = findViewById(R.id.m_416_volumn_iv);
        m_416_volumn_title = findViewById(R.id.m_416_volumn_title);
        m_416_reset = findViewById(R.id.m_416_reset);
        m_416_viewPager = findViewById(R.id.m_416_viewPager);
        m_416_title = findViewById(R.id.m_416_title);
        m_416_detail = findViewById(R.id.m_416_detail);
        m_416_detail_opt = findViewById(R.id.m_416_detail_opt);

        m_417 = findViewById(R.id.m_417);
        m_417_ajy_bt = findViewById(R.id.m_417_ajy_bt);
        m_417_kjqjy_bt = findViewById(R.id.m_417_kjqjy_bt);
        m_417_zc = findViewById(R.id.m_417_zc);
        m_417_zc_xyz = findViewById(R.id.m_417_zc_xyz);
        m_417_detail = findViewById(R.id.m_417_detail);
        m_417_detail_opt = findViewById(R.id.m_417_detail_opt);
        m_417_viewPager_fl = findViewById(R.id.m_417_viewPager_fl);
        m_417_viewPager = findViewById(R.id.m_417_viewPager);
        m_417_title = findViewById(R.id.m_417_title);
        m_417_wrzj = findViewById(R.id.m_417_wrzj);
        m_417_wrzj_iv = findViewById(R.id.m_417_wrzj_iv);
        m_417_wrzj_title = findViewById(R.id.m_417_wrzj_title);
        m_417_chwq = findViewById(R.id.m_417_chwq);
        m_417_chwq_iv = findViewById(R.id.m_417_chwq_iv);
        m_417_chwq_title = findViewById(R.id.m_417_chwq_title);
        m_417_zdywz = findViewById(R.id.m_417_zdywz);
        m_417_zdywz_iv = findViewById(R.id.m_417_zdywz_iv);
        m_417_zdywz_title = findViewById(R.id.m_417_zdywz_title);
        m_417_volumn = findViewById(R.id.m_417_volumn);
        m_417_volumn_process = findViewById(R.id.m_417_volumn_process);
        m_417_volumn_0 = findViewById(R.id.m_417_volumn_0);
        // Initialize data first

        initListeners();
    }

    private void initListeners() {
        nav_yx.setOnClickListener(v -> {
            //finish();
            moveTaskToBack(true);
        });
        nav_cj_bt.setOnClickListener(v -> {
            if (status == YxActionStatus.Cjing || status == YxActionStatus.CjPause) {
                switchNavTab("nav_cj_action");
                // startWaveformAnimation();
            } else {
                switchNavTab("nav_cj");
            }
            if (status != YxActionStatus.Cjing) mediaPlayer.stopMedia();
        });
        nav_bb_bt.setOnClickListener(v -> {
            switchNavTab("nav_bb");

            //TODO: 测试代码，需要关闭
            //testSendBroadcast();
        });
        nav_cj_action.setOnClickListener(v -> {
            Log.d(TAG, "nav_cj_action clicked " + status);
            if (status == YxActionStatus.Init) {
                status = YxActionStatus.CjPause;
                mediaPlayer.pauseMedia();
            } else if (status == YxActionStatus.Cjing) {
                status = YxActionStatus.CjPause;
                mediaPlayer.pauseMedia();
                pauseWaveformAnimation();
            } else if (status == YxActionStatus.CjPause) {
                status = YxActionStatus.Cjing;
                mediaPlayer.startMedia();
                resumeWaveformAnimation();
            } else if (status == YxActionStatus.CJFinish) {
                status = YxActionStatus.CjPause;
                mediaPlayer.playMedia(this, "all.mp3");
                startWaveformAnimation();
                beibaoCount = 0;
            } else if (status == YxActionStatus.CJFinishView) {
                status = YxActionStatus.CJFinishSure;
                mediaPlayer.stopMedia();
            } else if (status == YxActionStatus.CJFinishSure) {
                status = YxActionStatus.Cjing;
                mediaPlayer.playMedia(this, "all.mp3");
                startWaveformAnimation();
                beibaoCount = 0;
            }
            resetCjAction();
        });

        m_411_kscj_bt.setOnClickListener(v -> {
            Log.i(TAG,"m_411_kscj_bt " + status);
            beibaoCount = 0;
            switchNavTab("nav_cj_action_start");
            if (status == YxActionStatus.Init) {
                status = YxActionStatus.Cjing;
                mediaPlayer.playMedia(this, "all.mp3");
                startWaveformAnimation();
            } else if (status == YxActionStatus.Cjing) {
                status = YxActionStatus.CjPause;
                mediaPlayer.pauseMedia();
                pauseWaveformAnimation();
            } else if (status == YxActionStatus.CjPause) {
                status = YxActionStatus.Cjing;
                mediaPlayer.startMedia();
                resumeWaveformAnimation();
            } else if (status == YxActionStatus.CJFinish) {
                status = YxActionStatus.CjPause;
                mediaPlayer.playMedia(this, "all.mp3");
                startWaveformAnimation();
            } else if (status == YxActionStatus.CJFinishView) {
                status = YxActionStatus.CJFinishSure;
                mediaPlayer.stopMedia();
            } else if (status == YxActionStatus.CJFinishSure) {
                status = YxActionStatus.Cjing;
                mediaPlayer.playMedia(this, "all.mp3");
                startWaveformAnimation();
            }
        });
        //Bird song; running; whistle; waterfall; flock of birds; deer; jungle; cow;
        m_412_yp_title_1.setOnClickListener(view -> {
            //switchNavTab("nav_cj_action_finish");
            status = YxActionStatus.CJFinishView;
            //resetCjAction();
            playFile = imageList.get(0).mediaFile;

            createNoteAnimation2(m_412_yp_title_1);
            mediaPlayer.playMedia(this, playFile);
        });
        m_412_yp_title_2.setOnClickListener(view -> {
            //switchNavTab("nav_cj_action_finish");
            status = YxActionStatus.CJFinishView;
            //resetCjAction();
            playFile = imageList.get(1).mediaFile;

            createNoteAnimation2(m_412_yp_title_2);
            mediaPlayer.playMedia(this, playFile);
        });
        m_412_yp_title_3.setOnClickListener(view -> {
            //switchNavTab("nav_cj_action_finish");
            status = YxActionStatus.CJFinishView;
            //resetCjAction();
            playFile = imageList.get(2).mediaFile;

            createNoteAnimation2(m_412_yp_title_3);
            mediaPlayer.playMedia(this, playFile);
        });
        m_412_yp_title_4.setOnClickListener(view -> {
            //switchNavTab("nav_cj_action_finish");
            status = YxActionStatus.CJFinishView;
            //resetCjAction();
            playFile = imageList.get(3).mediaFile;

            createNoteAnimation2(m_412_yp_title_4);
            mediaPlayer.playMedia(this, playFile);
        });
        m_412_yp_title_5.setOnClickListener(view -> {
            //switchNavTab("nav_cj_action_finish");
            status = YxActionStatus.CJFinishView;
            //resetCjAction();
            playFile = imageList.get(4).mediaFile;

            createNoteAnimation2(m_412_yp_title_5);
            mediaPlayer.playMedia(this, playFile);
        });
        m_412_yp_title_6.setOnClickListener(view -> {
            //switchNavTab("nav_cj_action_finish");
            status = YxActionStatus.CJFinishView;
            //resetCjAction();
            playFile = imageList.get(5).mediaFile;

            createNoteAnimation2(m_412_yp_title_6);
            mediaPlayer.playMedia(this, playFile);
        });
        m_412_yp_title_7.setOnClickListener(view -> {
            //switchNavTab("nav_cj_action_finish");
            status = YxActionStatus.CJFinishView;
            //resetCjAction();
            playFile = imageList.get(6).mediaFile;

            createNoteAnimation2(m_412_yp_title_7);
            mediaPlayer.playMedia(this, playFile);
        });
        m_412_yp_title_8.setOnClickListener(view -> {
            //switchNavTab("nav_cj_action_finish");
            status = YxActionStatus.CJFinishView;
            //resetCjAction();
            playFile = imageList.get(7).mediaFile;

            createNoteAnimation2(m_412_yp_title_8);
            mediaPlayer.playMedia(this, playFile);
        });

        m_414_pd_1.setOnClickListener(v -> {
            createNoteAnimation(m_414_pd_1);
            mediaPlayer.playMedia(this, playFile, 0);
        });
        m_414_pd_2.setOnClickListener(v -> {
            createNoteAnimation(m_414_pd_2);
            mediaPlayer.playMedia(this, playFile, 1000);
        });
        m_414_pd_3.setOnClickListener(v -> {
            createNoteAnimation(m_414_pd_3);
            mediaPlayer.playMedia(this, playFile, 3000);
        });

        m_415_kjqjy_bt.setOnClickListener(v -> {
            m_415.setVisibility(View.GONE);
            m_417.setVisibility(View.VISIBLE);

            status = YxActionStatus.KJMainView;
            setM417ViewPaperFrameLayout();
            freshM417Zc();
            setupGalleryM417(-1);
            sendStartDataToMcu();

            mediaPlayer.stopMedia();
            audioTrackMediaPlayer.setLooping(true);  // 设置循环播放
            audioTrackMediaPlayer.playMedia(this, "ambient.wav");
        });

        m_416_ajy_bt.setOnClickListener(v -> {
            m_416.setVisibility(View.GONE);
            m_415.setVisibility(View.VISIBLE);

            setupGalleryM415();
            sendStopDataToMcu();

            mediaPlayer.stopMedia();
            audioTrackMediaPlayer.stopMedia();
        });
        m_416_kjqjy_bt.setOnClickListener(v -> {
            m_416.setVisibility(View.GONE);
            m_417.setVisibility(View.VISIBLE);

            status = YxActionStatus.KJMainView;
            setM417ViewPaperFrameLayout();
            freshM417Zc();
            setupGalleryM417(-1);
            sendStartDataToMcu();

            mediaPlayer.stopMedia();
            audioTrackMediaPlayer.setLooping(true);  // 设置循环播放
            audioTrackMediaPlayer.playMedia(this, "ambient.wav");
        });

        m_416_play.setOnClickListener(v -> {
            if (status == YxActionStatus.MusicPlaying) {
                status = YxActionStatus.Init;
                m_416_play.setImageResource(R.mipmap.yx_416_1);
                if (musicPlayAnimator != null) {
                    musicPlayAnimator.pause();
                }
                mediaPlayer.pauseMedia();
            } else {
                status = YxActionStatus.MusicPlaying;
                m_416_play.setImageResource(R.drawable.yx_416_1_1);
                if (musicPlayAnimator != null) {
                    musicPlayAnimator.resume();
                    mediaPlayer.startMedia();
                } else {
                    int targetWidthDp = 190;

                    // Get current width in pixels
                    int startWidth = m_416_play_process.getWidth();
                    startWidth = 0;// 从0开始

                    // Create width animation
                    musicPlayAnimator = ValueAnimator.ofInt(startWidth, targetWidthDp);
                    musicPlayAnimator.setDuration(3000); // 3 seconds duration
                    musicPlayAnimator.setInterpolator(new LinearInterpolator());

                    musicPlayAnimator.addUpdateListener(animation -> {
                        int width = (int) animation.getAnimatedValue();
                        ViewGroup.LayoutParams params = m_416_play_process.getLayoutParams();
                        params.width = width;
                        m_416_play_process.setLayoutParams(params);
                        if (width >= targetWidthDp) {
                            setMusicPlayEnd(true);
                        }
                    });

                    musicPlayAnimator.start();

                    GalleryAdapter ga = (GalleryAdapter) m_416_viewPager.getAdapter();
                    List<GalleryImg> imageList = ga.getImageList();
                    GalleryImg item = imageList.get(m_416_viewPager.getCurrentItem());
                    mediaPlayer.playMedia(this, (null != item.mediaFile) ? item.mediaFile : "all.mp3");
                }
            }
        });
        m_416_sure.setOnClickListener(v -> {
            m_416_sure.setBackgroundResource(R.mipmap.yx_416_4_2);
            m_416_sure_iv.setVisibility(View.VISIBLE);
            m_416_sure_title.setTextColor(getResources().getColor(R.color.selected_text_color_2, null));

            GalleryAdapter ga = (GalleryAdapter) m_416_viewPager.getAdapter();
            List<GalleryImg> imageList = ga.getImageList();
            GalleryImg item = imageList.get(m_416_viewPager.getCurrentItem());
            for (GalleryImg i : imageList) {
                if (i.id != item.id) {
                    i.isOpen = false;
                } else {
                    i.isOpen = true;
                }
            }

            freshM416(m_416_viewPager.getCurrentItem());

            if (null != item && null != item.mediaFile) setDefaultAudio(item.mediaFile);
        });
        m_416_volumn.setOnClickListener(v -> {
            m_416_volumn.setBackgroundResource(R.mipmap.yx_416_4_2);
            m_416_volumn_iv.setVisibility(View.VISIBLE);
            m_416_volumn_title.setTextColor(getResources().getColor(R.color.selected_text_color_2, null));

            GalleryAdapter ga = (GalleryAdapter) m_416_viewPager.getAdapter();
            List<GalleryImg> imageList = ga.getImageList();
            GalleryImg item = imageList.get(m_416_viewPager.getCurrentItem());
            for (GalleryImg i : imageList) {
                if (i.id != item.id) {
                    i.volumn = 0;
                } else {
                    i.volumn = 1;
                }
            }

            freshM416(m_416_viewPager.getCurrentItem());
        });
        m_416_reset.setOnClickListener(v -> {
            m_416_sure.setBackgroundResource(R.mipmap.yx_416_3);
            m_416_sure_iv.setVisibility(View.GONE);
            m_416_sure_title.setTextColor(getResources().getColor(R.color.white, null));

            m_416_volumn.setBackgroundResource(R.mipmap.yx_416_4);
            m_416_volumn_iv.setVisibility(View.GONE);
            m_416_volumn_title.setTextColor(getResources().getColor(R.color.white, null));

            GalleryAdapter ga = (GalleryAdapter) m_416_viewPager.getAdapter();
            List<GalleryImg> imageList = ga.getImageList();
            GalleryImg item = imageList.get(m_416_viewPager.getCurrentItem());
            for (GalleryImg i : imageList) {
                if (i.id != item.id) {

                } else {
                    i.isOpen = false;
                    i.volumn = 1;
                }
            }

            freshM416(m_416_viewPager.getCurrentItem());
            setDefaultAudio("all.mp3");
        });

        View.OnClickListener ajyClickListener = v -> m_416_ajy_bt.performClick();
        m_416_detail.setOnClickListener(ajyClickListener);
        m_416_detail_opt.setOnClickListener(ajyClickListener);

        m_417_ajy_bt.setOnClickListener(v -> {
            m_417.setVisibility(View.GONE);
            m_415.setVisibility(View.VISIBLE);

            setupGalleryM415();
            sendStopDataToMcu();

            mediaPlayer.stopMedia();
            audioTrackMediaPlayer.stopMedia();

        });
        m_417_kjqjy_bt.setOnClickListener(v -> {
            status = YxActionStatus.KJMainView;
            setM417ViewPaperFrameLayout();
            freshM417Zc();
            setupGalleryM417(-1);
            sendStartDataToMcu();
        });

        m_417_wrzj.setOnClickListener(v -> {
            resetM417Btns("wrzj", !(View.VISIBLE == m_417_wrzj_iv.getVisibility()));

            GalleryAdapter ga = (GalleryAdapter) m_417_viewPager.getAdapter();
            List<GalleryImg> imageList = ga.getImageList();
            GalleryImg item = imageList.get(m_417_viewPager.getCurrentItem());
            for (GalleryImg i : imageList) {
                if (i.id == item.id) {
                    i.mode = 1;
                    i.x = centerPoint.x;
                    i.y = 0;
                    i.z = centerPoint.y;
                }
            }

            freshM417Zc();
            sendDataToMcu();
        });
        m_417_chwq.setOnClickListener(v -> {
            resetM417Btns("chwq", !(View.VISIBLE == m_417_chwq_iv.getVisibility()));

            GalleryAdapter ga = (GalleryAdapter) m_417_viewPager.getAdapter();
            List<GalleryImg> imageList = ga.getImageList();
            GalleryImg item = imageList.get(m_417_viewPager.getCurrentItem());
            for (GalleryImg i : imageList) {
                if (i.id == item.id) {
                    i.mode = 2;
                    i.x = centerPoint.x;
                    i.y = 0;
                    i.z = centerPoint.y;
                }
            }

            freshM417Zc();
            sendDataToMcu();
        });
        m_417_zdywz.setOnClickListener(v -> {
            resetM417Btns("zdywz", !(View.VISIBLE == m_417_zdywz_iv.getVisibility()));

            GalleryAdapter ga = (GalleryAdapter) m_417_viewPager.getAdapter();
            List<GalleryImg> imageList = ga.getImageList();
            GalleryImg item = imageList.get(m_417_viewPager.getCurrentItem());
            for (GalleryImg i : imageList) {
                if (i.id == item.id) {
                    i.mode = 3;
                }
            }

            freshM417Zc();
            sendDataToMcu();
        });
        m_417_volumn_process.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 当进度改变时调用
                // progress: 当前进度值
                // fromUser: 是否由用户操作触发
                Log.d("SeekBar", "Progress: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 当用户开始拖动滑块时调用
                Log.d("SeekBar", "Start tracking");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 当用户停止拖动滑块时调用
                Log.d("SeekBar", "Stop tracking");
                // 获取当前进度
                int progress = seekBar.getProgress();

                GalleryAdapter ga = (GalleryAdapter) m_417_viewPager.getAdapter();
                List<GalleryImg> imageList = ga.getImageList();
                GalleryImg item = imageList.get(m_417_viewPager.getCurrentItem());
                for (GalleryImg i : imageList) {
                    if (i.id == item.id) {
                        i.volumn = progress;
                    }
                }
                //imageList.get(m_417_viewPager.getCurrentItem()).volumn = progress;
                sendDataToMcu();
            }
        });
        m_417_volumn_0.setOnClickListener(v -> {

        });
        View.OnClickListener kjqjyClickListener = v -> m_417_kjqjy_bt.performClick();
        m_417_detail.setOnClickListener(kjqjyClickListener);
        m_417_detail_opt.setOnClickListener(kjqjyClickListener);
    }

    private void setM417ViewPaperFrameLayout() {
        LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) m_417_viewPager_fl.getLayoutParams();
        if (status == YxActionStatus.KJMainView) {
            p.setMarginStart((int) PxUtil.dpTOpx(-2000));
        } else {
            p.setMarginStart((int) PxUtil.dpTOpx(-600));
        }
        m_417_viewPager_fl.setLayoutParams(p);
    }

    private void resetM417Btns(String key, boolean isOn) {
        if ("wrzj".equals(key)) {
            if (!isOn) {
                m_417_wrzj.setBackgroundResource(R.mipmap.yx_416_3);
                m_417_wrzj_iv.setVisibility(View.GONE);
                m_417_wrzj_title.setTextColor(getResources().getColor(R.color.white, null));
            } else {
                m_417_wrzj.setBackgroundResource(R.mipmap.yx_416_4_2);
                m_417_wrzj_iv.setVisibility(View.VISIBLE);
                m_417_wrzj_title.setTextColor(getResources().getColor(R.color.selected_text_color_2, null));

                //其他按钮都取消
                m_417_chwq.setBackgroundResource(R.mipmap.yx_416_3);
                m_417_chwq_iv.setVisibility(View.GONE);
                m_417_chwq_title.setTextColor(getResources().getColor(R.color.white, null));
                m_417_zdywz.setBackgroundResource(R.mipmap.yx_416_3);
                m_417_zdywz_iv.setVisibility(View.GONE);
                m_417_zdywz_title.setTextColor(getResources().getColor(R.color.white, null));
            }
        } else if ("chwq".equals(key)) {
            if (!isOn) {
                m_417_chwq.setBackgroundResource(R.mipmap.yx_416_3);
                m_417_chwq_iv.setVisibility(View.GONE);
                m_417_chwq_title.setTextColor(getResources().getColor(R.color.white, null));
            } else {
                m_417_chwq.setBackgroundResource(R.mipmap.yx_416_4_2);
                m_417_chwq_iv.setVisibility(View.VISIBLE);
                m_417_chwq_title.setTextColor(getResources().getColor(R.color.selected_text_color_2, null));

                //其他按钮都取消
                m_417_wrzj.setBackgroundResource(R.mipmap.yx_416_3);
                m_417_wrzj_iv.setVisibility(View.GONE);
                m_417_wrzj_title.setTextColor(getResources().getColor(R.color.white, null));
                m_417_zdywz.setBackgroundResource(R.mipmap.yx_416_3);
                m_417_zdywz_iv.setVisibility(View.GONE);
                m_417_zdywz_title.setTextColor(getResources().getColor(R.color.white, null));
            }
        } else if ("zdywz".equals(key)) {
            if (!isOn) {
                m_417_zdywz.setBackgroundResource(R.mipmap.yx_416_3);
                m_417_zdywz_iv.setVisibility(View.GONE);
                m_417_zdywz_title.setTextColor(getResources().getColor(R.color.white, null));
            } else {
                m_417_zdywz.setBackgroundResource(R.mipmap.yx_416_4_2);
                m_417_zdywz_iv.setVisibility(View.VISIBLE);
                m_417_zdywz_title.setTextColor(getResources().getColor(R.color.selected_text_color_2, null));

                //其他按钮都取消
                m_417_wrzj.setBackgroundResource(R.mipmap.yx_416_3);
                m_417_wrzj_iv.setVisibility(View.GONE);
                m_417_wrzj_title.setTextColor(getResources().getColor(R.color.white, null));
                m_417_chwq.setBackgroundResource(R.mipmap.yx_416_3);
                m_417_chwq_iv.setVisibility(View.GONE);
                m_417_chwq_title.setTextColor(getResources().getColor(R.color.white, null));
            }
        }
    }

    private void resetCjAction() {
        if (status == YxActionStatus.Init) {
            nav_cj_action_iv1.setImageResource(R.mipmap.yx_412_2_1);
            nav_cj_action_name.setText(R.string.zt);
            m_411_kscj_bt.setText(R.string.zt);
            m_412_yp_title_1.setVisibility(View.GONE);
            m_412_yp_title_2.setVisibility(View.GONE);
            m_412_yp_title_3.setVisibility(View.GONE);
            m_412_yp_title_4.setVisibility(View.GONE);
            m_412_yp_title_5.setVisibility(View.GONE);
            m_412_yp_title_6.setVisibility(View.GONE);
            m_412_yp_title_7.setVisibility(View.GONE);
            m_412_yp_title_8.setVisibility(View.GONE);
        } else if (status == YxActionStatus.Cjing) {
            nav_cj_action_iv1.setImageResource(R.mipmap.yx_412_2_1);
            nav_cj_action_name.setText(R.string.zt);
            m_411_kscj_bt.setText(R.string.zt);
            m_412_yp_title_1.setVisibility(View.GONE);
            m_412_yp_title_2.setVisibility(View.GONE);
            m_412_yp_title_3.setVisibility(View.GONE);
            m_412_yp_title_4.setVisibility(View.GONE);
            m_412_yp_title_5.setVisibility(View.GONE);
            m_412_yp_title_6.setVisibility(View.GONE);
            m_412_yp_title_7.setVisibility(View.GONE);
            m_412_yp_title_8.setVisibility(View.GONE);
        } else if (status == YxActionStatus.CjPause) {
            nav_cj_action_iv1.setImageResource(R.drawable.yx_412_2_4);
            nav_cj_action_name.setText(R.string.jxcj);
            m_411_kscj_bt.setText(R.string.jxcj);
            m_412_yp_title_1.setVisibility(View.GONE);
            m_412_yp_title_2.setVisibility(View.GONE);
            m_412_yp_title_3.setVisibility(View.GONE);
            m_412_yp_title_4.setVisibility(View.GONE);
            m_412_yp_title_5.setVisibility(View.GONE);
            m_412_yp_title_6.setVisibility(View.GONE);
            m_412_yp_title_7.setVisibility(View.GONE);
            m_412_yp_title_8.setVisibility(View.GONE);
        } else if (status == YxActionStatus.CJFinish) {
            nav_cj_action_iv1.setImageResource(R.mipmap.yx_413_1_1);
            nav_cj_action_name.setText(R.string.cxcj);
            m_411_kscj_bt.setText(R.string.cxcj);
            m_412_yp_title_1.setVisibility(View.VISIBLE);
            m_412_yp_title_2.setVisibility(View.VISIBLE);
            m_412_yp_title_3.setVisibility(View.VISIBLE);
            m_412_yp_title_4.setVisibility(View.VISIBLE);
            m_412_yp_title_5.setVisibility(View.VISIBLE);
            m_412_yp_title_6.setVisibility(View.VISIBLE);
            m_412_yp_title_7.setVisibility(View.VISIBLE);
            m_412_yp_title_8.setVisibility(View.VISIBLE);
            m_412.setVisibility(View.VISIBLE);
        } else if (status == YxActionStatus.CJFinishView) {
            nav_cj_action_iv1.setImageResource(R.mipmap.yx_414_3_1);
            nav_cj_action_name.setText(R.string.wc);
            m_411_kscj_bt.setText(R.string.wc);
            m_412_yp_title_1.setVisibility(View.GONE);
            m_412_yp_title_2.setVisibility(View.GONE);
            m_412_yp_title_3.setVisibility(View.GONE);
            m_412_yp_title_4.setVisibility(View.GONE);
            m_412_yp_title_5.setVisibility(View.GONE);
            m_412_yp_title_6.setVisibility(View.GONE);
            m_412_yp_title_7.setVisibility(View.GONE);
            m_412_yp_title_8.setVisibility(View.GONE);
            m_414.setVisibility(View.VISIBLE);
        } else if (status == YxActionStatus.CJFinishSure) {
            nav_cj_action_iv1.setImageResource(R.mipmap.yx_413_1_1);
            nav_cj_action_name.setText(R.string.cxcj);
            m_411_kscj_bt.setText(R.string.cxcj);
            m_412_yp_title_1.setVisibility(View.VISIBLE);
            m_412_yp_title_2.setVisibility(View.VISIBLE);
            m_412_yp_title_3.setVisibility(View.VISIBLE);
            m_412_yp_title_4.setVisibility(View.VISIBLE);
            m_412_yp_title_5.setVisibility(View.VISIBLE);
            m_412_yp_title_6.setVisibility(View.VISIBLE);
            m_412_yp_title_7.setVisibility(View.VISIBLE);
            m_412_yp_title_8.setVisibility(View.VISIBLE);
            m_414.setVisibility(View.GONE);
            m_412.setVisibility(View.VISIBLE);
        }
    }

    private void initImageList() {
        imageList.add(new GalleryImg(R.mipmap.yx_415_5_1, R.mipmap.yx_415_5_3, getString(R.string.yx_1), "yx_1.mp3"));
        imageList.add(new GalleryImg(R.mipmap.yx_415_2_1, R.mipmap.yx_415_2_3, getString(R.string.yx_2), "yx_2.mp3"));
        imageList.add(new GalleryImg(R.mipmap.yx_415_3_1, R.mipmap.yx_415_3_3, getString(R.string.yx_3), "yx_3.mp3"));
        imageList.add(new GalleryImg(R.mipmap.yx_415_4_1, R.mipmap.yx_415_4_3, getString(R.string.yx_4), "yx_4.mp3"));
        imageList.add(new GalleryImg(R.mipmap.yx_415_1_1, R.mipmap.yx_415_1_3, getString(R.string.yx_5), "yx_5.mp3"));
        imageList.add(new GalleryImg(R.mipmap.yx_415_6_1, R.mipmap.yx_415_6_3, getString(R.string.yx_6), "yx_6.mp3"));
        imageList.add(new GalleryImg(R.mipmap.yx_415_7_1, R.mipmap.yx_415_7_3, getString(R.string.yx_7), "yx_7.mp3"));
        imageList.add(new GalleryImg(R.mipmap.yx_415_8_1, R.mipmap.yx_415_8_3, getString(R.string.yx_8), "yx_8.mp3"));
        initImageListProperty();
    }

    private void initImageListProperty() {
        for (GalleryImg item : imageList) {
            item.isOpen = false;
            item.mode = 1;
            if (item.isOpen) {
                item.x = centerPoint.x;
                item.y = 0;
                item.z = centerPoint.y;
            } else {
                item.x = 0;
                item.y = 0;
                item.z = 0;
            }
        }
    }

    private void initM416ImageListProperty() {
        for (GalleryImg item : imageList) {
            item.isOpen = false;
            item.volumn = 0;
        }
    }

    private void initM417ImageListProperty() {
        for (GalleryImg item : imageList) {
            item.isOpen = true;
            item.volumn = 50;
            item.mode = 1;
        }
    }

    private void setupGalleryM415() {
        initImageListProperty();

        // 创建一个新的图片列表用于循环
        List<GalleryImg> infiniteImageList = new ArrayList<>();
        // 添加足够多的重复项以实现无限循环效果
        int repeatCount = 100; // 设置一个较大的重复次数
        for (int i = 0; i < repeatCount; i++) {
            infiniteImageList.addAll(imageList);
        }

        GalleryAdapter adapter = new GalleryAdapter();
        adapter.initImages(infiniteImageList);
        adapter.setHasBtn(false);

        // Set up ViewPager2
        m_415_viewPager.setAdapter(adapter);
        m_415_viewPager.setOffscreenPageLimit(8);
        // 设置初始位置在中间，这样可以向左右滑动
        int startPosition = (repeatCount / 2) * imageList.size();
        m_415_viewPager.setCurrentItem(startPosition, false);

        // Set page transformer for scaling effect
        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer((page, position) -> {
            float absPosition = Math.abs(position);
            // Scale from 1.0 at center to 0.7 at edges
            float scale = Math.max(0.7f, 1f - (absPosition * 0.15f));
            page.setScaleX(scale);
            page.setScaleY(scale);

            // Adjust alpha based on position
            float alpha = Math.max(0.5f, 1f - (absPosition * 0.5f));
            //page.setAlpha(alpha);

            // Add horizontal spacing
            float gap = 450 * position; // Gap between items in dp
            float translation = -page.getWidth() * position;
            if (position > 0) {
                translation += TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, gap, page.getResources().getDisplayMetrics());
            } else {
                translation += TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, gap, page.getResources().getDisplayMetrics());
            }
            page.setTranslationX(translation);
        });
        m_415_viewPager.setPageTransformer(transformer);

        // Add edge effect to prevent over-scrolling
        try {
            Field recyclerViewField = ViewPager2.class.getDeclaredField("mRecyclerView");
            recyclerViewField.setAccessible(true);
            RecyclerView recyclerView = (RecyclerView) recyclerViewField.get(m_415_viewPager);
            if (recyclerView != null) {
                recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
                recyclerView.setClipToPadding(false);
                recyclerView.setPadding(150, 0, 150, 0); // Add padding to show side items

                recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                    private GestureDetector gestureDetector = new GestureDetector(recyclerView.getContext(), new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onSingleTapUp(MotionEvent e) {
                            return true;
                        }
                    });

                    @Override
                    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                        View child = rv.findChildViewUnder(e.getX(), e.getY());
                        if (child != null && gestureDetector.onTouchEvent(e)) {
                            RecyclerView.ViewHolder viewHolder = rv.findContainingViewHolder(child);
                            if (viewHolder != null) {
                                int clickedPosition = viewHolder.getAdapterPosition();
                                int currentPosition = m_415_viewPager.getCurrentItem();
                                Log.i(TAG, "Clicked position: " + clickedPosition + ", Current position: " + currentPosition);

                                // 获取点击位置在屏幕上的坐标
                                float clickX = e.getX();
                                // 获取RecyclerView的中心点X坐标
                                float centerX = rv.getWidth() / 2f;
                                // 计算点击位置与中心的偏移量
                                float offset = Math.abs(clickX - centerX);
                                Log.i(TAG, "Click X: " + clickX + ", Center X: " + centerX + ", Offset: " + offset);

                                // 如果点击位置在中心点附近(允许342dp的误差)
                                if (offset < 342) {
                                    // 点击中心item，进入详情页面
                                    switchNavTab("nav_bb_item");
                                } else {
                                    // 根据点击位置相对于中心的方向决定滑动方向
                                    int direction = clickX > centerX ? 1 : -1;

                                    // 计算需要滑动的格数
                                    int steps = (int) Math.round((offset - 342) / 342);
                                    // 确保至少滑动一格
                                    steps = Math.max(1, steps);

                                    Log.i(TAG, "Direction: " + direction + ", Steps: " + steps + ", Will move: " + (steps * direction) + " positions");

                                    // 滑动指定格数
                                    m_415_viewPager.setCurrentItem(currentPosition + (steps * direction), true);
                                }
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                    }

                    @Override
                    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Register callback to handle edge cases and update visual effects
        m_415_viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.i(TAG, "ViewPager page selected: " + position);

                // 更新所有可见项的缩放和透明度
                RecyclerView recyclerView = (RecyclerView) m_415_viewPager.getChildAt(0);
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View child = recyclerView.getChildAt(i);
                    int childPosition = recyclerView.getChildAdapterPosition(child);
                    if (childPosition != RecyclerView.NO_POSITION) {
                        float scale = childPosition == position ? 1.0f : 0.85f;
                        float alpha = childPosition == position ? 1.0f : 0.25f;
                        child.setScaleX(scale);
                        child.setScaleY(scale);
                        //child.setAlpha(alpha);
                    }
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);

                // 在滑动过程中平滑更新缩放和透明度
                RecyclerView recyclerView = (RecyclerView) m_415_viewPager.getChildAt(0);
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View child = recyclerView.getChildAt(i);
                    int childPosition = recyclerView.getChildAdapterPosition(child);
                    if (childPosition != RecyclerView.NO_POSITION) {
                        if (childPosition == position) {
                            float scale = 1.0f - (0.15f * positionOffset);
                            float alpha = 1.0f - (0.75f * positionOffset);
                            child.setScaleX(scale);
                            child.setScaleY(scale);
                            //child.setAlpha(alpha);
                        } else if (childPosition == position + 1) {
                            float scale = 0.85f + (0.15f * positionOffset);
                            float alpha = 0.25f + (0.75f * positionOffset);
                            child.setScaleX(scale);
                            child.setScaleY(scale);
                            //child.setAlpha(alpha);
                        } else {
                            child.setScaleX(0.85f);
                            child.setScaleY(0.85f);
                            //child.setAlpha(0.25f);
                        }
                    }
                }
            }
        });
    }

    private void setupGalleryM416(int index) {
        Log.i(TAG, "Setup gallery with index: " + index);

        initM416ImageListProperty();

        // 创建一个新的图片列表用于循环
        List<GalleryImg> infiniteImageList = new ArrayList<>();
        // 添加足够多的重复项以实现无限循环效果
        int repeatCount = 100; // 设置一个较大的重复次数
        for (int i = 0; i < repeatCount; i++) {
            infiniteImageList.addAll(imageList);
        }

        GalleryAdapter adapter = new GalleryAdapter();
        adapter.initImages(infiniteImageList);
        adapter.setHasBtn(false);

        // Set up ViewPager2
        m_416_viewPager.setAdapter(adapter);
        m_416_viewPager.setOffscreenPageLimit(8);
        // 设置初始位置在中间，这样可以向左右滑动
        int startPosition = (repeatCount / 2) * imageList.size();
        //m_416_viewPager.setCurrentItem(startPosition, false);
        m_416_viewPager.setCurrentItem(index, false);

        // Set page transformer for scaling effect
        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer((page, position) -> {
            float absPosition = Math.abs(position);
            // Scale from 1.0 at center to 0.7 at edges
            float scale = Math.max(0.7f, 1f - (absPosition * 0.15f));
            page.setScaleX(scale);
            page.setScaleY(scale);

            // Adjust alpha based on position
            float alpha = Math.max(0.5f, 1f - (absPosition * 0.5f));
            //page.setAlpha(alpha);

            // Add horizontal spacing
            float gap = 500; // Gap between items in dp
            float translation = -page.getWidth() * position;
            if (position > 0) {
                translation += TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, gap * position, page.getResources().getDisplayMetrics());
            } else {
                translation += TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, gap * position, page.getResources().getDisplayMetrics());
            }
            page.setTranslationX(translation);
        });
        m_416_viewPager.setPageTransformer(transformer);

        // Add edge effect to prevent over-scrolling
        try {
            Field recyclerViewField = ViewPager2.class.getDeclaredField("mRecyclerView");
            recyclerViewField.setAccessible(true);
            RecyclerView recyclerView = (RecyclerView) recyclerViewField.get(m_416_viewPager);
            if (recyclerView != null) {
                recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
                recyclerView.setClipToPadding(false);
                recyclerView.setPadding(150, 0, 150, 0); // Add padding to show side items

                recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                    private GestureDetector gestureDetector = new GestureDetector(recyclerView.getContext(), new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onSingleTapUp(MotionEvent e) {
                            return true;
                        }
                    });

                    @Override
                    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                        View child = rv.findChildViewUnder(e.getX(), e.getY());
                        if (child != null && gestureDetector.onTouchEvent(e)) {
                            RecyclerView.ViewHolder viewHolder = rv.findContainingViewHolder(child);
                            if (viewHolder != null) {
                                int clickedPosition = viewHolder.getAdapterPosition();
                                int currentPosition = m_416_viewPager.getCurrentItem();
                                Log.i(TAG, "Clicked position: " + clickedPosition + ", Current position: " + currentPosition);

                                // 获取点击位置在屏幕上的坐标
                                float clickX = e.getX();
                                // 获取RecyclerView的中心点X坐标
                                float centerX = rv.getWidth() / 2f;
                                // 计算点击位置与中心的偏移量
                                float offset = Math.abs(clickX - centerX);
                                Log.i(TAG, "Click X: " + clickX + ", Center X: " + centerX + ", Offset: " + offset);

                                // 如果点击位置在中心点附近(允许342dp的误差)
                                if (offset < 342) {
                                } else {
                                    // 根据点击位置相对于中心的方向决定滑动方向
                                    int direction = clickX > centerX ? 1 : -1;

                                    // 计算需要滑动的格数
                                    int steps = (int) Math.round((offset - 342) / 342);
                                    // 确保至少滑动一格
                                    steps = Math.max(1, steps);

                                    Log.i(TAG, "Direction: " + direction + ", Steps: " + steps + ", Will move: " + (steps * direction) + " positions");

                                    // 滑动指定格数
                                    m_416_viewPager.setCurrentItem(currentPosition + (steps * direction), true);
                                }
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                    }

                    @Override
                    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Register callback to handle edge cases and update visual effects
        m_416_viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.i(TAG, "ViewPager page selected: " + position);

                // 更新所有可见项的缩放和透明度
                RecyclerView recyclerView = (RecyclerView) m_416_viewPager.getChildAt(0);
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View child = recyclerView.getChildAt(i);
                    int childPosition = recyclerView.getChildAdapterPosition(child);
                    if (childPosition != RecyclerView.NO_POSITION) {
                        float scale = childPosition == position ? 1.0f : 0.85f;
                        float alpha = childPosition == position ? 1.0f : 0.25f;
                        child.setScaleX(scale);
                        child.setScaleY(scale);
                        //child.setAlpha(alpha);
                    }
                }

                freshM416(position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);

                // 在滑动过程中平滑更新缩放和透明度
                RecyclerView recyclerView = (RecyclerView) m_416_viewPager.getChildAt(0);
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View child = recyclerView.getChildAt(i);
                    int childPosition = recyclerView.getChildAdapterPosition(child);
                    if (childPosition != RecyclerView.NO_POSITION) {
                        if (childPosition == position) {
                            float scale = 1.0f - (0.15f * positionOffset);
                            float alpha = 1.0f - (0.75f * positionOffset);
                            child.setScaleX(scale);
                            child.setScaleY(scale);
                            //child.setAlpha(alpha);
                        } else if (childPosition == position + 1) {
                            float scale = 0.85f + (0.15f * positionOffset);
                            float alpha = 0.25f + (0.75f * positionOffset);
                            child.setScaleX(scale);
                            child.setScaleY(scale);
                            //child.setAlpha(alpha);
                        } else {
                            child.setScaleX(0.85f);
                            child.setScaleY(0.85f);
                            //child.setAlpha(0.25f);
                        }
                    }
                }
            }
        });
    }

    private void setupGalleryM417(int index) {
        setM417ViewPaperFrameLayout();
        initM417ImageListProperty();

        // 创建一个新的图片列表用于循环
        List<GalleryImg> infiniteImageList = new ArrayList<>();
        // 添加足够多的重复项以实现无限循环效果
        int repeatCount = 100; // 设置一个较大的重复次数
        for (int i = 0; i < repeatCount; i++) {
            infiniteImageList.addAll(imageList.subList(0, 4));
        }

        GalleryAdapter adapter = new GalleryAdapter();
        adapter.initImages(infiniteImageList);
        adapter.setHasBtn(true);

        // Set up ViewPager2
        m_417_viewPager.setAdapter(adapter);
        m_417_viewPager.setOffscreenPageLimit(8);
        // 设置初始位置在中间，这样可以向左右滑动
        if (index == -1) {//表示居中
            index = (repeatCount / 2) * 4;
        }
        m_417_viewPager.setCurrentItem(index, false);

        // Set page transformer for scaling effect
        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer((page, position) -> {
            float absPosition = Math.abs(position);
            // Scale from 1.0 at center to 0.7 at edges
            float scale = Math.max(0.7f, 1f - (absPosition * 0.15f));
            page.setScaleX(scale);
            page.setScaleY(scale);

            // Adjust alpha based on position
            float alpha = Math.max(0.5f, 1f - (absPosition * 0.5f));
            //page.setAlpha(alpha);

            // Add horizontal spacing
            float gap = 600; // Gap between items in dp
            float translation = -page.getWidth() * position;
            if (position > 0) {
                translation += TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, gap * position, page.getResources().getDisplayMetrics());
            } else {
                translation += TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, gap * position, page.getResources().getDisplayMetrics());
            }
            page.setTranslationX(translation);
        });
        m_417_viewPager.setPageTransformer(transformer);

        // Add edge effect to prevent over-scrolling
        try {
            Field recyclerViewField = ViewPager2.class.getDeclaredField("mRecyclerView");
            recyclerViewField.setAccessible(true);
            RecyclerView recyclerView = (RecyclerView) recyclerViewField.get(m_417_viewPager);
            if (recyclerView != null) {
                recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
                recyclerView.setClipToPadding(false);
                recyclerView.setPadding(150, 0, 150, 0); // Add padding to show side items

                recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                    private GestureDetector gestureDetector = new GestureDetector(recyclerView.getContext(), new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onSingleTapUp(MotionEvent e) {
                            return true;
                        }
                    });

                    @Override
                    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                        View child = rv.findChildViewUnder(e.getX(), e.getY());
                        if (child != null && gestureDetector.onTouchEvent(e)) {
                            RecyclerView.ViewHolder viewHolder = rv.findContainingViewHolder(child);
                            if (viewHolder != null) {
                                int clickedPosition = viewHolder.getAdapterPosition();
                                int currentPosition = m_417_viewPager.getCurrentItem();
                                Log.i(TAG, "Clicked position: " + clickedPosition + ", Current position: " + currentPosition);

                                // 获取点击位置在屏幕上的坐标
                                float clickX = e.getX();
                                // 获取RecyclerView的中心点X坐标
                                float centerX = rv.getWidth() / 2f;
                                // 计算点击位置与中心的偏移量
                                float offset = Math.abs(clickX - centerX);
                                Log.i(TAG, "Click X: " + clickX + ", Center X: " + centerX + ", Offset: " + offset);

                                // 如果点击位置在中心点附近(允许342dp的误差)
                                if (offset < 342) {
                                    Log.i(TAG, "YxActionStatus=" + status);
                                    if (status == YxActionStatus.KJMainView) {
                                        status = YxActionStatus.KJDetailView;
                                        setupGalleryM417(currentPosition);
                                    } else {
                                        GalleryAdapter ga = (GalleryAdapter) m_417_viewPager.getAdapter();
                                        List<GalleryImg> imageList = ga.getImageList();
                                        GalleryImg item = imageList.get(currentPosition);
                                        boolean isOpen = item.isOpen;
                                        for (GalleryImg i : imageList) {
                                            if (i.id == item.id) {
                                                i.isOpen = !isOpen;
                                                //Log.i(TAG, "check " + item.id + "==" + i.id + " " + i.isOpen);
                                            }
                                        }
                                    }
                                    freshM417(currentPosition);
                                    sendDataToMcu();
                                } else {
                                    // 根据点击位置相对于中心的方向决定滑动方向
                                    int direction = clickX > centerX ? 1 : -1;

                                    // 计算需要滑动的格数
                                    int steps = (int) Math.round((offset - 342) / 342);
                                    // 确保至少滑动一格
                                    steps = Math.max(1, steps);

                                    Log.i(TAG, "Direction: " + direction + ", Steps: " + steps + ", Will move: " + (steps * direction) + " positions");

                                    // 滑动指定格数
                                    m_417_viewPager.setCurrentItem(currentPosition + (steps * direction), true);
                                }
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                    }

                    @Override
                    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Register callback to handle edge cases and update visual effects
        m_417_viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.i(TAG, "ViewPager page selected: " + position);

                // 更新所有可见项的缩放和透明度
                RecyclerView recyclerView = (RecyclerView) m_417_viewPager.getChildAt(0);
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View child = recyclerView.getChildAt(i);
                    int childPosition = recyclerView.getChildAdapterPosition(child);
                    if (childPosition != RecyclerView.NO_POSITION) {
                        float scale = childPosition == position ? 1.0f : 0.85f;
                        float alpha = childPosition == position ? 1.0f : 0.25f;
                        child.setScaleX(scale);
                        child.setScaleY(scale);
                        //child.setAlpha(alpha);
                    }
                }
                freshM417(position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);

                // 在滑动过程中平滑更新缩放和透明度
                RecyclerView recyclerView = (RecyclerView) m_417_viewPager.getChildAt(0);
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View child = recyclerView.getChildAt(i);
                    int childPosition = recyclerView.getChildAdapterPosition(child);
                    if (childPosition != RecyclerView.NO_POSITION) {
                        if (childPosition == position) {
                            float scale = 1.0f - (0.15f * positionOffset);
                            float alpha = 1.0f - (0.75f * positionOffset);
                            child.setScaleX(scale);
                            child.setScaleY(scale);
                            //child.setAlpha(alpha);
                        } else if (childPosition == position + 1) {
                            float scale = 0.85f + (0.15f * positionOffset);
                            float alpha = 0.25f + (0.75f * positionOffset);
                            child.setScaleX(scale);
                            child.setScaleY(scale);
                            //child.setAlpha(alpha);
                        } else {
                            child.setScaleX(0.85f);
                            child.setScaleY(0.85f);
                            //child.setAlpha(0.25f);
                        }
                    }
                }
            }
        });
    }

    private void freshM416(int position) {
        Log.i(TAG, "freshM416 position=" + position);

        GalleryAdapter ga = (GalleryAdapter) m_416_viewPager.getAdapter();
        List<GalleryImg> imageList = ga.getImageList();
        GalleryImg item = imageList.get(position);

        m_416_title.setText(item.name);

        m_416_sure.setBackgroundResource(R.mipmap.yx_416_3);
        m_416_sure_iv.setVisibility(View.GONE);
        m_416_sure_title.setTextColor(getResources().getColor(R.color.white, null));

        m_416_volumn.setBackgroundResource(R.mipmap.yx_416_4);
        m_416_volumn_iv.setVisibility(View.GONE);
        m_416_volumn_title.setTextColor(getResources().getColor(R.color.white, null));

        if (item.isOpen) {
            // 打开确认按钮
            m_416_sure.setBackgroundResource(R.mipmap.yx_416_4_2);
            m_416_sure_iv.setVisibility(View.VISIBLE);
            m_416_sure_title.setTextColor(getResources().getColor(R.color.selected_text_color_2, null));
        }
        if (item.volumn == 1) {
            // 打开音量
            m_416_volumn.setBackgroundResource(R.mipmap.yx_416_4_2);
            m_416_volumn_iv.setVisibility(View.VISIBLE);
            m_416_volumn_title.setTextColor(getResources().getColor(R.color.selected_text_color_2, null));
        }

        RecyclerView recyclerView = (RecyclerView) m_416_viewPager.getChildAt(0);
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View child = recyclerView.getChildAt(i);
            LinearLayout gitem = (LinearLayout) child.findViewById(R.id.gallery_item);
            GalleryImg tmp = (GalleryImg) gitem.getTag(R.id.gallery_item);
            //Log.i(TAG, "freshM416 i=" + i + " " + tmp.id + " " + tmp.isOpen + " " + tmp.volumn);
            if (tmp.isOpen || tmp.volumn == 1) {
                ((FrameLayout) child.findViewById(R.id.gallery_image_bg)).setBackgroundResource(R.mipmap.yx_415_6);
            } else {
                ((FrameLayout) child.findViewById(R.id.gallery_image_bg)).setBackground(null);
            }
        }

        setMusicPlayEnd(false);
    }

    private void freshM417(int position) {
        GalleryAdapter ga = (GalleryAdapter) m_417_viewPager.getAdapter();
        List<GalleryImg> imageList = ga.getImageList();
        GalleryImg item = imageList.get(position);

        // 设置标题
        if (m_417_title != null) m_417_title.setText(item.name);
        // 设置模式
        if (1 == item.mode) {
            resetM417Btns("wrzj", true);
        } else if (2 == item.mode) {
            resetM417Btns("chwq", true);
        } else if (3 == item.mode) {
            resetM417Btns("zdywz", true);
        } else {
            resetM417Btns("wrzj", false);
            resetM417Btns("chwq", false);
            resetM417Btns("zdywz", false);
        }
        // 设置音量
        m_417_volumn_process.setProgress(item.volumn);

        RecyclerView recyclerView = (RecyclerView) m_417_viewPager.getChildAt(0);
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View child = recyclerView.getChildAt(i);
            LinearLayout gitem = (LinearLayout) child.findViewById(R.id.gallery_item);
            GalleryImg tmp = (GalleryImg) gitem.getTag(R.id.gallery_item);
            Log.i(TAG, "freshM417 i=" + i + " " + tmp.id + " " + tmp.isOpen + " " + tmp.volumn);
            if (tmp.isOpen) {
                //((ImageView) rv.getChildAt(pagerPosition).findViewById(R.id.gallery_btn)).setImageResource(imageList.get(pagerPosition).isOpen ? R.mipmap.yx_417_9_2 : R.mipmap.yx_417_9_1);
                ((ImageView) child.findViewById(R.id.gallery_btn)).setImageResource(R.mipmap.yx_417_9_2);
            } else {
                ((ImageView) child.findViewById(R.id.gallery_btn)).setImageResource(R.mipmap.yx_417_9_1);
            }
        }

        freshM417Zc();
    }

    private void setMusicPlayEnd(boolean isAnimationPlayEnd) {
        status = YxActionStatus.Init;
        m_416_play.setImageResource(R.mipmap.yx_416_1);
        if (null != musicPlayAnimator) {
            musicPlayAnimator.cancel();
            musicPlayAnimator = null;
        }
        mediaPlayer.stopMedia();
        if (!isAnimationPlayEnd) {
            ViewGroup.LayoutParams params = m_416_play_process.getLayoutParams();
            params.width = 0;
            m_416_play_process.setLayoutParams(params);
        }
    }

    private void switchNavTab(String nav) {
        //Log.i(TAG, "switchNavTab " + nav);
        // 隐藏所有相关视图
        m_411.setVisibility(View.GONE);
        m_412.setVisibility(View.GONE);
        m_414.setVisibility(View.GONE);
        m_415.setVisibility(View.GONE);
        m_416.setVisibility(View.GONE);
        m_417.setVisibility(View.GONE);

        // 重置所有导航按钮样式
        nav_cj.setBackgroundResource(0);
        nav_bb.setBackgroundResource(0);
        nav_cj_bt.setTextColor(getResources().getColor(android.R.color.white, null) & 0x99FFFFFF);  // 60% opacity
        nav_bb_bt.setTextColor(getResources().getColor(android.R.color.white, null) & 0x99FFFFFF);  // 60% opacity

        // 根据选中的导航设置对应样式和显示相应视图
        if ("nav_cj".equals(nav)) {  // 采集
            nav_cj.setBackgroundResource(R.mipmap.yx_411_2);
            nav_cj_bt.setTextColor(getResources().getColor(R.color.selected_text_color, null));

            nav_cj_action_and_time.setVisibility(View.GONE);
            m_411.setVisibility(View.VISIBLE);

            if (status != YxActionStatus.Cjing) {
                View waveformView1 = findViewById(R.id.m_412_yp);
                View waveformView2 = findViewById(R.id.m_412_yp_second);
                waveformView1.setVisibility(View.GONE);
                waveformView2.setVisibility(View.GONE);
                waveformView1.setTranslationZ(waveformView1.getWidth());
                waveformView2.setTranslationZ(waveformView2.getWidth() * 2);
            }
        } else if ("nav_bb".equals(nav)) {  // 背包
            nav_bb.setBackgroundResource(R.mipmap.yx_411_2);
            nav_bb_bt.setTextColor(getResources().getColor(R.color.selected_text_color, null));
            if (status == YxActionStatus.Cjing || status == YxActionStatus.CjPause) {
                nav_cj_action_and_time.setVisibility(View.VISIBLE);
            } else {
                nav_cj_action_and_time.setVisibility(View.GONE);
            }

            if (beibaoCount > 0) {
                m_415.setVisibility(View.VISIBLE);  // Shows ViewPager2 gallery
                setupGalleryM415();
            } else {
                if (status == YxActionStatus.Cjing) {
                    status = YxActionStatus.CjPause;
                    resetCjAction();
                    mediaPlayer.pauseMedia();
                    pauseWaveformAnimation();
                    m_411.setVisibility(View.VISIBLE);
                } else {
                    m_411.setVisibility(View.VISIBLE);
                }
            }
        } else if ("nav_bb_item".equals(nav)) {
            nav_bb.setBackgroundResource(R.mipmap.yx_411_2);
            nav_bb_bt.setTextColor(getResources().getColor(R.color.selected_text_color, null));

            nav_cj_action_and_time.setVisibility(View.GONE);
            m_416.setVisibility(View.VISIBLE);  // Shows ViewPager2 gallery

            setupGalleryM416(m_415_viewPager.getCurrentItem());
            GalleryAdapter ga = (GalleryAdapter)m_416_viewPager.getAdapter();
            List<GalleryImg> imageList = ga.getImageList();
            GalleryImg item = imageList.get(m_416_viewPager.getCurrentItem());
            m_416_title.setText(item.name);
        } else if ("nav_cj_action".equals(nav)) {
            nav_cj.setBackgroundResource(R.mipmap.yx_411_2);
            nav_cj_bt.setTextColor(getResources().getColor(R.color.selected_text_color, null));
            nav_cj_action.setBackgroundResource(R.mipmap.yx_413_1);
            nav_cj_action_and_time.setVisibility(View.VISIBLE);
            m_412.setVisibility(View.VISIBLE);
        } else if ("nav_cj_action_start".equals(nav)) {
            nav_cj.setBackgroundResource(R.mipmap.yx_411_2);
            nav_cj_bt.setTextColor(getResources().getColor(R.color.selected_text_color, null));

            nav_cj_action.setBackgroundResource(R.mipmap.yx_413_1);
            nav_cj_action_iv1.setImageResource(R.mipmap.yx_412_2_1);
            nav_cj_action_name.setText(R.string.zt);
            nav_cj_action_and_time.setVisibility(View.VISIBLE);
            nav_cj_progress_time_tv.setText("00:00");//设置开始播放时长
            nav_cj_total_time_tv.setText("00:15");//设置最长播放时长

            m_412.setVisibility(View.VISIBLE);

            m_412_yp_title_1.setVisibility(View.GONE);
            m_412_yp_title_2.setVisibility(View.GONE);
            m_412_yp_title_3.setVisibility(View.GONE);
            m_412_yp_title_4.setVisibility(View.GONE);
            m_412_yp_title_5.setVisibility(View.GONE);
            m_412_yp_title_6.setVisibility(View.GONE);
            m_412_yp_title_7.setVisibility(View.GONE);
            m_412_yp_title_8.setVisibility(View.GONE);
        } else if ("nav_cj_action_finish".equals(nav)) {
            nav_cj.setBackgroundResource(R.mipmap.yx_411_2);
            nav_cj_bt.setTextColor(getResources().getColor(R.color.selected_text_color, null));

            nav_cj_action.setBackgroundResource(R.mipmap.yx_413_1);
            nav_cj_action_iv1.setImageResource(R.mipmap.yx_414_3_1);
            nav_cj_action_name.setText(R.string.wc);
            nav_cj_action_and_time.setVisibility(View.VISIBLE);
            m_414.setVisibility(View.VISIBLE);
        }
    }

    private void createNoteAnimation2(View anchorView) {
        // Create an ImageView for the musical note
        ImageView noteView = new ImageView(this);

        // Set initial size of the note
        int noteSize = 600; // Increased size for better visibility
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(noteSize, noteSize);

        // Position the note at the bottom of the anchor view
        int[] location = new int[2];
        anchorView.getLocationInWindow(location);
        //params.leftMargin = location[0] + anchorView.getWidth() / 2 - noteSize / 2;
        //Log.i(TAG, String.format("noteView %s %s %s %s %s", location[0], location[1], anchorView.getX(), anchorView.getWidth(), noteSize));
        params.leftMargin = location[0] + anchorView.getWidth() / 2 - noteSize;
        params.topMargin = location[1];
        params.topMargin = 0;
        noteView.setLayoutParams(params);

        // Add note to the parent FrameLayout (m_414)
        m_412.addView(noteView);

        // Create animation set
        //AnimatorSet segmentAnimatorSet = new AnimatorSet();

        // Translate animation (move up)
        ValueAnimator translateAnim = ValueAnimator.ofFloat(params.topMargin, params.topMargin - 400); // Increased travel distance
        translateAnim.setDuration(5000); // Increased duration
        final int[] n = {0};
        n[0] = (int) (Math.random() * 120);
        translateAnim.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            params.topMargin = (int) value;
            //noteView.setLayoutParams(params);

            if (n[0] > 119) n[0] = 0;
            String in = "yf_" + String.format("%02d", n[0]++);
            int rid = getResources().getIdentifier(in, "drawable", getPackageName());
            noteView.setImageResource(rid);
        });

        // Alpha animation (fade out)
        ValueAnimator alphaAnim = ValueAnimator.ofFloat(1.0f, 0f);
        alphaAnim.setDuration(5000);
        alphaAnim.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            noteView.setAlpha(value);
        });

        // Clear
        segmentAnimatorSet.cancel();
        segmentAnimatorSet.removeAllListeners();

        // Play animations together
        segmentAnimatorSet.playTogether(translateAnim, alphaAnim);
        segmentAnimatorSet.setInterpolator(new LinearInterpolator());

        // Remove view after animation
        segmentAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                m_412.removeView(noteView);
            }
        });

        segmentAnimatorSet.start();
    }

    private void createNoteAnimation(View anchorView) {
        // Create an ImageView for the musical note
        ImageView noteView = new ImageView(this);

        // Set initial size of the note
        int noteSize = 600; // Increased size for better visibility
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(noteSize, noteSize);

        // Position the note at the bottom of the anchor view
        int[] location = new int[2];
        anchorView.getLocationInWindow(location);
        //params.leftMargin = location[0] + anchorView.getWidth() / 2 - noteSize / 2;
        //Log.i(TAG, String.format("noteView %s %s %s %s %s", location[0], location[1], anchorView.getX(), anchorView.getWidth(), noteSize));
        params.leftMargin = location[0] + anchorView.getWidth() / 2 - noteSize;
        params.topMargin = location[1];
        params.topMargin = 0;
        noteView.setLayoutParams(params);

        // Add note to the parent FrameLayout (m_414)
        m_414.addView(noteView);

        // Create animation set
        AnimatorSet animatorSet = new AnimatorSet();

        // Translate animation (move up)
        ValueAnimator translateAnim = ValueAnimator.ofFloat(params.topMargin, params.topMargin - 400); // Increased travel distance
        translateAnim.setDuration(5000); // Increased duration
        final int[] n = {0};
        n[0] = (int) (Math.random() * 120);
        translateAnim.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            params.topMargin = (int) value;
            //noteView.setLayoutParams(params);

            if (n[0] > 119) n[0] = 0;
            String in = "yf_" + String.format("%02d", n[0]++);
            int rid = getResources().getIdentifier(in, "drawable", getPackageName());
            noteView.setImageResource(rid);
        });

        // Alpha animation (fade out)
        ValueAnimator alphaAnim = ValueAnimator.ofFloat(1.0f, 0f);
        alphaAnim.setDuration(5000);
        alphaAnim.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            noteView.setAlpha(value);
        });

        // Play animations together
        animatorSet.playTogether(translateAnim, alphaAnim);
        animatorSet.setInterpolator(new LinearInterpolator());

        // Remove view after animation
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                m_414.removeView(noteView);
            }
        });

        animatorSet.start();
    }

    // 开启波纹动画
    private void startWaveformAnimation() {
        isAnimationPaused = false;
        totalElapsedTime = 0;
        startTime = 0;
        if (waveformAnimatorSet != null) {
            waveformAnimatorSet.cancel();
        }

        View waveformView1 = findViewById(R.id.m_412_yp);
        View waveformView2 = findViewById(R.id.m_412_yp_second);
        waveformView1.setVisibility(View.VISIBLE);
        waveformView2.setVisibility(View.VISIBLE);
        waveformView1.post(() -> {
            int width = waveformView1.getWidth();

            // 初始化位置
            waveformView1.setTranslationX(width);
            waveformView2.setTranslationX(width * 2);
            // 第一个波形图动画
            ValueAnimator wave1Move = ValueAnimator.ofFloat(0f, -width * 2);
            wave1Move.setDuration(15000);
            wave1Move.setInterpolator(new LinearInterpolator());
            wave1Move.setRepeatCount(ValueAnimator.INFINITE);
            wave1Move.addUpdateListener(animation -> {
                if (!isAnimationPaused) {
                    float value = (float) animation.getAnimatedValue();
                    waveformView1.setTranslationX(value + width);
                }
            });

            // 第二个波形图动画
            ValueAnimator wave2Move = ValueAnimator.ofFloat(0f, -width * 2);
            wave2Move.setDuration(15000);
            wave2Move.setInterpolator(new LinearInterpolator());
            wave2Move.setRepeatCount(ValueAnimator.INFINITE);
            wave2Move.addUpdateListener(animation -> {
                if (!isAnimationPaused) {
                    float value = (float) animation.getAnimatedValue();
                    waveformView2.setTranslationX(value + width * 2);
                    //Log.i(TAG,"onAnimationUpdate wave2Move " + value + "," + width);
                }
            });

            // 添加动画监听器
            wave1Move.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                private boolean needsReset = false;
                private float lastValue = 0f;
                private long lastResetTime = 0;
                private boolean firstRoundCompleted = false;

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (isAnimationPaused) {
                        return;
                    }

                    float value = (float) animation.getAnimatedValue();
                    long currentTime = System.currentTimeMillis();

                    if (startTime == 0) {
                        startTime = currentTime;
                    }
                    //Log.i(TAG,"onAnimationUpdate wave1Move " + value + "," + width);
                    if (value <= -width * 2 && !needsReset && value < lastValue && (currentTime - lastResetTime > 50)) {
                        needsReset = true;
                        lastResetTime = currentTime;

                        if (!firstRoundCompleted) {
                            firstRoundCompleted = true;
                        }
                    } else if (value > -width * 2) {
                        needsReset = false;
                    }
                    lastValue = value;

                    // 更新进度时间显示和总时间
                    if (!isAnimationPaused) {
                        long newElapsedTime = totalElapsedTime + (currentTime - startTime);
                        int progressSeconds = (int) (newElapsedTime / 1000);
                        String timeStr = String.format("%02d:%02d", progressSeconds / 60, progressSeconds % 60);
                        nav_cj_progress_time_tv.setText(timeStr);
                        Log.i(TAG, String.format("更新进度时间显示和总时间 %s,%s,%s,%s,%s", timeStr, progressSeconds, newElapsedTime, totalElapsedTime, startTime));
                        // 在15秒时停止动画
                        if (newElapsedTime >= 15000) {
                            stopWaveformAnimation();
                            status = YxActionStatus.CJFinish;

                            mediaPlayer.stopMedia();
                            resetCjAction();

                            //记录采集的数量并写入文件
                            saveCjRecord(8);
                        }
                    }
                }
            });

            // 重置开始时间
            startTime = System.currentTimeMillis();

            // 创建动画集合并启动
            waveformAnimatorSet = new AnimatorSet();
            waveformAnimatorSet.playTogether(wave1Move, wave2Move);
            waveformAnimatorSet.start();
        });
    }

    private void saveCjRecord(int count) {
        beibaoCount = count;
        String saveData = String.format("%d", beibaoCount);
        //保存数据至
        String fileName = "cj_record.txt";
        // 1. 获取文件路径
        File filesDir = getExternalFilesDir(fileDir);
        if (filesDir == null) {
            Log.e(TAG, "无法获取文件目录");
            return;
        }
        String filePath = filesDir.getAbsolutePath() + "/" + fileName;

        // 2. 写入数据
        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.write(saveData + "\n");
        } catch (IOException e) {
            Log.e(TAG, "写入文件失败", e);
        }

        //TODO: 测试代码，需要关闭
        // 3. 读取数据
//        try (FileReader reader = new FileReader(filePath)) {
//            BufferedReader bufferedReader = new BufferedReader(reader);
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                Log.i(TAG, "读取文件数据: " + line);
//            }
//        } catch (IOException e) {
//            Log.e(TAG, "读取文件失败", e);
//        }
    }

    // 暂停波纹动画
    private void pauseWaveformAnimation() {
        if (!isAnimationPaused && startTime > 0) {
            isAnimationPaused = true;
            // 保存已经播放的时间
            totalElapsedTime += System.currentTimeMillis() - startTime;

            if (waveformAnimatorSet != null) {
                waveformAnimatorSet.pause();
            }
        }
    }

    // 恢复波纹动画
    private void resumeWaveformAnimation() {
        if (isAnimationPaused) {
            isAnimationPaused = false;
            startTime = System.currentTimeMillis();
            if (waveformAnimatorSet != null) {
                waveformAnimatorSet.resume();
            }
        }
    }

    // 停止波纹动画
    private void stopWaveformAnimation() {
        isAnimationPaused = true;
        totalElapsedTime = 0;
        startTime = 0;
        if (waveformAnimatorSet != null) {
            waveformAnimatorSet.cancel();
            waveformAnimatorSet = null;
        }
    }

    private void setDefaultAudio(String file) {
        Log.i(TAG, "setDefaultAudio " + file);
        // 设置当前音频为系统默认音频
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                // 如果没有权限，请求权限
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // 如果找不到对应的Activity，尝试使用ACTION_SETTINGS
                    //Intent fallbackIntent = new Intent(Settings.ACTION_SETTINGS);
                    //startActivity(fallbackIntent);
                    //Toast.makeText(this, "请在系统设置中手动授予修改系统设置权限", Toast.LENGTH_LONG).show();
                }
            } else {
                // 有权限，执行设置操作
                Log.i(TAG, "setDefaultAudio 1");
                try {
                    // 构建音频文件的Uri
                    Uri audioUri = Uri.parse("content://com.desaysv.assetcontentprovider/" + file);
                    Log.i(TAG, "setDefaultAudio 2");
                    // 设置为系统默认音频
                    RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, audioUri);
                    Log.i(TAG, "已设置为系统默认音频");
                } catch (Exception e) {
                    Log.e(TAG, "设置系统默认音频失败", e);
                }
            }
        }
    }

    // 刷新座舱
    private void freshM417Zc() {
        // 关闭座舱
        if (status == YxActionStatus.KJMainView) {
            m_417_zc.setAlpha(0.4f);
            m_417_zc.removeAllViews();
            m_417_zc_xyz.setVisibility(View.GONE);
            m_417_detail.setVisibility(View.GONE);
            m_417_detail_opt.setVisibility(View.GONE);
            return;
        }

        m_417_detail.setVisibility(View.VISIBLE);
        m_417_detail_opt.setVisibility(View.VISIBLE);

        boolean open = false;

        GalleryAdapter ga = (GalleryAdapter) m_417_viewPager.getAdapter();
        List<GalleryImg> imageList = ga.getImageList();
        //m_417_zc.removeAllViews();
        int c = m_417_zc.getChildCount();
        List<View> delList = new ArrayList<View>();
        List<View> addList = new ArrayList<View>();
        for (GalleryImg item : imageList) {
            if (item.isOpen) {
                //Log.i(TAG, "zc mode=" + item.mode + " xyz:" + item.x + " " + item.y + " " + item.z);
                //已开启，当mode=1|2时，重置坐标
                if (3 != item.mode) {
                    item.x = centerPoint.x;
                    item.y = 0;
                    item.z = centerPoint.y;
                }

                boolean had = false;
                for (int i = 0; i < c; i++) {
                    View v = m_417_zc.getChildAt(i);
                    //Log.i(TAG, "zc " + v.getId() + "==" + item.id + " tag:" + v.getTag());
                    if (v.getId() == item.id || v.getTag() == item.name) {
                        had = true;
                        break;
                    }
                }
                if (!had) {
                    TextView titleTextView = new TextView(this);
                    titleTextView.setTextSize(18); // 设置字体大小
                    titleTextView.setTextColor(getColor(R.color.white)); // 设置文本颜色
                    titleTextView.setGravity(Gravity.CENTER); // 设置文本居中对齐
                    titleTextView.setTypeface(null, Typeface.BOLD);// 设置字体样式为加粗
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(96, 40);
                    titleTextView.setLayoutParams(layoutParams);
                    titleTextView.setBackgroundResource(R.mipmap.yx_417_7_1);
                    titleTextView.setId(item.id);
                    titleTextView.setTranslationZ(3f);
                    titleTextView.setText(item.name);
                    titleTextView.setTag(item.name);

                    if (3 == item.mode) {
                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) titleTextView.getLayoutParams();
                        params.leftMargin = item.x - (int) (PxUtil.dpTOpx(96) / 2);
                        params.topMargin = item.z - (int) (PxUtil.dpTOpx(40) / 2);
                        titleTextView.setLayoutParams(params);
                    } else {
                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) titleTextView.getLayoutParams();
                        params.leftMargin = centerPoint.x - (int) (PxUtil.dpTOpx(96) / 2);
                        params.topMargin = centerPoint.y - (int) (PxUtil.dpTOpx(40) / 2);
                        titleTextView.setLayoutParams(params);
                    }

                    addList.add(titleTextView);
                }
                open = true;
            } else {
                for (int i = 0; i < c; i++) {
                    View v = m_417_zc.getChildAt(i);
                    if (v.getId() == item.id) {
                        delList.add(v);
                    }
                }
                //未开启，重置坐标
                item.x = centerPoint.x;
                item.y = 0;
                item.z = centerPoint.y;
            }
        }

        if (!delList.isEmpty()) {
            delList.stream().forEach(v -> {
                m_417_zc.removeView(v);
            });
        }
        if (!addList.isEmpty()) {
            List<View> unique = addList.stream().collect(Collectors.toMap(View::getId, // key 是 name
                            item -> item, // value 是 Person 对象
                            (existing, replacement) -> existing)) // 如果有重复，保留第一个
                    .values().stream().collect(Collectors.toList());

            unique.forEach(v -> {
                m_417_zc.addView(v);
            });
        }

        // 设置透明度
        //m_417_zc.setAlpha(open ? 1f : 0.4f);
        if (open) {
            m_417_zc.setAlpha(1f);
            // 显示坐标
            m_417_zc_xyz.setVisibility(View.VISIBLE);
        } else {
            m_417_zc.setAlpha(0.4f);
            m_417_zc_xyz.setVisibility(View.GONE);
        }
    }

    private void sendStartDataToMcu() {
        SocketAnalysis.getInstance().analysisMsgToPC(SocketConstants.Request.ORDER_AR_HUD, buildMeg(3));
        //同时发广播
        broadcastService.sendBroadcast("ACTION_SVSOUND_ENTRY", "sessionid", "" + audioTrackMediaPlayer.getAudioSessionId());
    }

    private void sendStopDataToMcu() {
        // 判断是否有进入过空间全景音，有则发出退出信号，无则无需发送
        if (m_417_viewPager.getAdapter() == null) {
            return;
        }
        SocketAnalysis.getInstance().analysisMsgToPC(SocketConstants.Request.ORDER_AR_HUD, buildMeg(1));
        //同时发广播
        broadcastService.sendBroadcast("ACTION_SVSOUND_EXIT", null, null);
    }

    private void sendDataToMcu() {
        SocketAnalysis.getInstance().analysisMsgToPC(SocketConstants.Request.ORDER_AR_HUD, buildMeg(3));
    }

    public int[] buildMeg(int command) {
        // 取4个通道的开关
        GalleryImg td;
        String openStr = "";
        int[] mode = new int[]{1, 1, 1, 1};
        int[] volumn = new int[]{0, 0, 0, 0};
        int[] x = new int[]{0, 0, 0, 0};
        int[] y = new int[]{0, 0, 0, 0};
        int[] z = new int[]{0, 0, 0, 0};
        GalleryAdapter ga = (GalleryAdapter) m_417_viewPager.getAdapter();
        List<GalleryImg> imageList = ga.getImageList();
        for (int i = 0; i < 4; i++) {
            td = imageList.get(i);
            if (td.isOpen) {
                openStr = "1" + openStr;
            } else {
                openStr = "0" + openStr;
            }
            mode[i] = td.mode;
            volumn[i] = td.volumn;
            x[i] = td.x;
            y[i] = td.y;
            z[i] = td.z;
        }
        int[] msg = new int[22];
        //选项1
        msg[0] = command;
        msg[1] = Integer.parseInt(openStr, 2);
        //选项2
        msg[2] = volumn[0];
        msg[3] = volumn[1];
        msg[4] = volumn[2];
        msg[5] = volumn[3];
        //选项3
        msg[6] = mode[0];
        msg[7] = mode[1];
        msg[8] = mode[2];
        msg[9] = mode[3];
        //选项4
        int maxX = (int) PxUtil.dpTOpx(980);
        int maxZ = (int) PxUtil.dpTOpx(524);
        int textMaxX = (int) PxUtil.dpTOpx(96);
        int textMaxZ = (int) PxUtil.dpTOpx(40);
        // 微调
        if (msg[10] > maxX) {
            msg[10] = maxX;
        }
        if (msg[13] > maxX) {
            msg[13] = maxX;
        }
        if (msg[16] > maxX) {
            msg[16] = maxX;
        }
        if (msg[19] > maxX) {
            msg[19] = maxX;
        }
        if (msg[12] > maxZ) {
            msg[12] = maxZ;
        }
        if (msg[15] > maxZ) {
            msg[15] = maxZ;
        }
        if (msg[18] > maxZ) {
            msg[18] = maxZ;
        }
        if (msg[21] > maxZ) {
            msg[21] = maxZ;
        }
        //Log.i(TAG,"AnalysisMsgToPC " + (255 - (int)(255*x[0]/maxX)) + " "  );
        msg[10] = 255 - (int) (255 * x[0] / maxX);
        msg[11] = 0;
        msg[12] = 255 - (int) (255 * z[0] / maxZ);
        //选项5
        msg[13] = 255 - (int) (255 * x[1] / maxX);
        msg[14] = 0;
        msg[15] = 255 - (int) (255 * z[1] / maxZ);
        //选项6
        msg[16] = 255 - (int) (255 * x[2] / maxX);
        msg[17] = 0;
        msg[18] = 255 - (int) (255 * z[2] / maxZ);
        //选项7
        msg[19] = 255 - (int) (255 * x[3] / maxX);
        msg[20] = 0;
        msg[21] = 255 - (int) (255 * z[3] / maxZ);

        //重新计算4个通道的y轴值
        msg[11] = (int) 255 * (msg[10] * msg[10] + msg[12] * msg[12]) / (2 * 255 * 255);
        msg[14] = (int) 255 * (msg[13] * msg[13] + msg[15] * msg[15]) / (2 * 255 * 255);
        msg[17] = (int) 255 * (msg[16] * msg[16] + msg[18] * msg[18]) / (2 * 255 * 255);
        msg[20] = (int) 255 * (msg[19] * msg[19] + msg[21] * msg[21]) / (2 * 255 * 255);
        return msg;
    }

    public void updateImageCoordinates(int imageId, float newX, float newY) {
        //Log.i(TAG, "updateImageCoordinates");
        GalleryAdapter ga = (GalleryAdapter) m_417_viewPager.getAdapter();
        List<GalleryImg> imageList = ga.getImageList();
        for (GalleryImg image : imageList) {
            if (image.id == imageId) {
                image.x = (int) newX + (int) (PxUtil.dpTOpx(96) / 2);
                image.z = (int) newY + +(int) (PxUtil.dpTOpx(40) / 2);
                //Log.i(TAG, "updateImageCoordinates id:x,y=" + image.id + ":" + image.x + "," + image.y);
                image.mode = 3;
                // 设置模式
                resetM417Btns("wrzj", false);
                resetM417Btns("chwq", false);
                resetM417Btns("zdywz", true);
                break;
            }
        }
        sendDataToMcu();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 解绑服务
        if (broadcastService != null) {
            unbindService(serviceConnection);
        }
        if (waveformAnimatorSet != null) {
            waveformAnimatorSet.cancel();
            waveformAnimatorSet = null;
        }
        mediaPlayer.releaseMedia();
        audioTrackMediaPlayer.releaseMedia();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void testSendBroadcast() {
        broadcastService.sendBroadcast("com.desaysv.sceneengine.ACTION_SCENE_CHANGE_TOAPP", "data", "5");
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAllAnimations();
        mediaPlayer.pauseMedia();
        audioTrackMediaPlayer.pauseMedia();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initAnimationVariables();
        initPage();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopAllAnimations();
        mediaPlayer.pauseMedia();
        audioTrackMediaPlayer.pauseMedia();
    }

    /**
     * 停止所有动画
     */
    private void stopAllAnimations() {
        stopWaveformAnimation();
        
        if (segmentAnimatorSet != null) {
            segmentAnimatorSet.cancel();
        }
        
        if (musicPlayAnimator != null) {
            musicPlayAnimator.cancel();
        }

        View waveformView1 = findViewById(R.id.m_412_yp);
        View waveformView2 = findViewById(R.id.m_412_yp_second);
        waveformView1.setVisibility(View.GONE);
        waveformView2.setVisibility(View.GONE);
    }

    /**
     * 初始化动画相关变量
     */
    private void initAnimationVariables() {
        isAnimationPaused = false;
        totalElapsedTime = 0;
        startTime = 0;
    }

    private void initPage(){
        status = YxActionStatus.Init;
        nav_cj_progress_time_tv.setText("00:00");
        switchNavTab("nav_cj");
    }
}