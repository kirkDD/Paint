package painter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.HashMap;

import painter.superactions.AbstractSuperAction;
import painter.superactions.ActionSave;

public class SuperActionManager extends FrameLayout {

    static final String TAG = "-=-= SuperActionManager";

    public SuperActionManager(@NonNull Context context) {
        super(context);
    }

    AbstractSuperAction currentAction;

    HashMap<Button, AbstractSuperAction> actionButtonMap;
    void init() {
        actionButtonMap = new HashMap<>();
        AbstractSuperAction action = new ActionSave(getContext(), paper);
        Button b = new Button(getContext());
        b.setText("save");
        b.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        b.setOnClickListener((v) -> setCurrentAction((Button) v));
        addView(b);
        actionButtonMap.put(b, action);
    }

    void setCurrentAction(Button button) {
        if (actionButtonMap.containsKey(button)) {
            AbstractSuperAction act = actionButtonMap.get(button);
            if (currentAction == act) {
                currentAction = null;
            } else {
                currentAction = act;
            }
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
            currentAction = null;
        }
        return re;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw: ");
        super.onDraw(canvas);
    }
}
