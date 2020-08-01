package painter.actions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.Log;
import android.view.MotionEvent;

import java.nio.file.ClosedDirectoryStreamException;
import java.util.HashMap;

public class ActionRectangle extends AbstractPaintActionExtendsView {


    // positions
    float[] coors; // x1,y1, x2,y2

    static Paint paint;

    int myColor;
    float myWidth;
    Paint.Style myStyle;
    float rotateAngle;

    public ActionRectangle(Context context) {
        super(context);
        if (paint == null) {
            paint = new Paint();
            paint.setAntiAlias(true);
        }
        idMap = new HashMap<>();
        coors = new float[4];
        // default
        myColor = Color.GREEN;
        myWidth = 10;
        myStyle = Paint.Style.STROKE;
        rotateAngle = 0;



    }


    // associate id to index
    HashMap<Integer, Integer> idMap;
    boolean firstTouch = true;
    int action = 0; // 0 resizing, 1 moving, 2 rotating
    float lastX, lastY;
    @Override
    public boolean handleTouch(MotionEvent e) {
        int index = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        int id = e.getPointerId(index);

        if (!idMap.containsKey(id) && idMap.size() < 2) { // at most 2 fingers
            idMap.put(id, idMap.size() * 2); // hack  value = 0 or 2
        }

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (currentState == ActionState.NEW) {
                    // init both points
                    coors[0] = e.getX(index);
                    coors[1] = e.getY(index);
                    coors[2] = e.getX(index);
                    coors[3] = e.getY(index);
                    firstTouch = false;
                    currentState = ActionState.STARTED;
                } else if (currentState == ActionState.FINISHED) {
                    // done
                    callWhenDone.apply(this);
                    return false;
                } else {
                    // more than one finger or resize or move
                    if (e.getPointerCount() == 1) {
                        action = 1;
                        lastX = e.getX(index);
                        lastY = e.getY(index);
                    } else if (e.getPointerCount() == 2) {
                        action = 0;
                    } else {
                        action = 2;
                    }
                }
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (action == 1) {
                    // moving
                    coors[0] -= lastX - e.getX(index);
                    coors[1] -= lastY - e.getY(index);
                    coors[2] -= lastX - e.getX(index);
                    coors[3] -= lastY - e.getY(index);
                    lastX = e.getX(index);
                    lastY = e.getY(index);
                } else if (action == 2) {
                    // resizing
                    // rotating todo
                    rotateAngle = (float)
                            (Math.atan2((coors[0] + coors[2]) / 2 - e.getX(index),
                                (coors[1] + coors[3]) / 2 - e.getY(index))
                            * 180 / Math.PI);
                    // snap to angle
                    for (int i = -180; i <= 180; i += 90) {
                        if (Math.abs(rotateAngle - i) < 3) {
                            rotateAngle = i;
                            break;
                        }
                    }
                } else {
                    // resizing
                    for (int i : idMap.keySet()) {
                        if (e.findPointerIndex(i) != -1) {
                            // resize it
                            coors[idMap.get(i)] = e.getX(e.findPointerIndex(i));
                            coors[idMap.get(i) + 1] = e.getY(e.findPointerIndex(i));
                        }
                    }
                }
                invalidate();
                return true;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                if (e.getPointerCount() == 1) {
                    // last finger up
                    if (currentState == ActionState.STARTED) {
                        currentState = ActionState.FINISHED;
                    }
                }
                invalidate();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void setStyle(Paint p) {
        myColor = p.getColor();
        myWidth = p.getStrokeWidth();
        myStyle = p.getStyle();
    }


    float time = 0;
    @Override
    protected void onDraw(Canvas canvas) {

        paint.setColor(myColor);
        paint.setStrokeWidth(myWidth);
        paint.setStyle(myStyle);

        if (rotateAngle != 0) {
            canvas.translate((coors[0] + coors[2]) / 2, (coors[1] + coors[3]) / 2);
            canvas.rotate(-rotateAngle);
            canvas.translate(-(coors[0] + coors[2]) / 2, -(coors[1] + coors[3]) / 2);
        }

        canvas.drawRect(
                Math.min(coors[0], coors[2]),
                Math.min(coors[1], coors[3]),
                Math.max(coors[0], coors[2]),
                Math.max(coors[1], coors[3]),
                paint);

        // show editing
        conditionalDrawHighlight(canvas);
    }


    void conditionalDrawHighlight(Canvas canvas) {
        if (currentState == ActionState.REVISING || currentState == ActionState.STARTED) {
            // draw indicator
            paint.setAlpha(120);
            canvas.drawCircle((coors[0] + coors[2]) / 2f, (coors[1] + coors[3]) / 2f,
                    (float) (dist(coors[0], coors[1], coors[2], coors[3]) / 20f * (0.1 * Math.sin(time) + 1)),
                    paint);
            time += 0.08;
            invalidate();
        }
    }

}
