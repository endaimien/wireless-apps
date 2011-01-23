package org.expressme.wireless.reader.parser;

/**
 * Listener for XML parsing.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public interface FeedParserListener {

    void onFeedTitleLoad(String feedTitle);

    void onFeedDescriptionLoad(String feedDescription);

    void onItemLoad(FeedItem item);
}
