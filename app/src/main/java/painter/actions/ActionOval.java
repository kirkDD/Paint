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


        if (rotateAngle != 0) {
            canvas.translate((coors[0] + coors[2]) / 2, (coors[1] + coors[3]) / 2);
            canvas.rotate(-rotateAngle);
            canvas.translate(-(coors[0] + coors[2]) / 2, -(coors[1] + coors[3]) / 2);
        }

        canvas.drawOval(
                Math.min(coors[0], coors[2]),
                Math.min(coors[1], coors[3]),
                Math.max(coors[0], coors[2]),
                Math.max(coors[1], coors[3]),
                paint);

        conditionalDrawHighlight(canvas);
    }

    @Override
    public boolean contains(float x, float y, float radius) {
        // (x-h)^2/a^2 + (y-k)^2/b^2 <= 1
        return false;
    }
}
