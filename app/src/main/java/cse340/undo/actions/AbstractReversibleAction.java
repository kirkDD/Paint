package cse340.undo.actions;

import android.support.annotation.CallSuper;
import android.util.Log;

import cse340.undo.app.DrawingView;

/**
 * An extension of a normal AbstractAction which adds the ability to reverse the action on a DrawingView.
 * To make a new reversible action, subclass this and implement the required methods.
 *
 * @see AbstractAction
 * @see DrawingView
 * @see AbstractReversibleAction#doAction(DrawingView)
 * @see AbstractReversibleAction#undoAction(DrawingView)
 */
public abstract class AbstractReversibleAction extends AbstractAction {
    /** Whether or not this action is currently done (a.k.a. not undone). */
    private boolean mDone;

    /**
     * @return True if the action has already been done, false otherwise.
     */
    public boolean isDone() {
        return mDone;
    }

    /**
     * Does this action, which is guaranteed to have occurred, to the given DrawingView.
     *
     * @param view  DrawingView in which to apply this action.
     * @throws IllegalStateException if the action has already been done.
     */
    @CallSuper
    public void doAction(DrawingView view) {
        if (mDone) {
            throw new IllegalStateException("Trying to do action which has already been undone: " + toString());
        }
        super.doAction(view);

        mDone = true;
    }

    /**
     * Undoes this action, which is guaranteed to have occurred, to the given DrawingView.
     *
     * @param view  DrawingView in which to apply this action.
     * @throws IllegalStateException if the action hasn't already been done.
     */
    @CallSuper
    public void undoAction(DrawingView view) {
        if (!mDone) {
            throw new IllegalStateException("Trying to undo action which hasn't been done: " + toString());
        }

        Log.i(LOG_TAG, "Undoing: " + toString());
        mDone = false;
    }

    /** @inheritDoc */
    @Override
    public abstract String toString();
}
