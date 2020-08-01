package painter.actions;

import java.util.function.UnaryOperator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * parent of all actions
 * a view that should be added, removed from paper
 */
public abstract class AbstractPaintActionExtendsView extends View {
    static final String TAG = "-=-= Abstract Action";


    public AbstractPaintActionExtendsView(Context context) {
        super(context);
        currentState = ActionState.NEW;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e(TAG, "onDraw: don't call default onDraw");
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setTextSize(100);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(TAG + " fk, not implemented", getWidth() / 2f, getHeight() / 2f, paint);
    }

    /**
     * main way to draw/interact with user
     * @param e the touch event
     */
    public abstract boolean handleTouch(MotionEvent e);

    /**
     * set the styles, ie color, thickness, ...
     * @param p Paint that has those info
     */
    public abstract void setStyle(Paint p);

//    /**
//     * report if this action is finished
//     * @return true iff done
//     */
//    public abstract boolean yóuD¤ne();

    UnaryOperator<AbstractPaintActionExtendsView> callWhenDone;
    public void setOnCompletion(UnaryOperator<AbstractPaintActionExtendsView> calledWhenDone) {
        callWhenDone = calledWhenDone;
    }

    /**
     * switch to edit, then call callWhenDone
     */
    ActionState currentState;
    public void editButtonClicked() {
        switch (currentState) {
            case NEW:
                return;
            case STARTED:
            case FINISHED:
                currentState = ActionState.REVISING;
                break;
            case REVISING:
                currentState = ActionState.FINISHED;
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
        if (currentState == ActionState.NEW) {
            return false;
        } else {
            currentState = ActionState.FINISHED;
            invalidate();
            return true;
        }
    }

    /**
     * useful helpers
     */
    double dist(float a, float b, float x, float y) {
        return Math.sqrt(Math.pow(a - x, 2) + Math.pow(b - y, 2));
    }

    enum ActionState {
        NEW, STARTED, FINISHED, REVISING;
    }

}
