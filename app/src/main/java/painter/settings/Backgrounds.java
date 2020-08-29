package painter.settings;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.MotionEvent;

public class Backgrounds extends Setting {

    int COLOR;

    void changeBackgroundColor() {
        paper.setBackgroundColor(COLOR);
    }

    @Override
    void privateInit() {
        paint.setTextSize(iW / 1.7f);
        paint.setTextAlign(Paint.Align.CENTER);
        COLOR = paper.getBackgroundColor();
    }

    @Override
    public void drawIcon(Canvas canvas) {
        paint.setColor(Color.BLACK);
        canvas.drawRoundRect(iLeft, iTop, iLeft + iW, iTop + iH, 50, 50, paint);
//        canvas.drawCircle(iLeft + iW / 2f, iTop + iH / 2f, Math.min(iW, iH) / 2f, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("\u274f", iLeft + iW / 2f,
                iTop + iH / 2f + paint.descent() + 5, paint);
    }

    @Override
    public boolean handleQuickEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (inIcon(e.getX(), e.getY())) {
                    START_MAIN_ACTION.run();
                } else {
                    END_MAIN_ACTION.run();
                }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void drawMain(Canvas canvas) {
        // rg combination
        // b combination
        for (int i = 0; i < mW; i+=10) {
            for (int j = 0; j < mH / 2; j+=10) {
                paint.setColor(Color.rgb(
                        (int) map(i, 0, mW, 0, 255),
                        (int) map(j, 0, mH / 2f, 0, 255),
                        Color.blue(COLOR)));
                canvas.drawRect(i, j, i + 10, j + 10, paint);
            }
        }
        for (int i = 0; i < mW; i+=10) {
            for (int j = 0; j < mH / 2; j+=10) {
                paint.setColor(Color.rgb(
                        Color.red(COLOR),
                        (int) map(i, 0, mW, 0, 255),
                        (int) map(j, 0, mH / 2f, 0, 255)
                        ));
                canvas.drawRect(i, mH / 2f + j, i + 10, mH /2f + j + 10, paint);
            }
        }
        // draw a ring at current color
        paint.setColor(COLOR);
        // top:
        float s = 200;
        float tX = map(Color.red(COLOR), 0, 255, 0, mW);
        float tY = map(Color.green(COLOR), 0, 255, 0, mH / 2f);
        canvas.save();
        canvas.clipRect(tX - s, tY - s, tX + s, tY + s);
        s = 150;
        canvas.clipOutRect(tX - s, tY - s, tX + s, tY + s);
        s = 200;
        canvas.drawRect(tX - s, tY - s, tX + s, tY + s, paint);
        canvas.restore();
        // bottom
        tX = map(Color.green(COLOR), 0, 255, 0, mW);
        tY = mH / 2f + map(Color.blue(COLOR), 0, 255, 0, mH / 2f);
        canvas.save();
        canvas.clipRect(tX - s, tY - s, tX + s, tY + s);
        s = 150;
        canvas.clipOutRect(tX - s, tY - s, tX + s, tY + s);
        s = 200;
        canvas.drawRect(tX - s, tY - s, tX + s, tY + s, paint);
        canvas.restore();
    }

    boolean skip = false;
    @Override
    public boolean handleMainEvent(MotionEvent e) {
        float x = Math.max(0, Math.min(mW, e.getX() - iW));
        float y = e.getY();
        if (e.getActionMasked() == MotionEvent.ACTION_DOWN && x < iW) {
            skip = true;
        }
        if (skip) {
            if (x < iW) {
                if (e.getActionMasked() == MotionEvent.ACTION_UP ) {
                    END_MAIN_ACTION.run();
                    skip = false;
                }
                return true;
            } else {
                skip = false;
            }
        }
        if (y < mH / 2f) {
            // top
            COLOR = Color.rgb(
                    (int) map(x, 0, mW, 0, 255),
                    (int) map(y, 0, mH / 2f, 0, 255),
                    Color.blue(COLOR)
                    );
        } else {
            COLOR = Color.rgb(
                    Color.red(COLOR),
                    (int) map(x, 0, mW, 0, 255),
                    (int) map(y - mH / 2f, 0, mH / 2f, 0, 255)
            );
        }
        invalidate();
        changeBackgroundColor();
        return true;
    }
}
