package org.expressme.wireless.reader.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

/**
 * ContentProvider of Item and Subs.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class ReadingProvider extends ContentProvider {

    public static final String AUTHORITY = ReadingProvider.class.getName().toLowerCase();

    private static final int TYPE_ALL_SUBSCRIPTIONS   = 0;
    private static final int TYPE_SINGLE_SUBSCRIPTION = 1;
    private static final int TYPE_ALL_ITEMS           = 2;
    private static final int TYPE_SINGLE_ITEM         = 3;

    private ReadingOperator operator = null;

    private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        matcher.addURI(AUTHORITY, "subscriptions", TYPE_ALL_SUBSCRIPTIONS);
        matcher.addURI(AUTHORITY, "subscriptions/#", TYPE_SINGLE_SUBSCRIPTION);
        matcher.addURI(AUTHORITY, "items", TYPE_ALL_ITEMS);
        matcher.addURI(AUTHORITY, "items/#", TYPE_SINGLE_ITEM);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        switch (matcher.match(uri)) {
        case TYPE_ALL_SUBSCRIPTIONS:
            return operator.getCurrentDb().delete(SubscriptionColumns.TABLE_NAME, where, whereArgs);
        case TYPE_SINGLE_SUBSCRIPTION:
            String s_id = uri.getPathSegments().get(1);
            return operator.getCurrentDb().delete(SubscriptionColumns.TABLE_NAME, SubscriptionColumns._ID + "=" + s_id, null);
        case TYPE_ALL_ITEMS:
            return operator.getCurrentDb().delete(ItemColumns.TABLE_NAME, where, whereArgs);
        case TYPE_SINGLE_ITEM:
            String i_id = uri.getPathSegments().get(1);
            return operator.getCurrentDb().delete(ItemColumns.TABLE_NAME, ItemColumns._ID + "=" + i_id, null);
        }
        throw new IllegalArgumentException("Unsupported URI: " + uri);
    }

    @Override
    public String getType(Uri uri) {
        switch (matcher.match(uri)) {
        case TYPE_ALL_SUBSCRIPTIONS:
            return "vnd.expressme.wireless.reader.subscriptions/readingprovidercontent";
        case TYPE_SINGLE_SUBSCRIPTION:
            return "vnd.expressme.wireless.reader.subscription/readingprovidercontent";
        case TYPE_ALL_ITEMS:
            return "vnd.expressme.wireless.reader.items/readingprovidercontent";
        case TYPE_SINGLE_ITEM:
            return "vnd.expressme.wireless.reader.item/readingprovidercontent";
        }
        throw new IllegalArgumentException("Unsupported URI: " + uri);
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        for (ContentValues initialValues : values) {
            insert(uri, initialValues);
        }
        return values.length;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        long id = 0L;
        switch (matcher.match(uri)) {
        case TYPE_ALL_SUBSCRIPTIONS:
            id = operator.getCurrentDb().insertOrThrow(SubscriptionColumns.TABLE_NAME, null, initialValues);
            return ContentUris.withAppendedId(SubscriptionColumns.URI, 0L);
        case TYPE_ALL_ITEMS:
            id = operator.getCurrentDb().insertOrThrow(ItemColumns.TABLE_NAME, null, initialValues);
            return ContentUris.withAppendedId(ItemColumns.URI, id);
        }
        throw new IllegalArgumentException("Illegal Uri: " + uri.toString());
    }

    @Override
    public boolean onCreate() {
        operator = new ReadingOperator(getContext());
        return true;
    }

    @Override
    public void onLowMemory() {
        if (operator!=null) {
            operator.close();
            operator = null;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sort) {
        switch (matcher.match(uri)) {
        case TYPE_ALL_SUBSCRIPTIONS:
            return operator.getCurrentDb().query(SubscriptionColumns.TABLE_NAME, projection, selection, selectionArgs, null, null, sort);
        case TYPE_SINGLE_SUBSCRIPTION:
            String s_id = uri.getPathSegments().get(1);
            return operator.getCurrentDb().query(SubscriptionColumns.TABLE_NAME, projection, SubscriptionColumns._ID + "=" + s_id, null, null, null, null);
        case TYPE_ALL_ITEMS:
            return operator.getCurrentDb().query(ItemColumns.TABLE_NAME, projection, selection, selectionArgs, null, null, sort);
        case TYPE_SINGLE_ITEM:
            String i_id = uri.getPathSegments().get(1);
            return operator.getCurrentDb().query(ItemColumns.TABLE_NAME, projection, ItemColumns._ID + "=" + i_id, null, null, null, null);
        }
        throw new IllegalArgumentException("Illegal Uri: " + uri.toString());
    }

    @Override
    public int update(Uri uri, ContentValues initialValues, String where, String[] whereArgs) {
        switch (matcher.match(uri)) {
        case TYPE_ALL_SUBSCRIPTIONS:
            return operator.getCurrentDb().update(SubscriptionColumns.TABLE_NAME, initialValues, where, whereArgs);
        case TYPE_ALL_ITEMS:
            return operator.getCurrentDb().update(ItemColumns.TABLE_NAME, initialValues, where, whereArgs);
        }
        throw new IllegalArgumentException("Illegal Uri: " + uri.toString());
    }

}
