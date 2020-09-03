package painter.actions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;

public class ActionDash extends ActionStroke {
    PathEffect pathEffect;
    public ActionDash(Context context) {
        super(context);
        pathEffect = new DashPathEffect(new float[]{10, 20}, 0);
    }

    @Override
    void onDraw2(Canvas canvas) {
        paint.setColor(thisColor);
        paint.setStrokeCap(Paint.Cap.SQUARE);
        paint.setStrokeWidth(thisWidth);
        paint.setStrokeJoin(thisJoin);
        paint.setStyle(Paint.Style.STROKE);

        paint.setPathEffect(pathEffect);
        canvas.drawPath(myPath, paint);
        paint.setPathEffect(null);
    }
}
