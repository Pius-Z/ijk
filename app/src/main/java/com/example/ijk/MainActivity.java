package com.example.ijk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.net.TrafficStats;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import tv.danmaku.ijk.media.player.IMediaPlayer;

public class MainActivity extends AppCompatActivity implements VideoListener{

    private VideoPlayer videoPlayer1;
    private VideoPlayer videoPlayer2;
    private VideoPlayer videoPlayer3;

    private TextView speed;

    private long lastTotalRxBytes = 0;
    private long lastTimeStamp = 0;

    Handler mHandler = new MyHandler(this);

    private boolean[] state;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoPlayer1 = findViewById(R.id.video_view1);

        videoPlayer1.setVideoListener(this);
        videoPlayer1.setPath("rtsp://codeshark.top:8554/test");
        // rtsp://codeshark.top:8554/test   1.116.191.98
        // http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear3/prog_index.m3u8


        try {
            videoPlayer1.load();
        } catch (IOException e) {
            Toast.makeText(this,"播放失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }


        ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);

        pool.scheduleAtFixedRate(this::showSpeed, 0, 800, TimeUnit.MILLISECONDS);

    }

    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {

    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {

    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        videoPlayer1.start();
    }

    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {

    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {

    }

    private class MyHandler extends Handler {
        //持有弱引用MainActivity,GC回收时会被回收掉.
        private final WeakReference<MainActivity> mActivty;

        public MyHandler(MainActivity activity) {
            mActivty = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivty.get();
            if (activity != null) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 100:
                        speed = findViewById(R.id.speed);
                        speed.setText((String)msg.obj);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void showSpeed() {

        long nowTotalRxBytes = TrafficStats.getUidRxBytes(this.getApplicationInfo().uid); // uid: android.os.Process.myUid()
        long nowTimeStamp = System.currentTimeMillis();

        // 计算网速，单位 KB/S
        long speedInteger = ((nowTotalRxBytes - lastTotalRxBytes) / (nowTimeStamp - lastTimeStamp)); // 整数部分
        long speedDecimal = ((nowTotalRxBytes - lastTotalRxBytes) % (nowTimeStamp - lastTimeStamp)); // 小数部分

        // 更新时间戳与上个时间戳传输总字节数
        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;

        // 更新速度显示
        Message msg = mHandler.obtainMessage();
        msg.what = 100;
        if (speedInteger == 0)
            msg.obj = speedDecimal + " B/s";
        else if (speedInteger < 1024)
            msg.obj = speedInteger + "." + speedDecimal / 102 + " KB/s";
        else
            msg.obj = speedInteger / 1024 + "." + speedDecimal / 10 + " MB/S";
        mHandler.sendMessage(msg);//更新界面
    }

    public void singleScreen(View view) {
        // 从双窗口或三窗口转回时，停止其他播放并将其view隐藏
        if (videoPlayer2 != null && videoPlayer2.getVisibility() == View.VISIBLE)
        {
            videoPlayer2.stop();
            videoPlayer2.setVisibility(View.GONE);
        }

        if (videoPlayer3 != null && videoPlayer3.getVisibility() == View.VISIBLE) {
            videoPlayer3.stop();
            videoPlayer3.setVisibility(View.GONE);
        }


        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) videoPlayer1.getLayoutParams();
        layoutParams.verticalBias = (float) 0.5;
        videoPlayer1.setLayoutParams(layoutParams);
    }

    public void doubleScreen(View view) {

        // 从三窗口转回时，停止videoPlayer3播放，并将其view隐藏
        if (videoPlayer3 != null && videoPlayer3.getVisibility() == View.VISIBLE)
        {
            videoPlayer3.stop();
            videoPlayer3.setVisibility(View.GONE);
        }

        // 为空时创建，非空时直接加载
        if (videoPlayer2 == null)
            createVideo2();
        else if (videoPlayer2.getVisibility() == View.GONE)
            try {
                videoPlayer2.load();
            } catch (IOException e) {
                Toast.makeText(this,"播放失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        // 设置双窗口下videoPlayer1的位置
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) videoPlayer1.getLayoutParams();
        layoutParams.verticalBias = (float) 0.3;
        videoPlayer1.setLayoutParams(layoutParams);

        // 设置双窗口下videoPlayer2位置
        layoutParams = (ConstraintLayout.LayoutParams)videoPlayer2.getLayoutParams();
        layoutParams.verticalBias = (float) 0.762;
        videoPlayer2.setVisibility(View.VISIBLE);
    }

    public void multiScreen(View view) {
        // 为空时创建，非空时直接加载
        if (videoPlayer2 == null)
            createVideo2();
        else if (videoPlayer2.getVisibility() == View.GONE)
            try {
                videoPlayer2.load();
            } catch (IOException e) {
                Toast.makeText(this,"播放失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        if (videoPlayer3 == null)
            createVideo3();
        else if (videoPlayer3.getVisibility() == View.GONE)
            try {
                videoPlayer3.load();
            } catch (IOException e) {
                Toast.makeText(this, "播放失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        // 设置三窗口下videoPlayer1的位置
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) videoPlayer1.getLayoutParams();
        layoutParams.verticalBias = (float) 0.076;
        videoPlayer1.setLayoutParams(layoutParams);

        // 设置三窗口下videoPlayer2位置
        layoutParams = (ConstraintLayout.LayoutParams)videoPlayer2.getLayoutParams();
        layoutParams.verticalBias = (float) 0.538;
        videoPlayer2.setVisibility(View.VISIBLE);

        // 设置三窗口下videoPlayer3位置
        layoutParams = (ConstraintLayout.LayoutParams)videoPlayer3.getLayoutParams();
        layoutParams.verticalBias = (float) 1;
        videoPlayer3.setVisibility(View.VISIBLE);
    }
    //0.076 0.538 1

    public void createVideo2 () {
        videoPlayer2 = findViewById(R.id.video_view2);
        videoPlayer2.setVideoListener(this);
        videoPlayer2.setPath("rtsp://codeshark.top:8554/test");

        try {
            videoPlayer2.load();
        } catch (IOException e) {
            Toast.makeText(this,"播放失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void createVideo3 () {
        videoPlayer3 = findViewById(R.id.video_view3);
        videoPlayer3.setVideoListener(this);
        videoPlayer3.setPath("rtsp://codeshark.top:8554/test");

        try {
            videoPlayer3.load();
        } catch (IOException e) {
            Toast.makeText(this,"播放失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void setIP(View view) {
        // 隐藏按钮
        Button button1 =findViewById(R.id.button1);
        button1.setVisibility(View.GONE);
        Button button2 =findViewById(R.id.button2);
        button2.setVisibility(View.GONE);
        Button button3 =findViewById(R.id.button3);
        button3.setVisibility(View.GONE);
        Button button4 =findViewById(R.id.button4);
        button4.setVisibility(View.GONE);

        // 显示设置栏
        LinearLayout setting = findViewById(R.id.setting);
        setting.setVisibility(View.VISIBLE);

        LinearLayout cameraIPSet2 = findViewById(R.id.cameraIPSet2);
        LinearLayout cameraIPSet3 = findViewById(R.id.cameraIPSet3);

        // 存放videoPlayer2和videoPlayer3是否显示的状态
        state = new boolean[2];
        state[0] = true;
        state[1] = true;

        // 隐藏正在播放的视频
        videoPlayer1.setVisibility(View.INVISIBLE);

        if (videoPlayer2 != null && videoPlayer2.getVisibility() == View.VISIBLE) {
            videoPlayer2.setVisibility(View.INVISIBLE);
            cameraIPSet2.setVisibility(View.VISIBLE);
        }
        else {
            state[0] = false;
            cameraIPSet2.setVisibility(View.GONE);
        }

        if (videoPlayer3 != null && videoPlayer3.getVisibility() == View.VISIBLE) {
            videoPlayer3.setVisibility(View.INVISIBLE);
            cameraIPSet3.setVisibility(View.VISIBLE);
        }
        else {
            state[1] = false;
            cameraIPSet3.setVisibility(View.GONE);
        }
    }

    public void save(View view) {
        // 隐藏设置栏
        LinearLayout setting = findViewById(R.id.setting);
        setting.setVisibility(View.GONE);

        // 显示按钮
        Button button1 =findViewById(R.id.button1);
        button1.setVisibility(View.VISIBLE);
        Button button2 =findViewById(R.id.button2);
        button2.setVisibility(View.VISIBLE);
        Button button3 =findViewById(R.id.button3);
        button3.setVisibility(View.VISIBLE);
        Button button4 =findViewById(R.id.button4);
        button4.setVisibility(View.VISIBLE);

        // 显示设置前播放的视频
        videoPlayer1.setVisibility(View.VISIBLE);
        EditText ip = findViewById(R.id.cameraIP1);
        loadNew(videoPlayer1, ip.getText().toString());

        if (videoPlayer2 != null && state[0]) {
            videoPlayer2.setVisibility(View.VISIBLE);
            ip = findViewById(R.id.cameraIP2);
            loadNew(videoPlayer2, ip.getText().toString());
        }

        if (videoPlayer3 != null && state[1]) {
            videoPlayer3.setVisibility(View.VISIBLE);
            ip = findViewById(R.id.cameraIP3);
            loadNew(videoPlayer3, ip.getText().toString());
        }
    }

    public void exit(View view) {
        LinearLayout setting = findViewById(R.id.setting);
        setting.setVisibility(View.GONE);

        Button button1 =findViewById(R.id.button1);
        button1.setVisibility(View.VISIBLE);
        Button button2 =findViewById(R.id.button2);
        button2.setVisibility(View.VISIBLE);
        Button button3 =findViewById(R.id.button3);
        button3.setVisibility(View.VISIBLE);
        Button button4 =findViewById(R.id.button4);
        button4.setVisibility(View.VISIBLE);

        videoPlayer1.setVisibility(View.VISIBLE);

        if (videoPlayer2 != null && state[0])
            videoPlayer2.setVisibility(View.VISIBLE);

        if (videoPlayer3 != null && state[1])
            videoPlayer3.setVisibility(View.VISIBLE);
    }

    public boolean ipCheck(String ip) {
        boolean result = false;
        if (ip != null && !ip.isEmpty() && ip.length()<16){

            // 定义正则表达式
            String regex = "([1-9] | [1-9]\\d | 1\\d\\d | 2[0-4]\\d | 25[0-5])\\." +
                    "(\\d | [1-9]\\d | 1\\d\\d | 2[0-4]\\d | 25[0-5])\\." +
                    "(\\d | [1-9]\\d | 1\\d\\d | 2[0-4]\\d | 25[0-5])\\." +
                    "(\\d | [1-9]\\d | 1\\d\\d | 2[0-4]\\d | 25[0-5])";
            result =  ip.matches(regex);
        }
        return result;
    }

    // 加载新设置的ip
    public void loadNew(VideoPlayer videoPlayer, String ip){
        if (ipCheck(ip)) {
            System.out.println("IP1" + ip);
            String newPath = "rtsp://" + ip + ".top:8554/test";
            String oldPath = videoPlayer.getPath();

            if (!oldPath.equals(newPath))
            {
                videoPlayer.setPath(newPath);
                try {
                    videoPlayer.load();
                } catch (IOException e) {
                    Toast.makeText(this, "播放失败", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }
}