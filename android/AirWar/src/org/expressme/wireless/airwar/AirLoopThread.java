package org.expressme.wireless.airwar;

import java.util.List;

import org.expressme.wireless.game.GameLoopThread;
import org.expressme.wireless.game.GameSpriteManager;
import org.expressme.wireless.game.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class AirLoopThread extends GameLoopThread {

    GameSpriteManager enemyManager = new GameSpriteManager();
    GameSpriteManager bulletManager = new GameSpriteManager();
    Player player;
    int surface_width;
    int surface_height;

    private static final int TIME_GENERATE_ENEMY = 30;
    private int time_generate_enemy = TIME_GENERATE_ENEMY;

    public AirLoopThread(SurfaceHolder surfaceHolder, Context context) {
        super(surfaceHolder);
        ResourceHolder.init(context.getResources());
        this.player = new Player();
    }

    @Override
    public int getSurfaceWidth() {
        return this.surface_width;
    }

    @Override
    public int getSurfaceHeight() {
        return this.surface_height;
    }

    @Override
    public void setSurfaceSize(int width, int height) {
        this.surface_width = width;
        this.surface_height = height;
        Utils.log("AirLoopThread.setSurfaceSize: ", width, ", ", height);
        this.player.move(this.surface_width / 2, this.surface_height - this.player.getHeight());
        this.player.setMoveDestination(this.surface_width / 2);
    }

    public void setPlayerLocation(int x, int y) {
        this.player.move(x, y);
    }

    @Override
    protected void drawCanvas(Canvas canvas) {
        canvas.drawColor(BG_COLOR);
        this.enemyManager.draw(canvas);
        this.bulletManager.draw(canvas);
        this.player.draw(canvas);
    }

    @Override
    protected void ai() {
        // enemy:
        Enemy enemy = generateEnemy();
        if (enemy!=null) {
            enemy.move(Utils.nextRandomInt(this.surface_width), -enemy.getHeight()/2);
            enemyManager.append(enemy);
        }
        List<Enemy> enemyList = enemyManager.getSprites();
        for (Enemy e : enemyList) {
            e.moveDown();
            if (e.getGameState()==Enemy.ENEMY_STATE_CRASHING)
                e.countDownCrashingTime();
            else if (e.isOutOfRect(0, 0, this.surface_width, this.surface_height))
                e.setRemovable();
        }
        // bullet:
        List<Bullet> bulletList = bulletManager.getSprites();
        for (Bullet b : bulletList) {
            b.moveOn();
            if (b.isOutOfRect(0, 0, this.surface_width, this.surface_height))
                b.setRemovable();
        }
        // player shot:
        Bullet bullet = player.shot();
        if (bullet!=null)
            bulletManager.append(bullet);
        switch (player.getGameState()) {
        case Player.PLAYER_STATE_FLYING:
            // detect collision:
            player.moveIfNecessary();
            detectCollisions();
            break;
        case Player.PLAYER_STATE_SHIELD:
            player.moveIfNecessary();
            player.countDownShieldTime();
            break;
        case Player.PLAYER_STATE_CRASHING:
            player.countDownCrashingTime();
            break;
        case Player.PLAYER_STATE_OVER:
            // show game over!
            break;
        }
        enemyManager.clean();
        bulletManager.clean();
    }

    private void detectCollisions() {
        List<Bullet> bulletList = bulletManager.getSprites();
        List<Enemy> enemyList = enemyManager.getSprites();
        for (Bullet b : bulletList) {
            if (b.isFromPlayer) {
                for (Enemy e : enemyList) {
                    if (b.collidesWith(e)) {
                        Utils.log("AirLoopThread.detectCollisions: collided!");
                        e.switchToCrashingState();
                        b.setVisible(false);
                        b.setRemovable();
                    }
                }
            }
        }
    }

    // draw background /////////////////////////////////////////////////////////

    private static final int BG_COLOR = 0xff0040c0;

    // create enemy:
    private Enemy generateEnemy() {
        time_generate_enemy --;
        if (time_generate_enemy<=0) {
            time_generate_enemy = TIME_GENERATE_ENEMY;
            if (Utils.nextRandomInt() % 5==0) {
                Utils.log("AirLoopThread.generateEnemy() generate medium enemy!");
                return new Enemy(
                        ResourceHolder.ENEMY_TYPE_MEDIUM,
                        Enemy.ENEMY_MEDIUM_FRAMES_COUNT,
                        Enemy.ENEMY_MEDIUM_FRAMES_FLYING,
                        Enemy.ENEMY_MEDIUM_FRAMES_CRASHING
                );
            }
            Utils.log("AirLoopThread.generateEnemy() generate small enemy!");
            int smallEnemyType = Utils.nextRandomInt(Enemy.ENEMY_SMALL_TYPES);
            return new Enemy(
                    ResourceHolder.ENEMY_TYPE_SMALL,
                    Enemy.ENEMY_SMALL_FRAMES_COUNT,
                    Enemy.ENEMY_SMALL_FRAMES_FLYING[smallEnemyType],
                    Enemy.ENEMY_SMALL_FRAMES_CRASHING
            );
        }
        return null;
    }

    private static final float MIN_SENSOR_X = -2.5f;
    private static final float MAX_SENSOR_X = 0 - MIN_SENSOR_X;

    public void sensorChanged(float x) {
        if (x < MIN_SENSOR_X)
            x = MIN_SENSOR_X;
        else if (x > MAX_SENSOR_X)
            x = MAX_SENSOR_X;
        int half = this.surface_width / 2;
        int new_x = (int) ((half * x) / MAX_SENSOR_X + half);
        player.setMoveDestination(new_x);
    }

}
