package org.expressme.wireless.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceHolder;

/**
 * Background thread for drawing.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public abstract class GameLoopThread extends Thread {

    public static final int DEFAULT_FRAME_TIME = 100;
    public static final int DEFAULT_FRAME_TIME_HALF = 50;

    public final SurfaceHolder surfaceHolder;

    private volatile boolean running = true;
    private int frameTime = DEFAULT_FRAME_TIME;
    private boolean focus = false;

    public GameLoopThread(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
    }

    /**
     * Set frame time in milli-seconds. The less, the more resources game uses.
     */
    public final void setFrameTime(int milliSeconds) {
        if (milliSeconds<=0)
            throw new GameException("Cannot set frame time to non-positive value.");
        this.frameTime = milliSeconds;
    }

    @Override
    public final void run() {
        Utils.log("GameLoopThread.run(): start...");
        try {
            runInternal();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runInternal() {
        while (running) {
            long current = System.currentTimeMillis();
            if (focus) {
                execute();
            }
            long sleepTime = frameTime + current - System.currentTimeMillis();
            if (sleepTime>DEFAULT_FRAME_TIME_HALF)
                System.gc();
            sleepTime = frameTime + current - System.currentTimeMillis();
            if (sleepTime>0) {
                try {
                    sleep(sleepTime);
                }
                catch (InterruptedException e) {
                }
            }
            else {
                // WARNING: game is taking 100% system resources...
                Utils.log("GameLoopThread.WARNING: game is slow, not enough system resources!");
            }
        }
        Utils.log("GameLoopThread.run(): end...");
    }

    /**
     * Game logic
     */
    private void execute() {
        Utils.log("GameLoopThread.execute()");
        Utils.log("GameLoopThread.ai() start...");
        long start = System.currentTimeMillis();
        ai();
        long ai_last = System.currentTimeMillis() - start;
        Utils.log("GameLoopThread.ai() execute ", ai_last);
        Canvas canvas = null;
        try {
            canvas = surfaceHolder.lockCanvas(null);
            synchronized (surfaceHolder) {
                Utils.log("GameLoopThread.drawCanvas() start...");
                start = System.currentTimeMillis();
                drawCanvas(canvas);
                // TODO: draw the performance bar???
                /*
                long draw_last = System.currentTimeMillis() - start;
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(ai_last<=100 ? Color.GREEN : Color.RED);
                canvas.drawRect(new Rect(0, 0, (int) ai_last, 8), paint);
                paint.setColor(draw_last<=100 ? Color.GREEN : Color.RED);
                canvas.drawRect(new Rect(0, 10, (int) draw_last, 18), paint);
                */
                Utils.log("GameLoopThread.drawCanvas() end.");
            }
        }
        finally {
            // do this in a finally so that if an exception is thrown
            // during the above, we don't leave the Surface in an
            // inconsistent state
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    /**
     * Do artificial intelligence.
     */
    protected abstract void ai();

    /**
     * Draw canvas in the loop thread.
     */
    protected abstract void drawCanvas(Canvas canvas);

    public void requestStop() {
        Utils.log("GameLoopThread.requestStop()");
        this.running = false;
    }

    public void requestAndWaitUtilStop() {
        Utils.log("GameLoopThread.requestAndWaitUtilStop() start...");
        this.running = false;
        boolean retry = true;
        while (retry) {
            try {
                this.join();
                retry = false;
            }
            catch (InterruptedException e) {
            }
        }
        Utils.log("GameLoopThread.requestAndWaitUtilStop() ok.");
    }

    /**
     * Start or resume the game after gain focus.
     */
    public void doStart() {
        focus = true;
    }

    /**
     * Pause the game after lose focus.
     */
    public void doPause() {
        focus = false;
    }

    public abstract int getSurfaceWidth();

    public abstract int getSurfaceHeight();

    public abstract void setSurfaceSize(int width, int height);
}
