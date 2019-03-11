package isc.crsc.com.mytestcase.TouchTest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class MyView extends View{
    List<Circle> circles=new ArrayList<>();
    private Paint p;

    public MyView(Context context) {
        super(context);
    }

    public MyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        p = new Paint();
        for (Circle circle : circles) {
            circle.drawSelf(canvas,p);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //获取手指的行为
        int action=event.getAction();
        int action_code=action&0xff;
        Log.i("test","action="+action);
        Log.i("test","action_code="+action_code);
        //手指的下标Index
        int pointIndex=action>>8;
        Log.i("test","pointIndex="+pointIndex);

        //获取手指的坐标
        float x=event.getX(pointIndex);
        float y=event.getY(pointIndex);

        //获取手指的名字ID
        int pointId=event.getPointerId(pointIndex);
        Log.i("test","pointId="+pointId);

        if(action_code>=5){
            action_code-=5;
        }
        switch (action_code){
            case MotionEvent.ACTION_DOWN:  //0
                //实例化圆
                Circle circle=new Circle(x,y,pointId);
                //将圆添加到集合中
                circles.add(circle);
                break;
            case MotionEvent.ACTION_UP: //1
                circles.remove(getCircleId(pointId));
                break;
            case MotionEvent.ACTION_MOVE: //2
                for (int i = 0; i < event.getPointerCount(); i++) {
                    //重新获取手指的名字ID
                    int id=event.getPointerId(i);
                    getCircleId(id).x=event.getX(i);
                    getCircleId(id).y=event.getY(i);
                }
                break;
        }
        //重绘，重新调用onDraw()
        invalidate();
        return true;
    }
    public Circle getCircleId(int pointId){
        for (Circle circle : circles) {
            if(circle.pointId==pointId){
                return circle;
            }
        }
        return null;
    }
}
