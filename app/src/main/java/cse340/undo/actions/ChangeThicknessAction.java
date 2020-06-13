package cse340.undo.actions;

import android.graphics.Paint;
import android.support.annotation.NonNull;

import cse340.undo.app.DrawingView;

/**
 * Reversible action which changes the thickness of the DrawingView's paint.
 */
public class ChangeThicknessAction extends AbstractReversibleAction {
    /** The thickness that this action changes the current paint to. */
    private final int mThickness;

    /** The thickness that this action changes the current paint from. */
    private float mPrev;

    /**
     * Creates an action that changes the paint thickness.
     *
     * @param thickness New thickness for DrawingView paint.
     * @throws IllegalArgumentException if thickness not positive.
     */
    public ChangeThicknessAction(int thickness) { this.mThickness = thickness; }

    /** @inheritDoc */
    @Override
    public void doAction(DrawingView view) {
        super.doAction(view);
        //  update the thickness in the view
        //  store any information you'll need to undo this later
        //  don't store any information you won't need
        Paint cur = view.getCurrentPaint();
        mPrev =cur.getStrokeWidth();
        cur.setStrokeWidth(mThickness);
    }

    /** @inheritDoc */
    @Override
    public void undoAction(DrawingView view) {
        super.undoAction(view);
        //  update the thickness in the view
        view.getCurrentPaint().setStrokeWidth(mPrev);
    }

    /** @inheritDoc */
    @NonNull
    @Override
    public String toString() {
        return "Change thickness to " + mThickness;
    }
}
