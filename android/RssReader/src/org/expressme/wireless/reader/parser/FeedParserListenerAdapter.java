package org.expressme.wireless.reader.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for FeedParserListener.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class FeedParserListenerAdapter implements FeedParserListener {

    private String feedTitle;
    private String feedDescription;
    private List<FeedItem> items = new ArrayList<FeedItem>();

    public String getFeedTitle() {
        return feedTitle;
    }

    public String getFeedDescription() {
        return feedDescription;
    }

    public FeedItem[] getFeedItems() {
        return items.toArray(new FeedItem[items.size()]);
    }

    public void onFeedDescriptionLoad(String feedDescription) {
        this.feedDescription = feedDescription;
    }

    public void onFeedTitleLoad(String feedTitle) {
        this.feedTitle = feedTitle;
    }

    public void onItemLoad(FeedItem item) {
        items.add(item);
    }
}
