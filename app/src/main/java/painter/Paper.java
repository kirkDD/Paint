package painter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Stack;

import painter.actions.AbstractPaintActionExtendsView;
import painter.actions.ActionArrow;
import painter.actions.ActionOval;
import painter.actions.ActionRectangle;
import painter.actions.ActionStraightLine;

/**
 * the paper to hold all drawings (views)
 */
public class Paper extends FrameLayout {
    static final String TAG = "-=-= Paper";

    ArrayList<AbstractPaintActionExtendsView> history;
    Stack<AbstractPaintActionExtendsView> redoStack;
    // current action
    AbstractPaintActionExtendsView action;
    // current action's class
    Class<? extends AbstractPaintActionExtendsView> actionClass;
    static Paint theOneAndOnlyPaint;

    int background_color = -1;

    public Paper(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "Paper: initializing");
        theOneAndOnlyPaint = new Paint();
        history = new ArrayList<>();
        redoStack = new Stack<>();

        setNextAction(ActionArrow.class);
    }


    /**
     * set next action - line, rect...
     *
     * @param nextAction the action's class
     */
    void setNextAction(Class<? extends AbstractPaintActionExtendsView> nextAction) {
        actionClass = nextAction;
        try {
            Log.d(TAG, "initAction: " + actionClass.getConstructor(Context.class));
            action = actionClass.getConstructor(Context.class).newInstance(getContext());
            Log.d(TAG, "initAction: adding view");
            addView(action);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            Log.e(TAG, "initAction: cannot init class", e);
        }
        action.setOnCompletion((action) -> {
            addToHistory();
            return null;
        });

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (action != null) {
            // editing historys
            if (selectingHistoryAction(event)) {
                return true;
            }
            boolean b = action.handleTouch(event);
            if (!b) { // action has changed through call back
                b = action.handleTouch(event);
            }
            return b;

        }
        return false;
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
        if (action != null) {
            action.setStyle(theOneAndOnlyPaint);
        } else {
            Log.e(TAG, "applySettingToAction: changing style on null action");
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
        if (history.size() > 0) {
            redoStack.push(history.get(history.size() - 1));
            removeView(history.get(history.size() - 1));
            history.remove(history.size() - 1);
        } else {
            Log.i(TAG, "unDo: nothing to undo");
        }
    }

    public void redo() {
        if (redoStack.size() > 0) {
            history.add(redoStack.pop());
            addView(history.get(history.size() - 1));
        } else {
            Log.i(TAG, "redo: nothing to redo");
        }
    }

    // you can undo a clear, slowly
    public void clear() {
        for (int i = 0; i < history.size(); i++) {
            redoStack.push(history.get(history.size() - 1 - i));
        }
        history.clear();
        removeAllViews();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawHistory(canvas);
    }



    /////////////////////
    // let actions add themselves
    /////////////////////

    void addToHistory() {
        history.add(action);
        setNextAction(actionClass); // same action
    }

    // let actions know if they are editing or done
    public void editActionButtonClicked() {
        if (action == null) return;
        action.editButtonClicked();
    }


    // EXPERIMENTTs

    void drawHistory(Canvas canvas) {
        if (history == null) {
            return;
        }
        // also draw the history
        canvas.save();
        canvas.translate(0, getHeight() * 0.9f);
        canvas.scale(0.1f, 0.1f);
        float dx = getWidth() * 10;
        for (int i = history.size() - 1; i >= 0; i--) {
            if (dx <= 0) {
                break;
            }
            dx -= getWidth();
            canvas.save();
            canvas.translate(dx, 0);
            theOneAndOnlyPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(0, 0, getWidth(), getHeight(), theOneAndOnlyPaint);
//            canvas.drawPaint(theOneAndOnlyPaint);
            history.get(i).draw(canvas);
            canvas.restore();
        }
        canvas.restore();
    }


    boolean selectingHistory = false;

    boolean selectingHistoryAction(MotionEvent e) {
        if (e.getActionMasked() == MotionEvent.ACTION_DOWN && e.getY() > getHeight() * 0.9f) {
            selectingHistory = true;
            return true;
        }
        if (selectingHistory) {
            if (e.getActionMasked() == MotionEvent.ACTION_UP) {
                // select index
                int index = history.size() - 10 + (int) (e.getX() * 10 / getWidth());
                index = Math.max(0, Math.min(history.size() - 1, index));
                // move index
                action = history.remove(index);
                selectingHistory = false;
                Log.i(TAG, "selectingHistoryAction: selected: " + index);
            }
            return true;
        }
        return false;
    }
}
