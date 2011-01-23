package org.expressme.wireless.reader.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.util.Log;

/**
 * Simple JavaBean that hold a feed item.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class FeedItem {

    public String url;
    public String title;
    public String author;
    public String date;
    public String content;
    private long published = (-1);

    static String[] DATE_FORMATS = {
        "EEE, dd MMM yyyy HH:mm:ss Z"
    };

    public long getDate() {
        if (published==(-1))
            published = parse();
        return published;
    }

    private long parse() {
        if (date==null)
            return 0L;
        String s = date;
        if (s.endsWith(" Z")) {
            s = s.substring(0, s.length()-2) + " +0000";
        }
        for (String format : DATE_FORMATS) {
            try {
                return new SimpleDateFormat(format, Locale.US).parse(s).getTime();
            }
            catch (ParseException e) {
            }
        }
        if (s.indexOf('T')!=(-1)) {
            return parseDateWithT();
        }
        Log.w("RSS", "Cannot parse date: " + s);
        return 0L;
    }

    long parseDateWithT() {
        // 2006-08-24T17:16:06.000+08:00
        StringBuilder sb = new StringBuilder(32);
        int pos_t = date.indexOf('T');
        sb.append(date.substring(0, pos_t).trim()).append(' '); // put date as 'yyyy-MM-dd'
        int pos_tz = date.indexOf('+', pos_t);
        if (pos_tz==(-1))
            pos_tz = date.indexOf('-', pos_t);
        if (pos_tz==(-1)) {
            // no tz info
            sb.append(getTimeFormat(date.substring(pos_t+1).trim())).append(' ').append("+0000");
        }
        else {
            // has tz info:
            sb.append(getTimeFormat(date.substring(pos_t+1, pos_tz).trim()))
              .append(' ')
              .append(getTimeZoneFormat(date.substring(pos_tz).trim()));
        }
        String format_date = sb.toString();
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S Z", Locale.US).parse(format_date).getTime();
        }
        catch (ParseException e) {
            return 0L;
        }
    }

    String getTimeZoneFormat(String s) {
        // input: "+0800", "+900", "+0000", "+08:00", "+8:00", "-11:30"
        int pos_colon = s.indexOf(':');
        if (pos_colon==(-1)) {
            if (s.length()==5)
                return s;
            return s.charAt(0) + "0" + s.substring(1);
        }
        StringBuilder sb = new StringBuilder(5);
        sb.append(s.charAt(0));
        String left = s.substring(1, pos_colon);
        if (left.length()==1)
            sb.append('0');
        sb.append(left);
        String right = s.substring(pos_colon+1);
        if (right.length()==1)
            sb.append('0');
        sb.append(right);
        return sb.toString();
    }

    String getTimeFormat(String time) {
        // input: "9:10:30", "9:10:30.000", "23:59:9.102"
        int n = time.indexOf('.');
        if (n==(-1))
            return time + ".000";
        return time;
    }

    @Override
    public String toString() {
        return title;
//        StringBuilder sb = new StringBuilder(1024);
//        sb.append("[Item]\n")
//          .append("  url: ").append(url).append('\n')
//          .append("  title: ").append(title).append('\n')
//          .append("  author: ").append(author).append('\n')
//          .append("  date: ").append(date).append('\n')
//          .append("  format: ").append(getDate()).append('\n')
//          .append("  content: ");
//        String s = content.length()>60 ? content.substring(0, 60) + " ..." : content;
//        return sb.append(s).append('\n').toString();
    }
}
