package painter.settings;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import painter.Paper;

/**
 * knobs and dials for paper and paint objects
 */
public abstract class Setting { // ????????????????

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
     * where init for certain class happens
     */
    abstract void privateInit();

    public boolean inIcon(float xPos, float yPos) {
        return xPos >= iLeft && xPos <= iLeft + iW &&
                yPos >= iTop && yPos <= iTop + iH;
    }


    /**
     * draw the setting icon
     */
    public abstract void drawIcon(Canvas canvas);

    /**
     * quick event on the Icon
     */
    public abstract boolean handleQuickEvent(MotionEvent e);

    Runnable START_MAIN_ACTION;
    public void setStartMainAction(Runnable r) {
        START_MAIN_ACTION = r;
    }
    Runnable END_MAIN_ACTION;
    public void setEndMainAction(Runnable r) {
        END_MAIN_ACTION = r;
    }

    /**
     * draw the whole UI
     */
    public abstract void drawMain(Canvas canvas);

    /**
     * interact with tool
     */
    public abstract boolean handleMainEvent(MotionEvent e);

    View PARENT_VIEW;
    public void setView(View v) {
        PARENT_VIEW = v;
    }
    public void invalidate() {
        PARENT_VIEW.invalidate();
    }


    // helpers

    float dist(float x, float y, float a, float b) {
        return (float) Math.sqrt(Math.pow(x - a, 2) + Math.pow(y - b, 2));
    }

    float map(float val, float vLow, float vHigh, float tLow, float tHigh) {
        // scale and shift
        val *= (tHigh - tLow) / (vHigh - vLow);
        val += (tLow - vLow);
        return val;
    }
}
