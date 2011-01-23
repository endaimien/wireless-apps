package org.expressme.wireless.reader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.expressme.wireless.reader.fetcher.FeedFetcher;
import org.expressme.wireless.reader.provider.BriefSubscription;
import org.expressme.wireless.reader.service.ReadingService;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * Manage all subscriptions.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class SubsActivity extends ListActivity {

    private final int MENU_ADD = Menu.FIRST;
    private final int MENU_DEL = Menu.FIRST + 1;

    private final Log log = Utils.getLog(getClass());

    private int selected = (-1);
    private ProgressDialog progress = null;

    private ReadingService serviceBinder = null;
    private ArrayAdapter<BriefSubscription> subsListAdapter = null;

    private ServiceConnection serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                serviceBinder = ((ReadingService.ReadingBinder)service).getService();
                refreshSubscriptions();
            }

            public void onServiceDisconnected(ComponentName className) {
                serviceBinder = null;
            }
    };

    private void refreshSubscriptions() {
        List<BriefSubscription> list = serviceBinder.queryBriefSubscriptions();
        subsListAdapter.clear();
        for (BriefSubscription subs : list) {
            subsListAdapter.add(subs);
        }
    }

    private void addFeeds(final String[] feeds) {
        if (serviceBinder==null)
            return;
        List<BriefSubscription> added = new ArrayList<BriefSubscription>(feeds.length);
        for (String feed : feeds) {
            try {
                feed = formatURL(feed);
            }
            catch (MalformedURLException e) {
                continue;
            }
            BriefSubscription s = serviceBinder.addSubscription(feed);
            if (s!=null) {
                added.add(s);
            }
        }
        CharSequence title = added.isEmpty()
                ? getResources().getString(R.string.dialog_title_no_feed_found)
                : getResources().getString(R.string.dialog_title_feed_found);
        CharSequence message = added.isEmpty()
                ? getResources().getString(R.string.dialog_message_no_feed_found)
                : getResources().getString(R.string.dialog_message_feed_found, added.size());
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(
                        android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }
                ).show();
        if (added.size()>0)
            refreshSubscriptions();
    }

    // discover feeds from URL async:
    void discover(String value) {
        String url = null;
        try {
            url = formatURL(value);
        }
        catch (MalformedURLException e) {
            new AlertDialog.Builder(this)
                    .setTitle(getResources().getText(R.string.dialog_title_add_sub))
                    .setMessage(getResources().getText(R.string.dialog_message_malformed_url))
                    .setPositiveButton(
                            android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            }
                    ).show();
            return;
        }
        this.progress = ProgressDialog.show(
                this,
                getResources().getText(R.string.dialog_title_loading),
                getResources().getText(R.string.dialog_message_loading),
                true
        );
        AsyncTask<String, ProgressDialog, String[]> asyncTask = new AsyncTask<String, ProgressDialog, String[]>() {
            @Override
            protected String[] doInBackground(String... params) {
                final FeedFetcher fetcher = new FeedFetcher();
                return fetcher.discover(params[0]);
            }

            @Override
            protected void onPostExecute(String[] result) {
                // this method is running on UI thread,
                // so it is safe to update UI:
                if (SubsActivity.this.progress!=null) {
                    SubsActivity.this.progress.dismiss();
                    SubsActivity.this.progress = null;
                }
                addFeeds(result);
            }
        };
        asyncTask.execute(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ADD, 0, getResources().getString(R.string.menu_add)).setIcon(android.R.drawable.ic_menu_add);
        menu.add(0, MENU_DEL, 0, getResources().getString(R.string.menu_del)).setIcon(android.R.drawable.ic_menu_delete);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(MENU_DEL);
        item.setEnabled(selected!=(-1));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ADD:
            addSubscription();
            break;
        case MENU_DEL:
            deleteSubscription();
            break;
        }
        return true;
    }

    private void addSubscription() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getResources().getText(R.string.dialog_title_add_sub));
        alert.setMessage(getResources().getText(R.string.dialog_message_add_sub));

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setSingleLine();
        input.setText("http://");
        alert.setView(input);
        alert.setPositiveButton(
                android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        discover(input.getText().toString());
                    }
                }
        );
        alert.setNegativeButton(
                android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }
        );
        alert.show();
    }

    private String formatURL(String value) throws MalformedURLException {
        int n = value.indexOf("://");
        if (n!=(-1)) {
            String scheme = value.substring(0, n);
            if (!"http".equals(scheme) && !"https".equals(scheme))
                throw new MalformedURLException();
        }
        if (!value.startsWith("http://") && !value.startsWith("https://"))
            value = "http://" + value;
        URL url = new URL(value);
        StringBuilder sb = new StringBuilder(256);
        sb.append(url.getProtocol())
          .append("://")
          .append(url.getHost().toLowerCase());
        if ("http".equals(url.getProtocol()) && (url.getPort()!=(-1) && url.getPort()!=(80)))
            sb.append(':').append(url.getPort());
        else if ("https".equals(url.getProtocol()) && (url.getPort()!=(-1) && url.getPort()!=(443)))
            sb.append(':').append(url.getPort());
        String path = url.getPath();
        sb.append(path==null || path.length()==0 ? "/" : path);
        String query = url.getQuery();
        if (query!=null)
            sb.append('?').append(query);
        return sb.toString();
    }

    private void deleteSubscription() {
        if (this.selected==(-1)) {
            return;
        }
        final BriefSubscription sub = subsListAdapter.getItem(this.selected);
        // confirm:
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getResources().getString(R.string.dialog_title_del_sub));
        alert.setMessage(getResources().getString(R.string.dialog_message_del_sub, sub.title));
        alert.setPositiveButton(
                android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        serviceBinder.removeSubscription(String.valueOf(sub.id));
                        refreshSubscriptions();
                    }
                }
        );
        alert.setNegativeButton(
                android.R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }
        );
        alert.show();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        select(v, position);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subs);
        setTitle(getResources().getString(R.string.title_subs));
        this.subsListAdapter = new ArrayAdapter<BriefSubscription>(this, android.R.layout.simple_list_item_single_choice);
        setListAdapter(this.subsListAdapter);
        getListView().setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        log.info("onItemSelected: " + position);
                        select(view, position);
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        log.info("onNothingSelected.");
                        select(null, (-1));
                    }

                }
        );
        // bind service:
        Intent bindIntent = new Intent(this, ReadingService.class);
        bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void select(View view, int position) {
        clearLastSelection();
        this.selected = position;
        if (this.selected!=(-1)) {
            if (view instanceof CheckedTextView) {
                ((CheckedTextView) view).setChecked(true);
            }
        }
    }

    private void clearLastSelection() {
        if (this.selected!=(-1)) {
            View last = this.getListView().getChildAt(selected);
            if (last instanceof CheckedTextView) {
                ((CheckedTextView) last).setChecked(false);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unbind service:
        unbindService(serviceConnection);
    }

}
