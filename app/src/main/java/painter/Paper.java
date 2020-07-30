package painter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import java.lang.reflect.InvocationTargetException;

import painter.actions.AbstractPaintActionExtendsView;
import painter.actions.ActionStraightLine;

/**
 * the paper to hold all drawings (views)
 */
public class Paper extends FrameLayout {

    public Paper(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setNextAction(ActionStraightLine.class);
    }

    AbstractPaintActionExtendsView action;

    Class<? extends AbstractPaintActionExtendsView> actionClass;

    void setNextAction(Class<? extends AbstractPaintActionExtendsView> nextAction) {
        actionClass = nextAction;
        initAction();
    }

    void initAction() {
        try {
            action = actionClass.getDeclaredConstructor(Context.class).newInstance(getContext());
            addView(action);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
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
