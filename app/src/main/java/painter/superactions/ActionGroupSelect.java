package painter.superactions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;

import java.util.HashSet;

import painter.Paper;
import painter.actions.AbstractPaintActionExtendsView;
import painter.actions.ActionStroke;

public class ActionGroupSelect extends AbstractSuperAction {
    static final String TAG = "-=-= GroupSelect";

    static Paint paint;
    Path selectionPath;
    HashSet<AbstractPaintActionExtendsView> selected;

    public ActionGroupSelect(Context context, Paper paper) {
        super(context, paper);
        if (paint == null) {
            paint = new Paint();
            paint.setStrokeWidth(4);
            int col = paper.getBackgroundColor();
            paint.setColor(Color.rgb(255 - Color.red(col), 255 - Color.green(col), 255 - Color.blue(col)));
            paint.setStyle(Paint.Style.STROKE);
        }
        selectionPath = new Path();
        selected = new HashSet<>();
    }

    static final int SELECTED = 65480;
    static final int NEW = 987985;
    int state = NEW;

    float ctx, cty; // current points
    float ptx, pty; // previous points
    @Override
    public boolean handleTouch(MotionEvent e) {
        if (super.handleTouch(e)) {
            return true;
        }
        ctx = e.getX();
        cty = e.getY();
        if (state == SELECTED) {
            // confirm delete move group
            boolean re = true;
            for (AbstractPaintActionExtendsView act : selected) {
                re = re && act.handleTouch(e);
            }
            if (!re) {
                done = true;
            }
            return true;
        }
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                selectionPath.moveTo(ctx, cty);
                ptx = ctx;
                pty = cty;
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                if (Math.sqrt(Math.pow(ctx - ptx, 2) + Math.pow(cty - pty, 2)) > ActionStroke.MIN_MOVE_DIST) {
                    selectionPath.quadTo(ptx, pty,
                            (ptx + ctx) / 2f, (pty + cty) / 2f);
                    ptx = ctx;
                    pty = cty;
                }
                for (AbstractPaintActionExtendsView action : myPaper.history) {
                    if (!selected.contains(action) && action.touchesPath(selectionPath)) {
                        selected.add(action);
                        if (action.getCurrentState() == AbstractPaintActionExtendsView.ActionState.FINISHED) {
                            action.editButtonClicked();
                        }
                    }
                }
                if (e.getActionMasked() == MotionEvent.ACTION_MOVE) break;
                // end
                if (selected.size() > 0) {
                    state = SELECTED;
                }
                selectionPath.close();
                selectionPath.rewind();
                // text
                AbstractPaintActionExtendsView combined = new ActionStroke(context);
                Path combinedPath = new Path();
                for (AbstractPaintActionExtendsView act : selected) {
                    act.addToPath(combinedPath);
                    myPaper.history.remove(act);
                    myPaper.removeView(act);
                }
                combined.setStrokePath(combinedPath);
                myPaper.history.add(combined);
                myPaper.addView(combined);
                myPaper.action = combined;
                myPaper.applyPaintEdit();
                done = true;
        }

        return true;
    }

    @Override
    public void focusChange(boolean active) {
        super.focusChange(active);
        done = !active;
        resetSelf();
    }

    void resetSelf() {
        for (AbstractPaintActionExtendsView act : selected) {
            act.focusLost();
        }
        selected.clear();
        state = NEW;
    }

    @Override
    public String getName() {
        return "Group Select";
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(selectionPath, paint);
    }
}
