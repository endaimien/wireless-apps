package org.expressme.wireless.reader;

import org.expressme.wireless.reader.provider.ItemColumns;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Show item's full content in WebView.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class ReadActivity extends Activity {

    static final int MENU_MORE = Menu.FIRST;

    static final String AD;
    static final String CSS;
    static {
        AD = new StringBuilder(256)
                .append("<script type=\"text/javascript\"><!--\n")
                .append("window.googleAfmcRequest = {\n")
                .append("client: 'ca-mb-pub-6727358730461554',\n")
                .append("ad_type: 'text_image',\n")
                .append("output: 'html',\n")
                .append("channel: '0003502550',\n")
                .append("format: '300x250_as',\n")
                .append("oe: 'utf8',\n")
                .append("color_border: 'FFFFFF',\n")
                .append("color_bg: 'FFFFFF',\n")
                .append("color_link: '0000FF',\n")
                .append("color_text: '000000',\n")
                .append("color_url: '008000',\n")
                .append("};\n")
                .append("//--></script>\n")
                .append("<script type=\"text/javascript\" src=\"http://pagead2.googlesyndication.com/pagead/show_afmc_ads.js\"></script>\n")
                .toString();
        CSS = new StringBuilder(256)
                .append("<style type=\"text/css\">\n")
                .append("<!--\n")
                .append("body { color: #FFF; background-color: #000; }\n")
                .append("a:link,a:visited { color:#09F; }\n")
                .append("a:active,a:hover { color:#F60; }\n")
                .append("-->\n")
                .append("</style>\n")
                .toString();
    }

    private final Log log = Utils.getLog(getClass());
    private String url = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_MORE, 0, getResources().getString(R.string.menu_orig_link)).setIcon(android.R.drawable.ic_menu_more);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==MENU_MORE) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read);

        Intent intent = getIntent();
        long id = intent.getLongExtra(BaseColumns._ID, (-1));
        Cursor cursor = getContentResolver().query(ItemColumns.URI, ItemColumns.ALL_COLUMNS, ItemColumns._ID + "=" + id, null, null);
        if (!cursor.moveToFirst()) {
            show404();
            return;
        }
        url = cursor.getString(cursor.getColumnIndex(ItemColumns.URL));
        String title = cursor.getString(cursor.getColumnIndex(ItemColumns.TITLE));
        String content = cursor.getString(cursor.getColumnIndex(ItemColumns.CONTENT));
        cursor.close();

        // set title:
        setTitle(title);

        // load html:
        StringBuilder html = new StringBuilder(content.length()+200);
        html.append("<html><head><title>")
            .append(title)
            .append("</title>\n")
            .append(CSS)
            .append("</head><body>")
            .append(content)
            .append("<p>")
            .append(AD)
            .append("</p><p><a target='_blank' href='")
            .append(url)
            .append("'>")
            .append(getResources().getString(R.string.menu_orig_link))
            .append("</a></p>")
            .append("</body></html>");
        String baseUrl = getBaseUrl(url);
        log.info(url);
        log.info("base url:" + baseUrl);

        WebView web = (WebView) this.findViewById(R.id.webview);
        web.getSettings().setJavaScriptEnabled(true);
        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                log.info("onLoadResource: " + url);
            }
        });
        web.loadDataWithBaseURL(
                url,
                html.toString(),
                "text/html",
                "UTF-8",
                null
        );
    }

    void show404() {
        WebView web = (WebView) this.findViewById(R.id.webview);
        web.loadData(
                "<html><body><h1>404 Not Found</h1><p>The item was deleted.</p></body></html>",
                "text/html",
                "UTF-8"
        );
    }

    String getBaseUrl(String url) {
        int n = url.lastIndexOf('/');
        if (n > "https://".length()) {
            return url.substring(0, n+1);
        }
        return url + "/";
    }

}
