package org.expressme.wireless.reader.fetcher;

import java.util.List;

/**
 * Adapter for FeedFetcherListener.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class FeedFetcherListenerAdapter implements FeedFetcherListener {

    public void onDiscovered(String url, List<String> feeds) {
    }

    public void onException(Exception e) {
    }

    public void onFetched(String feedUrl, String charset, byte[] content) {
    }

    public boolean onProgress(int percent) {
        return true;
    }

}
