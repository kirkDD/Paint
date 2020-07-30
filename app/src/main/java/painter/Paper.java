package painter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import java.lang.reflect.InvocationTargetException;

import painter.actions.AbstractPaintActionExtendsView;
import painter.actions.ActionStraightLine;

/**
 * the paper to hold all drawings (views)
 */
public class Paper extends FrameLayout {
    static final String TAG = "-=-= Paper";

    public Paper(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "Paper: initializing");
        setNextAction(ActionStraightLine.class);
    }

    // current action
    AbstractPaintActionExtendsView action;
    // current action's class
    Class<? extends AbstractPaintActionExtendsView> actionClass;

    /**
     * set next action - line, rect...
     * @param nextAction the action's class
     */
    void setNextAction(Class<? extends AbstractPaintActionExtendsView> nextAction) {
        actionClass = nextAction;
        initAction();
    }

    void initAction() {
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
}
