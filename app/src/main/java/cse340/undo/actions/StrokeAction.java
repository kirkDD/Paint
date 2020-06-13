package cse340.undo.actions;


import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.view.View;
import cse340.undo.app.StrokeView;
import cse340.undo.app.DrawingView;

/**
 * Reversible action which renders a stroke in DrawingView.
 */
public class StrokeAction extends AbstractReversibleViewAction {
    /** Path for this stroke. */
    private final Path mPath;

    /** Paint used to draw this stroke. */
    private final Paint mPaint;

    /** View being used to render this stroke (or null if not rendered). */
    private View mStrokeView;

    /**
     * Creates an action that renders a stroke.
     *
     * @param path    Path for stroke.
     * @param paint Paint for stroke.
     * @throws IllegalArgumentException if stroke or paint are null.
     */
    public StrokeAction(Path path, Paint paint) {
        if (path == null || paint == null) {
            throw new IllegalStateException("Null stroke or paint");
        }

        this.mPath = path;

        // Copy-construct paint to prevent changes to the original object from affecting this.
        this.mPaint = new Paint(paint);
    }

    /**
     * Renders the stroke in the given view.
     *
     * @param view  DrawingView in which to render the stroke.
     */
    @Override
    public void doAction(DrawingView view) {
        super.doAction(view);
        mStrokeView = new StrokeView(view.getContext(), mPath, mPaint);
        view.addView(mStrokeView);
    }

    /**
     * De-renders the stroke in the given view.
     *
     * @param view  DrawingView in which to de-render the stroke.
     */
    @Override
    public void undoAction(DrawingView view) {
        super.undoAction(view);
        if (view.indexOfChild(mStrokeView) < 0) {
            throw new IllegalStateException("StrokeView not found");
        }

        view.removeView(mStrokeView);
    }

    /**
     * Invalidates the rendered stroke. Useful if the path or paint have changed.
     */
    @Override
    public void invalidate() {
        mStrokeView.invalidate();
    }

    @NonNull
    @Override
    public String toString() {
        return "Drawing path " + mPath.toString();
    }

}
