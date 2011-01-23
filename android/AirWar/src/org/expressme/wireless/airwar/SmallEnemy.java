package org.expressme.wireless.airwar;

import org.expressme.wireless.game.GameSprite;

import android.graphics.Bitmap;

public class SmallEnemy extends GameSprite {

    public static final int ENEMY_SMALL_FRAMES_COUNT = 21;

    public static final int[] ENEMY_SMALL_0_FRAMES_FLYING = { 2, 0, 2, 1 };
    public static final int[] ENEMY_SMALL_1_FRAMES_FLYING = { 5, 3, 5, 4 };
    public static final int[] ENEMY_SMALL_2_FRAMES_FLYING = { 8, 6, 8, 7 };
    public static final int[] ENEMY_SMALL_3_FRAMES_FLYING = { 11, 9, 11, 10 };
    public static final int[] ENEMY_SMALL_4_FRAMES_FLYING = { 14, 12, 14, 13 };

    public static final int[][] ENEMY_SMALL_X_FRAMES_FLYING = {
        ENEMY_SMALL_0_FRAMES_FLYING,
        ENEMY_SMALL_1_FRAMES_FLYING,
        ENEMY_SMALL_2_FRAMES_FLYING,
        ENEMY_SMALL_3_FRAMES_FLYING,
        ENEMY_SMALL_4_FRAMES_FLYING
    };

    public SmallEnemy(Bitmap image) {
        super(image, ENEMY_SMALL_FRAMES_COUNT);
    }

}
