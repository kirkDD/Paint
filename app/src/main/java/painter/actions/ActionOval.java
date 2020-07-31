package painter.actions;

import android.content.Context;
import android.graphics.Canvas;

public class ActionOval extends ActionRectangle {

    public ActionOval(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setStyle(myStyle);
        paint.setColor(myColor);
        paint.setStrokeWidth(myWidth);
        canvas.drawOval(
                Math.min(coors[0], coors[2]),
                Math.min(coors[1], coors[3]),
                Math.max(coors[0], coors[2]),
                Math.max(coors[1], coors[3]),
                paint);

    }
}
