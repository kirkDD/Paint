package painter.actions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import java.util.Arrays;

import painter.help.InterestingPoints;

public class ActionTriangle extends AbstractPaintActionExtendsView {

    static int NUM_ACTIVE = 0;
    static boolean MOVING_CORNER = false;
    boolean ME_WROTE_MOVING_CORNER;

    static Paint paint;
    float strokeWidth;
    int color;
    Paint.Style style = Paint.Style.STROKE;
    float[] pos;
    int[] colors;

    public ActionTriangle(Context context) {
        super(context);
        if (paint == null) {
            paint = new Paint();
            paint.setAntiAlias(true);
        }
        pos = new float[6];
        colors = new int[6];
    }

    @Override
    public void setStyle(Paint p) {
        super.setStyle(p);
        color = p.getColor();
        strokeWidth = p.getStrokeWidth();
        style = p.getStyle();
        Arrays.fill(colors, color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw a triangle
        paint.setStyle(style);
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
//        canvas.drawPath(myPath, paint);

        canvas.drawVertices(Canvas.VertexMode.TRIANGLE_FAN, 6, pos, 0, null, 0,
                colors, 0, null, 0, 0, paint);

        if (currentState == ActionState.REVISING) {
            paint.setStrokeWidth(HIGHLIGHT_STROKE_WIDTH);
            paint.setStyle(Paint.Style.STROKE);
            paint.setAlpha(255);
            for (int i = 0; i < 3; i++) {
                if (dragging && dragIndex == i) {
                    canvas.drawCircle(pos[i * 2], pos[i * 2 + 1], EDIT_TOUCH_RADIUS * 3, paint);
                } else {
                    canvas.drawCircle(pos[i * 2], pos[i * 2 + 1], EDIT_TOUCH_RADIUS, paint);
                }
            }
        }
    }

    float lastX, lastY;
    boolean dragging, moving;
    int dragIndex;
    @Override
    public boolean handleTouch(MotionEvent e) {
        if (super.handleTouch(e))
            return true;
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                switch (currentState) {
                    case NEW:
                        currentState = ActionState.STARTED;
                        for (int i = 0; i < 3; i++) {
                            pos[i * 2] = e.getX();
                            pos[i * 2 + 1] = e.getY();
                            snapToInterestingPoint(i);
                        }
                        updateMyPath();
                        break;
                    case FINISHED:
                        callWhenDone.apply(this);
                        return false;
                    case STARTED:
                        break;
                    case REVISING:
                        removeAllInterestingPoints();
                        NUM_ACTIVE++;
                        lastX = e.getX();
                        lastY = e.getY();
                        for (int i = 0; i < 3; i++) {
                            if (dist(pos[i * 2], pos[i * 2 + 1], lastX, lastY) < EDIT_TOUCH_RADIUS) {
                                dragging = true;
                                dragIndex = i;
                                if (!MOVING_CORNER) {
                                    MOVING_CORNER = true;
                                    ME_WROTE_MOVING_CORNER = true;
                                }
                                invalidate();
                                break;
                            }
                        }
                        if (!dragging) {
                            moving = contains(e.getX(), e.getY(), 1);
                        }
                        break;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                switch (currentState) {
                    case STARTED:
                        // make a nice triangle
                        pos[4] = e.getX();
                        pos[5] = e.getY();
                        snapToInterestingPoint(2);
                        if (pos[0] - pos[4] == 0) {
                            // vertical
                            pos[3] = (pos[1] + pos[5]) / 2;
                            pos[2] = (float) (pos[0] + Math.sqrt(3) / 2 * (pos[1] - pos[5]));
                        } else {
                            float slope = (pos[5] - pos[1]) / (pos[4] - pos[0]);
                            slope = -1 / slope;
                            float length = (float) (dist(pos[0], pos[1], pos[4], pos[5]) *
                                    Math.sqrt(3) / 2);
//                            Log.d(TAG, "handleTouch: length " + length);
                            pos[2] = (pos[0] + pos[4]) / 2;
                            pos[3] = (pos[1] + pos[5]) / 2;
                            float dx = (float) (length / (Math.sqrt(1 + slope * slope)));
                            if (pos[5] < pos[1]) {
                                dx = -dx;
                            }
                            float dy = dx * slope;
                            pos[2] += dx;
                            pos[3] += dy;
                        }
                        updateMyPath();
                        break;
                    case REVISING:
                        if (dragging) {
                            pos[dragIndex * 2] = e.getX();
                            pos[dragIndex * 2 + 1] = e.getY();
                            snapToInterestingPoint(dragIndex);
                        } else if (!MOVING_CORNER) {
                            if (dist(lastX , lastY, e.getX(), e.getY()) > 250) {
                                // skip if too large, 2 fingers
                                lastX = e.getX();
                                lastY = e.getY();
                            }
                            for (int i = 0; i < 3; i++) {
                                pos[i * 2] += e.getX() - lastX;
                                pos[i * 2 + 1] += e.getY() - lastY;
                            }
                            if (NUM_ACTIVE == 1) {
                                if (!shiftSnap()) {
                                    lastX = e.getX();
                                    lastY = e.getY();
                                }
                            } else {
                                lastX = e.getX();
                                lastY = e.getY();
                            }
                            Log.d(TAG, "handleTouch: num active " + NUM_ACTIVE);
                        }
                        updateMyPath();
                        break;
                    case NEW:
                    case FINISHED:
                        return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                switch (currentState) {
                    case STARTED:
                        currentState = ActionState.FINISHED;
                        addAllInterestingPoints();
                        invalidate();
                        break;
                    case REVISING:
                        NUM_ACTIVE--;
                        dragging = false;
                        moving = false;
                        if (ME_WROTE_MOVING_CORNER) {
                            MOVING_CORNER = false;
                            ME_WROTE_MOVING_CORNER = false;
                        }
                        removeAllInterestingPoints();
                        addAllInterestingPoints();
                        invalidate();
                        break;
                    case NEW:
                    case FINISHED:
                }
                break;
            default:
                return false;
        }
        return true;
    }


    void updateMyPath() {
        myPath.rewind();
        myPath.moveTo(pos[0], pos[1]);
        myPath.lineTo(pos[2], pos[3]);
        myPath.lineTo(pos[4], pos[5]);
        myPath.close();
        invalidate();
    }

    @Override
    public void addAllInterestingPoints() {
        super.addAllInterestingPoints();
        for (int i = 0; i < 3; i++) {
            interestingPoints.addPoint(this, pos[i * 2], pos[i * 2 + 1]);
        }
    }

    void snapToInterestingPoint(int index) {
        // snap to IP
        InterestingPoints.Point ip = interestingPoints.query(pos[index * 2], pos[index * 2 + 1]);
        if (ip != null) {
            pos[index * 2] = ip.x;
            pos[index * 2 + 1] = ip.y;
        }
    }

    boolean shiftSnap() {
        for (int i = 0; i < 3; i++) {
            InterestingPoints.Point point = interestingPoints.query(pos[i * 2], pos[i * 2 + 1]);
            if (point != null) {
                // this one
                float dx = point.x - pos[i * 2];
                float dy = point.y - pos[i * 2 + 1];
                for (int j = 0; j < 3; j++) {
                    pos[j * 2] += dx;
                    pos[j * 2 + 1] += dy;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    AbstractPaintActionExtendsView duplicateImp() {
        ActionTriangle other = new ActionTriangle(getContext());
        for (int i = 0; i < other.pos.length; i++) {
            other.pos[i] = pos[i] + DUPLICATE_OFFSET;
            other.colors[i] = colors[i];
        }
        other.color = color;
        other.strokeWidth = strokeWidth;
        other.style = style;
        other.currentState = ActionState.FINISHED;
        return other;
    }
}

