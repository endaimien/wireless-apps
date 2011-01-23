package org.expressme.wireless.airwar;

import org.expressme.wireless.game.GameView;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Window;

public class AirActivity extends Activity {

    private AirLoopThread loopThread;
    private GameView gameView;
    SensorManager sensorManager = null;
    Sensor sensor = null;

    SensorEventListener listener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent e) {
            float x = e.values[SensorManager.DATA_X];
            loopThread.sensorChanged(-x);
        }
        public void onAccuracyChanged(Sensor s, int accuracy) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // turn off window title:
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.game);
        gameView = (GameView) findViewById(R.id.gameView);
        loopThread = gameView.getGameLoopThread();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onStart() {
        super.onStart();
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(listener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(listener);
    }

}
