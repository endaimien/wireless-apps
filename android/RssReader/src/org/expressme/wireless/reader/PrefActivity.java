package org.expressme.wireless.reader;

import org.expressme.wireless.reader.service.ReadingService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;

/**
 * Manage user's preferences.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class PrefActivity extends Activity {

    private final Log log = Utils.getLog(getClass());
    private ReadingService serviceBinder = null;

    private ServiceConnection serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                serviceBinder = ((ReadingService.ReadingBinder)service).getService();
                loadPreferences();
            }

            public void onServiceDisconnected(ComponentName className) {
                serviceBinder = null;
            }
    };

    void savePreferences() {
        if (serviceBinder==null)
            return;
        boolean unreadOnly = ((RadioButton) findViewById(R.id.radio_reading_unread)).isChecked();
        int freq = ((Spinner) findViewById(R.id.spinner_freq)).getSelectedItemPosition() + 1;
        int expires = ((Spinner) findViewById(R.id.spinner_expires)).getSelectedItemPosition() + 1;
        serviceBinder.storePreferences(unreadOnly, freq, expires);
    }

    void loadPreferences() {
        log.info("Load pref...");
        boolean unreadOnly = serviceBinder.getPreferenceOfUnreadOnly();
        log.info("unreadOnly = " + unreadOnly);
        RadioButton radio = (RadioButton) findViewById(unreadOnly ? R.id.radio_reading_unread : R.id.radio_reading_all);
        radio.setChecked(true);

        int freq = serviceBinder.getPreferenceOfFreq();
        log.info("freq = " + freq);
        String[] freq_array = new String[ReadingService.FREQ_MAX];
        String unit_minute = getResources().getString(R.string.unit_minute);
        for (int i=0; i<freq_array.length; i++) {
            freq_array[i] = (i+1) + " " + unit_minute;
        }
        Spinner spinner_freq = (Spinner) findViewById(R.id.spinner_freq);
        spinner_freq.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, freq_array));
        spinner_freq.setSelection(freq-1);

        int expires = serviceBinder.getPreferenceOfExpires();
        log.info("expires = " + expires);
        String[] expires_array = new String[ReadingService.EXPIRES_MAX];
        String unit_week = getResources().getString(R.string.unit_week);
        for (int i=0; i<expires_array.length; i++) {
            expires_array[i] = (i+1) + " " + unit_week;
        }
        Spinner spinner_expires = (Spinner) findViewById(R.id.spinner_expires);
        spinner_expires.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, expires_array));
        spinner_expires.setSelection(expires-1);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pref);
        setTitle(getResources().getString(R.string.title_pref));

        // bind service:
        startService(new Intent(this, ReadingService.class));
        Intent bindIntent = new Intent(this, ReadingService.class);
        bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        // set button:
        Button ok = (Button) findViewById(R.id.pref_button_ok);
        ok.setOnClickListener(
                new OnClickListener() {
                    public void onClick(View view) {
                        savePreferences();
                        PrefActivity.this.finish();
                    }
                }
        );
        Button cancel = (Button) findViewById(R.id.pref_button_cancel);
        cancel.setOnClickListener(
                new OnClickListener() {
                    public void onClick(View view) {
                        PrefActivity.this.finish();
                    }
                }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

}
