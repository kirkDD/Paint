package painter.settings;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

public class PaperStates extends AbstractSetting {

    RectF[] states;
    static final String[] icons = new String[]{"✎", "⏍", "ි", "⬚"};
    public PaperStates() {
        states = new RectF[icons.length];
        for (int i = 0; i < icons.length; i++) {
            states[i] = new RectF();
        }
    }

    @Override
    void privateInit() {
        paint.setTextSize(iW);
        paint.setTextAlign(Paint.Align.CENTER);
        // position states
        float maxX = mW / states.length;
        float boxW = maxX * 0.6f;
        float startX = (maxX - boxW) / 2;
        for (RectF state : states) {
            state.set(startX, iTop, startX + boxW, iTop + iH);
            startX += maxX;
        }
    }

    // copy ⧉
    // select ⬚
    // clear ⮽
    // moving ✋
    @Override
    public void drawIcon(Canvas canvas) {
        super.drawIcon(canvas);
        paint.setColor(getContrastColor(paper.getBackgroundColor()));
        if (paper.isErasing()) {
            canvas.drawText(icons[2], iLeft + iW / 2f,
                    iTop + iH / 2f + paint.getTextSize() / 2 - paint.descent() / 2,
                    paint);
        } else if (paper.isPanning()) {
            canvas.drawText(icons[1], iLeft + iW / 2f,
                    iTop + iH / 2f + paint.getTextSize() / 2 - paint.descent() / 2,
                    paint);
        } else {
            canvas.drawText(icons[0], iLeft + iW / 2f,
                    iTop + iH / 2f + paint.getTextSize() / 2 - paint.descent() / 2,
                    paint);
        }
    }

    @Override
    public boolean handleQuickEvent(MotionEvent e) {
        super.handleQuickEvent(e);
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

    @Override
    public void drawMain(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < states.length; i++) {
            RectF box = states[i];
            paint.setColor(getContrastColor(paper.getBackgroundColor()));
            paint.setAlpha(200);
            canvas.drawRoundRect(box, 10, 10, paint);
            paint.setColor(paper.getBackgroundColor());
            canvas.drawText(icons[i], box.centerX(), box.centerY() + paint.getTextSize() / 2 - paint.descent() / 2, paint);
        }
    }

    @Override
    public boolean handleMainEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                for (int i = 0; i < states.length; i++) {
                    if (states[i].contains(e.getX() - iW, e.getY())) {
                        performAction(i);
                        break;
                    }
                }
                END_MAIN_ACTION.run();
        }
        return true;
    }

    void performAction(int iconIndex) {
        if (iconIndex == 2) {
            paper.toggleEraseMode();
        } else if (iconIndex == 1) {
            paper.togglePanningMode();
        } else if (iconIndex == 0) {
            if (paper.isPanning())
                paper.togglePanningMode();
            if (paper.isErasing())
                paper.toggleEraseMode();
        }
    }
}
