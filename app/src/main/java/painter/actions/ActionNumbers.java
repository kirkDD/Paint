package painter.actions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

public class ActionNumbers extends ActionStroke {

    StringBuilder stringBuilder;
    int nextIntToAdd = 0;
    float CHAR_WIDTH;

    public ActionNumbers(Context context) {
        super(context);
        paint.setTextSize(150);
        CHAR_WIDTH = paint.measureText("0  ") / 3f + 5; // tune width :(
        stringBuilder = new StringBuilder();
    }


    float lX, lY, distMoved;
    @Override
    public boolean handleTouch(MotionEvent e) {
        boolean re = super.handleTouch(e);

        if (currentState == ActionState.FINISHED) {
            lastTimeStamp -= 10000; // hack so just draw one continuos path instead of many
        }
        if (currentState != ActionState.STARTED) return re;

        // only add numbers when drawing, not revising
        if (e.getActionMasked() != MotionEvent.ACTION_DOWN) {
            // threshold to bypass multitouch
//            distMoved += dist(lX, lY, e.getX(), e.getY()) < 200 ? dist(lX, lY, e.getX(), e.getY()) : 0;
            distMoved += dist(lX, lY, e.getX(), e.getY());
        }
        lX = e.getX();
        lY = e.getY();
        while (stringBuilder.length() * CHAR_WIDTH < distMoved) {
            stringBuilder.append(++nextIntToAdd).append("  ");
        }
        return re;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(thisColor);
        paint.setStrokeCap(thisCap);
        paint.setStrokeWidth(thisWidth);
        paint.setStrokeJoin(thisJoin);
        canvas.translate(pathOffsetX, pathOffsetY);

        if (currentState == ActionState.REVISING || currentState == ActionState.STARTED) {
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(path, paint);
        }

        // add texts
        paint.setStyle(Paint.Style.FILL);
        canvas.drawTextOnPath(stringBuilder.toString(), path, 0, 0, paint);


        conditionalDrawHighlight(canvas);

    }
}
