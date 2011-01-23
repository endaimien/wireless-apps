package org.expressme.wireless.airwar;

import org.expressme.wireless.game.GameSprite;
import org.expressme.wireless.game.Utils;

public class Enemy extends GameSprite {

    /**
     * Enemy is beat on the battle.
     */
    public static final int ENEMY_STATE_FLYING = 0;

    /**
     * Enemy is crashing.
     */
    public static final int ENEMY_STATE_CRASHING = 1;

    /**
     * Enemy is flying out of screen.
     */
    public static final int ENEMY_STATE_END = 2;

    /**
     * Medium enemy frames.
     */
    public static final int ENEMY_MEDIUM_FRAMES_COUNT = 10;

    /**
     * Medium enemy flying sequence.
     */
    public static final int[] ENEMY_MEDIUM_FRAMES_FLYING = { 2, 0, 2, 1 };

    /**
     * Medium enemy crashing sequence.
     */
    public static final int[] ENEMY_MEDIUM_FRAMES_CRASHING = { 3, 4, 5, 6, 7, 8, 9 };

    /**
     * Small enemy frame count.
     */
    public static final int ENEMY_SMALL_FRAMES_COUNT = 21;

    /**
     * Small enemy 0, 1, 2, 3, 4 flying sequence.
     */
    public static final int[][] ENEMY_SMALL_FRAMES_FLYING = {
        { 2, 0, 2, 1 },
        { 5, 3, 5, 4 },
        { 8, 6, 8, 7 },
        { 11, 9, 11, 10 },
        { 14, 12, 14, 13 }
    };

    /**
     * How many types of small enemy.
     */
    public static final int ENEMY_SMALL_TYPES = ENEMY_SMALL_FRAMES_FLYING.length;

    /**
     * Small enemy crashing sequence.
     */
    public static final int[] ENEMY_SMALL_FRAMES_CRASHING = { 15, 16, 17, 18, 19, 20 };

    private static final int MOVE_DOWN_STEP = 2;

    private final int[] crashingSequence;
    private int time_crashing_count;

    public Enemy(int enemyType, int splitToFrames, int[] flyingSequence, int[] crashingSequence) {
        super(ResourceHolder.enemyTypes[enemyType], splitToFrames, flyingSequence);
        setRefPixel(getWidth() / 2, getHeight() / 2);
        this.crashingSequence = crashingSequence;
    }

    public void switchToCrashingState() {
        Utils.log("Enemy.switchToCrashingState");
        setGameState(ENEMY_STATE_CRASHING);
        setFrameSequence(this.crashingSequence);
        time_crashing_count = this.crashingSequence.length;
    }

    public void countDownCrashingTime() {
        time_crashing_count --;
        if (time_crashing_count<=0) {
            switchToEndState();
        }
    }

    public void switchToEndState() {
        Utils.log("Enemy.switchToEndState");
        setGameState(ENEMY_STATE_END);
        setVisible(false);
        setRemovable();
    }

    public void moveDown() {
        move(getLocationX(), getLocationY() + MOVE_DOWN_STEP);
    }

    @Override
    public boolean canCollide() {
        return getGameState()==Enemy.ENEMY_STATE_FLYING;
    }

}
