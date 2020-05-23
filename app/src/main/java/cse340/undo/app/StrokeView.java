package cse340.undo.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/**
 * Simple little view which takes a path and paint object and uses and renders them.
 */
@SuppressLint("ViewConstructor")
public class StrokeView extends View {
    /** The path for the stroke that was drawn */
    protected final Path mPath;

    /** The paint brush with which to draw the path */
    protected final Paint mPaint;

    /**
     * Create a new stroke view to show on the DrawingView
     * @param context the context of this new view
     * @param path The path that will be added to the new view
     * @param paint The paint with which to draw the stroke
     */
    public StrokeView(Context context, Path path, Paint paint) {
        super(context);
        this.mPath = path;
        this.mPaint = paint;
    }

    /**
     * Renders the stroke by drawing the path on the view Canvas.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(mPath, mPaint);
    }
}
