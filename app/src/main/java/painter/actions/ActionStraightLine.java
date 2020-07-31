package painter.actions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

/**
 * draw a straight line
 */
public class ActionStraightLine extends AbstractPaintActionExtendsView {
    static final String TAG = "-=-= Straight Line";
    static final int ACTION_RADIUS = 100;

    // x1, y1, x2, y2
    float[] coordinates;

    static Paint paint; // all lines share paint

    int thisColor;
    float thisWidth;
    public ActionStraightLine(Context context) {
        super(context);
        if (paint == null) {
            paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setAntiAlias(true);
        }
        thisColor = Color.RED; // default
        thisWidth = 10f; // default
        coordinates = new float[4];
        started = false;
    }

    boolean started;
    int currentIndex; // can be -1, 0, 2
    float lastX, lastY;
    @Override
    public boolean handleTouch(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (!started) {
                    // init both end
                    coordinates[0] = e.getX();
                    coordinates[1] = e.getY();
                    coordinates[2] = e.getX();
                    coordinates[3] = e.getY();
                    started = true;
                    currentIndex = 2;
                } else {
                    // move or reshape line?
                    // which end point to move?
                    if (dist(coordinates[0], coordinates[1], e.getX(), e.getY()) < ACTION_RADIUS) {
                        currentIndex = 0;
                    } else if (dist(coordinates[2], coordinates[3], e.getX(), e.getY()) < ACTION_RADIUS) {
                        currentIndex = 2;
                    } else {
                        lastX = e.getX();
                        lastY = e.getY();
                        currentIndex = -1;
                    }
                }
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                if (currentIndex == -1) { // move both
                    coordinates[0] -= lastX - e.getX();
                    coordinates[1] -= lastY - e.getY();
                    coordinates[2] -= lastX - e.getX();
                    coordinates[3] -= lastY - e.getY();
                    lastX = e.getX();
                    lastY = e.getY();
                    // don't move off screen
//                    if () 
                } else { // move one end point
                    coordinates[currentIndex] = e.getX();
                    coordinates[currentIndex + 1] = e.getY();
                }
                invalidate();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void setStyle(Paint p) {
        thisWidth = p.getStrokeWidth();
        thisColor = p.getColor();
    }

    @Override
    public boolean yóuD¤ne() {
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // draw line
        paint.setColor(thisColor);
        paint.setStrokeWidth(thisWidth);
        canvas.drawLine(coordinates[0], coordinates[1], coordinates[2], coordinates[3], paint);
    }
}
