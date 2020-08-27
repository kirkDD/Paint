package painter.settings;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import painter.Paper;

public class Colors extends Setting {
    static final String TAG = "[][] Color Settings";

    static final int BOX_GAP = 20;

    int[] colors;
    RectF[] colorBoxes;
    int currColorIndex;

    String[] DEFAULT_COLORS = new String[]{
        "#c8af00fa", "#c8f23838", "#c8ffa600", "#c8fffb00", "#c804d400", "#c80080db", "#c8f3f3f3", "#c803fce3"
    };

    public Colors(int numColors) {
        colors = new int[numColors];
        colorBoxes = new RectF[numColors];
        for (int i = 0; i < colorBoxes.length; i++) {
            colorBoxes[i] = new RectF();
            if (i < DEFAULT_COLORS.length) {
                colors[i] = Color.parseColor(DEFAULT_COLORS[i]);
            }
        }
    }

    @Override
    void privateInit() {
        float boxW = iW - BOX_GAP * (colorBoxes.length - 1);
        boxW /= colorBoxes.length;
        if (boxW <= 10) {
            Log.e(TAG, "privateInit: box width too small");
        }
        for (int i = 0; i < colorBoxes.length; i++) {
            colorBoxes[i].set(
                    iLeft,
                    iTop + i * (boxW + BOX_GAP),
                    iLeft + iW / 1.5f,
                    iTop + i * (boxW + BOX_GAP) + boxW);
        }
        paint.setStyle(Paint.Style.FILL);
        paper.getPaintToEdit().setColor(colors[currColorIndex]);
        paper.applyPaintEdit();
    }


    @Override
    public void drawIcon(Canvas canvas) {
        // draw the color boxes
        for (int i = 0; i < colorBoxes.length; i++) {
            paint.setColor(colors[i]);
            canvas.drawRoundRect(colorBoxes[i], BOX_GAP, BOX_GAP, paint);
        }

    }

    @Override
    public boolean handleQuickEvent(MotionEvent e) {
        return false;
    }

    @Override
    public void drawMain(Canvas canvas) {

    }

    @Override
    public boolean handleMainEvent(MotionEvent e) {
        return false;
    }

}
