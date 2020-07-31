package painter.actions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;

import java.util.Arrays;

public class ActionArrow extends ActionStraightLine {

    float[] triangle;
    int[] colors;

    public ActionArrow(Context context) {
        super(context);
        // x1,y1, x2...
        triangle = new float[]{0, 30, 0, -30, 50, 0};
        colors = new int[]{Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED};
    }


    @Override
    public void setStyle(Paint p) {
        super.setStyle(p);
        // change size of triangle
        triangle[1] = thisWidth * 4;
        triangle[3] = thisWidth * 4;
        triangle[4] = thisWidth * 10;
        // color
        Arrays.fill(colors, thisColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // also draw triangle
        // calculate the triangle position
        // translate and rotate?
        canvas.translate(coordinates[2], coordinates[3]);
        canvas.rotate(180f + (float) (Math.atan2(coordinates[1] - coordinates[3],
                coordinates[0] - coordinates[2]) * 180 / Math.PI));
        canvas.drawVertices(Canvas.VertexMode.TRIANGLE_FAN, 6, triangle, 0, null, 0,
                colors, 0, null, 0, 0, paint);
    }
}
