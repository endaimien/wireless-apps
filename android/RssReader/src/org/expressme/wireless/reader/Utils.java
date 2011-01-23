package org.expressme.wireless.reader;

/**
 * Utils methods.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class Utils {

    public static Log getLog(Class<?> clazz) {
        return new Log(clazz);
    }
}
