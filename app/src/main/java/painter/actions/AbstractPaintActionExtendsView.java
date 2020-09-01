package painter.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Currency;
import java.util.function.UnaryOperator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import cse340.undo.R;

/**
 * parent of all actions
 * a view that should be added, removed from paper
 */
public abstract class AbstractPaintActionExtendsView extends View {
    static final String TAG = "-=-= Abstract Action";

    static final Xfermode HIGHLIGHT_PAINT_MODE = new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY);
    static final float HIGHLIGHT_STROKE_WIDTH = 4f;
    static final int HIGHLIGHT_ALPHA = 125;

    static Paint abstractActionPaint;

    // for all shapes
    Path containsPath;
    Path myPath;
    public AbstractPaintActionExtendsView(Context context) {
        super(context);
        if (abstractActionPaint == null) {
            abstractActionPaint = new Paint();
            abstractActionPaint.setStyle(Paint.Style.STROKE);
            abstractActionPaint.setStrokeWidth(4);
            abstractActionPaint.setColor(Color.GREEN);
            abstractActionPaint.setStrokeCap(Paint.Cap.ROUND);

//            abstractActionPaint.setXfermode(HIGHLIGHT_PAINT_MODE);
            abstractActionPaint.setTextAlign(Paint.Align.CENTER);
            abstractActionPaint.setTextSize(quickBoxWidth / 1.5f);
        }
        currentState = ActionState.NEW;
        containsPath = new Path();
        myPath = new Path();
    }

    static RectF quickEditBox;
    static final int quickBoxWidth = 70;
    // draw some quick clickable actions

    @Override
    protected void onDraw(Canvas canvas) {
        // default drawings
        // draw action box
        if (!shouldShowQuickAction ||
                currentState == ActionState.NEW ||
                currentState == ActionState.STARTED) {
            return;
        }
        if (quickEditBox == null) {
            quickEditBox = new RectF(getWidth() - quickBoxWidth ,
                    getHeight() / 2f - quickBoxWidth,
                    getWidth(),
                    getHeight() / 2f + quickBoxWidth);
            toggleFillBox = new RectF(getWidth() - quickBoxWidth ,
                    getHeight() / 2f - quickBoxWidth * 3,
                    getWidth(),
                    getHeight() / 2f - quickBoxWidth);

        }


        if (animateQuickButton) {
            canvas.save();
            canvas.clipRect(animateBounds);
            abstractActionPaint.setAlpha(animateQBAlpha);
            abstractActionPaint.setStyle(Paint.Style.FILL);
//            abstractActionPaint.setXfermode(null);
            canvas.drawCircle(animateX, animateY, animateQBRadius, abstractActionPaint);
            canvas.restore();
            if (animateQBRadius < quickBoxWidth * 2) {
                animateQBRadius += (quickBoxWidth * 2 - animateQBRadius) * 0.08 + 1;
                animateQBAlpha = (int) (255 - 255 * (animateQBRadius / (quickBoxWidth * 2)));
                invalidate();
            }
        }

//        abstractActionPaint.setXfermode(HIGHLIGHT_PAINT_MODE);
        abstractActionPaint.setStyle(Paint.Style.STROKE);
        abstractActionPaint.setAlpha(255);
        canvas.drawRect(quickEditBox, abstractActionPaint);
        if (currentState == ActionState.FINISHED) {
            canvas.drawText("\u2704",
                    quickEditBox.centerX(),
                    quickEditBox.centerY() + abstractActionPaint.descent(),
                    abstractActionPaint);
        } else {
            canvas.drawText("\u2713",
                    quickEditBox.centerX(),
                    quickEditBox.centerY() + abstractActionPaint.descent(),
                    abstractActionPaint);
        }

//        canvas.drawRect(toggleFillBox, abstractActionPaint);
//        canvas.drawText("\u176e", toggleFillBox.centerX(), toggleFillBox.centerY(), abstractActionPaint);

    }



    boolean abstractSkippingEvent;
    /**
     * main way to draw/interact with user
     *
     * @param e the touch event
     */
    public boolean handleTouch(MotionEvent e) {
        // handle click on quick box
        if (abstractSkippingEvent) {
            if (e.getActionMasked() == MotionEvent.ACTION_UP) {
                abstractSkippingEvent = false;
            }
            return true;
        }
        if (e.getActionMasked() == MotionEvent.ACTION_DOWN &&
            e.getPointerCount() == 1) {
            if (actionBoxTouched(e.getX(), e.getY())) {
                if (currentState == ActionState.FINISHED) {
                    currentState = ActionState.REVISING;
                    abstractSkippingEvent = true;
                    startButtonClickedAnimation(quickEditBox, e.getX(), e.getY());
                    return true;
                } else if (currentState == ActionState.REVISING) {
                    currentState = ActionState.FINISHED;
                    abstractSkippingEvent = true;
                    startButtonClickedAnimation(quickEditBox, e.getX(), e.getY());
                    return true;
                    // skip this event
                }
//            } else if (toggleFillBoxTouched(e.getX(), e.getY())) {
//                toggleFill();
//                abstractSkippingEvent = true;
//                startButtonClickedAnimation(toggleFillBox, e.getX(), e.getY());
//                return true;
            }
        }
        return false;
    }

    /**
     * set the styles, ie color, thickness, ...
     * @param p Paint that has those info
     */
    public void setStyle(Paint p) {
        invalidate();
        abstractActionPaint.setColor(p.getColor());
    }

    // currState -> FINISHED
    // shouldShowQuickAction -> false
    UnaryOperator<AbstractPaintActionExtendsView> callWhenDone;
    UnaryOperator<AbstractPaintActionExtendsView> callWhenDoneSuper;

    public void setOnCompletion(UnaryOperator<AbstractPaintActionExtendsView> calledWhenDone) {
        callWhenDoneSuper = calledWhenDone;
        callWhenDone = abstractPaintActionExtendsView -> {
            callWhenDoneSuper.apply(this);
            currentState = ActionState.FINISHED;
            shouldShowQuickAction = false;
            invalidate();
            return null;
        };

    }

    /**
     * switch to edit, then call callWhenDone
     */
    ActionState currentState;
    boolean shouldShowQuickAction = true;
    public void editButtonClicked() {
        switch (currentState) {
            case NEW:
                return;
            case STARTED:
            case FINISHED:
                currentState = ActionState.REVISING;
                shouldShowQuickAction = true;
                break;
            case REVISING:
                callWhenDone.apply(this);
        }
        invalidate();
    }

    /**
     * change state to FINISHED
     * @return true if there is some thing in this action
     *      false if this is empty
     */
    public boolean focusLost() {
        shouldShowQuickAction = false;
        if (currentState == ActionState.NEW) {
            return false;
        } else {
            currentState = ActionState.FINISHED;
            invalidate();
            return true;
        }
    }

    /**
     * get state
     * @return the current state
     */
    public ActionState getCurrentState() {
        return currentState;
    }

    /**
     * test if a point touches actual content of action
     * @param x,y location
     * @param radius the bound to allow
     */
    public boolean contains(float x, float y, float radius) {
        containsPath.rewind();
        containsPath.moveTo(x, y);
        containsPath.addCircle(x, y, radius, Path.Direction.CW);
        return touchesPath(containsPath);
    }

    // touches by path: group select
    public boolean touchesPath(Path origin) {
        return containsPath.op(myPath, origin, Path.Op.INTERSECT) && !containsPath.isEmpty();
    }

    // combining actions
    public void addToPath(Path dst) {
        dst.addPath(myPath);
    }

    public void setStrokePath(Path src) {
        myPath.reset();
        myPath.addPath(src);
        currentState = ActionState.REVISING; // for Paper.setStyle to work
        invalidate();
    }

    /**
     * useful helpers
     */

    double dist(float a, float b, float x, float y) {
        return Math.sqrt(Math.pow(a - x, 2) + Math.pow(b - y, 2));
    }

    // in degrees
    double angleBetween(float a, float b, float x, float y) {
        return Math.atan2(a - x, b - y) * 180 / Math.PI;
    }

    // in degrees
    float snapAngle(float angle) {
        for (int i = -180; i <= 180; i += 45) {
            if (Math.abs(angle - i) < 3) {
                return i;
            }
        }
        return angle;
    }


    public enum ActionState {
        NEW, STARTED, FINISHED, REVISING;

    }

    // quick buttons
    boolean animateQuickButton;
    RectF animateBounds;
    float animateQBRadius, animateX, animateY;
    int animateQBAlpha = 255;
    void startButtonClickedAnimation(RectF bounds, float x, float y) {
        animateQBRadius = 0;
        animateQBAlpha = 255;
        animateQuickButton = true;
        animateX = x;
        animateY = y;
        animateBounds = bounds;
        invalidate();
    }


    boolean actionBoxTouched(float x, float y) {
        return shouldShowQuickAction && currentState != ActionState.NEW &&
                currentState != ActionState.STARTED && quickEditBox != null &&
                quickEditBox.contains(x, y);
    }

    static RectF toggleFillBox;

    boolean toggleFillBoxTouched(float x, float y) {
        return toggleFillBox != null && toggleFillBox.contains(x, y);
    }


    // internal sub classing
    void toggleFill() {

    }

    /**
     * for copy paste, if failed return null
     */
    public AbstractPaintActionExtendsView duplicate() {
        AbstractPaintActionExtendsView re = duplicateImp();
        if (re != null) {
            re.setOnCompletion(callWhenDoneSuper);
        }
        return re;
    }

    static final int DUPLICATE_OFFSET = 100;
    AbstractPaintActionExtendsView duplicateImp() {
        return null;
    }

}
