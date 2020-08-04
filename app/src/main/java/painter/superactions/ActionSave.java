package painter.superactions;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.JetPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.ClosedDirectoryStreamException;

import cse340.undo.R;
import painter.Paper;
import painter.actions.AbstractPaintActionExtendsView;

public class ActionSave extends AbstractSuperAction {
    static final String TAG = "-=-= ActionSave";


    public ActionSave(Context c, Paper paper) {
        super(c, paper);
    }

    @Override
    public void focusChange(boolean active) {
        super.focusChange(active);
        if (active) {
            new Thread(() -> new Handler(Looper.getMainLooper()).post(this::saveCurrentPaper)).start();
        }
        done = true;
    }


    void saveCurrentPaper() {
        Bitmap bitmap = Bitmap.createBitmap(myPaper.getWidth(), myPaper.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        myPaper.drawSelf(canvas);
        MediaStore.Images.Media.insertImage(
                context.getContentResolver(),
                bitmap, context.getResources().getString(R.string.app_name) + (System.currentTimeMillis() / 1000 % 100000),
                "saved drawing");
        Toast.makeText(context,"saved to Images", Toast.LENGTH_LONG).show();
    }

    @Override
    public String getName() {
        return "save";
    }
}
