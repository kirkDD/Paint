package painter.settings;

import android.graphics.Canvas;
import android.view.MotionEvent;

import painter.Paper;

/**
 * knobs and dials for paper and paint objects
 */
public interface Setting { // ????????????????

    /**
     * set the paper that this setting works on
     * @param paper the paper to be setted
     */
    void setPaper(Paper paper);

    /**
     * draw the setting icon
     * @param canvas
     */
    void drawIcon(Canvas canvas);

    /**
     * interact with tool
     * @param e
     * @return
     */
    boolean handleEvent(MotionEvent e);


}
