package painter.actions;

import android.content.Context;
import android.graphics.Canvas;

public class ActionNumbers extends ActionStroke {
    public ActionNumbers(Context context) {
        super(context);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawTextOnPath("1234", path, 0, 0, paint);
    }
}
