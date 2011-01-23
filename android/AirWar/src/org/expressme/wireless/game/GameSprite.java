package org.expressme.wireless.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * Sprite class that can hold many frames and create animation.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class GameSprite {

    private final Bitmap image;
    private final Rect[] frameRects;
    private final int width;
    private final int height;

    private int frameIndex = 0;
    private int[] frameSequence;

    private int reference_x = 0;
    private int reference_y = 0;
    private int location_x;
    private int location_y;
    private int gameState = 0;
    private boolean visible = true;
    private boolean removable = false;

    public GameSprite(Bitmap image) {
        this(image, 1, null);
    }

    public GameSprite(Bitmap image, final int splitToFrames) {
        this(image, splitToFrames, null);
    }

    public GameSprite(Bitmap image, final int splitToFrames, int[] sequence) {
        if (image==null)
            throw new NullPointerException("Image is null.");
        this.image = image;
        if (splitToFrames<1)
            throw new GameException("Must split to 1 or more.");
        // get width, height and frame count:
        final int w = image.getWidth();
        final int h = image.getHeight();
        if (w % splitToFrames != 0)
            throw new GameException("Could not split image (width: " + w + ") to " + splitToFrames + " parts.");
        this.width = w / splitToFrames;
        this.height = h;
        Utils.log("GameSprite.image loaded: ", w, "x", h, ", for ", splitToFrames, " parts: ", this.width, "x", this.height);
        // init rects:
        this.frameRects = new Rect[splitToFrames];
        int left = 0;
        for (int i=0; i<splitToFrames; i++) {
            this.frameRects[i] = new Rect(left, 0, left + this.width, h);
            Utils.log("GameSprite() init frameRects[", i, "] ", this.frameRects[i]);
            left += this.width;
        }
        // init sequence:
        if (sequence==null) {
            sequence = defaultSequence();
        }
        setFrameSequence(sequence);
    }

    /**
     * Set the frame sequence for this Sprite.<br/>
     * 
     * All Sprites have a default sequence that displays the Sprites frames in 
     * order. This method allows for the creation of an arbitrary sequence using 
     * the available frames. The current index in the frame sequence is reset to 
     * zero as a result of calling this method.<br/>
     * 
     * The contents of the sequence array are copied when this method is called; 
     * thus, any changes made to the array after this method returns have no 
     * effect on the Sprite's frame sequence.<br/>
     * 
     * Passing in null causes the Sprite to revert to the default frame sequence.
     */
    public final void setFrameSequence(int[] sequence) {
        Utils.log("GameSprite.setFrameSequence()");
        if (sequence==null)
            sequence = defaultSequence();
        else if (sequence.length==0)
            throw new GameException("Sequence must contain at least one element.");
        for (int seq : sequence)
            if (seq<0 || seq>=this.frameRects.length)
                throw new GameException("Sequence value out of range: " + seq);
        this.frameSequence = sequence;
        this.frameIndex = 0;
    }

    private int[] defaultSequence() {
        int[] sequence = new int[this.frameRects.length];
        for (int i=0; i<this.frameRects.length; i++)
            sequence[i] = i;
        return sequence;
    }

    public final int getFrameSequenceLength() {
        return this.frameSequence.length;
    }

    /**
     * Detect if collides with another sprite by rectangle.
     */
    public boolean collidesWith(GameSprite other) {
        if (!this.visible || !other.visible || !canCollide() || !other.canCollide())
            return false;

        int r1x1 = this.getAbsoluteX();
        int r2x1 = other.getAbsoluteX();

        int r1y1 = this.getAbsoluteY();
        int r2y1 = other.getAbsoluteY();

        int r1x2 = r1x1 + this.getWidth();
        int r2x2 = r2x1 + other.getWidth();

        int r1y2 = r1y1 + this.getHeight();
        int r2y2 = r2y1 + other.getHeight();

        return intersectRect(r1x1, r1y1, r1x2, r1y2, r2x1, r2y1, r2x2, r2y2);
    }

    /**
     * Defines the reference pixel for this Sprite. The pixel is defined by its 
     * location relative to the upper-left corner of the Sprite's un-transformed 
     * frame, and it may lay outside of the frame's bounds.
     */
    public final void setRefPixel(int x, int y) {
        Utils.log("GameSprite.set ref location(", x, ", ", y, ")");
        this.reference_x = x;
        this.reference_y = y;
    }

    /**
     * Get the reference definition of x of sprite.
     */
    public final int getRefPixelX() {
        return this.reference_x;
    }

    /**
     * Get the reference definition of y of sprite.
     */
    public final int getRefPixelY() {
        return this.reference_y;
    }

    /**
     * Get the absolute location of sprite.
     */
    public final int getAbsoluteX() {
        return this.location_x - this.reference_x;
    }

    /**
     * Get the absolute location of sprite.
     */
    public final int getAbsoluteY() {
        return this.location_y - this.reference_y;
    }

    public final void draw(Canvas canvas) {
        if (!this.visible)
            return;
        int left = getAbsoluteX();
        int top = getAbsoluteY();
        Utils.log("GameSprite.draw() from (left=", left, ", top=", top, ")");
        Rect destRect = new Rect(
                left,
                top,
                left + this.width,
                top + this.height
        );
        Rect sourceRect = this.frameRects[this.frameSequence[this.frameIndex]];
        Utils.log("GameSprite.draw() from ", sourceRect, " to ", destRect);
        canvas.drawBitmap(this.image, sourceRect, destRect, null);
        nextFrame();
    }

    public final int getWidth() {
        return this.width;
    }

    public final int getHeight() {
        return this.height;
    }

    /**
     * Get the location of sprite.
     */
    public final int getLocationX() {
        return this.location_x;
    }

    /**
     * Get the location of sprite.
     */
    public final int getLocationY() {
        return this.location_y;
    }

    /**
     * Move to location (x, y).
     */
    public final void move(int x, int y) {
        Utils.log("GameSprite.move(", x, ", ", y, ")");
        this.location_x = x;
        this.location_y = y;
    }

    public final int getGameState() {
        return gameState;
    }

    public final void setGameState(int gameState) {
        Utils.log("GameSprite.setGameState(", gameState, ")");
        this.gameState = gameState;
    }

    private void nextFrame() {
        if (frameIndex<(this.frameSequence.length-1))
            frameIndex ++;
        else
            frameIndex = 0;
        Utils.log("GameSprite.nextFrame[", frameIndex, "]=", frameSequence[frameIndex]);
    }

    /**
     * Detect if the sprite is out of rectangle.
     */
    public final boolean isOutOfRect(int left, int top, int width, int height) {
        int r1x1 = this.getAbsoluteX();
        int r1y1 = this.getAbsoluteY();
        int r1x2 = r1x1 + this.getWidth();
        int r1y2 = r1y1 + this.getHeight();
        return ! intersectRect(r1x1, r1y1, r1x2, r1y2, left, top, left+width, top+height);
    }

    public final void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Set sprite removable so the GameSpriteManager can safely remove it.
     */
    public final void setRemovable() {
        this.removable = true;
    }

    public final boolean isRemovable() {
        return this.removable;
    }

    /**
     * If false, collidesWith() always return false. Subclass needs override 
     * this method for game logic. e.g., return false when sprite is invisible, 
     * exploding, etc.
     * 
     * @return True if sprite is in normal state that can do 'collidesWith()'.
     */
    public boolean canCollide() {
        return true;
    }

    /**
     * Detect rectangle intersection.
     * 
     * @param r1x1 left co-ordinate of first rectangle
     * @param r1y1 top co-ordinate of first rectangle
     * @param r1x2 right co-ordinate of first rectangle
     * @param r1y2 bottom co-ordinate of first rectangle
     * @param r2x1 left co-ordinate of second rectangle
     * @param r2y1 top co-ordinate of second rectangle
     * @param r2x2 right co-ordinate of second rectangle
     * @param r2y2 bottom co-ordinate of second rectangle
     * @return True if there is rectangle intersection
     */
    private boolean intersectRect(int r1x1, int r1y1, int r1x2, int r1y2, 
            int r2x1, int r2y1, int r2x2, int r2y2)
    {
        if (r2x1 >= r1x2 || r2y1 >= r1y2 || r2x2 <= r1x1 || r2y2 <= r1y1) {
            return false;
        }
        else {
            return true;
        }
    }
}
