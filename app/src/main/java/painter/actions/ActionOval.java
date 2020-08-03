package painter.actions;

import android.content.Context;
import android.graphics.Canvas;

public class ActionOval extends ActionRectangle {

    public ActionOval(Context context) {
        super(context);
    }

    @Override
    void onDraw2(Canvas canvas) {
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
    }

    @Override
    public boolean contains(float x, float y, float radius) {
        // (x-h)^2/a^2 + (y-k)^2/b^2 <= 1

        // due to rotation, we need to shift x,y as well
        x -= (coors[0] + coors[2]) / 2f;
        y -= (coors[1] + coors[3]) / 2f;
        // rotate
        double r = dist(x, y, 0, 0);
        double ang = 90 + angleBetween(x, y, 0, 0) - rotateAngle;  // 90 is the offset needed
        x = (float) (Math.cos(ang / 180 * Math.PI) * r);
        y = (float) (Math.sin(ang / 180 * Math.PI) * r);
        // go back
        x += (coors[0] + coors[2]) / 2f;
        y += (coors[1] + coors[3]) / 2f;

        double magicEq = Math.pow((x - (coors[0] + coors[2]) / 2f), 2) / Math.pow((coors[0] - coors[2]) / 2f, 2) +
                Math.pow((y - (coors[1] + coors[3]) / 2f), 2) / Math.pow((coors[1] - coors[3]) / 2f, 2);
        return magicEq <= 1;
    }
}
