package painter.help;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import painter.Paper;

public class ContentPusher {
    static final String TAG = "-=-=";
    public static final String SERVER_POST_URL = "http://192.168.68.112:3000/send";

    Paper paper;
    Canvas canvas;
    Bitmap bitmap;

    HttpURLConnection urlConnection;

    static final int SIGCONN = 1;
    static final int SIGDISC = 2;
    static int SIGNAL = -1;
    static final Object LOCK = new Object();
    static Runnable SUCCEED;
    static Runnable FAIL;

    class NetworkThread extends Thread {
        @Override
        public void run() {
            while (true) {
                synchronized (LOCK) {
                    try {
                        LOCK.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (SIGNAL == SIGCONN) {
                    startConnection();
                } else if (SIGNAL == SIGDISC) {
                    // todo disconnect
                } else {
                    return;
                }
            }
        }

        void startConnection() {
            Log.d(TAG, "startConnection");
            if (CONNECTED) {
//                SUCCEED.run();
//                return;
            }
            if (canvas == null) {
                FAIL.run();
                Log.d(TAG, "startConnection: canvas is null");
                return;
            }

            // not connected
            try {
                urlConnection = (HttpURLConnection) new URL(SERVER_POST_URL).openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);
                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());

                paper.draw(canvas);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);

//                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
//                byte[] b = new byte[16];
//                while ((in.read(b) + 1 & -2) != 0) {
//                    Log.d(TAG, "startConnection: " + new String(b));
//                }
                out.flush();
            } catch (IOException e) {
                Log.e(TAG, "startConnection: url error", e);
                e.printStackTrace();
                FAIL.run();
                return;
            } finally {
                urlConnection.disconnect();
            }
            Log.d(TAG, "startConnection: communication finished");
            CONNECTED = true;
            SUCCEED.run();
        }
    }

    static boolean CONNECTED;

    NetworkThread networkThread;
    public ContentPusher(Paper paper) {

        networkThread = new NetworkThread();
        networkThread.start();

        this.paper = paper;

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(200);
                    if (paper.getWidth() != 0) {
                        bitmap = Bitmap.createBitmap(paper.getWidth(), paper.getHeight(), Bitmap.Config.ARGB_8888);
                        canvas = new Canvas(bitmap);
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    public void connect(Runnable succeed, Runnable fail) {
        SUCCEED = succeed;
        FAIL = fail;
        SIGNAL = SIGCONN;
        synchronized (LOCK) {
            LOCK.notify();
        }
    }

    public void disconnect() {
        SIGNAL = SIGDISC;
        synchronized (LOCK) {
            LOCK.notify();
        }
    }

}
