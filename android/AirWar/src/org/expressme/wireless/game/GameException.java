package org.expressme.wireless.game;

/**
 * Exception when game goes wrong.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class GameException extends RuntimeException {

    private static final long serialVersionUID = 7200253244601329387L;

    public GameException() {
    }

    public GameException(String detailMessage) {
        super(detailMessage);
    }

    public GameException(Throwable throwable) {
        super(throwable);
    }

    public GameException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

}
