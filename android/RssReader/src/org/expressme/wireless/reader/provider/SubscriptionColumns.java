package org.expressme.wireless.reader.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Column definition of Subscription, used in ContentProvider.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public interface SubscriptionColumns extends BaseColumns {

    static final Uri URI = Uri.parse("content://" + ReadingProvider.AUTHORITY + "/subscriptions");

    static final String TABLE_NAME = "subs";

    static final String URL = "url";

    static final String TITLE = "title";

    static final String DESCRIPTION = "description";

    static final String LAST_UPDATED = "last_updated";

    static final String FREQUENCY = "frequency";

    static final String[] ALL_COLUMNS = { _ID, URL, TITLE, DESCRIPTION, LAST_UPDATED, FREQUENCY };
}
