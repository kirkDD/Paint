package painter.actions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;

import painter.help.InterestingPoints;

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
                    // also snap
                    InterestingPoints.Point p = interestingPoints.query(e.getX(), e.getY());
                    if (p != null) {
                        coors[0] = p.x;
                        coors[1] = p.y;
                    }
                } else if (currentState == ActionState.FINISHED) {
                    // done
                    callWhenDone.apply(this);
                    return false;
                } else if (currentState == ActionState.STARTED || currentState == ActionState.REVISING) {
                    removeAllInterestingPoints();
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
                updateMyPath();

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
                    } else {
                        lastX = e.getX();
                        lastY = e.getY();
                    }
                    boolean snapped = false;
                    for (int i = 0; i < 2; i++) {
                        InterestingPoints.Point p = interestingPoints.query(coors[i * 2], coors[i * 2 + 1]);
                        if (p != null) {
                            // delta snap
                            float dx = p.x - coors[i * 2];
                            float dy = p.y - coors[i * 2 + 1];
                            coors[0] += dx;
                            coors[1] += dy;
                            coors[2] += dx;
                            coors[3] += dy;
                            snapped = true;
                            break;
                        }
                    }
                    if (!snapped) {
                        lastX = e.getX();
                        lastY = e.getY();
                    }
                } else { // move one end point
//                    coors[currentIndex] = e.getX();
//                    coors[currentIndex + 1] = e.getY();
                    // snap to ip
                    InterestingPoints.Point p = interestingPoints.query(e.getX(), e.getY());
                    if (p != null) {
                        // single snap
                        coors[currentIndex] = p.x;
                        coors[currentIndex + 1] = p.y;
                    } else {
                        coors[currentIndex] = e.getX();
                        coors[currentIndex + 1] = e.getY();
                    }
                    // snap to same x
                    if (Math.abs(coors[0] - coors[2]) < 10) {
                        coors[currentIndex] = coors[2 - currentIndex];
                    } else if (Math.abs(coors[1] - coors[3]) < 10) {
                        // same y
                        coors[currentIndex + 1] = coors[2 - currentIndex + 1];
                    }
                }

                updateMyPath();

                invalidate();

                if (e.getPointerCount() == 1 &&
                        e.getActionMasked() == MotionEvent.ACTION_UP) { // change this to click done or clicked edit
                    // set interesting points
                    removeAllInterestingPoints();
                    addAllInterestingPoints();
                    if (currentState == ActionState.STARTED) {
                        currentState = ActionState.FINISHED;
                    }
                }

                return true;
            default:
                return false;
        }
    }

    @Override
    public void addAllInterestingPoints() {
        super.addAllInterestingPoints();
        interestingPoints.addPoint(this, coors[0], coors[1]);
        interestingPoints.addPoint(this, coors[2], coors[3]);
    }

    void updateMyPath() {
        myPath.rewind();
        myPath.moveTo(coors[0], coors[1]);
        myPath.lineTo(coors[2], coors[3]);
        myPath.lineTo(coors[2] + 1, coors[3] + 1);
    }

    @Override
    public void setStyle(Paint p) {
        super.setStyle(p);
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
    AbstractPaintActionExtendsView duplicateImp() {
        ActionStraightLine re = new ActionStraightLine(getContext());
        for (int i = 0; i < coors.length; i++) {
            re.coors[i] = coors[i] + DUPLICATE_OFFSET;
        }
        re.thisColor = thisColor;
        re.thisWidth = thisWidth;
        re.currentState = ActionState.FINISHED;
        re.updateMyPath();
        return re;
    }
}
