package painter.settings;

import android.graphics.Canvas;
import android.view.MotionEvent;


/**
 * a draggable FAB that toggles
 */
public class Fab extends AbstractSetting {
    @Override
    void privateInit() {

    }

    @Override
    public void drawIcon(Canvas canvas) {
        super.drawIcon(canvas);
        paint.setColor(getContrastColor(paper.getBackgroundColor()));
        canvas.drawCircle(iLeft + iW / 2f, iTop + iH / 2f, Math.min(iW, iH) / 2f, paint);
        if (snapToEdge) {
            snapToEdge();
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
                END_MAIN_ACTION.run();
        }
        return true;
    }

    boolean snapToEdge;
    void snapToEdge() {
        float dist = iLeft + iW / 2f - (mW + 80) / 2f;
        if (Math.abs(dist) > (mW + 80) / 2f * 0.7) {
            // snapping
            if (dist > 0) {
                iLeft += Math.abs((mW + 80) / 2f - dist) * 0.2 + 1;
            } else {
                iLeft -= ((mW + 80) / 2 + dist) * 0.2 + 1;
            }
            invalidate();
            if (iLeft + iW / 2 < 0 || Math.abs(iLeft + iW / 2) > mW + 80) {
                snapToEdge = false;
            } else {
                snapToEdge = true;
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
