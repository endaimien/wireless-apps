package org.expressme.wireless.game;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * View that draws, takes keystrokes, etc. for a simple 2D game.
 */
public abstract class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private final SurfaceHolder surfaceHolder;

    /** The thread that actually draws the animation */
    private GameLoopThread thread;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        Utils.log("GameView.constructor begin...");
        // register our interest in hearing about changes to our surface
        this.surfaceHolder = getHolder();
        this.surfaceHolder.addCallback(this);
        setFocusable(true); // make sure we get key events

        // create thread only; it's started in surfaceCreated()
        thread = createGameLoopThread(this.surfaceHolder, context);
        Utils.log("GameView.construct ok.");
    }

    protected abstract GameLoopThread createGameLoopThread(SurfaceHolder holder, Context context);

    /**
     * Fetches the animation thread corresponding to this GameView.
     * 
     * @return the animation thread
     */
    @SuppressWarnings("unchecked")
    public <T extends GameLoopThread> T getGameLoopThread() {
        return (T) thread;
    }

    /**
     * Standard window-focus override. Notice focus lost so we can pause on
     * focus lost. e.g. user switches to take a call.
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (hasWindowFocus) {
            Utils.log("GameView.onWindowFocusChanged: true");
            thread.doStart();
        }
        else {
            Utils.log("GameView.onWindowFocusChanged: false");
            thread.doPause();
        }
    }

    /**
     * Callback invoked when the Surface has been created and is ready to be used.
     */
    public void surfaceCreated(SurfaceHolder holder) {
        Utils.log("GameView.surfaceCreated(), starting loop thread...");
        thread.start();
    }

    /**
     * Callback invoked when the Surface has been destroyed and must no longer 
     * be touched. WARNING: after this method returns, the Surface/Canvas must 
     * never be touched again!
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        Utils.log("GameView.surfaceDestroyed()");
        thread.requestAndWaitUtilStop();
    }
}
