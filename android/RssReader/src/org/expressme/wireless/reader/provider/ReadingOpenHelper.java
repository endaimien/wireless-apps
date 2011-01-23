package org.expressme.wireless.reader.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper class that needed for database operation.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class ReadingOpenHelper extends SQLiteOpenHelper {

    public ReadingOpenHelper(Context context) {
        super(context, "reader.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql_create_subs = cat(
                "CREATE TABLE ",
                SubscriptionColumns.TABLE_NAME,
                " (",
                SubscriptionColumns._ID, " INTEGER PRIMARY KEY AUTOINCREMENT, ",
                SubscriptionColumns.URL, " VARCHAR(1024), ",
                SubscriptionColumns.TITLE, " VARCHAR(256), ",
                SubscriptionColumns.DESCRIPTION, " VARCHAR(1024), ",
                SubscriptionColumns.LAST_UPDATED, " INTEGER, ",
                SubscriptionColumns.FREQUENCY, " INTEGER",
                ");"
        );
        String sql_index_subs_url = cat(
                "CREATE UNIQUE INDEX IDX_",
                SubscriptionColumns.TABLE_NAME,
                "_",
                SubscriptionColumns.URL,
                " ON ",
                SubscriptionColumns.TABLE_NAME,
                " (",
                SubscriptionColumns.URL,
                ");"
        );
        String sql_index_subs_title = cat(
                "CREATE INDEX IDX_",
                SubscriptionColumns.TABLE_NAME,
                "_",
                SubscriptionColumns.TITLE,
                " ON ",
                SubscriptionColumns.TABLE_NAME,
                " (",
                SubscriptionColumns.TITLE,
                ");"
        );
        String sql_create_item = cat(
                "CREATE TABLE ", ItemColumns.TABLE_NAME, " (",
                ItemColumns._ID, " INTEGER PRIMARY KEY AUTOINCREMENT, ",
                ItemColumns.SUBS_ID, " INTEGER, ",
                ItemColumns.PUBLISHED, " INTEGER, ",
                ItemColumns.UNREAD, " INTEGER, ",
                ItemColumns.URL, " VARCHAR(1024), ",
                ItemColumns.TITLE, " VARCHAR(256), ",
                ItemColumns.AUTHOR, " VARCHAR(256), ",
                ItemColumns.CONTENT, " TEXT",
                ");"
        );
        String sql_index_item_url = cat(
                "CREATE UNIQUE INDEX IDX_",
                ItemColumns.TABLE_NAME,
                "_",
                ItemColumns.URL,
                " ON ",
                ItemColumns.TABLE_NAME,
                " (",
                ItemColumns.URL,
                ");"
        );
        String sql_index_item_published = cat(
                "CREATE INDEX IDX_",
                ItemColumns.TABLE_NAME,
                "_",
                ItemColumns.PUBLISHED,
                " ON ",
                ItemColumns.TABLE_NAME,
                " (",
                ItemColumns.PUBLISHED,
                ");"
        );
        String sql_insert_subs = cat(
                "INSERT INTO ",
                SubscriptionColumns.TABLE_NAME, " (",
                SubscriptionColumns.URL, ",",
                SubscriptionColumns.TITLE, ",",
                SubscriptionColumns.DESCRIPTION, ",",
                SubscriptionColumns.LAST_UPDATED, ",",
                SubscriptionColumns.FREQUENCY,
                ") VALUES ('http://www.liaoxuefeng.com/feed.xml', 'Michael Liao', 'Official Blog of Michael Liao', 0, 0);"
        );
        db.execSQL(sql_create_subs);
        db.execSQL(sql_index_subs_url);
        db.execSQL(sql_index_subs_title);

        db.execSQL(sql_create_item);
        db.execSQL(sql_index_item_url);
        db.execSQL(sql_index_item_published);

        db.execSQL(sql_insert_subs);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion!=newVersion) {
            // drop db
            db.execSQL(cat("DROP TABLE ", ItemColumns.TABLE_NAME));
            db.execSQL(cat("DROP TABLE ", SubscriptionColumns.TABLE_NAME));
            onCreate(db);
        }
    }

    String cat(String... ss) {
        StringBuilder sb = new StringBuilder(ss.length << 3);
        for (String s : ss)
            sb.append(s);
        return sb.toString();
    }
}
