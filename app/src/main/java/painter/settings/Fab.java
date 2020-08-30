package painter.settings;

import android.graphics.Canvas;
import android.view.MotionEvent;


/**
 * a draggable FAB that toggles
 */
public class Fab extends Setting {
    @Override
    void privateInit() {

    }

    @Override
    public void drawIcon(Canvas canvas) {
        super.drawIcon(canvas);
        paint.setColor(getContrastColor(paper.getBackgroundColor()));
        canvas.drawCircle(iLeft + iW / 2f, iTop + iH / 2f, Math.min(iW, iH) / 2f, paint);
    }

    @Override
    public boolean inIcon(float xPos, float yPos) {
        return dist(xPos, yPos, iLeft + iW / 2f, iTop + iH / 2f) < Math.min(iW, iH) / 2f;
    }

    float sX, sY;
    boolean dragging;
    @Override
    public boolean handleQuickEvent(MotionEvent e) {
        super.handleQuickEvent(e);
        // click vs drag?
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                sX = e.getX();
                sY = e.getY();
            case MotionEvent.ACTION_MOVE:
                if (!dragging && dist(sX, sY, e.getX(), e.getY()) > 20) {
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
                END_MAIN_ACTION.run();
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

    Runnable toggle;
    public void setToggleWork(Runnable r) {
        toggle = r;
    }
}
