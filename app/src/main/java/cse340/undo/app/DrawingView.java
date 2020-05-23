package cse340.undo.app;


import android.content.Context;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import java.util.HashSet;
import java.util.Set;

import cse340.undo.actions.AbstractAction;
import cse340.undo.actions.AbstractReversibleViewAction;
import cse340.undo.actions.StrokeAction;

/***
 * The canvas on which the drawing takes place. Drawings are made up of
 * strokes, which are handled by the StrokeAction class.
 * This canvas doesn't know anything about undo, and you won't need
 * to modify it to add features, or to add support for undo.
 */
public class DrawingView extends FrameLayout {
    public static final String LOG_TAG = "DrawingView";

    /** State machine enum and field. */
    private enum DrawingModel {
        START, DRAWING
    }

    /** What state the PPS is in. */
    private DrawingModel mState;

    /** Drawing fields. */
    private Path mCurrentPath;
    private Paint mCurrentPaint;

    /** Stroke drawing buffer. Used to render the line while it's being drawn. */
    protected AbstractReversibleViewAction mBuffer;

    /** Stroke event listeners. */
    public interface OnStrokeCompletedListener {
        void onStrokeCompleted(AbstractAction action);
    }

    /** Collection of current stroke listeners. */
    private final Set<OnStrokeCompletedListener> mListeners;

    /** Min distance the user should move before you add to the path. */
    public static int MIN_MOVE_DIST = 5;

    /** Used to track last touch point for path drawing. */
    private final PointF mLastPoint;
    private final PointF mStartPoint;

    /**
     * Creates a new, empty DrawingView with default paint properties.
     */
    public DrawingView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        mListeners = new HashSet<>();

        mLastPoint = new PointF();
        mStartPoint = new PointF();

        mCurrentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCurrentPaint.setDither(true);
        mCurrentPaint.setStyle(Paint.Style.STROKE);
        mCurrentPaint.setStrokeJoin(Paint.Join.ROUND);
        mCurrentPaint.setStrokeCap(Paint.Cap.ROUND);

        mCurrentPath = new Path();

        mState = DrawingModel.START;
    }

    /**
     * Handles touch events for the purposes of drawing on the canvas. On touch down,
     * begins drawing a path using the current paint. On touch move, continues drawing.
     * On touch up, notifies listeners of the completed stroke.
     *
     * @param event Event to use for drawing.
     * @return True if the event was handled, false otherwise.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        Log.i(LOG_TAG, "Touch at (" + x + ", " + y + ")");

        // Handle input events.
        switch (mState) {
            case START:
                return handleStartState(event, x, y);
            case DRAWING:
                return handleDrawingState(event, x, y);
            default:
                break;
        }
        return false;
    }

    /**
     * Private helper method to handle the Start state in the PPS
     * @param event The MotionEvent that triggered onTouchEvent
     * @param x The x coordinate of the touch event
     * @param y The y coordinate of the touch event
     * @return true if the event was consumed, false otherwise
     */
    private boolean handleStartState(MotionEvent event, float x, float y) {
        Log.i(LOG_TAG, "onDrawStart");
        onDrawStart(x, y);
        mState = DrawingModel.DRAWING;
        return true;
    }

    /**
     * Private helper method to handle the Drawing state in the PPS
     * @param event The MotionEvent that triggered onTouchEvent
     * @param x The x coordinate of the touch event
     * @param y The y coordinate of the touch event
     * @return true if the event was consumed, false otherwise
     */
    private boolean handleDrawingState(MotionEvent event, float x, float y) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                Log.i(LOG_TAG, "onDrawMove");
                onDrawMove(x, y);
                return true;
            case MotionEvent.ACTION_UP:
                Log.i(LOG_TAG, "onDrawEnd");
                onDrawEnd(x, y);
                mState = DrawingModel.START;
                return true;
            case MotionEvent.ACTION_CANCEL:
                Log.i(LOG_TAG, "onDrawCancel");
                onDrawCancel();
                mState = DrawingModel.START;
                return true;
            default:
                break;
        }
        return false;
    }

    /**
     * Triggered when drawing starts.
     *
     * @param x Horizontal coordinate of touch.
     * @param y Vertical coordinate of touch.
     */
    protected void onDrawStart(float x, float y) {
        // Start a new drawing path.
        mCurrentPath.moveTo(x, y);
        mStartPoint.x = x;
        mStartPoint.y = y;
        mLastPoint.x = x;
        mLastPoint.y = y;
        Log.i(LOG_TAG, "onDrawStart: starting new stroke @ " + mLastPoint);

        mBuffer = new StrokeAction(mCurrentPath, mCurrentPaint);
        mBuffer.doAction(this);
    }

    /**
     * Triggered when drawing moves. If we've moved enough, add a new point to the path.
     *
     * @param x Horizontal coordinate of touch.
     * @param y Vertical coordinate of touch.
     */
    protected void onDrawMove(float x, float y) {
        // Only add a bezier when the distance is larger than a threshold (MIN_MOVE_DIST).
        // If the distance is smaller, wait until a ACTION_MOVE event that creates a large enough distance.
        if (Math.sqrt(Math.pow(x - mLastPoint.x, 2) + Math.pow(y - mLastPoint.y, 2)) >= MIN_MOVE_DIST) {
            // For each ACTION_MOVE event, add a quadratic bezier from the last point (in the drawing path) to current point.
            // Each bezier is a smooth arc to be added in the drawing path.
            mCurrentPath.quadTo(mLastPoint.x, mLastPoint.y,
                    (x + mLastPoint.x) / 2, (y + mLastPoint.y) / 2);
            mLastPoint.x = x;
            mLastPoint.y = y;

            // The stroke buffer has access to currentPath, invalidate to trigger redraw.
            mBuffer.invalidate();
        }
    }

    /**
     * Triggered when drawing ends. Commits the current buffer as a done action by triggering
     * callbacks.
     */
    protected void onDrawEnd(float x, float y) {
        if (Math.sqrt(Math.pow(mStartPoint.x - mLastPoint.x, 2) + Math.pow(mStartPoint.y - mLastPoint.y, 2)) >= MIN_MOVE_DIST) {

            mBuffer.undoAction(this);

            Log.i(LOG_TAG, "Stroke completed, triggering " + mListeners.size() + " listener" + (mListeners.size() == 1 ? "" : "s"));
            for (OnStrokeCompletedListener l : mListeners) {
                l.onStrokeCompleted(mBuffer);
            }
        }

        // Very important; buffer has a reference to currentPath. If we don't reinitialize, every
        // buffer will share the same path.
        mCurrentPath = new Path();
        mBuffer = null;
    }

    /**
     * Triggered when drawing is cancelled. Trashes the current buffer and ignores callbacks.
     */
    protected void onDrawCancel() {
        if (mBuffer != null) {
            mBuffer.undoAction(this);
        }
        mBuffer = null;

        mCurrentPath.reset();
    }

    //region Getters & Setters
    /**
     * Adds a new listener for stroke completion.
     *
     * @param listener  Listener to add.
     * @return true if the listener was added, false on duplicate.
     */
    public boolean addListener(OnStrokeCompletedListener listener) {
        return mListeners.add(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener  Listener to remove.
     * @return true if the listener removed, false if not present.
     */
    public boolean removeListener(OnStrokeCompletedListener listener) {
        return mListeners.remove(listener);
    }

    public Paint getCurrentPaint() {
        return mCurrentPaint;
    }

    public void setCurrentPaint(Paint paint) {
        mCurrentPaint = paint;
    }
    //endregion
}