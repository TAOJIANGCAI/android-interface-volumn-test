
package isc.crsc.com.mytestcase;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import isc.crsc.com.mytestcase.KeyTest.KeyActivity;
import isc.crsc.com.mytestcase.MikeTest.MikeActivity;
import isc.crsc.com.mytestcase.ScreenTest.ScreenTestActivity;
import isc.crsc.com.mytestcase.TouchTest.TouchTestActivity;
import isc.crsc.com.mytestcase.util.AudioRecorder;
import isc.crsc.com.mytestcase.util.AudioTrackManager;
import isc.crsc.com.mytestcase.util.GpioControl;
import isc.crsc.com.mytestcase.util.I2cUtil;
import isc.crsc.com.mytestcase.util.RecorderUtil;
import isc.crsc.com.mytestcase.util.ShellCommandExecutor;
import isc.crsc.com.mytestcase.util.hideScreen;

public class MainActivity extends Activity implements CompoundButton.OnCheckedChangeListener {
    private isc.crsc.com.mytestcase.util.hideScreen hideScreen = new hideScreen();
    private TextView gpioTx;
    private SeekBar seekLight;
    private CheckBox cbLight;
    private SeekBar seekVol;
    private AudioManager am;
    private VolumeReceiver receiver;
    private CheckBox cbVol;
    private ToggleButton mTogBtn, mTogBtn1;
    private EditText timeTx;
    private Button play, jnitest;
    private static final int TIME_UPDATE = 3;
    private List<String> list = new ArrayList<>();
    /**
     * 资料夹名称
     **/
    private static final String FOLDER_PATH = "RecorderUtil";

    /**
     * 资料夹名称
     **/
    private static final String FOLDER = "AudioRecoder";
    /**
     * /**
     * 待命中
     **/
    private final static int FLAG_IDLE = -1;
    /**
     * amr录音中
     **/
    private final static int FLAG_AMR = 1;

    /**
     * 保存中
     **/
    private final static int FLAG_SAVE = 3;
    /**
     * 状态标示
     **/
    private int mState = FLAG_IDLE;
    /**
     * 录音工具
     **/
    private RecorderUtil recorderUtil;
    /**
     * 档案路径
     **/
    private String amrFilePath;
    /**
     * 提示文字
     **/
    private TextView txt;

    /**
     * 定时变更录音时间
     **/
    private MainTimerTask timerTask;
    /**
     * 开始录音时间
     **/
    private long startTime;
    private String floderPath;
    private MediaPlayer mPlayer = null;// 播放器

    private AudioRecorder audioRecorder = null;
    private AudioTrackManager audioTrackManager = null;
    private int fd;
    private List<Integer> volList;


    static {
        System.loadLibrary("native-lib");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        hideScreen.hideSystemUI(getWindow().getDecorView());

        volList = new ArrayList<>();
        for (int i = 0; i <= 127; i++) {
            volList.add(i);
        }
        Collections.reverse(volList);

        initView();
        initEvent();

        new ShellCommandExecutor()
                .addCommand("chmod 777 /dev/i2c-0")
                .execute();
        fd = open_i2c();
        if (fd == -1) {
            Log.i("I2C", "open i2c fail");
            return;
        }

        if (check_i2c(fd) == -1) {
            Log.i("I2C", "check i2c fail");
            return;
        }
    }


    /**
     * 开始录音
     **/
    private void record(int mFlag) {
        if (mState != FLAG_IDLE) {
            return;
        }
        boolean result = false;
        switch (mFlag) {
            case FLAG_AMR:
                amrFilePath = floderPath + getTime() + ".amr";
                result = recorderUtil.startMediaRecording(amrFilePath);
                break;
        }
        if (result) {
            mState = mFlag;
            txt.setText("录音中");
            startTimer();
        }
    }

    /**
     * 停止录音
     **/
    private void stop() {
        switch (mState) {
            case FLAG_AMR:
                mState = FLAG_SAVE;
                recorderUtil.stopMediaRecording();
                String[] amrStr = amrFilePath.split("/");
                stopTimer();
                break;
            default:
                if (recorderUtil != null) {
                    recorderUtil.stopRawRecording();
                    recorderUtil.stopMediaRecording();
                }
                stopTimer();
                break;
        }
    }

    // 播放录音
    private void playRecord(String playFileName) {
        // 对按钮的可点击事件的控制是保证不出现空指针的重点！！
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        mPlayer = new MediaPlayer();
        // 播放完毕的监听
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                // 播放完毕改变状态，释放资源
                mPlayer.release();
                mPlayer = null;
            }
        });
        try {
            // 播放所选中的录音
            mPlayer.setDataSource(floderPath + playFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (Exception e) {
            // 若出现异常被捕获后，同样要释放掉资源
            // 否则程序会不稳定，不适合正式项目上使用
            if (mPlayer != null) {
                mPlayer.release();
                mPlayer = null;
            }
            Toast.makeText(this, "播放失败,可返回重试！", Toast.LENGTH_LONG).show();
        }
    }

    public void bt1(View view) {
        startActivity(new Intent(MainActivity.this, ScreenTestActivity.class));
    }

    public void bt2(View view) {
        startActivity(new Intent(MainActivity.this, TouchTestActivity.class));
    }

    public void bt3(View view) {
        startActivity(new Intent(MainActivity.this, KeyActivity.class));

    }

    public void bt4(View view) {
        startActivity(new Intent(MainActivity.this, MikeActivity.class));
    }

    public void bt5(View view) {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();

        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        StringBuilder sb = new StringBuilder();
        while (deviceIterator.hasNext()) {
            UsbDevice usbDevice = deviceIterator.next();
            int deviceClass = usbDevice.getDeviceClass();
            if (deviceClass == 0) {
                UsbInterface anInterface = usbDevice.getInterface(0);
                int interfaceClass = anInterface.getInterfaceClass();
                if (interfaceClass == 8) {
                    sb.append("DeviceName=" + usbDevice.getDeviceName() + "\n");
                    sb.append("U 盘已插入");
                    Toast.makeText(this, sb, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void bt6(View view) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            sb.append(readAdcResult(i, fd) + "    ");
        }
        Toast.makeText(this, sb, Toast.LENGTH_SHORT).show();
    }

    public void bt7(View view) {

    }

    public void bt8(View view) {
        String datetime = "20181128.110000";
        setSystemTime(this, timeTx.toString());
    }

    public void play(View view) {
        // 根据后缀名进行判断、获取文件夹中的音频文件
        File file = new File(floderPath);
        File files[] = file.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().indexOf(".") >= 0) {
                    list.add(files[i].getName());
                }
            }
        } else {
            return;
        }
        playRecord(list.get(list.size() - 1));
    }

    public void play1(View view) {
        if (audioTrackManager == null) {
            audioTrackManager = new AudioTrackManager();
        }
        // 根据后缀名进行判断、获取文件夹中的音频文件
        File file = new File(audioTrackManager.getSDPath(FOLDER) + File.separator);
        File files[] = file.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().indexOf(".") >= 0) {
                    list.add(files[i].getName());
                }
            }
        } else {
            return;
        }
        audioTrackManager.startPlay(audioTrackManager.getFloderPath(list.get(list.size() - 1)));
    }

    public static void setSystemTime(final Context cxt, String datetimes) {
        // yyyyMMdd.HHmmss】
        /**
         * 可用busybox 修改时间
         */
        /*
         * String
         * cmd="busybox date  \""+bt_date1.getText().toString()+" "+bt_time1
         * .getText().toString()+"\""; String cmd2="busybox hwclock  -w";
         */
        try {
            Process process = Runtime.getRuntime().exec("su");
            //          String datetime = "20131023.112800"; // 测试的设置的时间【时间格式
            String datetime = ""; // 测试的设置的时间【时间格式
            datetime = datetimes.toString(); // yyyyMMdd.HHmmss】
            DataOutputStream os = new DataOutputStream(
                    process.getOutputStream());
            os.writeBytes("setprop persist.sys.timezone GMT\n");
            os.writeBytes("/system/bin/date -s " + datetime + "\n");
            os.writeBytes("clock -w\n");
            os.writeBytes("exit\n");
            os.flush();
        } catch (IOException e) {
            Toast.makeText(cxt, "请获取Root权限", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 初始化监听
     */
    private void initEvent() {
        //设置seekBar进度被改变的时候的时间监听
        seekLight.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());
        //设置CheckBox的点选监听事件
        cbLight.setOnCheckedChangeListener(this);

        seekVol.setOnSeekBarChangeListener(new MyOnSeekBarVolChangeListener());

        mTogBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub

                if (isChecked) {
                    record(FLAG_AMR);
                } else {
                    stop();
                }
            }
        });

//        mTogBtn1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView,
//                                         boolean isChecked) {
//                // TODO Auto-generated method stub
//
//                if (isChecked) {
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            audioRecorder.startRawRecording(getTime() + ".pcm");
//                        }
//                    }).start();
//                } else {
//                    audioRecorder.stopRecord();
//                }
//            }
//        });
    }

    /**
     * 初始化控件的一些操作
     */
    private void initView() {
        txt = findViewById(R.id.tx);
        play = findViewById(R.id.play);
        seekLight = findViewById(R.id.seekLight);
        cbLight = findViewById(R.id.cbLight);
        mTogBtn = findViewById(R.id.mTogBtn); // 获取到控件
//        mTogBtn1 = findViewById(R.id.mTogBtn1); // 获取到控件
        //设置最大刻度
        seekLight.setMax(255);
        //设置初始的Progress
        seekLight.setProgress(getSystemBrightness());
        //出世设置checkBox为选中状态
        cbLight.setChecked(true);
        //设置初始的屏幕亮度与系统一致
        changeAppBrightness(getSystemBrightness());


        seekVol = findViewById(R.id.btnVol);
        cbVol = findViewById(R.id.cbVol);
        if (cbVol.isChecked()) {
            am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//            int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            seekVol.setMax(127);
//            int currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
//            seekVol.setProgress(currentVolume);
            seekVol.setProgress(getEarVolume(fd));
        } else {
            am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//            int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
            seekVol.setMax(127);
//            int currentVolume = am.getStreamVolume(AudioManager.STREAM_SYSTEM);
//            seekVol.setProgress(currentVolume);
            seekVol.setProgress(getSparkVolume(fd));
        }
        receiver = new VolumeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(receiver, filter);

        if (!recorderUtil.checkSD()) {
            Toast.makeText(this, "SD卡状态异常", Toast.LENGTH_LONG).show();
        }

        recorderUtil = new RecorderUtil(FOLDER_PATH, handler);

        floderPath = recorderUtil.getFloderPath() + File.separator;

        audioRecorder = new AudioRecorder();
        audioRecorder.initAudioRecorder(FOLDER);

        audioTrackManager = new AudioTrackManager();
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RecorderUtil.RECORDER_OK:
                    Toast.makeText(MainActivity.this, "录音成功", Toast.LENGTH_SHORT).show();
                    mState = FLAG_IDLE;
                    txt.setText("待命中");
                    break;
                case RecorderUtil.RECORDER_NG:
                    Toast.makeText(MainActivity.this, "录音失败", Toast.LENGTH_SHORT).show();
                    mState = FLAG_IDLE;
                    txt.setText("待命中");
                    break;
                case TIME_UPDATE:
                    long currentTime = System.currentTimeMillis();
                    int during = (int) (currentTime - startTime) / 100;
                    txt.setText("录音中：" + (during / 10f) + "s");
            }
        }

    };

    /**
     * 获得系统亮度
     *
     * @return
     */
    private int getSystemBrightness() {
        int systemBrightness = 0;
        try {
            systemBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return systemBrightness;
    }

    /**
     * 改变App当前Window亮度
     *
     * @param brightness
     */
    public void changeAppBrightness(int brightness) {
        Window window = this.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        if (brightness == -1) {
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        } else {
            lp.screenBrightness = (brightness <= 0 ? 1 : brightness) / 255f;
        }
        window.setAttributes(lp);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            Toast.makeText(this, getSystemBrightness() + "", Toast.LENGTH_SHORT).show();
            changeAppBrightness(getSystemBrightness());
        } else {
            int seekBarProgress = seekLight.getProgress();
            changeAppBrightness(seekBarProgress);
        }
    }

    class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //seekBar进度条被改变的时候取消checkBox的点选
            cbLight.setChecked(false);
            //改变亮度
            changeAppBrightness(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    class MyOnSeekBarVolChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            if (fromUser) {
                if (cbVol.isChecked()) {
//                    am.setStreamVolume(setEarVolume(progress, fd), progress, 0);
//                    int currentVolume = am.getStreamVolume(getEarVolume(fd));
//                    seekBar.setProgress(currentVolume);
                    setEarVolume(volList.get(progress), fd);
                    int earVolume = getEarVolume(fd);
                    seekBar.setProgress(volList.get(earVolume));
                    Log.i("I2C", "EAR volume is " + earVolume);
                } else {
//                    am.setStreamVolume(setSparkVolume(progress, fd), progress, 0);
//                    int currentVolume = am.getStreamVolume(getSparkVolume(fd));
//                    seekBar.setProgress(currentVolume);
                    setSparkVolume(volList.get(progress), fd);
                    int sparkVolume = getSparkVolume(fd);
                    seekBar.setProgress(volList.get(sparkVolume));
                    Log.i("I2C", "SPARK volume is " + sparkVolume);
                }

            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }


    private class VolumeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                if (cbVol.isChecked()) {
//                    int currentVolume = am.getStreamVolume(getEarVolume(fd));
                    seekVol.setProgress(volList.get(getEarVolume(fd)));
                } else {
//                    int currentVolume = am.getStreamVolume(getSparkVolume(fd));
                    seekVol.setProgress(volList.get(getSparkVolume(fd)));
                }

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if (mState == FLAG_AMR) {
            recorderUtil.stopMediaRecording();
        }
        if (audioTrackManager != null) {
            audioTrackManager.stopPlay();
        }
        if (fd != -1) {
            close_i2c(fd);
            Log.i("I2C", "close i2c !!!");
        }
    }


    /**
     * 开始计时
     **/
    private void startTimer() {
        startTime = System.currentTimeMillis();
        timerTask = new MainTimerTask();
        new Timer().schedule(timerTask, 0, 100);
    }

    /**
     * 停止计时
     **/
    private void stopTimer() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    /**
     * 定时变更录音时间
     **/
    private class MainTimerTask extends TimerTask {
        @Override
        public void run() {
            handler.sendEmptyMessage(TIME_UPDATE);
        }
    }

    // 获得当前时间
    private String getTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        // 获取当前时间
        String time = formatter.format(new Date());
        return time;
    }

    public native int open_i2c();

    public native int check_i2c(int fd);

    public native void close_i2c(int fd);

    public native float readAdcResult(int i, int fd);

    public native int setSparkVolume(int vol_spk, int fd);

    public native int getSparkVolume(int fd);

    public native int setEarVolume(int vol_ear, int fd);

    public native int getEarVolume(int fd);

}
