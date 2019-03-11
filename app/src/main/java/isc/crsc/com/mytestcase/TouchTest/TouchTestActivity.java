package isc.crsc.com.mytestcase.TouchTest;

import android.app.Activity;
import android.os.Bundle;

import isc.crsc.com.mytestcase.util.hideScreen;

public class TouchTestActivity extends Activity {
    private hideScreen hideScreen = new hideScreen();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideScreen.hideSystemUI(getWindow().getDecorView());
        setContentView(new MyView(this));
    }
}
