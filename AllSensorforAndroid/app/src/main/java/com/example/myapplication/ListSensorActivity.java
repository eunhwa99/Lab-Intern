package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class ListSensorActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private String sSensorList = "";
    TextView tvListSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_sensor);

        tvListSensor = (TextView) findViewById(R.id.tvListSensor);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> lstSensor = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for(int i=0;i<lstSensor.size();i++){
            Sensor sensor = lstSensor.get(i);
            sSensorList += "Name : " + sensor.getName() + "\n" +
                    "Vendor : " + sensor.getVendor() + "\n" +
                    "toString : " + sensor.toString() + "\n"+"\n\n";
        }
        tvListSensor.setText(sSensorList);
    }
}
