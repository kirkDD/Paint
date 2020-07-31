package painter;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import java.lang.reflect.InvocationTargetException;

import painter.actions.AbstractPaintActionExtendsView;
import painter.actions.ActionOval;
import painter.actions.ActionRectangle;
import painter.actions.ActionStraightLine;

/**
 * the paper to hold all drawings (views)
 */
public class Paper extends FrameLayout {
    static final String TAG = "-=-= Paper";


    // current action
    AbstractPaintActionExtendsView action;
    // current action's class
    Class<? extends AbstractPaintActionExtendsView> actionClass;
    static Paint theOneAndOnlyPaint;

    int background_color = -1;

    public Paper(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "Paper: initializing");
        setNextAction(ActionOval.class);
        theOneAndOnlyPaint = new Paint();
    }


    /**
     * set next action - line, rect...
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
    }




    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (action != null) {
            return action.handleTouch(event);
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



}
