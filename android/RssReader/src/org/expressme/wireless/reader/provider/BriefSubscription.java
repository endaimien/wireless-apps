package org.expressme.wireless.reader.provider;

/**
 * Simple JavaBean that holds only 2 fields of subscription. Displayed in SubsActivity.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class BriefSubscription {

    public static final BriefSubscription ALL = new BriefSubscription((-1L), "All Subscriptions");

    public long id;
    public String title;

    public BriefSubscription(long id, String title) {
        this.id = id;
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
