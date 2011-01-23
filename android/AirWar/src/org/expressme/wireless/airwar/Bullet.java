package org.expressme.wireless.airwar;

import org.expressme.wireless.game.GameSprite;
import org.expressme.wireless.game.Utils;

public class Bullet extends GameSprite {

    private static final int SPEED = 6;

    public final boolean isFromPlayer;
    private final double sin;
    private final double cos;

    public Bullet(int x, int y, double angle, boolean isFromPlayer) {
        super(ResourceHolder.bullet, 2, isFromPlayer ? new int[] { 0 } : new int[] { 1 });
        setRefPixel(getWidth() / 2, getHeight() / 2);
        move(x, y);
        this.isFromPlayer = isFromPlayer;
        this.sin = isFromPlayer ? 0 : Math.sin(angle);
        this.cos = isFromPlayer ? 0 : Math.cos(angle);
        Utils.log("Bullet.constructed at (", x, ", ", y, ")");
    }

    public void moveOn() {
        if (isFromPlayer) {
            move(getLocationX(), getLocationY() - SPEED);
            return;
        }
        int offset_x = (int) (SPEED * sin + 0.5);
        int offset_y = (int) (SPEED * cos + 0.5);
        move(getLocationX() + offset_x, getLocationY() + offset_y);
    }

}
