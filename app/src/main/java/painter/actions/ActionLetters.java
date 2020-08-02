package painter.actions;

import android.content.Context;
import android.util.Log;

import java.util.Random;

public class ActionLetters extends ActionNumbers {

    Random random;

    public ActionLetters(Context context) {
        super(context);
        random = new Random();
    }



    int letter = (int) 'A';
    @Override
    void addThingToString() {
//        stringBuilder.append((char) letter++).append("  ");

        // some fum todo
        try {
            stringBuilder.append((char) (random.nextInt(0x10FFFF)));
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "addThingToString: failed");
        }
        stringBuilder.append("  ");
    }
}
