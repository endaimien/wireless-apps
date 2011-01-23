package org.expressme.wireless.airwar;

import org.expressme.wireless.game.GameLoopThread;
import org.expressme.wireless.game.GameView;
import org.expressme.wireless.game.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class AirView extends GameView {

    private AirLoopThread thread;
    private int surfaceWidth;
    private int halfSurfaceWidth;

    public AirView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected GameLoopThread createGameLoopThread(SurfaceHolder holder, Context context) {
        thread = new AirLoopThread(holder, context);
        return thread;
    }

    /**
     * Callback invoked when the surface dimensions change.
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Utils.log("AirView.surfaceChanged(): ", width, ", ", height);
        this.surfaceWidth = width;
        this.halfSurfaceWidth = width / 2;
        synchronized(holder) {
            thread.setSurfaceSize(width, height);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("EVENT", "onTouchEvent: " + event.getX() + ", " + event.getY());
        boolean moveLeft = event.getX()<this.halfSurfaceWidth;
        synchronized(thread.surfaceHolder) {
            int x = this.thread.player.getLocationX();
            int dest_x = moveLeft ? (x - Player.PLAYER_MOVE_STEP) : (x + Player.PLAYER_MOVE_STEP);
            if (dest_x<0)
                dest_x = 0;
            else if (dest_x>=this.surfaceWidth)
                dest_x = this.surfaceWidth - 1;
            this.thread.player.setMoveDestination(dest_x);
        }
        return true;
    }

}
