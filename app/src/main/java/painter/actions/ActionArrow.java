package painter.actions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.Log;
import android.view.MotionEvent;

import java.util.Arrays;

public class ActionArrow extends ActionStraightLine {
    static final String TAG = "-=-= ActionArrow";

    float[] triangle;
    int[] colors;

    public ActionArrow(Context context) {
        super(context);
        // x1,y1, x2...
        triangle = new float[]{0, 30, 0, -30, 50, 0};
        colors = new int[]{Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED};

        Shader shader = new LinearGradient(0,0,50,50,Color.RED,Color.GREEN, Shader.TileMode.REPEAT);
        paint.setShader(shader);
    }


    @Override
    public void setStyle(Paint p) {
        super.setStyle(p);
        // change size of triangle
        triangle[1] = thisWidth * 2.5f;
        triangle[3] = - thisWidth * 2.5f;
        triangle[4] = thisWidth * 5.5f;
        // color
        Arrays.fill(colors, thisColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // highlight by super
        super.onDraw(canvas);
        // also draw triangle
        // calculate the triangle position
        // translate and rotate?
        paint.setColor(thisColor);
        canvas.translate(coors[2], coors[3]);
        canvas.rotate(180f + (float) (Math.atan2(coors[1] - coors[3],
                coors[0] - coors[2]) * 180 / Math.PI));
        canvas.drawVertices(Canvas.VertexMode.TRIANGLE_FAN, 6, triangle, 0, null, 0,
                colors, 0, null, 0, 0, paint);
    }
}
