package painter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;

import painter.settings.Colors;
import painter.settings.Setting;


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
        verticalSettings = new ArrayList<>();
        verticalSettings.add(new Colors(6));
    }

    Paper paper;
    void setPaper(Paper p) {
        paper = p;
    }

    int W, H;
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        W = right - left;
        H = bottom - top;
        // make rooms todo
    }


    int activeSettingIndex;
    float startX, startY;
    SettingTouchState vBarState;
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                return
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
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
    }

    boolean touchUp(MotionEvent e) {
        switch (vBarState) {
            case IDLE:
                return false;
            case QUICK_ACTION:

                break;
            case MAIN_ACTION:
                break;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < verticalSettings.size(); i++) {
            verticalSettings.get(i).drawIcon(canvas);
        }
    }

    enum SettingTouchState {
        IDLE, QUICK_ACTION, MAIN_ACTION
    }
}
