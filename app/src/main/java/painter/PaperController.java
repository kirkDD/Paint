package painter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;

import painter.settings.Backgrounds;
import painter.settings.Colors;
import painter.settings.Fab;
import painter.settings.Setting;
import painter.settings.Shapes;
import painter.settings.Strokes;
import painter.settings.UndoRedoClear;


/**
 * holds a collection of widgets that manipulates the Paper
 */
public class PaperController extends View {

    ArrayList<Setting> verticalSettings;

    public PaperController(Context context) {
        super(context);
        init();
    }

    public PaperController(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    Paint paint;
    Colors colors;
    Strokes strokes;
    Shapes shapes;
    Backgrounds backgrounds;
    UndoRedoClear undoRedoClear;
    Fab toggleUI;
    void init() {
        paint = new Paint();
        vBarState = SettingTouchState.IDLE;
        verticalSettings = new ArrayList<>();

        colors = new Colors(6);  // 0
        strokes = new Strokes();  // 1 strokes
        shapes = new Shapes();  // 2 shapes
        backgrounds = new Backgrounds();
        undoRedoClear = new UndoRedoClear();
        toggleUI = new Fab();

        verticalSettings.add(colors);
        verticalSettings.add(strokes);
        verticalSettings.add(shapes);
        verticalSettings.add(backgrounds);
        verticalSettings.add(undoRedoClear);
        verticalSettings.add(toggleUI);

        toggleUI.setToggleWork(this::toggleUI);

        Runnable startMain = () -> {
            if (activeSettingIndex >= 0 && activeSettingIndex < verticalSettings.size()) {
                vBarState = SettingTouchState.MAIN_ACTION;
                invalidate();
            }
        };

        Runnable stopMain = () -> {
            vBarState = SettingTouchState.IDLE;
            invalidate();
        };
        for (Setting s : verticalSettings) {
            s.setView(this);
            s.setStartMainAction(startMain);
            s.setEndMainAction(stopMain);
        }
    }

    Paper paper;
    void setPaper(Paper p) {
        paper = p;
    }

    static final int BAR_W = 80;
    int W, H;
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        W = right - left;
        H = bottom - top;

        int yTop = H / 4;
        backgrounds.init(paper, BAR_W, 120, W - BAR_W, H, yTop - 190, 0);
        colors.init(paper, BAR_W, (int) (H * 0.3), W - BAR_W, H, yTop, 0);
        yTop += H * 0.3 + 80;
        strokes.init(paper, BAR_W, 80, W - BAR_W, H, yTop, 0);
        yTop += 80 + 80;
        shapes.init(paper, BAR_W, 80, W - BAR_W, H, yTop, 0);
        yTop += 80 + 80;
        undoRedoClear.init(paper, BAR_W, 120, W - BAR_W, H, yTop, 0);

        toggleUI.init(paper, W / 8, W / 8, W - BAR_W, H, 80, 80);

    }


    int activeSettingIndex;
    SettingTouchState vBarState;
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (hideUI) {
            if (toggleUI.inIcon(e.getX(), e.getY())) {
                vBarState = SettingTouchState.QUICK_ACTION;
            }
            if (vBarState == SettingTouchState.QUICK_ACTION)
                return toggleUI.handleQuickEvent(e);
            if (vBarState == SettingTouchState.MAIN_ACTION)
                return toggleUI.handleMainEvent(e);
            return false;
        }
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if(vBarState == SettingTouchState.IDLE) {
                    for (int i = 0; i < verticalSettings.size(); i++) {
                        if (verticalSettings.get(i).inIcon(e.getX(), e.getY())) {
                            activeSettingIndex = i;
                            vBarState = SettingTouchState.QUICK_ACTION;
                            return verticalSettings.get(i).handleQuickEvent(e);
                        }
                    }
                    break;
                }
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                switch (vBarState) {
                    case QUICK_ACTION:
                        return verticalSettings.get(activeSettingIndex).handleQuickEvent(e);
                    case MAIN_ACTION:
                        return verticalSettings.get(activeSettingIndex).handleMainEvent(e);
                }
        }
        return false;
    }


    float vBarXOff;
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(vBarXOff, 0);
        if (vBarState == SettingTouchState.MAIN_ACTION) {
            // draw bound
            paint.setColor(Color.argb(50,50, 50, 50));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, 0, BAR_W, H, paint);
        }

        for (int i = 0; i < verticalSettings.size(); i++) {
            if (verticalSettings.get(i) == toggleUI) {
                // draw toggle ui here since it position should not change
                canvas.save();
                canvas.translate(-vBarXOff, 0);
            }
            verticalSettings.get(i).drawIcon(canvas);
            if (activeSettingIndex == i && vBarState == SettingTouchState.MAIN_ACTION) {
                canvas.save();
                canvas.translate(BAR_W, 0);
                canvas.clipRect(0, 0, W - BAR_W, H);
                verticalSettings.get(i).drawMain(canvas);
                canvas.restore();
            }
            if (verticalSettings.get(i) == toggleUI) {
                // draw toggle ui here since it position should not change
                canvas.restore();
            }
        }

        canvas.restore();

        if (hideUI) {
            if (vBarXOff > -BAR_W) {
                vBarXOff += (-BAR_W - vBarXOff) * 0.2 - 1;
            }
            invalidate();
        } else if (vBarXOff < 0) {
            vBarXOff += - vBarXOff * 0.2 + 1;
            if (vBarXOff > 0) {
                vBarXOff = 0;
            }
            invalidate();
        }
        // animate
    }

    boolean hideUI = false;
    void toggleUI() {
        hideUI = !hideUI;
        invalidate();
    }


    enum SettingTouchState {
        IDLE, QUICK_ACTION, MAIN_ACTION
    }
}
