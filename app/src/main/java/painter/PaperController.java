package painter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;

import painter.settings.Colors;
import painter.settings.Setting;
import painter.settings.Strokes;


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
    void init() {
        paint = new Paint();
        vBarState = SettingTouchState.IDLE;
        verticalSettings = new ArrayList<>();
        verticalSettings.add(new Colors(6));
        verticalSettings.add(new Strokes());



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

        // make rooms todo
        int yTop = H / 4;
        verticalSettings.get(0).init(paper, BAR_W, (int) (H * 0.3), W - BAR_W, H, yTop, 0);
        yTop += H * 0.3 + 50;
        verticalSettings.get(1).init(paper, BAR_W, 80, W - BAR_W, H, yTop, 0);
    }


    int activeSettingIndex;
    SettingTouchState vBarState;
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                return touchDown(e);
            case MotionEvent.ACTION_MOVE:
                return touchMove(e);
            case MotionEvent.ACTION_UP:
                return touchUp(e);
            default:
                return false;
        }
    }

    boolean touchDown(MotionEvent e) {
        switch (vBarState) {
            case IDLE:
                for (int i = 0; i < verticalSettings.size(); i++) {
                    if (verticalSettings.get(i).inIcon(e.getX(), e.getY())) {
                        activeSettingIndex = i;
                        vBarState = SettingTouchState.QUICK_ACTION;
                        return verticalSettings.get(i).handleQuickEvent(e);
                    }
                }
                break;
            case QUICK_ACTION:
                return verticalSettings.get(activeSettingIndex).handleQuickEvent(e);
            case MAIN_ACTION:
                return verticalSettings.get(activeSettingIndex).handleMainEvent(e);
        }
        return false;
    }

    boolean touchMove(MotionEvent e) {
        switch (vBarState) {
            case IDLE:
                return false;
            case QUICK_ACTION:
                return verticalSettings.get(activeSettingIndex).handleQuickEvent(e);
            case MAIN_ACTION:
                return verticalSettings.get(activeSettingIndex).handleMainEvent(e);
        }
        return false;
    }

    boolean touchUp(MotionEvent e) {
        switch (vBarState) {
            case IDLE:
                return false;
            case QUICK_ACTION:
                return verticalSettings.get(activeSettingIndex).handleQuickEvent(e);
            case MAIN_ACTION:
                return verticalSettings.get(activeSettingIndex).handleMainEvent(e);
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (vBarState == SettingTouchState.MAIN_ACTION) {
            // draw bound
            paint.setColor(Color.rgb(50, 50, 50));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, 0, BAR_W, H, paint);
        }

        for (int i = 0; i < verticalSettings.size(); i++) {
            verticalSettings.get(i).drawIcon(canvas);
            if (activeSettingIndex == i && vBarState == SettingTouchState.MAIN_ACTION) {
                canvas.save();
                canvas.translate(BAR_W, 0);
                canvas.clipRect(0, 0, W - BAR_W, H);
                verticalSettings.get(i).drawMain(canvas);
                canvas.restore();
            }
        }
    }

    enum SettingTouchState {
        IDLE, QUICK_ACTION, MAIN_ACTION
    }
}