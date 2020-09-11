package painter.help;

import android.graphics.Color;

public class Calculator {
    public static double DIST(float x1, float y1, float x2, float y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public static float MAP(float val, float vLow, float vHigh, float tLow, float tHigh) {
        // shift to 0, scale and shift
        val -= vLow;
        val = val * (tHigh - tLow) / (vHigh - vLow);
        val += tLow;
        return val;
    }

    public static int CONTRAST_COLOR(int color) {
        int y = (299 * Color.red(color) + 587 * Color.green(color) + 114 * Color.blue(color)) / 1000;
        if (Color.alpha(color) < 140) {
            return Color.WHITE;
        }
        return y >= 128 ? Color.BLACK : Color.WHITE;
    }
}
