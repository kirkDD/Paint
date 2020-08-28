package painter.settings;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

public class Strokes extends Setting {

    float STROKE_WIDTH = 10;
    RectF iconBox;
    public Strokes() {
        iconBox = new RectF();
    }

    void changeStroke() {
        paper.getPaintToEdit().setStrokeWidth(STROKE_WIDTH);
        paper.applyPaintEdit();
    }

    @Override
    void privateInit() {
        paint.setStrokeWidth(10);
        iconBox.set(iLeft, iTop, iLeft + iW, iTop + iH);
    }

    @Override
    public void drawIcon(Canvas canvas) {
        // draw at width
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.rgb(60,60,60));
        canvas.drawCircle(iconBox.centerX(), iconBox.centerY(), iW / 3f, paint);

        paint.setColor(Color.rgb(180,180,180));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(iconBox.centerX(), iconBox.centerY(), iW / 5f, paint);
    }

    float cX, cY;
    @Override
    public boolean handleQuickEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                cX = e.getX();
                cY = e.getY();
            case MotionEvent.ACTION_MOVE:
                float delta = (e.getY() - cY) * 0.1f;
                STROKE_WIDTH = Math.max(5, STROKE_WIDTH - delta);
                changeStroke();
                cX = e.getX();
                cY = e.getY();
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                if (iconBox.contains(e.getX(), e.getY())) {
                    START_MAIN_ACTION.run();
                } else {
                    END_MAIN_ACTION.run();
                }
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
