package isc.crsc.com.mytestcase.LightTest;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Toast;

import isc.crsc.com.mytestcase.R;


public class LightActivity extends Activity implements CompoundButton.OnCheckedChangeListener {

    private SeekBar seekBar;
    private CheckBox cb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.light_main);
        initView();
        initEvent();
    }

    /**
     * 初始化监听
     */
    private void initEvent() {
        //设置seekBar进度被改变的时候的时间监听
        seekBar.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());
        //设置CheckBox的点选监听事件
        cb.setOnCheckedChangeListener(this);
    }

    /**
     * 初始化控件的一些操作
     */
    private void initView() {
        seekBar = findViewById(R.id.seek);
        cb = findViewById(R.id.cb);
        //设置最大刻度
        seekBar.setMax(255);
        //设置初始的Progress
        seekBar.setProgress(getSystemBrightness());
        //出世设置checkBox为选中状态
        cb.setChecked(true);
        //设置初始的屏幕亮度与系统一致
        changeAppBrightness(getSystemBrightness());
    }

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
            int seekBarProgress = seekBar.getProgress();
            changeAppBrightness(seekBarProgress);
        }
    }

    class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //seekBar进度条被改变的时候取消checkBox的点选
            cb.setChecked(false);
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

}

