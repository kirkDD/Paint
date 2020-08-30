package painter.settings;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import painter.Paper;

/**
 * knobs and dials for paper and paint objects
 */
public abstract class AbstractSetting { // ????????????????

    static final int BACKGROUND_COLOR = Color.argb(150, 0, 0, 0);

    /**
     * set the paper that this setting works on
     * @param paper the paper to be setted
     */
    Paper paper;
    Paint paint;
    int iW, iH, mW, mH, iTop, iLeft;
    public void init(Paper paper, int iconW, int iconH, int mainW, int mainH, int iconTop, int iconLeft) {
        paint = new Paint();
        this.paper = paper;
        iW = iconW;
        iH = iconH;
        mW = mainW;
        mH = mainH;
        iTop = iconTop;
        iLeft = iconLeft;
        privateInit();
    }

    /**
     * where init for certain classes happens
     */
    abstract void privateInit();

    /**
     * draw the setting icon
     */
    public void drawIcon(Canvas canvas) {
        // BACKGROUND & ANIMATION ONLY
//        paint.setColor(Color.argb(100, 0, 0, 0));
//        canvas.drawRoundRect(iLeft, iTop, iLeft + iW, iTop + iH, 5, 5, paint);
        if (SU_clickSize < 100) {
            // animate touch
            paint.setColor(getContrastColor(paper.getBackgroundColor()));
            paint.setAlpha((int) map(SU_clickSize,0,100,180, 20));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(SU_posX, SU_posY, SU_clickSize, paint);
            SU_clickSize += 0.1 * (100 - SU_clickSize) + 1;
            invalidate();
        }
    }

    float SU_posX, SU_posY;
    float SU_clickSize = 999;
    /**
     * quick event on the Icon
     */
    public boolean handleQuickEvent(MotionEvent e) {
        // ANIMATION ONLY
        if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
            SU_posX = e.getX();
            SU_posY = e.getY();
            SU_clickSize = 0;
            invalidate();
        }
        return false;
    }

    /**
     * draw the whole UI
     */
    public abstract void drawMain(Canvas canvas);

    /**
     * interact with tool
     */
    public abstract boolean handleMainEvent(MotionEvent e);

    // taken care of paperController
    View PARENT_VIEW;
    public void setView(View v) {
        PARENT_VIEW = v;
    }
    public void invalidate() {
        PARENT_VIEW.invalidate();
    }
    Runnable START_MAIN_ACTION;
    public void setStartMainAction(Runnable r) {
        START_MAIN_ACTION = r;
    }
    Runnable END_MAIN_ACTION;
    public void setEndMainAction(Runnable r) {
        END_MAIN_ACTION = r;
    }

    // helpers

    public boolean inIcon(float xPos, float yPos) {
        return xPos >= iLeft && xPos <= iLeft + iW &&
                yPos >= iTop && yPos <= iTop + iH;
    }

    float dist(float x, float y, float a, float b) {
        return (float) Math.sqrt(Math.pow(x - a, 2) + Math.pow(y - b, 2));
    }

    float map(float val, float vLow, float vHigh, float tLow, float tHigh) {
        // shift to 0, scale and shift
        val -= vLow;
        val = val * (tHigh - tLow) / (vHigh - vLow);
        val += tLow;
        return val;
    }

     int getContrastColor(int color) {
        int y = (299 * Color.red(color) + 587 * Color.green(color) + 114 * Color.blue(color)) / 1000;
        return y >= 128 ? Color.BLACK : Color.WHITE;
    }

}
