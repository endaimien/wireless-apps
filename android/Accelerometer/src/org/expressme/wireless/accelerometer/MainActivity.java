package org.expressme.wireless.accelerometer;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final float MAX_ACCELEROMETER = 9.81f;

    boolean init = false;
    int container_width = 0;
    int container_height = 0;
    int ball_width = 0;
    int ball_height = 0;
    BallView ball = null;
    TextView prompt = null;
    SensorManager sensorManager = null;
    Sensor sensor = null;

    SensorEventListener listener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent e) {
            if (!init)
                return;
            float x = e.values[SensorManager.DATA_X];
            float y = e.values[SensorManager.DATA_Y];
            float z = e.values[SensorManager.DATA_Z];
            prompt.setText("Accelerometer: " + x + ", " + y + ", " + z);
            moveTo(-x, y);
        }

        public void onAccuracyChanged(Sensor s, int accuracy) {
        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !init) {
            init();
            init = true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        prompt = (TextView) findViewById(R.id.ball_prompt);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onStart() {
        super.onStart();
        register();
    }

    @Override
    protected void onResume() {
        super.onResume();
        register();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        register();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregister();
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

    void init() {
        View container = findViewById(R.id.ball_container);
        container_width = container.getWidth();
        container_height = container.getHeight();
        ball = (BallView) findViewById(R.id.ball);
        ball_width = ball.getWidth();
        ball_height = ball.getHeight();
        log("container: ", container_width, ", ", container_height);
        log("ball: ", ball_width, ", ", ball_height);
        moveTo(0f, 0f);
    }

    void register() {
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    void unregister() {
        sensorManager.unregisterListener(listener);
    }

    // x, y is between [-MAX_ACCELEROMETER, MAX_ACCELEROMETER]
    void moveTo(float x, float y) {
        log("move to: ", x + ", ", y, " (float)");
        int max_x = (container_width - ball_width) / 2;
        int max_y = (container_height - ball_height) / 2;
        int pixel_x = (int) (max_x * x / MAX_ACCELEROMETER + 0.5);
        int pixel_y = (int) (max_y * y / MAX_ACCELEROMETER + 0.5);
        log("move to: ", pixel_x + ", ", pixel_y);
        translate(pixel_x, pixel_y);
    }

    void translate(int pixelX, int pixelY) {
        int x = pixelX + container_width / 2 - ball_width / 2;
        int y = pixelY + container_height / 2 - ball_height / 2;
        log("layout x=", x, ", y=", y);
        ball.moveTo(x, y);
    }

    void log(Object... objs) {
        StringBuilder sb = new StringBuilder(64);
        for (Object o : objs)
            sb.append(o==null ? "(null)" : o.toString());
        Log.i("Sensor", sb.toString());
    }
}
