package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MotionTestActivity extends AppCompatActivity implements SensorEventListener {

    SensorManager sensorManager;
    Sensor proximitySensor, linearAccSensor;

    LinearLayout backGround;
    TextView txvMotionTest1, txvMotionTest2;

    float cx, cy, cz, clight;
    int colorarr[] = { Color.RED, Color.GREEN, Color.BLUE, Color.WHITE };
    int cnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motion_test);

        backGround = (LinearLayout) findViewById(R.id.motionTestBackGround);
        txvMotionTest1 = (TextView) findViewById(R.id.txvMotionTest1);
        txvMotionTest2 = (TextView) findViewById(R.id.txvMotionTest2);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        linearAccSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(this, linearAccSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    float abs(float a){
        return a<0?-a:a;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.sensor == proximitySensor){
            clight = event.values[0];
            txvMotionTest2.setText("cm : " + clight);
        }

        if(event.sensor == linearAccSensor){
            cx = event.values[0];
            cy = event.values[1];
            cz = event.values[2];

            txvMotionTest1.setText(cx+ ","+cy+","+cz);

            if(abs(cx)>=10 && clight <= 4){
                backGround.setBackgroundColor(colorarr[cnt]);
                cnt++;
                cnt = cnt%4;
            }
            else if(abs(cy)>=10 && clight<=4){
                backGround.setBackgroundColor(colorarr[cnt]);
                cnt++;
                cnt = cnt%4;
            }
            else if(abs(cz)>=10 && clight<=4){
                backGround.setBackgroundColor(colorarr[cnt]);
                cnt++;
                cnt = cnt%4;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
