package painter.superactions;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

import painter.Paper;

/**
 * not even a view
 */
public abstract class AbstractSuperAction {

    static Paint abstractPaint;
    Paper myPaper;
    int paperW, paperH;
    public AbstractSuperAction(Paper paper) {
        if (abstractPaint == null) {
            abstractPaint = new Paint();
            abstractPaint.setColor(Color.BLACK);
            abstractPaint.setTextSize(100);
            abstractPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        }
        myPaper = paper;
        paperW = paper.getWidth();
        paperH = paper.getHeight();
    }







    ///////////////
    // interface
    ///////////////

    public boolean handleTouch(MotionEvent e) {
        return false;
    }


    public void onDraw(Canvas canvas) {

    }



}
