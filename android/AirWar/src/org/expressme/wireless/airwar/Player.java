package org.expressme.wireless.airwar;

import org.expressme.wireless.game.GameSprite;
import org.expressme.wireless.game.Utils;

/**
 * Holds player sprite.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class Player extends GameSprite {

    public static final int PLAYER_MOVE_STEP = 20;
    public static final int PLAYER_MOVE_LITTLE_STEP = 4;

    public static final int PLAYER_FRAMES_COUNT = 11;

    /**
     * Player is under protected with shield.
     */
    public static final int PLAYER_STATE_SHIELD = 0;

    public static final int[] PLAYER_FRAMES_SHIELD = { 2, 3 };

    /**
     * Player is beat on the battle.
     */
    public static final int PLAYER_STATE_FLYING = 1;

    public static final int[] PLAYER_FRAMES_FLYING = { 2, 0, 2, 1 };

    /**
     * Player is crashing.
     */
    public static final int PLAYER_STATE_CRASHING = 2;

    public static final int[] PLAYER_FRAMES_CRASHING = { 4, 5, 6, 7, 8, 9, 10};

    /**
     * Player is game over.
     */
    public static final int PLAYER_STATE_OVER = 3;

    private static final int TIME_SHOT_MAX = 5;
    private static final int TIME_SHIELD_MAX = 6;
    private static final int TIME_CRASH_MAX = PLAYER_FRAMES_CRASHING.length;

    private int move_destination_x;
    private int time_shot_count = TIME_SHOT_MAX;
    private int time_shield_count = 0;
    private int time_crashing_count = 0;

    public Player() {
        super(ResourceHolder.player, PLAYER_FRAMES_COUNT, PLAYER_FRAMES_SHIELD);
        setRefPixel(getWidth() / 2, getHeight() / 2);
        switchToShieldState();
    }

    public void switchToShieldState() {
        Utils.log("Player.switchToShieldState()");
        setGameState(PLAYER_STATE_SHIELD);
        setFrameSequence(PLAYER_FRAMES_SHIELD);
        setVisible(true);
        this.time_shield_count = TIME_SHIELD_MAX;
    }

    public void countDownShieldTime() {
        this.time_shield_count --;
        if (this.time_shield_count<=0)
            switchToFlyingState();
    }

    private void switchToFlyingState() {
        setGameState(PLAYER_STATE_FLYING);
        setFrameSequence(PLAYER_FRAMES_FLYING);
    }

    public void switchToCrashingState() {
        setGameState(PLAYER_STATE_CRASHING);
        setFrameSequence(PLAYER_FRAMES_CRASHING);
        this.time_crashing_count = TIME_CRASH_MAX;
    }

    public Bullet shot() {
        int gameState = getGameState();
        if (gameState<=PLAYER_STATE_FLYING) {
            this.time_shot_count--;
            if (this.time_shot_count<=0) {
                this.time_shot_count = TIME_SHOT_MAX;
                return new Bullet(
                        getLocationX(), getAbsoluteY(),
                        0d,
                        true
                );
            }
        }
        return null;
    }
    public void countDownCrashingTime() {
        this.time_crashing_count --;
        if (this.time_crashing_count<=0)
            switchToOverState();
    }

    private void switchToOverState() {
        setVisible(false);
    }

    public void setMoveDestination(int x) {
        this.move_destination_x = x;
        Utils.log("Player.setMoveDestination:", x);
    }

    public void moveIfNecessary() {
        int location_x = getLocationX();
        int location_y = getLocationY();
        if (location_x < this.move_destination_x) {
            // move right:
            int offset = this.move_destination_x - location_x;
            if (offset>PLAYER_MOVE_LITTLE_STEP)
                offset = PLAYER_MOVE_LITTLE_STEP;
            Utils.log("Player.moveIfNecessary >> ", offset);
            move(location_x + offset, location_y);
        }
        else if (location_x > this.move_destination_x) {
            // move left:
            int offset = location_x - this.move_destination_x;
            if (offset>PLAYER_MOVE_LITTLE_STEP)
                offset = PLAYER_MOVE_LITTLE_STEP;
            Utils.log("Player.moveIfNecessary << ", offset);
            move(location_x - offset, location_y);
        }
    }

    @Override
    public boolean canCollide() {
        return getGameState()==Player.PLAYER_STATE_FLYING;
    }

}
