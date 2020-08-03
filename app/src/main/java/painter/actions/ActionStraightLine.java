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
    float[] coors;

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
        coors = new float[4];
    }


    int currentIndex; // can be -1, 0, 2
    float lastX, lastY;
    @Override
    public boolean handleTouch(MotionEvent e) {
        if (super.handleTouch(e)) {
            return true;
        }
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (currentState == ActionState.NEW) {
                    // init both end
                    coors[0] = e.getX();
                    coors[1] = e.getY();
                    coors[2] = e.getX();
                    coors[3] = e.getY();
                    // change current state
                    currentIndex = 2;
                    currentState = ActionState.STARTED;
                } else if (currentState == ActionState.FINISHED) {
                    // done
                    callWhenDone.apply(this);
                    return false;
                } else if (currentState == ActionState.STARTED || currentState == ActionState.REVISING) {
                    // decide: resize, move
                    // which end point to move?
                    if (dist(coors[0], coors[1], e.getX(), e.getY()) < ACTION_RADIUS) {
                        currentIndex = 0;
                    } else if (dist(coors[2], coors[3], e.getX(), e.getY()) < ACTION_RADIUS) {
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
                    if (Math.abs(lastX - e.getX()) < 250 && Math.abs(lastY - e.getY()) < 250) {
                        coors[0] -= lastX - e.getX();
                        coors[1] -= lastY - e.getY();
                        coors[2] -= lastX - e.getX();
                        coors[3] -= lastY - e.getY();
                    }
                    lastX = e.getX();
                    lastY = e.getY();
                } else { // move one end point
                    coors[currentIndex] = e.getX();
                    coors[currentIndex + 1] = e.getY();
                    // snap to same x
                    if (Math.abs(coors[0] - coors[2]) < 10) {
                        coors[currentIndex] = coors[2 - currentIndex];
                    } else if (Math.abs(coors[1] - coors[3]) < 10) {
                        // same y
                        coors[currentIndex + 1] = coors[2 - currentIndex + 1];
                    }
                }
                invalidate();
                if (e.getPointerCount() == 1 &&
                        e.getActionMasked() == MotionEvent.ACTION_UP &&
                        currentState == ActionState.STARTED) { // change this to click done or clicked edit
                    currentState = ActionState.FINISHED;
                }
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

    float animate = 0;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // draw line
        paint.setColor(thisColor);
        paint.setStrokeWidth(thisWidth);
        canvas.drawLine(coors[0], coors[1], coors[2], coors[3], paint);

        // draw high light
        conditionalDrawHighlight(canvas);


    }


    void conditionalDrawHighlight(Canvas canvas) {
        // draw high light
        if (currentState == ActionState.REVISING || currentState == ActionState.STARTED) {
            paint.setAlpha(HIGHLIGHT_ALPHA);
            paint.setStrokeWidth(HIGHLIGHT_STROKE_WIDTH);
            paint.setXfermode(HIGHLIGHT_PAINT_MODE);
            canvas.drawCircle((coors[0] + coors[2]) / 2f, (coors[1] + coors[3]) / 2f,
                    thisWidth * 3 * (float) (1 + Math.sin(animate) * 0.1), paint);
            paint.setXfermode(null);
            animate += 0.08;
            invalidate();
        }
    }

    @Override
    public boolean contains(float x, float y, float radius) {
        return false;
    }
}
