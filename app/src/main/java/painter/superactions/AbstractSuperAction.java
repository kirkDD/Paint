package painter.superactions;


import android.content.Context;
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
    Context context;
    public AbstractSuperAction(Context context, Paper paper) {
        if (abstractPaint == null) {
            abstractPaint = new Paint();
            abstractPaint.setColor(Color.BLACK);
            abstractPaint.setTextSize(100);
            abstractPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        }
        this.context = context;
        myPaper = paper;
        paperW = paper.getWidth();
        paperH = paper.getHeight();
    }


    ////////////
    // helpers
    ////////////




    ///////////////
    // interface
    ///////////////

    public boolean handleTouch(MotionEvent e) {
        return false;
    }


    public void onDraw(Canvas canvas) {

    }

    boolean done;
    public boolean isDone() {
        return done;
    }

    // selected and is in currentAction
    public void focusChange(boolean active) {

    }

    public abstract String getName();


}
