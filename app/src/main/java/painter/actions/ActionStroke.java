package painter.actions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;


public class ActionStroke extends AbstractPaintActionExtendsView {

    static final float MIN_MOVE_DIST = 5;
    static final long NEW_INSTANCE_DELAY_MS = 300;

    int thisColor;
    float thisWidth;
    Paint.Cap thisCap;
    Paint.Join thisJoin;

    static Paint paint;
    Path path, savedPath;
    float pathOffsetX, pathOffsetY;
    Matrix pathTransform;
    float boundW, boundH, boundCX, boundCY; // remember info about path when updating savdPath

    public ActionStroke(Context context) {
        super(context);
        if (paint == null) {
            paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setAntiAlias(true);
        }
        path = new Path();
        savedPath = new Path();
        bound = new RectF();
        pathTransform = new Matrix();

        containsPath = new Path();
    }

    long lastTimeStamp; // should combine quick strokes
    int pointerId = -1;

    float lastX, lastY;
    int action = 0; // 0 move, 1 scale, 2 rotate
    int secondPointerId = -1;
    @Override
    public boolean handleTouch(MotionEvent e) {
        int index = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        int id = e.getPointerId(index);
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                if (currentState == ActionState.NEW) {
                    pointerId = id;
                    lastX = e.getX(index);
                    lastY = e.getY(index);
                    path.moveTo(lastX, lastY);
                    currentState = ActionState.STARTED;
                } else if (currentState == ActionState.FINISHED) {
                    if (System.currentTimeMillis() - lastTimeStamp <= NEW_INSTANCE_DELAY_MS) {
                        // keep going
                        pointerId = id;
                        lastX = e.getX(index);
                        lastY = e.getY(index);
                        path.moveTo(lastX, lastY);
                        currentState = ActionState.STARTED;
                    } else {
                        callWhenDone.apply(this);
                        return false;
                    }
                } else if (currentState == ActionState.REVISING) {
                    // editing?
                    if (action == 2) return true;  // rotating, ignores new finger
                    if (e.getPointerCount() == 1) {
                        pointerId = id;
                        action = 0;
                        lastX = e.getX(index);
                        lastY = e.getY(index);
                    } else if (e.getPointerCount() == 2) {
                        if (bound.contains(e.getX(index) - pathOffsetX, e.getY(index) - pathOffsetY)) {
                            action = 1;
                            // scale
                            secondPointerId = id;
                            path.offset(-bound.centerX(), -bound.centerY());
                            pathOffsetX = (e.getX(e.findPointerIndex(pointerId)) + e.getX(e.findPointerIndex(secondPointerId))) / 2f;
                            pathOffsetY = (e.getY(e.findPointerIndex(pointerId)) + e.getY(e.findPointerIndex(secondPointerId))) / 2f;
                            boundW = bound.width();
                            boundH = bound.height();
                        } else {
                            action = 2;
                            // rotate
                            pathTransform.setScale(1, 1);
                            boundCX = bound.centerX();
                            boundCY = bound.centerY();
                        }
                        savedPath.rewind();
                        savedPath.addPath(path);

                    }
                }
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                index = e.findPointerIndex(pointerId);
                if (index == -1) return true;
                if (currentState == ActionState.STARTED) {
                    if (dist(lastX, lastY, e.getX(index), e.getY(index)) >= MIN_MOVE_DIST) {
                        path.quadTo(lastX,lastY,
                                (lastX + e.getX(index)) / 2f,
                                (lastY + e.getY(index)) / 2f);
                        lastX = e.getX(index);
                        lastY = e.getY(index);
                        invalidate();
                    }
                } else if (currentState == ActionState.REVISING) {
                    // translate
                    if (action == 0) {
                        pathOffsetX += e.getX(index) - lastX;
                        pathOffsetY += e.getY(index) - lastY;
                        lastX = e.getX(index);
                        lastY = e.getY(index);
                    } else if (action == 1) {
                        if (e.findPointerIndex(pointerId) == -1 || e.findPointerIndex(secondPointerId) == -1) {
                            return true; // finger disappeared
                        }
                        pathTransform.setScale(
                                -(e.getX(e.findPointerIndex(pointerId)) - e.getX(e.findPointerIndex(secondPointerId))) / boundW,
                                -(e.getY(e.findPointerIndex(pointerId)) - e.getY(e.findPointerIndex(secondPointerId))) / boundH,
                                    bound.centerX(), bound.centerY());
                        savedPath.transform(pathTransform, path);
                        // also translate
                        pathOffsetX = (e.getX(e.findPointerIndex(pointerId)) + e.getX(e.findPointerIndex(secondPointerId))) / 2f;
                        pathOffsetY = (e.getY(e.findPointerIndex(pointerId)) + e.getY(e.findPointerIndex(secondPointerId))) / 2f;
                    } else if (action == 2) {
                        // rotate
                        pathTransform.setRotate(
                                snapAngle((float) -angleBetween(boundCX + pathOffsetX, boundCY + pathOffsetY, e.getX(), e.getY())),
                                boundCX, boundCY);
                        savedPath.transform(pathTransform, path);
                    }
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                if (currentState == ActionState.STARTED) {
                    currentState = ActionState.FINISHED;
                    lastTimeStamp = System.currentTimeMillis();
                } else if (currentState == ActionState.REVISING) {
                    // revising
                    if (action == 1 && e.getPointerCount() == 2) {
                        action = 0; // back to move
                        lastX = e.getX(e.findPointerIndex(pointerId));
                        lastY = e.getY(e.findPointerIndex(pointerId));
                    } if (action == 2 && e.getPointerCount() == 1) {
                        // stop
                        action = 0;
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
        thisColor = p.getColor();
        thisWidth = p.getStrokeWidth();
        thisCap = p.getStrokeCap();
        thisJoin = p.getStrokeJoin();
    }

    RectF bound;
    float animate;
    @Override
    protected void onDraw(Canvas canvas) {

        paint.setColor(thisColor);
        paint.setStrokeCap(thisCap);
        paint.setStrokeWidth(thisWidth);
        paint.setStrokeJoin(thisJoin);
        canvas.translate(pathOffsetX, pathOffsetY);

        canvas.drawPath(path, paint);
        if (currentState == ActionState.REVISING) {
            // high light
            paint.setAlpha((int) (70 + 70 * Math.sin(animate)));
            animate += 0.1;
            invalidate();
            // or bound?
            paint.setStrokeWidth(HIGHLIGHT_STROKE_WIDTH);
            path.computeBounds(bound, false);
            canvas.drawRect(bound, paint);

            if (action == 2) {
                // rotate center
                paint.setAlpha(HIGHLIGHT_ALPHA);
                canvas.drawCircle(boundCX, boundCY, 10, paint);
            }
        }

    }

    Path containsPath;
    @Override
    public boolean contains(float x, float y, float radius) {
        containsPath.addRect(x - radius, y - radius, x + radius, y - radius, Path.Direction.CW);
        containsPath.op(path, Path.Op.DIFFERENCE);
        post(() -> containsPath.rewind());
        return containsPath.isEmpty();
    }
}
