package painter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.ClosedDirectoryStreamException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

import painter.actions.AbstractPaintActionExtendsView;
import painter.actions.ActionArrow;
import painter.actions.ActionLetters;
import painter.actions.ActionNumbers;
import painter.actions.ActionOval;
import painter.actions.ActionRectangle;
import painter.actions.ActionStraightLine;
import painter.actions.ActionStroke;

/**
 * the paper to hold all drawings (views)
 */
public class Paper extends FrameLayout {
    static final String TAG = "-=-= Paper";


    /////////////////////////
    // for super actions
    /////////////////////////
    // rep expose?


    /**
     * invariant if u can keep it:
     *      action is not null
     *      action is added to view, view group
     *      action is not "not" in history, changed Sunday
     */

    public ArrayList<AbstractPaintActionExtendsView> history;
    public Stack<AbstractPaintActionExtendsView> redoStack;
    // current action
    AbstractPaintActionExtendsView action;
    // current action's class
    Class<? extends AbstractPaintActionExtendsView> actionClass = ActionStroke.class;
    static Paint theOneAndOnlyPaint;

    int background_color = -1;

    public Paper(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "Paper: initializing");
        theOneAndOnlyPaint = new Paint();
        theOneAndOnlyPaint.setColor(Color.RED);
        theOneAndOnlyPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        theOneAndOnlyPaint.setStrokeWidth(10);
        theOneAndOnlyPaint.setTextSize(120);

        history = new ArrayList<>();
        redoStack = new Stack<>();

        initCurrentAction();
    }


    /**
     * initialize current action from actionClass
     */
    void initCurrentAction() {
        try {
            action = actionClass.getConstructor(Context.class).newInstance(getContext());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            Log.e(TAG, "initAction: cannot init class", e);
        }
        action.setStyle(theOneAndOnlyPaint); // apply current style
        // add to view
        addView(action);
        // add to history
        history.add(action);
        // animation start
        histTranslateX = getWidth() * 11;
        invalidate();
        // callback
        action.setOnCompletion((action) -> {
            initCurrentAction();    // let actions clone themselves
            return null;
        });
    }

    /**
     * end the current action
     * may produce empty history!
     */
    void finishAction() {
        if (!action.focusLost()) {
            removeView(history.remove(history.size() - 1));
        }
    }

    /**
     * interface to select an action
     * set next action - line, rect...
     */
    public void setDrawAction(Class<? extends AbstractPaintActionExtendsView> action) {
        if (erasing) toggleEraseMode();
        finishAction();
        actionClass = action;
        initCurrentAction();
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // show/hide history
        if (action.contains(getWidth() / 2f, getHeight() * -0.1f, getWidth() / 2f)) {
            // hide
            histYTarget = getHeight() * -0.1f;
            delayCount += 1;
            postDelayed(showHistory, 200);
            performClick(); // weird lint issue
        } else {
            histYTarget = 0;
        }
        // erasing
        if (erasing) {
            eraseAction(event);
            invalidate();
            return true;
        } else if (action != null) {
            // editing historys
            if (selectingHistoryAction(event)) {
                return true;
            }
            // real action with Actions
            boolean b = action.handleTouch(event);
            if (!b) { // action has changed through call back
                b = action.handleTouch(event);
            }
            invalidate();
            return b;
        }
        return false;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    ///////////////////
    // for the settings
    ///////////////////

    // here u go, just edit the paint
    public Paint getPaintToEdit() {
        return theOneAndOnlyPaint;
    }

    // apply settings
    public void applyPaintEdit() {
        if (action.getCurrentState() == AbstractPaintActionExtendsView.ActionState.REVISING) {
            action.setStyle(theOneAndOnlyPaint);
        }
    }

    // other settings

    @Override
    public void setBackgroundColor(int color) {
        background_color = color;
        super.setBackgroundColor(background_color);
    }

    public int getBackgroundColor() {
        return background_color;
    }


    ////////////////////
    // manage itself
    ////////////////////

    public void undo() {
        finishAction();
        if (canUndo()) {
            redoStack.push(history.remove(history.size() - 1));
            removeView(redoStack.peek());
        } else {
            Log.i(TAG, "unDo: nothing to undo");
        }
        if (canUndo()) {
            action = history.get(history.size() - 1);
        } else {
            initCurrentAction();
        }
        invalidate();
    }

    public void redo() {
        if (canRedo()) {
            finishAction();
            history.add(redoStack.pop());
            action = history.get(history.size() - 1);
            addView(action);
            histTranslateX = getWidth() * 11;
            invalidate();
        } else {
            Log.i(TAG, "redo: nothing to redo");
        }
    }

    // you can undo a clear, slowly
    public void clear() {
        finishAction();
        for (int i = 0; i < history.size(); i++) {
            redoStack.push(history.get(history.size() - 1 - i));
        }
        history.clear();
        removeAllViews();
        initCurrentAction();
    }

    public boolean canUndo() {
        return !history.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    float currPointerX, currPointerY;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawHistory(canvas);

        if (erasing) {
            // show touch
            canvas.drawCircle(currPointerX, currPointerY, eraserRadius, internalPaint);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
//        if (histY == 0) {
//            Log.d(TAG, "onLayout: 1st time");
//            histY = 0;
//            histYTarget = histY;
//        }
    }


    // let actions know if they are editing or done
    public void editActionButtonClicked() {
        if (erasing) {
            toggleEraseMode();
            // stop erasing
        }
        if (action.getCurrentState() == AbstractPaintActionExtendsView.ActionState.NEW) {
            if (history.size() <= 1) {
                Log.i(TAG, "editActionButtonClicked: nothing to edit");
                return;
            } else {
                // edit the last one, not this one
                finishAction();
                action = history.get(history.size() - 1);
            }
        }
        // edit current action
        action.editButtonClicked();
    }

    /**
     * erase action
     */
    boolean erasing;
    float eraserRadius = 10;
    public void toggleEraseMode() {
        erasing = !erasing;
        if (erasing) {
            // check current
            finishAction();
        } else {
            if (canUndo()) {
                action = history.get(history.size() - 1);
            } else {
                initCurrentAction();
            }
        }
        invalidate();
    }

    void eraseAction(MotionEvent event) {
        currPointerX = event.getX();
        currPointerY = event.getY();
        for (int i = history.size() - 1; i >= 0; i--) {
            if (history.get(i).contains(currPointerX, currPointerY, eraserRadius)) {
                redoStack.add(history.remove(i));
                removeView(redoStack.peek());
            }
        }
    }


    // EXPERIMENTTs
    static Paint internalPaint;
    float histTranslateX, histY, histYTarget;
    void drawHistory(Canvas canvas) {
        if (history == null) {
            return;
        }
        if (internalPaint == null) {
            internalPaint = new Paint();
            internalPaint.setColor(Color.BLACK);
            internalPaint.setStyle(Paint.Style.STROKE);
        }
        // also draw the history
        canvas.save();
        canvas.translate(0, histY); // getHeight() * 0.9f
        canvas.scale(0.1f, 0.1f);
        float dx = histTranslateX; // getWidth() * 10;

        for (int i = history.size() - 1; i >= 0; i--) {
            if (dx <= 0) {
                break;
            }
            dx -= getWidth();
            canvas.save();
            canvas.translate(dx, 0);
            internalPaint.setStrokeWidth(20);
            canvas.drawRect(0, 0, getWidth(), getHeight(), internalPaint);
            history.get(i).draw(canvas);
            canvas.restore();
        }
        canvas.restore();

        // animate
        if (histTranslateX > getWidth() * 10) {
            histTranslateX -= (histTranslateX - getWidth() * 10) * 0.2 + 0.5;
            invalidate();
        } else {
            histTranslateX = getWidth() * 10;
        }
        if (Math.abs(histY - histYTarget) > 1) {
            histY += (histYTarget - histY) * 0.2;
            invalidate();
        }
    }


    int delayCount = 0;
    Runnable showHistory = () -> {
        delayCount --;
        if (delayCount == 0) {
            histYTarget = 0;
            invalidate();
        }
    };



    boolean selectingHistory = false;
    // select history
    boolean selectingHistoryAction(MotionEvent e) {
        if (history.size() == 0) {
            return false;
        }
        // the height of histroy is 0.1 h
        if (e.getActionMasked() == MotionEvent.ACTION_DOWN && e.getY() < histY + getHeight() * 0.1) {
            selectingHistory = true;
            return true;
        }
        if (selectingHistory) {
            if (e.getActionMasked() == MotionEvent.ACTION_UP) {
                // deal with current action
                finishAction(); // zero size array
                if (!canUndo()) {
                    initCurrentAction();
                    return true;
                }
                // select index
                int index = history.size() - 10 + (int) (e.getX() * 10 / getWidth());
                index = Math.max(0, Math.min(history.size() - 1, index));
                // move index
                action = history.remove(index);
                history.add(action);
                action.editButtonClicked();
                selectingHistory = false;
                invalidate();
            }
            return true;
        }
        return false;
    }


    //////////////////
    // super actions
    //////////////////

    public void drawSelf(Canvas c) {
        finishAction();
        c.drawColor(background_color);
        for (AbstractPaintActionExtendsView act : history) {
            act.draw(c);
        }
        if (history.size() == 0) {
            initCurrentAction();
        }
    }

}
