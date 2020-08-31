package painter.actions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
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
    }


    // associate id to index
    HashMap<Integer, Integer> idMap;
    boolean firstTouch = true;
    int action = 0; // 0 resizing, 1 moving, 2 rotating
    float lastX, lastY;
    @Override
    public boolean handleTouch(MotionEvent e) {

        if (super.handleTouch(e)) {
            return true;
        }

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
                        if (dist((coors[0] + coors[2]) / 2, (coors[1] + coors[3]) / 2,e.getX(index), e.getY(index)) <
                                Math.max(Math.abs(coors[2] - coors[0]), Math.abs(coors[3] - coors[1]))) {
                            action = 0; // resize
                        } else {
                            action = 2; // rotate
                        }
                    }
                }
                updateMyPath();
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
                    // rotating
                    rotateAngle = (float) angleBetween(
                            (coors[0] + coors[2]) / 2, (coors[1] + coors[3]) / 2,
                            e.getX(index), e.getY(index));
                    // snap to angle
                    rotateAngle = snapAngle(rotateAngle);
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
                updateMyPath();
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
                updateMyPath();
                invalidate();
                return true;
            default:
                return false;
        }
    }

    Matrix rotationMatrix;
    void updateMyPath() {
        if (rotationMatrix == null) {
            rotationMatrix = new Matrix();
        }
        rotationMatrix.setRotate(-rotateAngle, (coors[0] + coors[2]) / 2f, (coors[1] + coors[3]) / 2f);
        myPath.rewind();
        myPath.addRect(
                Math.min(coors[0], coors[2]),
                Math.min(coors[1], coors[3]),
                Math.max(coors[0], coors[2]),
                Math.max(coors[1], coors[3]), Path.Direction.CW);
        myPath.transform(rotationMatrix);
    }

    @Override
    public void setStyle(Paint p) {
        super.setStyle(p);
        myColor = p.getColor();
        myWidth = p.getStrokeWidth();
        myStyle = p.getStyle();
    }


    float time = 0;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        onDraw2(canvas);

        // show editing
        conditionalDrawHighlight(canvas);
    }

    void onDraw2(Canvas canvas) {

        if (rotateAngle != 0) {
            canvas.translate((coors[0] + coors[2]) / 2, (coors[1] + coors[3]) / 2);
            canvas.rotate(-rotateAngle);
            canvas.translate(-(coors[0] + coors[2]) / 2, -(coors[1] + coors[3]) / 2);
        }

        paint.setColor(myColor);
        paint.setStrokeWidth(myWidth);
        paint.setStyle(myStyle);

        canvas.drawRect(
                Math.min(coors[0], coors[2]),
                Math.min(coors[1], coors[3]),
                Math.max(coors[0], coors[2]),
                Math.max(coors[1], coors[3]),
                paint);
    }


    void conditionalDrawHighlight(Canvas canvas) {
        if (currentState == ActionState.REVISING || currentState == ActionState.STARTED) {
            // draw indicator
            paint.setAlpha(HIGHLIGHT_ALPHA);
            paint.setStrokeWidth(HIGHLIGHT_STROKE_WIDTH);
            paint.setStyle(Paint.Style.STROKE);
            paint.setXfermode(HIGHLIGHT_PAINT_MODE);
            canvas.drawCircle((coors[0] + coors[2]) / 2f, (coors[1] + coors[3]) / 2f,
                    (float) (dist(coors[0], coors[1], coors[2], coors[3]) / 30f * (0.1 * Math.sin(time) + 1)) + 10,
                    paint);
            paint.setXfermode(null);
            time += 0.08;
            invalidate();
        }
    }

    // subclassing

    @Override
    void toggleFill() {
        if (myStyle == Paint.Style.FILL) {
            myStyle = Paint.Style.STROKE;
        } else if (myStyle == Paint.Style.STROKE) {
            myStyle = Paint.Style.FILL;
        } else {
            myStyle = Paint.Style.FILL;
        }
        invalidate();
    }
}
