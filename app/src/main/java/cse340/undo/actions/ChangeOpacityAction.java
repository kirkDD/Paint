package cse340.undo.actions;

import android.graphics.Paint;
import android.support.annotation.NonNull;

import cse340.undo.app.DrawingView;
public class ChangeOpacityAction extends AbstractReversibleAction{
    /** The alpha that this action changes the current paint to. */

    private final int mAlpha;

    /** The alpha that this action changes the current paint from. */
    private int mPrev;

    /**
     * Creates an action that changes the paint color.
     *
     * @param alpha New color for DrawingView paint.
     */
    public ChangeOpacityAction(float alpha) {
        this.mAlpha = (int)(alpha * 255);
    }

    /** @inheritDoc */
    @Override
    public void doAction(DrawingView view) {
        super.doAction(view);
        Paint cur = view.getCurrentPaint();
        mPrev = cur.getAlpha();
        cur.setAlpha(mAlpha);
    }

    /** @inheritDoc */
    @Override
    public void undoAction(DrawingView view) {
        super.undoAction(view);
        view.getCurrentPaint().setAlpha(mPrev);
    }
    @NonNull
    @Override
    public String toString() {
        return "Change alpha to " + mAlpha;
    }
}
