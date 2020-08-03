package painter.superactions;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.JetPlayer;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import painter.Paper;
import painter.actions.AbstractPaintActionExtendsView;

public class ActionSave extends AbstractSuperAction {
    static final String TAG = "-=-= ActionSave";

    static File saveDir;

    public ActionSave(Paper paper) {
        super(paper);
        saveDir = Environment.getExternalStorageDirectory();

    }


    @Override
    public boolean handleTouch(MotionEvent e) {
        if (super.handleTouch(e)) {
            return true;
        }
        // do work
        if (e.getActionMasked() == MotionEvent.ACTION_UP) {
            // done

        }
        return true;
    }


    void saveCurrentPaper() {
        int nameNum = 0;
        for (File f : saveDir.listFiles()) {
            Log.d(TAG, "saveCurrentPaper: " + f.toString());
        }
        Bitmap bitmap = Bitmap.createBitmap(paperW, paperH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        myPaper.draw(canvas);
        File out = new File(saveDir + "" + nameNum + ".jpeg");
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(out));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


}
