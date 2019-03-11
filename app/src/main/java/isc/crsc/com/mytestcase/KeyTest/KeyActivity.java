package isc.crsc.com.mytestcase.KeyTest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import isc.crsc.com.mytestcase.R;
import isc.crsc.com.mytestcase.util.ToastUtil;
import isc.crsc.com.mytestcase.util.hideScreen;

import static android.view.KeyEvent.KEYCODE_0;
import static android.view.KeyEvent.KEYCODE_1;
import static android.view.KeyEvent.KEYCODE_2;
import static android.view.KeyEvent.KEYCODE_3;
import static android.view.KeyEvent.KEYCODE_4;
import static android.view.KeyEvent.KEYCODE_5;
import static android.view.KeyEvent.KEYCODE_6;
import static android.view.KeyEvent.KEYCODE_7;
import static android.view.KeyEvent.KEYCODE_8;
import static android.view.KeyEvent.KEYCODE_9;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.KeyEvent.KEYCODE_F1;
import static android.view.KeyEvent.KEYCODE_F2;
import static android.view.KeyEvent.KEYCODE_F3;
import static android.view.KeyEvent.KEYCODE_F4;
import static android.view.KeyEvent.KEYCODE_F5;
import static android.view.KeyEvent.KEYCODE_F6;
import static android.view.KeyEvent.KEYCODE_F7;
import static android.view.KeyEvent.KEYCODE_F8;
import static android.view.KeyEvent.KEYCODE_MENU;
import static android.view.KeyEvent.KEYCODE_VOLUME_DOWN;
import static android.view.KeyEvent.KEYCODE_VOLUME_UP;


public class KeyActivity extends Activity {
    private EditText etAccout = null;

    public static final int KEYCODE_PTT = 141;
    public static final int KEYCODE_HANGUP = 142;

    public static final int KEYCODE_K1 = 143;
    public static final int KEYCODE_K2 = 116;
    public static final int KEYCODE_EMER = 113;

    public static final int KEYCODE_RESET = 124;

    public static final int KEYCODE_S = 139;
    public static final int KEYCODE_PO = 140;
    private hideScreen hideScreen = new hideScreen();
    private ToastUtil toastUtil;
    private TextView info;
    final int DISAPEAR_TEXT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.key_main);
        hideScreen.hideSystemUI(getWindow().getDecorView());
        etAccout = findViewById(R.id.accout);
        etAccout.setOnKeyListener(new onKeyListener());
        toastUtil = new ToastUtil();
        info = findViewById(R.id.info);

    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISAPEAR_TEXT:
                    info.setText("");
                    break;
            }
        }
    };


    private void release() {
        mHandler.sendEmptyMessageDelayed(DISAPEAR_TEXT, 5 * 1000);
    }
    //K1,149,23
    // K2,150,22
//*,146,20
//#,145,123
//* 139
// && (event.getRepeatCount() == 0)

    private class onKeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                switch (keyCode) {
                    case KEYCODE_HANGUP:
                        info.setText("HANGUP");
                        break;
                }
            }
            if ((event.getAction() == KeyEvent.ACTION_UP)) {
//                Log.i("KEYCODE", keyCode + "");
                switch (keyCode) {
                    case KEYCODE_0:
                        info.setText("0");
                        break;
                    case KEYCODE_1:
                        info.setText("1");
                        break;
                    case KEYCODE_2:
                        info.setText("2");
                    case KEYCODE_3:
                        info.setText("3");
                        break;
                    case KEYCODE_4:
                        Log.i("KEYCODE", "***********");
                        info.setText("4");
                        break;
                    case KEYCODE_5:
                        info.setText("5");
                        break;
                    case KEYCODE_6:
                        info.setText("6");
                        break;
                    case KEYCODE_7:
                        info.setText("7");
                        break;
                    case KEYCODE_8:
                        info.setText("8");
                        break;
                    case KEYCODE_9:
                        info.setText("9");
                        break;
                    case KEYCODE_F1:
                        info.setText("F1");
                        break;
                    case KEYCODE_F2:
                        info.setText("F2");
                        break;
                    case KEYCODE_F3:
                        info.setText("F3");
                        break;
                    case KEYCODE_F4:
                        info.setText("F4");
                        break;
                    case KEYCODE_F5:
                        info.setText("F5");
                        break;
                    case KEYCODE_F6:
                        info.setText("F6");
                        break;
                    case KEYCODE_F7:
                        info.setText("F7");
                        break;
                    case KEYCODE_F8:
                        info.setText("F8");
                        break;
                    case KEYCODE_S:
                        info.setText("*");
                        break;
                    case KEYCODE_PO:
                        info.setText("#");
                        break;
                    case KEYCODE_RESET:
                        info.setText("Reset");
                        break;
                    case KEYCODE_VOLUME_UP:
                        info.setText("+");
                        break;
                    case KEYCODE_VOLUME_DOWN:
                        info.setText("-");
                        break;
                    case KEYCODE_DPAD_UP:
                        Log.i("KEYCODE", "----------");
                        info.setText("UP");
                        break;
                    case KEYCODE_DPAD_DOWN:
                        info.setText("DOWN");
                        break;
                    case KEYCODE_DPAD_LEFT:
                        info.setText("LEFT");
                        break;
                    case KEYCODE_DPAD_RIGHT:
                        info.setText("RIGHT");
                        break;
                    case KEYCODE_MENU:
                        info.setText("菜单");
                        break;
                    case KEYCODE_BACK:
                        info.setText("取消");
                        break;
                    case KEYCODE_ENTER:
                        info.setText("Enter");
                        break;
                    case KEYCODE_K1:
                        info.setText("K1");
                        break;
                    case KEYCODE_K2:
                        info.setText("K2");
                        break;
                    case KEYCODE_EMER:
                        info.setText("EMER");
                        break;
                    case KEYCODE_PTT:
                        info.setText("PTT");
                        break;
                    default:
                        info.setText("None");
                        break;
                }
            }
            return false;
        }

    }
}
