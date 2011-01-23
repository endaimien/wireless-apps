package org.expressme.wireless.reader.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Column definition of Item, used in ContentProvider.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public interface ItemColumns extends BaseColumns {

    static final Uri URI = Uri.parse("content://" + ReadingProvider.AUTHORITY + "/items");

    static final String TABLE_NAME = "item";

    static final String SUBS_ID = "subs_id";

    static final String UNREAD = "unread";

    static final String URL = "url";

    static final String TITLE = "title";

    static final String AUTHOR = "author";

    static final String PUBLISHED = "published";

    static final String CONTENT = "content";

    static final String[] ALL_COLUMNS = { _ID, SUBS_ID, UNREAD, URL, TITLE, AUTHOR, PUBLISHED, CONTENT };

}
