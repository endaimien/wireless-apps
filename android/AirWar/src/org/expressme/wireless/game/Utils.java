package org.expressme.wireless.game;

import java.util.Random;

import android.util.Log;

/**
 * Utils methods.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class Utils {

    private static final String TAG_GAME = "GAME";

    private static final Random RANDOM = new Random();

    /**
     * Generate random integer between 0(include) and max(exclude).
     */
    public static int nextRandomInt(int max) {
        int n = RANDOM.nextInt();
        return (n<0 ? -n : n) % max;
    }

    /**
     * Generate random integer between 0(include) and Integer.MAX_VALUE.
     */
    public static int nextRandomInt() {
        int n = RANDOM.nextInt();
        return n<0 ? -n : n;
    }

    public static void log(Object... objs) {
        if (objs.length==0) {
            Log.i(TAG_GAME, "");
            return;
        }
        if (objs.length==1) {
            Object o = objs[0];
            if (o==null) {
                Log.i(TAG_GAME, "(null)");
            }
            else {
                String s = o.toString();
                int n = s.indexOf('.');
                Log.i(n==(-1) ? TAG_GAME : s.substring(0, n), s);
            }
            return;
        }
        StringBuilder sb = new StringBuilder(128);
        for (Object o : objs) {
            sb.append(o==null ? "(null)" : o.toString());
        }
        String s = sb.toString();
        int n = s.indexOf('.');
        Log.i(n==(-1) ? TAG_GAME : s.substring(0, n), s);
    }
}
