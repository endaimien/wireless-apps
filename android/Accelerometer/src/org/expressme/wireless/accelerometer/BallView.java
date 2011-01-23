package org.expressme.wireless.accelerometer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class BallView extends ImageView {

    public BallView(Context context) {
        super(context);
    }

    public BallView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BallView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void moveTo(int l, int t) {
        super.setFrame(l, t, l + getWidth(), t + getHeight());
    }

}
