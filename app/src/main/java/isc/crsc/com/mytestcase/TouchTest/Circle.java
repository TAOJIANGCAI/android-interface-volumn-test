package isc.crsc.com.mytestcase.TouchTest;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Random;

public class Circle {
    public float x;
    public float y;
    public int r=100;
    public int pointId;
    int red;
    int green;
    int blue;
    Random random=new Random();
    public Circle(float x, float y, int pointId) {
        this.x = x;
        this.y = y;
        this.pointId = pointId;
        red=random.nextInt(255);
        green=random.nextInt(255);
        blue=random.nextInt(255);
    }
    public void drawSelf(Canvas canvas, Paint paint){
        paint.setColor(Color.rgb(red,green,blue));
        canvas.drawCircle(x,y,r,paint);
    }
}
