package isc.crsc.com.mytestcase.VolumnTest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.SeekBar;

import isc.crsc.com.mytestcase.R;

public class VolumnActivity extends Activity {
    private SeekBar seekBar;
    private AudioManager am;
    private VolumeReceiver receiver;
    private CheckBox cb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.volumn_main);
        initView();
        initEvent();
    }

    /**
     * 初始化控件的一些操作
     */
    private void initView() {
        seekBar = findViewById(R.id.btnVolumn);
        cb = findViewById(R.id.btnCb);
        if (cb.isChecked()) {
            am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            seekBar.setMax(maxVolume);
            int currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            seekBar.setProgress(currentVolume);
        }else{
            am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
            seekBar.setMax(maxVolume);
            int currentVolume = am.getStreamVolume(AudioManager.STREAM_SYSTEM);
            seekBar.setProgress(currentVolume);
        }
        receiver = new VolumeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(receiver, filter);
    }

    /**
     * 初始化监听
     */
    private void initEvent() {
        //设置seekBar进度被改变的时候的时间监听
        seekBar.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());
    }


    class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                if(cb.isChecked()){
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                    int currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                    seekBar.setProgress(currentVolume);
                }else{
                    am.setStreamVolume(AudioManager.STREAM_SYSTEM, progress, 0);
                    int currentVolume = am.getStreamVolume(AudioManager.STREAM_SYSTEM);
                    seekBar.setProgress(currentVolume);
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
                if(cb.isChecked()){
                    int currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                    seekBar.setProgress(currentVolume);
                }else{
                    int currentVolume = am.getStreamVolume(AudioManager.STREAM_SYSTEM);
                    seekBar.setProgress(currentVolume);
                }

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
