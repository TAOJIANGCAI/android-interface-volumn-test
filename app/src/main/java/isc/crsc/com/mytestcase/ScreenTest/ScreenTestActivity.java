package isc.crsc.com.mytestcase.ScreenTest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import isc.crsc.com.mytestcase.R;
import isc.crsc.com.mytestcase.util.hideScreen;

public class ScreenTestActivity extends Activity implements View.OnClickListener {
    private TextView myTextView;
    private int flag;
    private int index = 0;
    private hideScreen hideScreen = new hideScreen();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_main);
        hideScreen.hideSystemUI(getWindow().getDecorView());
        myTextView = findViewById(R.id.myText);
        // myTextView.setText("屏幕测试");
        myTextView.setTextColor(Color.WHITE);
        myTextView.setOnClickListener(this);
        flag = 1;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void onClick(View view) {
        flag = flag % 5;
        switch (flag) {
        /*    case 0:
                myTextView.setBackgroundColor(Color.WHITE);
                break;*/
            case 1:
                myTextView.setBackgroundColor(Color.RED);
                index++;
                break;
            case 2:
                myTextView.setBackgroundColor(Color.BLUE);
                index++;
                break;
            case 3:
                myTextView.setBackgroundColor(Color.YELLOW);
                ScreenTestActivity.this.finish();
                return;
        }
        flag++;
    }
}
