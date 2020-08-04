package painter.actions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;

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
    void updateMyPath() {
        if (rotationMatrix == null) {
            rotationMatrix = new Matrix();
        }
        rotationMatrix.setRotate(-rotateAngle, (coors[0] + coors[2]) / 2f, (coors[1] + coors[3]) / 2f);
        myPath.rewind();
        myPath.addOval(
                Math.min(coors[0], coors[2]),
                Math.min(coors[1], coors[3]),
                Math.max(coors[0], coors[2]),
                Math.max(coors[1], coors[3]), Path.Direction.CW);
        myPath.transform(rotationMatrix);
    }


}
