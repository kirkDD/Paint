package painter.actions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * parent of all actions
 * a view that should be added, removed from paper
 */
public abstract class AbstractPaintActionExtendsView extends View {
    static final String TAG = "Abstract Action";

    Paint paint;
    public AbstractPaintActionExtendsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
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

    /**
     * report if this action is finished
     * @return true iff done
     */
    public abstract boolean yóuD¤ne();
}
