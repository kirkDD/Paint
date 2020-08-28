package painter.settings;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.ColorRes;
import android.telephony.AccessNetworkConstants;
import android.util.Log;
import android.view.MotionEvent;

import painter.Paper;

public class Colors extends Setting {
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
            mainColorBoxes[i].set(40, mH * 0.6f + i * 100, mW - 40, mH * 0.6f + i * 100 + 40);
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
            selectedColorXOff += 0.2 * diff + diff > 0 ? 1 : -1;
            invalidate();
        }

    }

    int nextColorBoxIndex;
    int currColorIndex;
    int prevColorIndex;

    @Override
    public boolean handleQuickEvent(MotionEvent e) {
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
    @Override
    public void drawMain(Canvas canvas) {
        paint.setColor(Color.argb(100, 0, 0, 0));
        canvas.drawRect(0, 0, mW, mH, paint);
        for (int i = 0; i < mW; i+=8) {
            for (int j = 0; j < mH / 2; j+=8) {
                paint.setColor(Color.rgb(i * 255 / mW, j * 255 / mH, Color.blue(colors[currColorIndex])));
                paint.setAlpha(Color.alpha(colors[currColorIndex]));
                canvas.drawRect(i, j, i + 8, j + 8, paint);
            }
        }
        // draw where the current point in
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(map(Color.red(colors[currColorIndex]), 0, 255, 40, mW - 40),
                map(Color.green(colors[currColorIndex]), 0, 255, 0, mH / 2f), 20, paint);
        // draw interactor
        for (int i = 0; i < 4; i++) {
            float buttonXPos;
            switch (i) {
                case 0:
                    paint.setColor(Color.RED);
                    buttonXPos = 40 + Color.red(colors[currColorIndex]) * (mW - 80) / 255f;
                    break;
                case 1:
                    paint.setColor(Color.GREEN);
                    buttonXPos = 40 + Color.green(colors[currColorIndex]) * (mW - 80) / 255f;
                    break;
                case 2:
                    paint.setColor(Color.BLUE);
                    buttonXPos = 40 + Color.blue(colors[currColorIndex]) * (mW - 80) / 255f;
                    break;
                default:
                    buttonXPos = 40 + Color.alpha(colors[currColorIndex]) * (mW - 80) / 255f;
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
    @Override
    public boolean handleMainEvent(MotionEvent e) {
        float eX = e.getX() - iW;
        float eY = e.getY();
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
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
                    int newColComponent = (int) Math.max(0, Math.min(255, (eX - 40) * 255 / (mW - 80)));
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
                        default: // from gradient
                            int boundX = (int) Math.max(0, Math.min(mW - iW, eX));
                            int boundY = (int) Math.max(0, Math.min(mH / 2f, eY));
                            boundX = (int) map(boundX, 0, mW - iW, 0, 255);
                            boundY = (int) map(boundY, 0, mH / 2f, 0, 255);
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
