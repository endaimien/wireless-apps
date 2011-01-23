package org.expressme.wireless.location;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Display my current location on Google map.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class MainActivity extends Activity {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private WebView webView;
    private boolean init = false;
    private boolean registered = false;
    private double lastLat = 0;
    private double lastLng = 0;

    private final Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebViewClient(
                new WebViewClient() {
                    @Override
                    public void onLoadResource(WebView view, String url) {
                        super.onLoadResource(view, url);
                        log("onLoadResource: ", url);
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        log("onPageFinished: ", url);
                    }
                }
        );
        // init location manager:
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location newLocation) {
                log("onLocationChanged: ", newLocation.getLatitude(), ", ", newLocation.getLongitude());
                MainActivity.this.onLocationChanged(newLocation);
            }
 
            public void onProviderDisabled(String provider) {
                log("onProviderDisabled: " + provider);
            }
 
            public void onProviderEnabled(String provider) {
                log("onProviderEnabled: " + provider);
            }
 
            public void onStatusChanged(String provider, int status, Bundle extras) {
                log("onStatusChanged: ", status);
            }
        };
    }

    private void onLocationChanged(Location newLocation) {
        // set marker in web view:
        this.lastLat = newLocation.getLatitude();
        this.lastLng = newLocation.getLongitude();
        if (!init)
            return;
        handler.post(
                new Runnable() {
                    public void run() {
                        webView.loadUrl("javascript:setMarker(" + lastLat + "," + lastLng + ")");
                    }
                }
        );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!init && hasFocus) {
            this.webView.loadDataWithBaseURL("http://localhost/map.html?lat=" + this.lastLat + "&lng=" + this.lastLng + "&w=" + webView.getWidth() + "&h=" + webView.getHeight(), loadHtml(), "text/html", "UTF-8", null);
            init = true;
        }
    }

    private String loadHtml() {
        InputStream input = null;
        try {
            input = getAssets().open("map.html");
            ByteArrayOutputStream result = new ByteArrayOutputStream(4096);
            byte[] buffer = new byte[1024];
            for (;;) {
                int n = input.read(buffer);
                if (n==(-1))
                    break;
                result.write(buffer, 0, n);
            }
            return result.toString("UTF-8");
        }
        catch (IOException e) {
            return "";
        }
        finally {
            if (input!=null) {
                try {
                    input.close();
                }
                catch (IOException e) {}
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        register();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        register();
    }

    @Override
    protected void onResume() {
        super.onResume();
        register();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregister();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregister();
    }

    private void register() {
        log("register()");
        if (registered)
            return;
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                10000L,
                0,
                locationListener
        );
        registered = true;
    }

    private void unregister() {
        log("unregister()");
        if (!registered)
            return;
        locationManager.removeUpdates(locationListener);
        registered = false;
    }

    private void log(Object... objs) {
        StringBuilder sb = new StringBuilder(64);
        for (Object o : objs)
            sb.append(o==null ? "(null)" : o.toString());
        Log.i("MAP", sb.toString());
    }
}
