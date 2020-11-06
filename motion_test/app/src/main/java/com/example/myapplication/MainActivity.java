package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnMain2ListSensor, btnMain2AccelerometerTest, btnMain2GyroscopeTest, btnMain2LinearAccelerometerTest;
    Button btnMain2GravityTest, btnMain2TemperatureTest, btnMain2AmbientTemperatureTest, btnMain2HumidityTest;
    Button btnMain2MagneticFieldTest, btnMain2OrientationTest, btnMain2MotionTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnMain2ListSensor = (Button) findViewById(R.id.btnMain2ListSensor);
        btnMain2AccelerometerTest = (Button) findViewById(R.id.btnMain2AccelerometerTest);
        btnMain2GyroscopeTest = (Button) findViewById(R.id.btnMain2GyroscopeTest);
        btnMain2LinearAccelerometerTest = (Button) findViewById(R.id.btnMain2LinearAccelerometerTest);
        btnMain2GravityTest = (Button) findViewById(R.id.btnMain2GravityTest);
        btnMain2TemperatureTest = (Button) findViewById(R.id.btnMain2TemperatureTest);
        btnMain2AmbientTemperatureTest = (Button) findViewById(R.id.btnMain2AmbientTemperatureTest);
        btnMain2HumidityTest = (Button) findViewById(R.id.btnMain2HumidityTest);
        btnMain2MagneticFieldTest = (Button) findViewById(R.id.btnMain2MagneticFieldTest);
        btnMain2OrientationTest = (Button) findViewById(R.id.btnMain2OrientationTest);
        btnMain2MotionTest = (Button) findViewById(R.id.btnMain2MotionTest);
    }

    public void Main2(View v) {
        Intent intent = null;
        switch(v.getId()){
            case R.id.btnMain2ListSensor:
                intent = new Intent(this, ListSensorActivity.class);
                break;
            case R.id.btnMain2AccelerometerTest:
                intent = new Intent(this, AccelerometerTestActivity.class);
                break;
            case R.id.btnMain2GyroscopeTest:
                intent = new Intent(this, GyroscopeTestActivity.class);
                break;
            case R.id.btnMain2LinearAccelerometerTest:
                intent = new Intent(this, LinearAccelerometerTestActivity.class);
                break;
            case R.id.btnMain2GravityTest:
                intent = new Intent(this, GravityTestActivity.class);
                break;
            case R.id.btnMain2BarometerTest:
                intent = new Intent(this, BarometerTestActivity.class);
                break;
            case R.id.btnMain2LightTest:
                intent = new Intent(this, LightTestActivity.class);
                break;
            case R.id.btnMain2ProximityTest:
                intent = new Intent(this, ProximityTestActivity.class);
                break;
            case R.id.btnMain2TemperatureTest:
                intent = new Intent(this, TemperatureTestActivity.class);
                break;
            case R.id.btnMain2AmbientTemperatureTest:
                intent = new Intent(this, AmbientTemperatureActivity.class);
                break;
            case R.id.btnMain2HumidityTest:
                intent = new Intent(this, HumidityTestActivity.class);
                break;
            case R.id.btnMain2MagneticFieldTest:
                intent = new Intent(this, MagneticFieldTestActivity.class);
                break;
            case R.id.btnMain2OrientationTest:
                intent = new Intent(this, OrientationTestActivity.class);
                break;
            case R.id.btnMain2MotionTest:
                intent = new Intent(this, MotionTestActivity.class);
                break;
        }
        startActivity(intent);
    }
}
