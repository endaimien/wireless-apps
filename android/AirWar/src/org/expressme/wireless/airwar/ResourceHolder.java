package org.expressme.wireless.airwar;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ResourceHolder {

    public static final int ENEMY_TYPE_SMALL = 0;
    public static final int ENEMY_TYPE_MEDIUM = 1;

    public static Bitmap player = null;
    public static Bitmap bullet = null;
    public static Bitmap[] enemyTypes = null;

    public static void init(Resources resources) {
        player = BitmapFactory.decodeResource(resources, R.drawable.player);
        bullet = BitmapFactory.decodeResource(resources, R.drawable.bullets);
        enemyTypes = new Bitmap[] { 
                BitmapFactory.decodeResource(resources, R.drawable.enemy_small),
                BitmapFactory.decodeResource(resources, R.drawable.enemy_medium)
        };
    }
}
