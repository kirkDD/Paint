package cse340.undo.actions;

import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import cse340.undo.app.DrawingView;

/**
 * Reversible action which changes the color of the DrawingView's paint.
 */
public class ChangeColorAction extends AbstractReversibleAction {
    /** The color that this action changes the current paint to. */
    @ColorInt
    protected final int mColor;

    /** The color that this action changes the current paint from. */
    @ColorInt
    protected int mPrev;

    /**
     * Creates an action that changes the paint color.
     *
     * @param color New color for DrawingView paint.
     */
    public ChangeColorAction(@ColorInt int color) {
        this.mColor = color;
    }

    /** @inheritDoc */
    @Override
    public void doAction(DrawingView view) {
        super.doAction(view);
        Paint cur = view.getCurrentPaint();
        mPrev = cur.getColor();
        cur.setColor(mColor);
    }

    /** @inheritDoc */
    @Override
    public void undoAction(DrawingView view) {
        super.undoAction(view);
        view.getCurrentPaint().setColor(mPrev);
    }

    @NonNull
    @Override
    public String toString() {
        return "Change color to RGBA = (" +
                Color.red(mColor) +
                ", " +
                Color.blue(mColor) +
                ", " +
                Color.green(mColor) +
                ", " +
                Color.alpha(mColor) +
                ")";
    }
}
