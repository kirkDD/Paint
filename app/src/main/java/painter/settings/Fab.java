package painter.settings;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.view.ActionBarPolicy;
import android.view.MotionEvent;

import java.util.HashMap;

import cse340.undo.R;
import painter.actions.AbstractPaintActionExtendsView;
import painter.actions.ActionArrow;
import painter.actions.ActionDash;
import painter.actions.ActionLetters;
import painter.actions.ActionNumbers;
import painter.actions.ActionOval;
import painter.actions.ActionPen;
import painter.actions.ActionRectangle;
import painter.actions.ActionStraightLine;
import painter.actions.ActionStroke;


/**
 * a draggable FAB that toggles
 */
public class Fab extends AbstractSetting {

    int iconRadius;
    HashMap<Class<? extends AbstractPaintActionExtendsView>, Integer> shapesMap;
    @Override
    void privateInit() {
        iconRadius = Math.min(iW, iH) / 2;
        paint.setTextSize(iconRadius / 1.7f);
        paint.setTextAlign(Paint.Align.CENTER);

        shapesMap = new HashMap<>();
//        shapesMap.put(ActionStroke.class, R.string.stroke);
        shapesMap.put(ActionPen.class, R.string.stroke);
        shapesMap.put(ActionStraightLine.class, R.string.line);
        shapesMap.put(ActionArrow.class, R.string.arrow);
        shapesMap.put(ActionLetters.class, R.string.letter);
        shapesMap.put(ActionNumbers.class, R.string.number);
        shapesMap.put(ActionRectangle.class, R.string.rect);
        shapesMap.put(ActionOval.class, R.string.oval);
        shapesMap.put(ActionDash.class, R.string.dash);
    }

    int iconAlpha = 0;
    int targetIconAlpha = 111;
    @Override
    public void drawIcon(Canvas canvas) {
        super.drawIcon(canvas);
        paint.setColor(getContrastColor(paper.getBackgroundColor()));
        paint.setAlpha(iconAlpha);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(iconRadius / 3f);
        canvas.drawCircle(iLeft + iW / 2f, iTop + iH / 2f, iconRadius - paint.getStrokeWidth(), paint);

        // show color
        paint.setColor(paper.getPaintToEdit().getColor());
        paint.setAlpha(iconAlpha);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(iLeft + iW / 2f, iTop + iH / 2f, iconRadius - paint.getStrokeWidth() / 2, paint);

        // show state
        if (!paper.isPanning() && !paper.isErasing()) {
            // show shapes
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(getContrastColor(paper.getBackgroundColor()) == Color.WHITE ? Color.BLACK : Color.WHITE);
            canvas.drawText(paper.getContext().getResources().getString(shapesMap.get(paper.getCurrentAction())),
                    iLeft + iW / 2f, iTop + iH / 2f + paint.getTextSize() / 2 - paint.descent() / 2, paint);
        }
        if (snapToEdge) {
            snapToEdge();
        }
        if (iconAlpha != targetIconAlpha) {
            iconAlpha += (targetIconAlpha - iconAlpha) * 0.2;
            if (targetIconAlpha > iconAlpha) {
                iconAlpha += 1;
            } else {
                iconAlpha -= 1;
            }
            invalidate();
        }
    }

    @Override
    public boolean inIcon(float xPos, float yPos) {
        return dist(xPos, yPos, iLeft + iW / 2f, iTop + iH / 2f) < Math.min(iW, iH) / 2f + 20; // 20 spare
    }

    float sX, sY;
    boolean dragging;
    @Override
    public boolean handleQuickEvent(MotionEvent e) {
        super.handleQuickEvent(e);
        // click vs drag?
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                snapToEdge = false;
                sX = e.getX();
                sY = e.getY();
                targetIconAlpha = 222;
                invalidate();
            case MotionEvent.ACTION_MOVE:
                if (!dragging && dist(sX, sY, e.getX(), e.getY()) > 40) {
                    dragging = true;
                }
                if (dragging) {
                    iLeft = (int) (e.getX() - iW / 2);
                    iTop = (int) (e.getY() - iH / 2);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                // decide
                if (!dragging) {
                    toggle.run();
                }
                dragging = false;
                snapToEdge();
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    if (!dragging) {
                        targetIconAlpha = 111;
                        invalidate();
                    }
                }).start();
                END_MAIN_ACTION.run();
        }
        return true;
    }

    boolean snapToEdge;
    void snapToEdge() {
        float dist = iLeft + iW / 2f - (mW + 80) / 2f;
        if (Math.abs(dist) > (mW + 80) / 2f * 0.7) {
            if (iLeft + iW / 2 < 10 || Math.abs(iLeft + iW / 2) > mW + 80 - 10) { // 10 is extra out from side
                // snapped into position, stop
                snapToEdge = false;
            } else {
                snapToEdge = true;
                // snapping
                if (dist > 0) {
                    iLeft += Math.abs((mW + 80) / 2f - dist) * 0.2 + 1;
                } else {
                    iLeft -= ((mW + 80) / 2 + dist) * 0.2 + 1;
                }
                invalidate();
            }
        } else {
            snapToEdge = false;
        }
    }

    @Override
    public void drawMain(Canvas canvas) {

    }

    @Override
    public boolean handleMainEvent(MotionEvent e) {
        END_MAIN_ACTION.run();
        return false;
    }

    Runnable toggle;
    public void setToggleWork(Runnable r) {
        toggle = r;
    }
}
