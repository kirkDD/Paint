package painter.actions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * draw a straight line
 */
public class ActionStraightLine extends AbstractPaintActionExtendsView {

    // x1, y1, x2, y2
    float[] coordinates;

    public ActionStraightLine(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(Color.RED); // default
        paint.setStrokeWidth(10); // default
        coordinates = new float[4];
        started = false;
    }

    boolean started;
    @Override
    boolean handleTouch(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (!started) {
                    // init both end
                    coordinates[0] = e.getX();
                    coordinates[1] = e.getY();
                    coordinates[2] = e.getX();
                    coordinates[3] = e.getY();
                    started = true;
                } else {
                    // move or reshape line?

                }
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                coordinates[2] = e.getX();
                coordinates[3] = e.getY();
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                coordinates[2] = e.getX();
                coordinates[3] = e.getY();
                invalidate();
                return true;
            default:
                return false;
        }
    }

    @Override
    void setStyle(Paint p) {
        paint.setStrokeWidth(p.getStrokeWidth());
        paint.setColor(p.getColor());
    }

    @Override
    boolean yóuD¤ne() {
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // draw line
        canvas.drawLine(coordinates[0], coordinates[1], coordinates[2], coordinates[3], paint);
    }
}
