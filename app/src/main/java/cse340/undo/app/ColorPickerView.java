package cse340.undo.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;


/**
 * This is a subclass of AbstractColorPickerView, that is, this View implements a ColorPicker.
 *
 * There are several class fields, enums, callback classes, and helper functions which have
 * been implemented for you.
 *
 * PLEASE READ AbstractColorPickerView.java to learn about these.
 */
public class ColorPickerView extends AbstractColorPickerView {
    /* ********************************************************************************************** *
     * All of your applications state (the model) and methods that directly manipulate it are here    *
     * This does not include mState which is the literal state of your PPS, which is inherited
     * ********************************************************************************************** */

    /**
     * The current color selected in the ColorPicker. Not necessarily the last
     * color that was sent to the listeners.
     */
    @ColorInt
    protected int mCurrentColor;
    protected int mPreviousColor;

    @Override
    public void setColor(@ColorInt int newColor) {
        mCurrentColor = newColor;
        invalidate();
    }

    private void updateModel(float x, float y) {
        // hint: we give you a very helpful function to call
        mCurrentColor = getColorFromAngle(getTouchAngle(x, y));

    }

    /* ********************************************************************************************** *
     *                               <End of model declarations />
     * ********************************************************************************************** */

    /* ********************************************************************************************** *
     * You may create any constants you wish here.                                                     *
     * You may also create any fields you want, that are not necessary for the state but allow       *
     * for better optimized or cleaner code                                                           *
     * ********************************************************************************************** */
    Paint paintCenter;
    Paint paintThumb;

    /* ********************************************************************************************** *
     *                               <End of other fields and constants declarations />
     * ********************************************************************************************** */

    /**
     * Constructor of the ColorPicker View
     * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view. This value may be null.
     */
    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Hint: also caching the paint objects is good style and avoids unnecessary computation.
        mState = State.START;
        mCurrentColor = DEFAULT_COLOR;
        paintCenter = new Paint();
        paintCenter.setStyle(Paint.Style.FILL);
        paintThumb = new Paint();
        paintThumb.setColor(Color.WHITE);
        paintThumb.setStyle(Paint.Style.FILL);

    }

    /**
     * Draw the ColorPicker on the Canvas
     * @param canvas the canvas that is drawn upon
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float thumbR = RADIUS_TO_THUMB_RATIO * mRadius;
        paintCenter.setColor(mCurrentColor);
        float angle = getAngleFromColor(mCurrentColor);
        float hypo = mRadius - thumbR;
        float x = (float) Math.cos(angle) * hypo;
        float y = (float) Math.sin(angle) * hypo;
        canvas.drawCircle(mCenterX + x, mCenterY + y, thumbR, paintThumb);

    }

    /**
     * Called when this view should assign a size and position to all of its children.
     * @param changed This is a new size or position for this view
     * @param left Left position, relative to parent
     * @param top Top position, relative to parent
     * @param right Right position, relative to parent
     * @param bottom Bottom position, relative to parent
     */
    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // Hint: the ColorPicker view is not a square, base it off the min of the width and height
        int width = right - left;
        int height = bottom - top;
        mRadius = (float) Math.min(width, height) / 2;
        mCenterX = (float) width / 2;
        mCenterY = (float) height / 2;
    }


    /**
     * Calculate the essential geometry given an event.
     *
     * @param event Motion event to compute geometry for, most likely a touch.
     * @return EssentialGeometry value.
     */
    @Override
    protected EssentialGeometry essentialGeometry(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float distance = (float) Math.sqrt(Math.pow(x - mCenterX, 2) + Math.pow(y - mCenterY, 2));
        if (distance <= mRadius && distance >= mRadius - 2 * RADIUS_TO_THUMB_RATIO * mRadius) {
            return EssentialGeometry.WHEEL;
        }
        return EssentialGeometry.OFFWHEEL;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        EssentialGeometry geometry = essentialGeometry(event);

        // make sure to make calls to updateModel, invalidate, and invokeColorChangeListeners

         switch (mState) {
             case START:
                 if (geometry == EssentialGeometry.WHEEL && event.getAction() == MotionEvent.ACTION_DOWN ) {
                     mPreviousColor = mCurrentColor;
                     updateModel(event.getX(), event.getY());
                     mState = State.INSIDE;
                     paintThumb.setAlpha((int) (0.5f * 255));
                     invalidate();
                     return true;
                 }
                 break;
             case INSIDE:
                 if (event.getAction() == MotionEvent.ACTION_MOVE) {
                     if(geometry == EssentialGeometry.WHEEL) {
                         updateModel(event.getX(), event.getY());
                         invalidate();
                         return true;
                     }
                     return true;
                 } else if (geometry == EssentialGeometry.WHEEL && event.getAction() == MotionEvent.ACTION_UP){
                     paintThumb.setAlpha((int) (1f * 255));
                     invokeColorChangeListeners(mCurrentColor);
                     invalidate();
                     mState = State.START;
                     return true;
                 } else if (geometry == EssentialGeometry.OFFWHEEL && event.getAction() == MotionEvent.ACTION_UP) {
                    resetColor();
                    invalidate();
                    mState = State.START;
                    return true;
                 }
                 break;
             default:
                 break;
         }
        return false;
    }

    private void resetColor() {
        mCurrentColor = mPreviousColor;
    }

    /**
     * Converts from a color to angle on the wheel.
     *
     * @param color RGB color as integer.
     * @return Position of this color on the wheel in radians.
     * @see AbstractColorPickerView#getTouchAngle(float, float)
     */
    public static float getAngleFromColor(int color) {
        float[] HSL = new float[3];
        ColorUtils.colorToHSL(color, HSL);
        float hue = HSL[0];
        return (float) Math.toRadians((hue - 90 + 360) % 360);
    }
}
