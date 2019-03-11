package isc.crsc.com.mytestcase.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import isc.crsc.com.mytestcase.R;

public class ToastUtil {

    @SuppressLint({"ResourceAsColor", "WrongConstant"})
    public void SetToast(Context context, String text) {
        Toast toast = new Toast(context);
        TextView view = new TextView(context);
        view.setBackgroundResource(R.color.toastColor);
        view.setTextColor(R.color.textColor);
        view.setTextSize(18);
        view.setText(text);
        //设置Toast要显示的位置，水平居中并在底部，X轴偏移0个单位，Y轴偏移70个单位，
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 70);
        //设置显示时间
        toast.setDuration(0);
        view.setPadding(30, 10, 30, 10);
//        toast.setGravity(Gravity.CENTER, 0, 40);
        toast.setView(view);
        toast.show();
    }


}
