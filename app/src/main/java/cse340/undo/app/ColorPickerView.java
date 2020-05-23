package cse340.undo.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.ColorInt;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import static cse340.undo.app.AbstractColorPickerView.EssentialGeometry.OFFWHEEL;

/**
 * This is a subclass of AbstractColorPickerView, that is, this View implements a ColorPicker.
 *
 * There are several class fields, enums, callback classes, and helper functions which have
 * been implemented for you.
 *
 * PLEASE READ AbstractColorPickerView.java to learn about these.
 */
public class ColorPickerView extends AbstractColorPickerView {
    /**
     * The current color selected in the ColorPicker. Not necessarily the last
     * color that was sent to the listeners.
     */
    @ColorInt
    protected int mCurrentColor;

    /**
     * From here on out, this is boilerplate.
     *
     * @param context
     * @param attrs
     */
    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected EssentialGeometry essentialGeometry(MotionEvent event) {
        return null;
    }

    @Override
    public void setColor(int newColor) {

    }
}
