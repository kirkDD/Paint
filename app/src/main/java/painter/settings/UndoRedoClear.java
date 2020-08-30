package painter.settings;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

public class UndoRedoClear extends Setting {
    @Override
    void privateInit() {
        paint.setStrokeWidth(4);
        paint.setTextSize(iW);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.BLACK);
    }

    @Override
    public void drawIcon(Canvas canvas) {
        super.drawIcon(canvas);
        canvas.save();
        canvas.translate(iLeft + iW / 2f, iTop + iH / 2f);
        canvas.rotate(90);
        paint.setColor(getContrastColor(paper.getBackgroundColor()));
        paint.setTextSize(iW);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawText("\u27f2", -iH / 7f, iW / 3.5f, paint);
        canvas.drawText("\u27f3", iH / 7f, iW / 3.5f, paint);
        canvas.restore();
        if (state != 0) {
            paint.setTextSize(450);
            paint.setAlpha(100);
            canvas.drawText("" + state, mW / 2f, mH / 2f, paint);
        }
        if (clear) {
            paint.setTextSize(300);
            paint.setAlpha(100);
            canvas.drawText("CLEAR", mW / 2f, mH / 2f, paint);
        }
    }

    float startY, startX;
    int state = 0;
    boolean clear = false;
    boolean clicking;
    @Override
    public boolean handleQuickEvent(MotionEvent e) {
        super.handleQuickEvent(e);
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startY = e.getY();
                startX = e.getX();
                clicking = true;
            case MotionEvent.ACTION_MOVE:
                int targetState = (int) Math.pow(Math.abs(e.getY() - startY) / 200, 1.4);
                if (e.getY() - startY < 0) targetState = - targetState;
                if (targetState == 0) {
                    // see if quick redo (nope!) or clear
                    clear = e.getX() - iW * 2 > mW * 0.5;
//                    if (!clear && e.getX() - iW - iW > mW / 4f) {
//                        targetState = 1;
//                    }
                } else {
                    clear = false;
                }
                while (state != targetState) {
                    if (targetState > state) {
                        if (paper.redo()) {
                            state++;
                        } else {
                            targetState--;
                        }
                    } else {
                        if (paper.undo()) {
                            state--;
                        } else {
                            targetState++;
                        }
                    }
                }
                // check for a click
                if (dist(startX, startY, e.getX(), e.getY()) > 20) {
                    clicking = false;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (clear) {
                    clear = false;
                    paper.clear();
                } else if (clicking) {
                    // quick undo
                    paper.undo();
                    clicking = false;
                }
                END_MAIN_ACTION.run();
                state = 0;
                invalidate();
        }
        return true;
    }

    @Override
    public void drawMain(Canvas canvas) {

    }

    @Override
    public boolean handleMainEvent(MotionEvent e) {
        END_MAIN_ACTION.run();
        return false;
    }
}
