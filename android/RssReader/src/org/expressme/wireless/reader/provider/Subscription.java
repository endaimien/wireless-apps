package org.expressme.wireless.reader.provider;

/**
 * Simple JavaBean that holds all fields of subscription.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class Subscription extends BriefSubscription {

    public String url;
    public String description;
    public long lastUpdated;
    public long frequency;

    public Subscription(long id, String title, String url, String description, long lastUpdated, long frequency) {
        super(id, title);
        this.url = url;
        this.description = description;
        this.lastUpdated = lastUpdated;
        this.frequency = frequency;
    }

}
