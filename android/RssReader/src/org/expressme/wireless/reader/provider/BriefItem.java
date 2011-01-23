package org.expressme.wireless.reader.provider;

/**
 * Simple JavaBean for feed item, with only 3 fields. Display in MainActivity.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class BriefItem {

    public long id;
    public String title;
    public boolean unread;

    public BriefItem(long id, String title, boolean unread) {
        this.id = id;
        this.title = title;
        this.unread = unread;
    }

    @Override
    public String toString() {
        return title;
    }
}
