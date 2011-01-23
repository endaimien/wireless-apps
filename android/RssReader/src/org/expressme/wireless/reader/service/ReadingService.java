package org.expressme.wireless.reader.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.expressme.wireless.reader.Log;
import org.expressme.wireless.reader.Utils;
import org.expressme.wireless.reader.fetcher.FeedFetcher;
import org.expressme.wireless.reader.fetcher.FeedFetcherListenerAdapter;
import org.expressme.wireless.reader.parser.FeedItem;
import org.expressme.wireless.reader.parser.FeedParser;
import org.expressme.wireless.reader.parser.FeedParserListenerAdapter;
import org.expressme.wireless.reader.provider.BriefItem;
import org.expressme.wireless.reader.provider.BriefSubscription;
import org.expressme.wireless.reader.provider.ItemColumns;
import org.expressme.wireless.reader.provider.Subscription;
import org.expressme.wireless.reader.provider.SubscriptionColumns;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

/**
 * Service component for all business logic and background work.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class ReadingService extends Service {

    private static final int MSG_TIMER = 0;

    private static final String PREF_NAME = "ReadingPref";
    private static final String PREF_UNREAD_ONLY = "UnreadOnly";
    private static final String PREF_FREQ = "Freq";
    private static final String PREF_EXPIRES = "Expires";

    public static final int REQUEST_CODE_PREF_UNCHANGED = 1;
    public static final int REQUEST_CODE_PREF_CHANGED = 2;

    public static final int FREQ_MAX = 10;
    public static final int FREQ_DEFAULT = 3;

    public static final int EXPIRES_MAX = 10;
    public static final int EXPIRES_DEFAULT = 3;

    public static final String NOTIFY_NEW_ITEMS = ReadingService.class.getName() + ".NOTIFY_NEW_ITEMS";
    public static final String NOTIFY_PREF_CHANGED = ReadingService.class.getName() + ".NOTIFY_PREF_CHANGED";
    public static final String NOTIFY_SUB_REMOVED = ReadingService.class.getName() + ".NOTIFY_SUB_REMOVED";

    private final Log log = Utils.getLog(getClass());

    private long ONE_MINUTE = 60L * 1000L;
    private long ONE_WEEK = 7L * 24L * 60L * ONE_MINUTE;
    private long delayed = FREQ_DEFAULT * ONE_MINUTE;
    private long expires = EXPIRES_DEFAULT * ONE_WEEK;

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_TIMER:
                log.info("Message: MSG_TIMER");
                removeExpires();
                refreshFeeds();
                break;
            }
        }
    };

    public boolean getPreferenceOfUnreadOnly() {
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return pref.getBoolean(PREF_UNREAD_ONLY, true);
    }

    public int getPreferenceOfFreq() {
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int frequency = pref.getInt(PREF_FREQ, FREQ_DEFAULT);
        if (frequency<1 || frequency>FREQ_MAX)
            frequency = FREQ_DEFAULT;
        return frequency;
    }

    public int getPreferenceOfExpires() {
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int expires = pref.getInt(PREF_EXPIRES, EXPIRES_DEFAULT);
        if (expires<1 || expires>EXPIRES_MAX)
            expires = EXPIRES_DEFAULT;
        return expires;
    }

    public void storePreferences(boolean unreadOnly, int freq, int expires) {
        if (freq<1 || freq>FREQ_MAX)
            freq = FREQ_DEFAULT;
        if (expires<1 || expires>EXPIRES_MAX)
            expires = EXPIRES_DEFAULT;
        SharedPreferences pref = this.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(PREF_UNREAD_ONLY, unreadOnly);
        editor.putInt(PREF_FREQ, freq);
        editor.putInt(PREF_EXPIRES, expires);
        editor.commit();
        // broadcast:
        Intent intent = new Intent(NOTIFY_PREF_CHANGED);
        sendBroadcast(intent);
        this.delayed = freq * ONE_MINUTE;
        this.expires = expires * ONE_WEEK;
    }

    public List<BriefSubscription> queryBriefSubscriptions() {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(SubscriptionColumns.URI, new String[] { SubscriptionColumns._ID, SubscriptionColumns.TITLE }, null, null, SubscriptionColumns.TITLE);
            if (cursor.moveToFirst()) {
                List<BriefSubscription> list = new ArrayList<BriefSubscription>(cursor.getCount());
                int col_id = cursor.getColumnIndex(SubscriptionColumns._ID);
                int col_title = cursor.getColumnIndex(SubscriptionColumns.TITLE);
                do {
                    long id = cursor.getLong(col_id);
                    String title = cursor.getString(col_title);
                    list.add(new BriefSubscription(id, title));
                }
                while (cursor.moveToNext());
                return list;
            }
            else {
                return Collections.emptyList();
            }
        }
        finally {
            close(cursor);
        }
    }

    /**
     * Query items by subscription id and unread condition. If no items found, 
     * an empty list will returned.
     * 
     * @param sub_id Subscription id, or (-1) for all subscriptions.
     * @param unreadOnly True if query unread items only, false if query all items.
     * @return List of BriefItem objects.
     */
    public List<BriefItem> queryBriefItems(long sub_id, boolean unreadOnly) {
        String selection = sub_id>=0 ? ItemColumns.SUBS_ID + "=" + sub_id : null;
        if (unreadOnly) {
            if (selection==null)
                selection = ItemColumns.UNREAD + ">0";
            else
                selection = selection + " AND " + ItemColumns.UNREAD + "=0";
        }
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                    ItemColumns.URI,
                    new String[] { ItemColumns._ID, ItemColumns.TITLE, ItemColumns.UNREAD },
                    selection,
                    null,
                    ItemColumns.PUBLISHED + " desc"
            );
            if (cursor.moveToFirst()) {
                List<BriefItem> list = new ArrayList<BriefItem>(cursor.getCount());
                int col_id = cursor.getColumnIndex(ItemColumns._ID);
                int col_title = cursor.getColumnIndex(ItemColumns.TITLE);
                int col_unread = cursor.getColumnIndex(ItemColumns.UNREAD);
                do {
                    long id = cursor.getLong(col_id);
                    String title = cursor.getString(col_title);
                    boolean unread = cursor.getInt(col_unread)!=0;
                    list.add(new BriefItem(id, title, unread));
                }
                while (cursor.moveToNext());
                return list;
            }
            else {
                return Collections.emptyList();
            }
        }
        finally {
            close(cursor);
        }
    }

    private void removeExpires() {
        long expiredTime = System.currentTimeMillis() - this.expires;
        try {
            String where = ItemColumns.PUBLISHED + "<" + expiredTime + " AND " + ItemColumns.UNREAD + "=0";
            getContentResolver().delete(ItemColumns.URI, where, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Refresh the feeds in background thread.
     */
    private void refreshFeeds() {
        final String url = findSubscriptionUrlByFreq();
        if (url==null) {
            triggerNextTimer();
            return;
        }
        new Thread() {
            public void run() {
                FeedFetcher fetcher = new FeedFetcher();
                fetcher.fetch(
                    url,
                    new FeedFetcherListenerAdapter() {
                            @Override
                            public void onFetched(String feedUrl, String charset, byte[] content) {
                                FeedParserListenerAdapter listener = new FeedParserListenerAdapter();
                                try {
                                    FeedParser.getDefault().parse(new ByteArrayInputStream(content), listener);
                                    updateFeed(url, listener.getFeedTitle(), listener.getFeedDescription(), listener.getFeedItems());
                                }
                                catch (Exception e) {
                                    log.info("Parse XML failed", e);
                                    return;
                                }
                                triggerNextTimer();
                            }
                        }
                );
            }
        }.start();
    }

    private String findSubscriptionUrlByFreq() {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(SubscriptionColumns.URI, new String[] { SubscriptionColumns.URL }, null, null, SubscriptionColumns.FREQUENCY + "," + SubscriptionColumns._ID + " desc");
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(SubscriptionColumns.URL));
            }
            return null;
        }
        finally {
            close(cursor);
        }
    }

    void triggerNextTimer() {
        Message msg = Message.obtain();
        msg.what = MSG_TIMER;
        handler.sendMessageDelayed(msg, delayed);
    }

    void updateFeed(String url, String feedTitle, String feedDescription, FeedItem[] feedItems) {
        // sort feed items:
        Arrays.sort(
                feedItems,
                new Comparator<FeedItem>() {
                    public int compare(FeedItem i1, FeedItem i2) {
                        long d1 = i1.getDate();
                        long d2 = i2.getDate();
                        if (d1==d2)
                            return i1.title.compareTo(i2.title);
                        return d1 < d2 ? (-1) : 1;
                    }
                }
        );
        // get subscription id by url:
        Subscription sub = querySubscriptionByUrl(url);
        if (sub==null)
            return;
        String sub_id = String.valueOf(sub.id);
        List<FeedItem> added = new ArrayList<FeedItem>(feedItems.length);
        ContentResolver cr = getContentResolver();
        for (FeedItem item : feedItems) {
            long d = item.getDate();
            if (d<=sub.lastUpdated)
                break;
            Cursor cursor = cr.query(
                    ItemColumns.URI,
                    new String[] { ItemColumns._ID },
                    ItemColumns.SUBS_ID + "=? and " + ItemColumns.URL + "=?",
                    new String[] { sub_id, item.url },
                    null
            );
            if (cursor.moveToFirst()) {
                // exist, so no need continue:
                cursor.close();
                break;
            }
            else {
                cursor.close();
                // add to database:
                added.add(item);
            }
        }
        // update subscription:
        long add_freq = 3 - added.size();
        if (add_freq < 1)
            add_freq = 1;
        ContentValues cv = new ContentValues();
        cv.put(SubscriptionColumns.TITLE, feedTitle);
        cv.put(SubscriptionColumns.DESCRIPTION, feedDescription);
        if (!added.isEmpty())
            cv.put(SubscriptionColumns.LAST_UPDATED, added.get(0).getDate());
        cv.put(SubscriptionColumns.FREQUENCY, sub.frequency + add_freq);
        int n = getContentResolver().update(
                SubscriptionColumns.URI,
                cv,
                SubscriptionColumns._ID + "=" + sub_id,
                null
        );
        if (n==1) {
            log.info("Feed updated: " + url);
        }
        if (added.isEmpty())
            return;
        addItems(sub_id, added);
        // notify:
        Intent intent = new Intent(NOTIFY_NEW_ITEMS);
        intent.putExtra(SubscriptionColumns._ID, sub_id);
        sendBroadcast(intent);
    }

    Subscription querySubscriptionByUrl(String feedUrl) {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                    SubscriptionColumns.URI,
                    SubscriptionColumns.ALL_COLUMNS,
                    SubscriptionColumns.URL + "=?",
                    new String[] { feedUrl },
                    null
            );
            if (cursor.moveToFirst()) {
                Subscription subscription = new Subscription(
                        cursor.getLong(cursor.getColumnIndex(SubscriptionColumns._ID)),
                        cursor.getString(cursor.getColumnIndex(SubscriptionColumns.TITLE)),
                        cursor.getString(cursor.getColumnIndex(SubscriptionColumns.URL)),
                        cursor.getString(cursor.getColumnIndex(SubscriptionColumns.DESCRIPTION)),
                        cursor.getLong(cursor.getColumnIndex(SubscriptionColumns.LAST_UPDATED)),
                        cursor.getLong(cursor.getColumnIndex(SubscriptionColumns.FREQUENCY))
                );
                return subscription;
            }
        }
        finally {
            close(cursor);
        }
        return null;
    }

    void addItems(String sub_id, List<FeedItem> items) {
        ContentResolver cr = getContentResolver();
        for (FeedItem item : items) {
            ContentValues cv = new ContentValues();
            cv.put(ItemColumns.SUBS_ID, sub_id);
            cv.put(ItemColumns.AUTHOR, item.author);
            cv.put(ItemColumns.PUBLISHED, item.getDate());
            cv.put(ItemColumns.TITLE, item.title);
            cv.put(ItemColumns.CONTENT, item.content);
            cv.put(ItemColumns.UNREAD, 1);
            cv.put(ItemColumns.URL, item.url);
            Uri uri = cr.insert(ItemColumns.URI, cv);
            log.info("Inserted new item: " + uri.toString());
        }
    }

    public BriefSubscription addSubscription(String url) {
        if (querySubscriptionByUrl(url)!=null)
            return null;
        ContentValues cv = new ContentValues();
        cv.put(SubscriptionColumns.TITLE, url);
        cv.put(SubscriptionColumns.URL, url);
        cv.put(SubscriptionColumns.DESCRIPTION, url);
        cv.put(SubscriptionColumns.LAST_UPDATED, 0L);
        cv.put(SubscriptionColumns.FREQUENCY, 0L);
        Uri uri = getContentResolver().insert(SubscriptionColumns.URI, cv);
        return new BriefSubscription(Long.parseLong(uri.getPathSegments().get(1)), url);
    }

    public void removeSubscription(String sub_id) {
        ContentResolver cr = getContentResolver();
        cr.delete(SubscriptionColumns.URI, SubscriptionColumns._ID + "=" + sub_id, null);
        cr.delete(ItemColumns.URI, ItemColumns.SUBS_ID + "=" + sub_id, null);
        Intent intent = new Intent(NOTIFY_SUB_REMOVED);
        intent.putExtra(SubscriptionColumns._ID, sub_id);
        sendBroadcast(intent);
    }

    /**
     * Mark an item as read.
     * 
     * @param item_id Id of the item.
     */
    public void markRead(long item_id) {
        markUnreadAs(item_id, false);
    }

    /**
     * Mark an item as unread.
     * 
     * @param item_id Id of the item.
     */
    public void markUnread(long item_id) {
        markUnreadAs(item_id, true);
    }

    private void markUnreadAs(long item_id, boolean unread) {
        ContentValues values = new ContentValues();
        values.put(ItemColumns.UNREAD, unread ? 1 : 0);
        getContentResolver().update(ItemColumns.URI, values, ItemColumns._ID + "=" + item_id, null);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        log.info("ReadingService.onCreate()");
        refreshFeeds();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log.info("ReadingService.onDestroy()");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        log.info("ReadingService.onLowMemory()");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void close(Cursor cursor) {
        if (cursor!=null) {
            cursor.close();
        }
    }

    private final IBinder binder = new ReadingBinder();

    public class ReadingBinder extends Binder {
        public ReadingService getService() {
            return ReadingService.this;
        }
    }

}
