package org.expressme.wireless.reader.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Make ease of using SQLite database.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class ReadingOperator {

    private SQLiteDatabase db = null;
    private ReadingOpenHelper helper = null;

    public ReadingOperator(Context context) {
        helper = new ReadingOpenHelper(context);
        db = helper.getWritableDatabase();
    }

    public synchronized void close() {
        db.close();
        helper.close();
    }

    public SQLiteDatabase getCurrentDb() {
        return db;
    }

    /**
     * Query a Subscription by id.
     */
    public Subscription selectSubs(long id) {
        Cursor cursor = null;
        try {
            cursor = db.query(SubscriptionColumns.TABLE_NAME, SubscriptionColumns.ALL_COLUMNS, SubscriptionColumns._ID + "=?", new String[] { String.valueOf(id) }, null, null, null);
            if (cursor.moveToFirst())
                return mappingToSubs(cursor);
            return null;
        }
        finally {
            close(cursor);
        }
    }

    /**
     * Query all Subscriptions.
     */
    public List<Subscription> selectAllSubs() {
        Cursor cursor = null;
        try {
            cursor = db.query(SubscriptionColumns.TABLE_NAME, SubscriptionColumns.ALL_COLUMNS, null, null, null, null, SubscriptionColumns.TITLE);
            int size = cursor.getCount();
            if (size==0)
                return Collections.emptyList();
            cursor.moveToFirst();
            List<Subscription> list = new ArrayList<Subscription>(size);
            while (!cursor.isAfterLast()) {
                list.add(mappingToSubs(cursor));
                cursor.moveToNext();
            }
            return list;
        }
        finally {
            close(cursor);
        }
    }

    // mapping current record to a Subs object:
    Subscription mappingToSubs(Cursor cursor) {
        long id = cursor.getLong(0);
        String url = cursor.getString(1);
        String title = cursor.getString(2);
        String description = cursor.getString(3);
        long lastVisit = cursor.getLong(4);
        long freq = cursor.getLong(5);
        return new Subscription(id, url, title, description, lastVisit, freq);
    }

    // close cursor:
    void close(Cursor cursor) {
        if (cursor!=null)
            cursor.close();
    }

    /**
     * Delete a Subscription by id.
     * 
     * @return True if delete successfully, false if no such record in database.
     */
    public boolean deleteSubs(long id) {
        return db.delete(SubscriptionColumns.TABLE_NAME, SubscriptionColumns._ID + "=?", new String[] { String.valueOf(id) })>0;
    }

    /**
     * Create a new Subscription.
     * 
     * @return Id of the new created Subscription.
     */
    public long insertSubs(String url, String title, String description) {
        ContentValues cv = new ContentValues();
        cv.put(SubscriptionColumns.URL, url);
        cv.put(SubscriptionColumns.TITLE, title);
        cv.put(SubscriptionColumns.DESCRIPTION, description);
        cv.put(SubscriptionColumns.LAST_UPDATED, 0L);
        cv.put(SubscriptionColumns.FREQUENCY, 0L);
        return db.insert(SubscriptionColumns.TABLE_NAME, null, cv);
    }

    public long insertItem(long subs_id, String url, long published, String title, String author, String content) {
        ContentValues cv = new ContentValues();
        cv.put(ItemColumns.SUBS_ID, subs_id);
        cv.put(ItemColumns.URL, url);
        cv.put(ItemColumns.PUBLISHED, published);
        cv.put(ItemColumns.UNREAD, 1);
        cv.put(ItemColumns.TITLE, title);
        cv.put(ItemColumns.AUTHOR, author);
        cv.put(ItemColumns.CONTENT, content);
        return db.insert(ItemColumns.TABLE_NAME, null, cv);
    }

}
