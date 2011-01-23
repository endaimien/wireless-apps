package org.expressme.wireless.game;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.graphics.Canvas;

/**
 * Manage sprites and draw all sprites.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class GameSpriteManager {

    private List<GameSprite> sprites = new LinkedList<GameSprite>();

    public GameSpriteManager() {
    }

    /**
     * Must be called in drawing-thread with synchronized(surfaceHolder) {}.
     */
    public void draw(Canvas canvas) {
        for (GameSprite sprite : sprites) {
            sprite.draw(canvas);
        }
    }

    /**
     * Return a read-only list.
     */
    @SuppressWarnings("unchecked")
    public <T extends GameSprite> List<T> getSprites() {
        return (List<T>) Collections.unmodifiableList(sprites);
    }

    public int getSpriteCount() {
        return sprites.size();
    }

    public void append(GameSprite sprite) {
        sprites.add(sprite);
    }

    public void insert(GameSprite sprite, int index) {
        sprites.add(index, sprite);
    }

    public void remove(GameSprite sprite) {
        sprites.remove(sprite);
    }

    /**
     * Remove all sprites that marked removable.
     */
    public void clean() {
        for (Iterator<GameSprite> it = sprites.iterator(); it.hasNext(); ) {
            if (it.next().isRemovable())
                it.remove();
        }
    }

    public void moveTo(GameSprite sprite, int index) {
        if (sprites.remove(sprite))
            sprites.add(0, sprite);
    }
}
