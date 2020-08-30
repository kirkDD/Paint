package painter.settings;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

public class Colors extends AbstractSetting {
    static final String TAG = "[][] Color Settings";

    static final int BOX_GAP = 20;

    int[] colors;
    RectF[] colorBoxes;

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
        mainColorBoxes = new RectF[4];
        for (int i = 0; i < mainColorBoxes.length; i++) {
            mainColorBoxes[i] = new RectF();
        }
    }

    @Override
    void privateInit() {
        paint.setStrokeWidth(10);
        X_OFFSET_AMOUNT = iW * 0.4f;
        float boxH = iH - BOX_GAP * (colorBoxes.length - 1);
        boxH /= colorBoxes.length;
        if (boxH <= 10) {
            Log.e(TAG, "privateInit: box width too small");
        }
        for (int i = 0; i < colorBoxes.length; i++) {
            colorBoxes[i].set(
                    iLeft,
                    iTop + i * (boxH + BOX_GAP),
                    iLeft + iW / 1.9f,
                    iTop + i * (boxH + BOX_GAP) + boxH);
        }
        for (int i = 0; i < mainColorBoxes.length; i++) {
            mainColorBoxes[i].set(MAIN_MARGIN, mH * 0.6f + i * 100, mW - MAIN_MARGIN, mH * 0.6f + i * 100 + 40);
        }
        changeColor();
    }

    void changeColor() {
        paint.setStyle(Paint.Style.FILL);
        paper.getPaintToEdit().setColor(colors[currColorIndex]);
        paper.applyPaintEdit();
    }

    float selectedColorXOff;
    float X_OFFSET_AMOUNT;
    @Override
    public void drawIcon(Canvas canvas) {
        // draw the color boxes
        super.drawIcon(canvas);

        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < colorBoxes.length; i++) {
            paint.setColor(colors[i]);
            canvas.save();
            if (i == currColorIndex) {
                canvas.translate(selectedColorXOff, 0);
            } else if (i == prevColorIndex && selectedColorXOff < X_OFFSET_AMOUNT) {
                canvas.translate(X_OFFSET_AMOUNT - selectedColorXOff, 0);
            } else if (i == nextColorBoxIndex) {
                canvas.translate(selectedColorXOff / 2, 0);
            }
            canvas.drawRoundRect(colorBoxes[i], iW, iW, paint);
            canvas.restore();
        }
        if (selectedColorXOff < X_OFFSET_AMOUNT) {
            float diff = X_OFFSET_AMOUNT - selectedColorXOff;
            selectedColorXOff += 0.4 * diff + (diff > 0 ? 1 : -1);
            invalidate();
        }

    }

    int nextColorBoxIndex;
    int currColorIndex;
    int prevColorIndex;

    @Override
    public boolean handleQuickEvent(MotionEvent e) {
        super.handleQuickEvent(e);
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < colorBoxes.length; i++) {
                    if (colorBoxes[i].contains(colorBoxes[i].centerX(), e.getY())) {
                        nextColorBoxIndex = i;
                        invalidate();
                        return true;
                    }
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (nextColorBoxIndex != currColorIndex) {
                    prevColorIndex = currColorIndex;
                    currColorIndex = nextColorBoxIndex;
                    selectedColorXOff = 0;
                    changeColor();
                    END_MAIN_ACTION.run();
                } else {
                    // edit color
                    START_MAIN_ACTION.run();
                }
                invalidate();
                return true;
            default:
                return false;
        }
    }

    RectF[] mainColorBoxes;
    static final int MAIN_MARGIN = 80;
    @Override
    public void drawMain(Canvas canvas) {
        paint.setColor(BACKGROUND_COLOR);
        canvas.drawRect(0, 0, mW, mH, paint);
        paint.setColor(colors[currColorIndex]);
        canvas.drawRect(0, 0, mW, mH / 2f, paint);
        for (int i = MAIN_MARGIN; i < mW - MAIN_MARGIN; i+=16) {
            for (int j = MAIN_MARGIN; j < mH / 2 - MAIN_MARGIN; j+=16) {
                paint.setColor(Color.argb(
                        Color.alpha(colors[currColorIndex]),
                        (int) map(i, MAIN_MARGIN, mW - MAIN_MARGIN, 0, 255),
                        (int) map(j, MAIN_MARGIN, mH / 2f - MAIN_MARGIN, 0, 255),
                        Color.blue(colors[currColorIndex])));
                canvas.drawRect(i, j, i + 16, j + 16, paint);
            }
        }
        // draw where the current point in
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(map(Color.red(colors[currColorIndex]), 0, 255, MAIN_MARGIN, mW - MAIN_MARGIN),
                map(Color.green(colors[currColorIndex]), 0, 255, MAIN_MARGIN, mH / 2f - MAIN_MARGIN), 20, paint);
        // draw interactor
        for (int i = 0; i < 4; i++) {
            float buttonXPos;
            switch (i) {
                case 0:
                    paint.setColor(Color.RED);
                    buttonXPos = MAIN_MARGIN + Color.red(colors[currColorIndex]) * (mW - MAIN_MARGIN * 2) / 255f;
                    break;
                case 1:
                    paint.setColor(Color.GREEN);
                    buttonXPos = MAIN_MARGIN + Color.green(colors[currColorIndex]) * (mW - MAIN_MARGIN * 2) / 255f;
                    break;
                case 2:
                    paint.setColor(Color.BLUE);
                    buttonXPos = MAIN_MARGIN + Color.blue(colors[currColorIndex]) * (mW - MAIN_MARGIN * 2) / 255f;
                    break;
                default:
                    buttonXPos = MAIN_MARGIN + Color.alpha(colors[currColorIndex]) * (mW - MAIN_MARGIN * 2) / 255f;
                    paint.setColor(Color.BLACK);
            }
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(mainColorBoxes[i], 10, 10, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(buttonXPos, mainColorBoxes[i].centerY(), 30, paint);
        }
    }

    int movingColorBoxIndex = -1;
    boolean skipping = false;

    @Override
    public boolean handleMainEvent(MotionEvent e) {
        if (skipping) {
            if (e.getPointerCount() == 1 && e.getActionMasked() == MotionEvent.ACTION_UP) {
                skipping = false;
                END_MAIN_ACTION.run();
            }
            return true;
        }
        float eX = e.getX() - iW;
        float eY = e.getY();
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (eX < 0) {
                    movingColorBoxIndex = -1;
                    skipping = true;
                    return true;
                }
            case MotionEvent.ACTION_MOVE:
                if (movingColorBoxIndex == -1) {
                    // which one?
                    for (int i = 0; i < mainColorBoxes.length; i++) {
                        if (mainColorBoxes[i].contains(eX + 20, eY + 20, eX - 20, eY - 20)) {
                            movingColorBoxIndex = i;
                            break;
                        }
                    }
                    // could be selecting from gradient
                    if (eY < mH / 2f) {
                        movingColorBoxIndex = 4;
                    }
                }

                if (movingColorBoxIndex != -1) {
                    // move it
                    int co = colors[currColorIndex];
                    int newColComponent = (int) Math.max(0, Math.min(255, (eX - MAIN_MARGIN) * 255 / (mW - MAIN_MARGIN * 2)));
                    switch (movingColorBoxIndex) {
                        case 0:
                            colors[currColorIndex] = Color.argb(Color.alpha(co), newColComponent, Color.green(co), Color.blue(co));
                            break;
                        case 1:
                            colors[currColorIndex] = Color.argb(Color.alpha(co), Color.red(co), newColComponent, Color.blue(co));
                            break;
                        case 2:
                            colors[currColorIndex] = Color.argb(Color.alpha(co), Color.red(co), Color.green(co), newColComponent);
                            break;
                        case 3:
                            colors[currColorIndex] = Color.argb(newColComponent, Color.red(co), Color.green(co), Color.blue(co));
                            break;
                        default: // from gradient
                            int boundX = (int) Math.max(MAIN_MARGIN, Math.min(mW - MAIN_MARGIN, eX));
                            int boundY = (int) Math.max(MAIN_MARGIN, Math.min(mH / 2f - MAIN_MARGIN, eY));
                            boundX = (int) map(boundX, MAIN_MARGIN, mW - MAIN_MARGIN, 0, 255);
                            boundY = (int) map(boundY, MAIN_MARGIN, mH / 2f - MAIN_MARGIN, 0, 255);
                            colors[currColorIndex] = Color.argb(Color.alpha(co), boundX, boundY, Color.blue(co));
                    }
                    invalidate();
                }
                // else quit
                break;
            case MotionEvent.ACTION_UP:
                changeColor();
                if (movingColorBoxIndex == -1) {
                    END_MAIN_ACTION.run();
                }
                movingColorBoxIndex = -1;
        }
        return true;
    }

}
