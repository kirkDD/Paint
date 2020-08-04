package painter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import painter.superactions.AbstractSuperAction;
import painter.superactions.ActionGroupSelect;
import painter.superactions.ActionSave;

public class SuperActionManager extends FrameLayout {

    static final String TAG = "-=-= SuperActionManager";
    ViewGroup.LayoutParams buttonLayoutParams;
    ManagerDrawing managerView;
    public SuperActionManager(@NonNull Context context) {
        super(context);
        buttonLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        managerView = new ManagerDrawing(context);
        addView(managerView);
    }

    AbstractSuperAction currentAction;

    HashMap<Button, AbstractSuperAction> actionButtonMap;
    float buttonX = 100;
    Class<? extends AbstractSuperAction>[] availableActions = new Class[]{
            ActionSave.class,
            ActionGroupSelect.class
    };

    void init() {
        actionButtonMap = new HashMap<>();

        for (Class<? extends AbstractSuperAction> actionClass : availableActions) {
            try {
                AbstractSuperAction action = actionClass
                        .getConstructor(Context.class, Paper.class)
                        .newInstance(getContext(), paper);
                Button b = new Button(getContext());
                b.setLayoutParams(buttonLayoutParams);
                b.setText(action.getName());
                b.setOnClickListener((v) -> setCurrentAction((Button) v));
                addView(b);
                b.animate().translationY(buttonX);
                buttonX += 100;
                actionButtonMap.put(b, action);
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }

        }

    }

    void setCurrentAction(Button button) {
        if (actionButtonMap.containsKey(button)) {
            AbstractSuperAction act = actionButtonMap.get(button);
            if (currentAction == act) {
                currentAction = null;
                act.focusChange(false);
            } else {
                currentAction = act;
                act.focusChange(true);
            }
            managerView.invalidate();
        }
    }



    Paper paper;

    void setPaper(Paper paper) {
        this.paper = paper;
        init();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // test
        if (currentAction == null) return false;
        boolean re = currentAction.handleTouch(event);
        if (currentAction.isDone()) {
            currentAction.focusChange(false);
            currentAction = null;
        }
        managerView.invalidate();
        return re;
    }


    private class ManagerDrawing extends View {

        public ManagerDrawing(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (currentAction != null) {
                currentAction.onDraw(canvas);
            }
        }
    }
}
