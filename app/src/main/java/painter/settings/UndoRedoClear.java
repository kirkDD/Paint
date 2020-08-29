package painter.settings;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

public class UndoRedoClear extends Setting {
    @Override
    void privateInit() {
        paint.setTextSize(iW);
        paint.setStrokeWidth(4);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    public void drawIcon(Canvas canvas) {
        canvas.save();
        canvas.translate(iLeft + iW / 2f, iTop + iH / 2f);
        canvas.rotate(90);
        paint.setAlpha(255);
        paint.setTextSize(iW);
        canvas.drawText("\u27f2\u27f3", 0, iW / 3f, paint);
        canvas.restore();
        if (state != 0) {
            paint.setTextSize(500);
            paint.setAlpha(200);
            canvas.drawText("" + state, mW / 2f, mH / 2f, paint);
        }
    }

    float startY;
    int state = 0;
    @Override
    public boolean handleQuickEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startY = e.getY();
            case MotionEvent.ACTION_MOVE:
                int targetState = (int) Math.pow(Math.abs(e.getY() - startY) / 200, 1.4);
                if (e.getY() - startY < 0) targetState = - targetState;
                while (state != targetState) {
                    if (targetState > state) {
                        state++;
                        paper.redo();
                    } else {
                        state--;
                        paper.undo();
                    }
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
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
        return false;
    }
}
