package com.example.ijk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.net.TrafficStats;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import tv.danmaku.ijk.media.player.IMediaPlayer;

public class MainActivity extends AppCompatActivity implements VideoListener{

    private VideoPlayer videoPlayer1;
    private VideoPlayer videoPlayer2;
    private VideoPlayer videoPlayer3;

    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;

    private LinearLayout linearLayout1;
    private LinearLayout linearLayout2;
    private LinearLayout linearLayout3;
    private LinearLayout linearLayout4;

    private TextView speed;

    private LinearLayout setting;

    private LinearLayout cameraIPSet2;
    private LinearLayout cameraIPSet3;

    private LinearLayout wcuIPSet1;
    private LinearLayout wcuIPSet2;

    private EditText ip1;
    private EditText ip2;
    private EditText ip3;

    private EditText wcuIP1;
    private EditText wcuIP2;
    private EditText wcuIP3;


    private long lastTotalRxBytes = 0;
    private long lastTimeStamp = 0;

    private long reconnectLastTotalRxBytes = 0;
    private long reconnectLastTimeStamp = 0;

    Handler mHandler = new MyHandler(this);

    private boolean[] state;

    ExecutorService executor; // 线程池，用于管理传感器数据接收线程




    public static final int MAX_RECV_DATA_LENGTH = 20;
    public static final int SENSOR_NUMBER = 4;
    public static final int SENSOR_NORMAL = 0x01;
    public static final int SENSOR_ERROR = 0x02;

    public static final int DISTANCE_BUF_SIZE = 10;
    public static final int STATUS_BUF_SIZE = 4;
    public static final int SELF_CHECK_DATA_LENGTH = 4;
    public static final int DISTANCE_DATA_LENGTH = 7;
    public static final int SERVER_PORT_FOR_CONSOLE = 3333;
    public static final int SELF_CHECK_NORMAL_LABEL = 0x20;
    public static final int SELF_CHECK_ERROR_LABEL = 0x21;
    public static final int MIN_DIS_LABEL = 0x30;
    public static final int DIS_LABEL = 0x31;
    public static final int ALARM_LABEL = 0x32;

    public static final int BRAKE_LABEL = 0x33;

    public static final int STATUS_LABEL = 0x34;
    public static final int SLEEP_LABEL = 0x40;
    public static final int UNBRAKE_LABEL = 0x41;

    public static final int MANUAL_MODE_LABEL = 0x50;
    public static final int AUTOMATIC_MODE_LABEL = 0x51;
    public static final int NET_ERROR = 0x61;
    public static final int NET_NORMAL = 0x60;

    public static final int OUTSIDE_VOLTAGE_LOW = 1;
    public static final int OUTSIDE_VOLTAGE_HIGH = 2;
    public static final int TEMPERATURE_LOW = 3;
    public static final int TEMPERATURE_HIGH = 4;
    public static final int DATA_OVERFLOW = 5;
    public static final int DATA_ERROR = 6;
    public static final int HIGH_INCIDENT_RAY = 7;
    public static final int WEAK_SIGNAL = 8;
    public static final int STRONG_SIGNAL = 9;
    public static final int HARDWARE_ERROR_1 = 10;
    public static final int HARDWARE_ERROR_2 = 11;
    public static final int HARDWARE_ERROR_3 = 12;
    public static final int HARDWARE_ERROR_4 = 13;
    public static final int HARDWARE_ERROR_5 = 14;
    public static final int HUGE_SHAKE = 15;
    public static final int INSIDE_VOLTAGE_LOW = 16;
    public static final int INSIDE_VOLTAGE_HIGH = 17;
    public static final int DISTENCE_TOO_CLOSE = 18;
    public static final int DISTENCE_TOO_FAR = 19;
    public static final int SENSOR_STATE_TYPES = 30;


    public static final int DEAL_DIS_NEED_WARN = 1;
    public static final int DEAL_DIS_CANNEL_WARN = 3;

    public static final int FONT_NUM = 4;
    public static final int SENSOR_REFLEX_GOOD = 0x32;
    public static final int SENSOR_REFLEX_NORMAL = 0x33;
    public static final int SENSOR_REFLEX_BAD = 0x34;
    public static final double LEVEL_MIN_DISTANCE = 0.75;
    public static final int VERTICAL_MIN_DISTANCE = 30;
    public static final int SHOW_DATA_LENGTH = 6;

    public static final int MANUAL_MODE = 1;
    public static final int AUTOMATIC_MODE = 2;

    private SurfaceView mSurfaceView;
    private VideoView videoView;
    private Button btn_setting;
    private TextView[] distance1 = new TextView[SENSOR_NUMBER];
    private TextView[] distance2 = new TextView[SENSOR_NUMBER];
    private TextView min_distance;
    private TextView net_status;
    private TextView drive_mode;
    private TextView brake_status;
    private TextView disconnect_text;


    private TextView log_net;
    private TextView log_fault_flash;
    private TextView log_laser;
    private static String log_net_str = "网络日志";
    private static String log_fault_flash_str = "故障刷新日志";
    private static String log_laser_str = "探头日志";
    private static int[] distance_Status = new int[SENSOR_NUMBER];

    private static final String LOG_NET = "正在检查网络状态...";
    private static final String LOG_NET_OK = "网络连接正常";
    private static final String LOG_NET_INVALID = "网络连接异常";

    private static final String LOG_FAULT_FLASH = "号探头故障刷新了";

    private static final String LOG_LASER_SEND = "探头正在发送命令...";
    private static final String LOG_LASER_READ_DATA = "探头正在接收数据...";
    private static final String LOG_LASER_ANALYSIS_DATA = "探头正在解析数据...";
    private static final String LOG_LASER_ANALYSIS_DATA_OK = "探头解析数据成功";
    private static final String LOG_LASER_ANALYSIS_DATA_INVALID = "探头解析数据失败";
    private static final String LOG_LASER_ANALYSIS_DATA_ALERT = "激光探头疑似有异物长时间遮挡,请在设置中保存重连";


    private static String[][] distance_str = new String[3][SENSOR_NUMBER];
    private static String min_distance_str = "0.0";
    private static String net_status_str = "网络连接中";
    private static String drive_mode_str = "手动驾驶";
    private static String brake_status_str = "继电器连接";

    private static int color_red = Color.rgb(255, 0, 0);
    private static int color_transparent = Color.argb(0, 0, 0, 0);

    private static int[][] distance_color = new int[3][SENSOR_NUMBER];
    private static int min_distance_color = color_red;
    private static int net_status_color = color_red;
    private static int drive_mode_color = color_red;
    private static int brake_status_color = color_red;

    private boolean isPingServerSuccess = false;

    //获得SharedPreferences的实例 sp_name是文件名
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    private static Toast toast;

    Socket sHost = null;

    private String []servAddr = {"192.168.0.2", "192.168.0.4"};
    private String serverIP = "";
    private int port = 3333;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        executor = Executors.newFixedThreadPool(3);

        createVideo1();

        sp = getSharedPreferences("ip_config_file", Context.MODE_PRIVATE);


        ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);

        // 网速监测
        pool.scheduleAtFixedRate(this::showSpeed, 0, 800, TimeUnit.MILLISECONDS);

        // 断网重连
        pool.scheduleAtFixedRate(this::reconnect, 0, 1000, TimeUnit.MILLISECONDS);

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

    private void initView() {

        linearLayout1 = findViewById(R.id.LinearLayout1);
        linearLayout2 = findViewById(R.id.LinearLayout2);
        linearLayout3 = findViewById(R.id.LinearLayout3);
        linearLayout4 = findViewById(R.id.LinearLayout4);


        button1 =findViewById(R.id.button1);
        button2 =findViewById(R.id.button2);
        button3 =findViewById(R.id.button3);
        button4 =findViewById(R.id.button4);

        speed = findViewById(R.id.speed);

        setting = findViewById(R.id.setting);

        cameraIPSet2 = findViewById(R.id.cameraIPSet2);
        cameraIPSet3 = findViewById(R.id.cameraIPSet3);

        wcuIPSet1 = findViewById(R.id.wcuIPSet1);
        wcuIPSet2 = findViewById(R.id.wcuIPSet2);

        ip1 = findViewById(R.id.cameraIP1);
        ip2 = findViewById(R.id.cameraIP2);
        ip3 = findViewById(R.id.cameraIP3);
        wcuIP1 = findViewById(R.id.wcuIP1);
        wcuIP2 = findViewById(R.id.wcuIP2);
//        wcuIP3 = findViewById(R.id.wcuIP3);


        distance1[0] = findViewById(R.id.leftfrontdistance1);
        distance1[1] = findViewById(R.id.rightfrontdistance1);
        distance1[2] = findViewById(R.id.leftdistance1);
        distance1[3] = findViewById(R.id.rightdistance1);

        distance2[0] = findViewById(R.id.leftfrontdistance2);
        distance2[1] = findViewById(R.id.rightfrontdistance2);
        distance2[2] = findViewById(R.id.leftdistance2);
        distance2[3] = findViewById(R.id.rightdistance2);
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
                        // 网速显示
                        speed.setText((String)msg.obj);
                        break;
                    case 99:
                        // 重连
                        // 关闭所有线程，并重建线程池
                        executor.shutdownNow();
                        executor =  Executors.newFixedThreadPool(3);
                        try {
                            videoPlayer1.load();
                            ConnectServer(1);
                            if (videoPlayer2 != null && videoPlayer2.getVisibility() == View.VISIBLE) {
                                videoPlayer2.load();
                                ConnectServer(2);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 1:
                        // video1 传感器数据显示
                        for (int i = 0; i < SENSOR_NUMBER; i++) {
                            activity.distance1[i].setText(distance_str[0][i]);
                            activity.distance1[i].setBackgroundColor(distance_color[0][i]);
                        }
//                        activity.min_distance.setText(min_distance_str);
//                        activity.net_status.setText(net_status_str);
//                        activity.drive_mode.setText(drive_mode_str);
//                        activity.brake_status.setText(brake_status_str);
//
//                        activity.log_laser.setText(log_laser_str);
//                        activity.log_fault_flash.setText(log_fault_flash_str);
//                        activity.log_net.setText(log_net_str);
//
//                        activity.min_distance.setBackgroundColor(min_distance_color);
//                        activity.net_status.setBackgroundColor(net_status_color);
//                        activity.drive_mode.setBackgroundColor(drive_mode_color);
//                        activity.brake_status.setBackgroundColor(brake_status_color);

                        break;

                    case 2:
                        // video2 传感器数据显示
                        for (int i = 0; i < SENSOR_NUMBER; i++) {
                            activity.distance2[i].setText(distance_str[1][i]);
                            activity.distance2[i].setBackgroundColor(distance_color[1][i]);
                        }
                        break;
                    case 3:
                        // 清除传感器显示数据
                        distance1[0].setText("");
                        distance1[1].setText("");
                        distance1[2].setText("");
                        distance1[3].setText("");
                        distance1[0].setBackgroundColor(Color.WHITE);
                        distance1[1].setBackgroundColor(Color.WHITE);
                        distance1[2].setBackgroundColor(Color.WHITE);
                        distance1[3].setBackgroundColor(Color.WHITE);
                        distance2[0].setText("");
                        distance2[1].setText("");
                        distance2[2].setText("");
                        distance2[3].setText("");
                        distance2[0].setBackgroundColor(Color.WHITE);
                        distance2[1].setBackgroundColor(Color.WHITE);
                        distance2[2].setBackgroundColor(Color.WHITE);
                        distance2[3].setBackgroundColor(Color.WHITE);
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

    private void reconnect() {
        long nowTotalRxBytes = TrafficStats.getUidRxBytes(this.getApplicationInfo().uid); // uid: android.os.Process.myUid()
//        long nowTimeStamp = System.currentTimeMillis();

        if (nowTotalRxBytes - reconnectLastTotalRxBytes == 0) {
            Message msg = mHandler.obtainMessage();
            msg.what = 99;
            mHandler.sendMessage(msg);//更新界面
        }

        // 更新时间戳与上个时间戳传输总字节数
//        reconnectLastTimeStamp = nowTimeStamp;
        reconnectLastTotalRxBytes = nowTotalRxBytes;

    }

    public void singleScreen(View view) {
        // 从双窗口或三窗口转回时，停止其他播放并将其view隐藏
        if (videoPlayer2 != null && videoPlayer2.getVisibility() == View.VISIBLE)
        {
            videoPlayer2.stop();
            videoPlayer2.setVisibility(View.GONE);
            linearLayout3.setVisibility(View.GONE);
            linearLayout4.setVisibility(View.GONE);
        }

        if (videoPlayer3 != null && videoPlayer3.getVisibility() == View.VISIBLE) {
            videoPlayer3.stop();
            videoPlayer3.setVisibility(View.GONE);
        }


        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) videoPlayer1.getLayoutParams();
        layoutParams.verticalBias = (float) 0.4; // 0.5
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
        layoutParams.verticalBias = (float) 0.194;  // 0,3
        videoPlayer1.setLayoutParams(layoutParams);

        // 设置双窗口下videoPlayer2位置
//        layoutParams = (ConstraintLayout.LayoutParams)videoPlayer2.getLayoutParams();
//        layoutParams.verticalBias = (float) 0.762;
        videoPlayer2.setVisibility(View.VISIBLE);

        linearLayout3.setVisibility(View.VISIBLE);
        linearLayout4.setVisibility(View.VISIBLE);

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


    public void createVideo1 () {
        videoPlayer1 = findViewById(R.id.video_view1);
        videoPlayer1.setVideoListener(this);
        videoPlayer1.setPath("rtsp://1.116.191.98:8554/test");
        // rtsp://codeshark.top:8554/test   1.116.191.98
        // http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear3/prog_index.m3u8

        try {
            videoPlayer1.load();
        } catch (IOException e) {
            Toast.makeText(this,"播放失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        ConnectServer(1);

    }

    public void createVideo2 () {
        videoPlayer2 = findViewById(R.id.video_view2);
        videoPlayer2.setVideoListener(this);
        videoPlayer2.setPath("rtsp://1.116.191.98:8554/test");

        try {
            videoPlayer2.load();
        } catch (IOException e) {
            Toast.makeText(this,"播放失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        ConnectServer(2);
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

        button1.setVisibility(View.GONE);

        button2.setVisibility(View.GONE);
        button3.setVisibility(View.GONE);
        button4.setVisibility(View.GONE);

        linearLayout1.setVisibility(View.GONE);
        linearLayout2.setVisibility(View.GONE);
        linearLayout3.setVisibility(View.GONE);
        linearLayout4.setVisibility(View.GONE);

        speed.setVisibility(View.GONE);

        // 显示设置栏
        setting.setVisibility(View.VISIBLE);

        // 存放videoPlayer2和videoPlayer3是否显示的状态
        state = new boolean[2];
        state[0] = true;
        state[1] = true;

        // 隐藏正在播放的视频
        videoPlayer1.setVisibility(View.INVISIBLE);

        if (videoPlayer2 != null && videoPlayer2.getVisibility() == View.VISIBLE) {
            videoPlayer2.setVisibility(View.INVISIBLE);
            cameraIPSet2.setVisibility(View.VISIBLE);
            wcuIPSet2.setVisibility(View.VISIBLE);
        }
        else {
            state[0] = false;
            cameraIPSet2.setVisibility(View.GONE);
            wcuIPSet2.setVisibility(View.GONE);
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
        setting.setVisibility(View.GONE);

        // 显示按钮
        button1.setVisibility(View.VISIBLE);
        button2.setVisibility(View.VISIBLE);
//        button3.setVisibility(View.VISIBLE);
        button4.setVisibility(View.VISIBLE);

        linearLayout1.setVisibility(View.VISIBLE);
        linearLayout2.setVisibility(View.VISIBLE);


        speed.setVisibility(View.VISIBLE);

        // 关闭所有线程，并重建线程池
        executor.shutdownNow();
        executor =  Executors.newFixedThreadPool(3);

        // 清楚传感器数据
        Message msg = mHandler.obtainMessage();
        msg.what = 3;
        mHandler.sendMessage(msg);

        // 显示设置后播放的视频
        videoPlayer1.setVisibility(View.VISIBLE);
        serverIP = wcuIP1.getText().toString();
        if (ipCheck(serverIP) && !serverIP.equals("") && !serverIP.equals(servAddr[0]))
            servAddr[0] = serverIP;
        loadNew(1, videoPlayer1, ip1.getText().toString());
        ConnectServer(1);



        if (videoPlayer2 != null && state[0]) {
            videoPlayer2.setVisibility(View.VISIBLE);
            linearLayout3.setVisibility(View.VISIBLE);
            linearLayout4.setVisibility(View.VISIBLE);
            serverIP = wcuIP2.getText().toString();
            if (ipCheck(serverIP) && !serverIP.equals("") && !serverIP.equals(servAddr[1]))
                servAddr[1] = serverIP;
            loadNew(2, videoPlayer2, ip2.getText().toString());
            ConnectServer(2);
        }

        if (videoPlayer3 != null && state[1]) {
            videoPlayer3.setVisibility(View.VISIBLE);
//            loadNew(videoPlayer3, ip3.getText().toString());
        }
    }

    public void exit(View view) {
        setting.setVisibility(View.GONE);

        button1.setVisibility(View.VISIBLE);
        button2.setVisibility(View.VISIBLE);
//        button3.setVisibility(View.VISIBLE);
        button4.setVisibility(View.VISIBLE);

        linearLayout1.setVisibility(View.VISIBLE);
        linearLayout2.setVisibility(View.VISIBLE);

        speed.setVisibility(View.VISIBLE);

        videoPlayer1.setVisibility(View.VISIBLE);

        if (videoPlayer2 != null && state[0]) {
            videoPlayer2.setVisibility(View.VISIBLE);
            linearLayout3.setVisibility(View.VISIBLE);
            linearLayout4.setVisibility(View.VISIBLE);
        }

        if (videoPlayer3 != null && state[1])
            videoPlayer3.setVisibility(View.VISIBLE);
    }

    public boolean ipCheck(String ip) {
        boolean result = false;
        if (ip != null && !ip.isEmpty() && ip.length()<16){

            // 定义正则表达式
            String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
            result =  ip.matches(regex);
        }
        return result;
    }

    // 加载新设置的ip
    public void loadNew(int id, VideoPlayer videoPlayer, String ip){
        if (ipCheck(ip)) {
            String newPath = "rtsp://" + ip + ":554/user=admin&password=&channel=2&stream=1.sdp?real_stream";
            String oldPath = videoPlayer.getPath();

            if (!oldPath.equals(newPath))
            {
                videoPlayer.stop();
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

    private void ConnectServer(int id) {  //连接硬件后台，获取距离数据

        executor.execute(() -> {
            Looper.prepare();
            try {
//                    String string = null;  //测试崩溃
//                    char[] chars = string.toCharArray();
//                    Log.i("mylog", "data-->" + chars);
//                    servAddr = sp.getString("wcu_ip", null);  //获取IP地址并判断
                if (sHost != null) {
                    sHost.close();

                } else if (servAddr[id - 1] == null || !ipCheck(servAddr[id - 1])) {
                    showToast("请输入WCUIp IP地址");
                }

                sHost = new Socket(servAddr[id - 1], port);
                ThreadRecvWcu(id);  //获取数据并显示

            } catch (IOException e) {
                e.printStackTrace();
            }

            Looper.loop();
        });
//
//        Thread connect = new Thread() {
//            public void run() { //创建子线程
//                Looper.prepare();
//                try {
////                    String string = null;  //测试崩溃
////                    char[] chars = string.toCharArray();
////                    Log.i("mylog", "data-->" + chars);
////                    servAddr = sp.getString("wcu_ip", null);  //获取IP地址并判断
//                    if (sHost != null) {
//                        sHost.close();
//
//                    } else if (servAddr[id - 1] == null || !ipCheck(servAddr[id - 1])) {
//                        showToast("请输入WCUIp IP地址");
//                    }
//
//                    sHost = new Socket(servAddr[id - 1], port);
//                    ThreadRecvWcu(id);  //获取数据并显示
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                Looper.loop();
//
//            }
//        };
//        connect.start();
    }

    void ThreadRecvWcu(int id) throws IOException {   //获取距离数据并处理

        Log.i("mylog", "ThreadRecvWcu");
        int[] iStaAlarm = {0, 0, 0, 0};//为最近距离显示是否报警使用
        int[] iDisAlarm = {0, 0, 0, 0};//同上
        char cJudgeBuf;

        //input->drive_mode->SetWindowText(_T("手动驾驶"));//初始化为手动模式
        int retVal;
        int ilabel = 0;
        char[] cDistence = new char[MAX_RECV_DATA_LENGTH];
        char[] cRecvData = new char[MAX_RECV_DATA_LENGTH];
        String sShowData;
        //int iGetLength = 0;
        int iSensorNo;
        int iGetLength;
        int DRIVE_MODE = MANUAL_MODE;

        log_laser_str = LOG_LASER_SEND;
        //    获取输出流，向服务器发送数据
        OutputStream os = sHost.getOutputStream();
        PrintWriter pw = new PrintWriter(os);
        String sendStr = sp.getString("distance1", "050") + sp.getString("distance2", "010") + sp.getString("distance3", "005") + sp.getString("distance4", "003");
        pw.write(sendStr);
        pw.flush();
        //关闭输出流
        sHost.shutdownOutput();

        log_laser_str = LOG_LASER_READ_DATA;
        //获取输入流，接收服务器发来的数据
        InputStream is = sHost.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        int data = 0, temp = 0;


        isPingServerSuccess = startPing(id);
        while (true) {
            if (isPingServerSuccess == false) {
                break;
            }
            data = br.read();
            if (data < 1)
                continue;

            cJudgeBuf = (char) data;
            log_laser_str = LOG_LASER_ANALYSIS_DATA + "[" + cJudgeBuf + "]";
            //Log.i("mylog", "cJudgeBuf-->" + cJudgeBuf);
            if (ilabel == 0) {
                if (cJudgeBuf == 'w') {
                    ilabel++;
                    continue;
                } else
                    return;
            } else if (ilabel == 1) {
                if (cJudgeBuf == 'c') {
                    ilabel++;
                    continue;
                } else {
                    ilabel = 0;
                    continue;
                }
            } else if (ilabel == 2) {
                if (cJudgeBuf == 'u') {
                    ilabel++;
                    continue;
                } else {
                    ilabel = 0;
                    continue;
                }
            } else if (ilabel == 3) {   // 37 117  w c u 7 1 2 3 4 5 6 7
                iGetLength = data;
                for (int i = 0; i < iGetLength; i++) {
                    temp = br.read();
                    Log.i("mylog", "temp-->" + temp + " " + (char) temp);
                    if (temp < 0)
                        continue;
                    cRecvData[i] = (char) temp;
                }
//                br.read(cRecvData, 0, iGetLength);

//            Log.i("mylog", "cRecvData-->" + String.valueOf(cRecvData));
                if (cRecvData[0] == '\0')
                    continue;
                if (cRecvData[0] == SELF_CHECK_ERROR_LABEL) {
                    for (int i = 0; i < SENSOR_NUMBER; i++) {
                        if (cRecvData[i + 1] == SENSOR_ERROR) {
                            distance_str[id - 1][i] = ("设备异常");
                            distance_color[id - 1][i] = color_red;
                            iStaAlarm[i] = 1;
                        } else if (cRecvData[i + 1] == SENSOR_NORMAL) {
                            distance_str[id - 1][i] = ("设备正常");
                            distance_color[id - 1][i] = color_transparent;
                            iStaAlarm[i] = 0;
                        }
                    }
                } else if (cRecvData[0] == SELF_CHECK_NORMAL_LABEL) {
                    for (int i = 0; i < SELF_CHECK_DATA_LENGTH; i++) {
                        distance_color[id - 1][i] = color_transparent;
                        iStaAlarm[i] = 0;
                    }
                } else if (cRecvData[0] == MIN_DIS_LABEL) {
                    if ((cRecvData[2] > '0') && (cRecvData[2] <= '9')) {
                        System.arraycopy(cRecvData, 2, cDistence, 0, SHOW_DATA_LENGTH);
                    } else
                        System.arraycopy(cRecvData, 3, cDistence, 0, SHOW_DATA_LENGTH - 1);
                    sShowData = String.valueOf(cDistence);
                    min_distance_str = sShowData;
                } else if (cRecvData[0] == DIS_LABEL) {
                    iSensorNo = cRecvData[1];
                    if (iStaAlarm[iSensorNo] == 1)
                        continue;
                    if ((cRecvData[2] > '0') && (cRecvData[2] <= '9'))
                        System.arraycopy(cRecvData, 2, cDistence, 0, SHOW_DATA_LENGTH);
                    else
                        System.arraycopy(cRecvData, 3, cDistence, 0, SHOW_DATA_LENGTH - 1);

                    sShowData = String.valueOf(cDistence);
                    distance_str[id - 1][iSensorNo] = sShowData.substring(0, 3);
                    log_laser_str = LOG_LASER_ANALYSIS_DATA_OK;

                } else if (cRecvData[0] == ALARM_LABEL) {
                    iSensorNo = cRecvData[1];
                    switch (cRecvData[2]) {
                        case DEAL_DIS_NEED_WARN: {
                            if ((iStaAlarm[iSensorNo] + iDisAlarm[iSensorNo]) == 0)
                                distance_color[id - 1][iSensorNo] = color_red;
                            min_distance_color = color_red;

                            iDisAlarm[iSensorNo] = 1;
                            break;
                        }
                        case DEAL_DIS_CANNEL_WARN: {
                            iDisAlarm[iSensorNo] = 0;
                            if ((iStaAlarm[iSensorNo] + iDisAlarm[iSensorNo]) == 0)
                                distance_color[id - 1][iSensorNo] = color_transparent;
                            if ((iDisAlarm[0] + iDisAlarm[1] + iDisAlarm[2] + iDisAlarm[3]) == 0)
                                min_distance_color = color_transparent;
                            break;
                        }
                    }
                }

                //添加的
                else if (cRecvData[0] == BRAKE_LABEL) {
                    brake_status_str = "继电器断开";
                    brake_status_color = color_red;
                } else if (cRecvData[0] == UNBRAKE_LABEL) {
                    brake_status_str = "继电器接通";
                    brake_status_color = color_transparent;
                } else if (cRecvData[0] == STATUS_LABEL) {
                    iSensorNo = cRecvData[1];
                    switch (cRecvData[2]) {
                        case SENSOR_STATE_TYPES: {
                            iStaAlarm[iSensorNo] = 0;
                            if ((iStaAlarm[iSensorNo] + iDisAlarm[iSensorNo]) == 0)
                                distance_color[id - 1][iSensorNo] = color_transparent;
                            break;
                        }
                        case TEMPERATURE_HIGH: {
                            iStaAlarm[iSensorNo] = 1;
//                               distance[iSensorNo]->SetFont(input -> font[0], false);
                            distance_color[id - 1][iSensorNo] = color_red;
                            distance_str[id - 1][iSensorNo] = "温度过高";
                            break;
                        }
                        case HUGE_SHAKE: {
                            iStaAlarm[iSensorNo] = 1;
//                              distance[iSensorNo]->SetFont(input -> font[0], false);
                            distance_color[id - 1][iSensorNo] = color_red;
                            distance_str[id - 1][iSensorNo] = "震动过大";
                            break;
                        }
                        case DISTENCE_TOO_FAR:
                        case HARDWARE_ERROR_1: {
                            iStaAlarm[iSensorNo] = 0;
//                                distance[iSensorNo]->SetFont(input -> font[0], false);
                            if ((iStaAlarm[iSensorNo] + iDisAlarm[iSensorNo]) == 0)
                                distance_color[id - 1][iSensorNo] = color_transparent;
                            //input->distance[iSensorNo]->SetFaceColor(RGB(30, 30, 30));
                            distance_str[id - 1][iSensorNo] = ("安全距离");
                            break;
                        }
                        default: {
                            iStaAlarm[iSensorNo] = 1;
                            distance_color[id - 1][iSensorNo] = color_red;
                            distance_str[id - 1][iSensorNo] = "探头故障" + "[" + (int) cRecvData[2] + "]";
                            log_laser_str = LOG_LASER_ANALYSIS_DATA_INVALID;
//                                distance[iSensorNo]->SetFont(input -> font[0], false);
                            break;
                        }
                    }

                } else if (cRecvData[0] == SLEEP_LABEL) {
//                       min_distance -> SetFont(input -> font[2], false);
                    min_distance_str = "关闭状态";
                    min_distance_color = color_transparent;
                    for (int i = 0; i < SENSOR_NUMBER; i++) {
                        distance_color[id - 1][i] = color_transparent;
                        distance_str[id - 1][i] = " ";
                    }
                } else if (cRecvData[0] == MANUAL_MODE_LABEL) {
                    drive_mode_str = "手动驾驶";
                    DRIVE_MODE = MANUAL_MODE;
                    drive_mode_color = color_transparent;
                } else if (cRecvData[0] == AUTOMATIC_MODE_LABEL) {
                    drive_mode_str = "自动驾驶";
                    DRIVE_MODE = AUTOMATIC_MODE;
                    drive_mode_color = color_red;
                } else if (cRecvData[0] == NET_ERROR) {
                    net_status_str = ("网络异常");
                    net_status_color = color_red;

                } else if (cRecvData[0] == NET_NORMAL) {
                    net_status_str = ("网络正常");
                    net_status_color = color_transparent;
                } else {
                    ilabel = 0;
                    continue;
                }
                ilabel = 0;

                Message msg = new Message();
                msg.what = id;
                //更新界面
                mHandler.sendMessage(msg);
            }
        }
    }

    private boolean startPing(int id) {
        Log.i("Ping", "startPing...");
        boolean success = false;
        Process p = null;

        try {
            p = Runtime.getRuntime().exec("ping -c 1 -i 0.2 -W 1 " + servAddr[id - 1 ]); //-c 1为发送的次数，1为表示发送1次，-w 表示发送后等待响应的时间。
            int status = p.waitFor();
            if (status == 0) {
                success = true;
            } else {
                success = false;
            }
        } catch (IOException | InterruptedException e) {
            success = false;
        } finally {
            p.destroy();
        }

        Log.i("Ping result", ":" + success);
        return success;
    }

    void showToast(String message) {

        if (toast == null) {
            toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        } else {
            toast.setText(message);
        }
        toast.show();

    }
}