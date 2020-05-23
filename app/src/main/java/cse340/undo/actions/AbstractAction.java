package cse340.undo.actions;

import android.support.annotation.CallSuper;
import android.util.Log;

import cse340.undo.app.DrawingView;

/**
 * Represents any action which can be performed on a DrawingView. To make a new action, subclass
 * this and implement the required methods.
 *
 * @see DrawingView
 * @see AbstractAction#doAction(DrawingView)
 */
public abstract class AbstractAction {
    final String LOG_TAG = "ACTION/" + getClass().getCanonicalName();

    /**
     * Applies this action to the given DrawingView.
     *
     * @param view  DrawingView in which to apply this action.
     */
    @CallSuper
    public void doAction(DrawingView view) {
        Log.i(LOG_TAG, "Doing: " + toString());
    }

    /**
     * @return String representation of this action (name and arguments).
     */
    @Override
    public abstract String toString();
}
