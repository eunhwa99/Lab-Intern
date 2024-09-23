package com.example.sensortest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileWriter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    Button btnStart, btnStop;
    private Sensor gyroSensor;
    private Sensor accelSensor;
    SensorManager sensorManager;
    MainActivity.MyThread thread = new MainActivity.MyThread();
    private double a = 0.2f;
    private double gyroX,gyroY,gyroZ;
    private double accX,accY,accZ;
    private double pitch,roll,yaw;
    private double accPitch,accRoll;
    private double temp;
    private double compPitch, compRoll;
    private double timestamp;
    private double dt;

    private double RAD2DGR=180/Math.PI;
    private static final double NS2S = 1.0f/1000000000.0f;

    private boolean gyroRunning;
    private boolean accRunning;

    // 파일 입출력 허가
    private boolean fileWrite=false;
    private boolean fileRead=false;
    private String filename="";

    boolean fileReadPermission, fileWritePermission;

    float gyrox, gyroy, gyroz;
    float accx, accy, accz;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = (Button)findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            fileReadPermission = true;
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            fileWritePermission = true;
        }

        if(!fileWritePermission || !fileReadPermission){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100 && grantResults.length>0){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED) fileReadPermission = true;
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) fileWritePermission = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,gyroSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    public void complementary(double new_ts){
        gyroRunning = false;
        accRunning = false;

        accPitch=-Math.atan2(accX,accZ)*180.0/Math.PI;
        accRoll=Math.atan2(accY,accZ)*180.0/Math.PI;

        dt = (new_ts - timestamp) * NS2S;
        timestamp = new_ts;

        if(dt - timestamp*NS2S != 0){
            roll = roll + gyroX * dt;
            pitch = pitch + gyroY * dt;
            yaw = yaw + gyroZ * dt;

            temp = (1/a) * (accPitch - compPitch) + gyroY;
            compPitch = compPitch + (temp*dt);

            temp = (1/a) * (accRoll - compRoll) + gyroX;
            compRoll = compRoll + (temp*dt);
        }


    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor==gyroSensor){
            gyroX=event.values[0];
            gyroY = event.values[1];
            gyroZ = event.values[2];
            if(!gyroRunning) gyroRunning = true;
        }

        if(event.sensor==accelSensor){
            accX=event.values[0];
            accY=event.values[1];
            accZ=event.values[2];
            if(!accRunning) accRunning = true;
        }

        if(gyroRunning && accRunning){
            complementary(event.timestamp);
        }
    }

    public void writeFile(){
        try{

            String dirpath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/myApp";
            File dir=new File(dirpath);
            if(!dir.exists()){
                dir.mkdir();
            }

            File file=new File(dir+"/gyroang.txt");
            if(!file.exists()) {
                file.createNewFile();
            }
            File file2=new File(dir+"/accang.txt");
            if(!file.exists()) {
                file2.createNewFile();
            }
            File file3=new File(dir+"/compang.txt");
            if(!file.exists()) {
                file3.createNewFile();
            }

            FileWriter fw=new FileWriter(file,true);
            fw.write(roll*RAD2DGR+","+pitch*RAD2DGR+"\n");
            fw.flush();
            fw.close();

            FileWriter fw2=new FileWriter(file2,true);
            fw2.write(accRoll+","+accPitch+"\n");
            fw2.flush();
            fw2.close();

            FileWriter fw3=new FileWriter(file3,true);
            fw3.write(compRoll+","+compPitch+"\n");
            fw3.flush();
            fw3.close();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    class MyThread extends Thread{
        @Override
        public void run(){
            try{
                while(!Thread.currentThread().isInterrupted()){
                    writeFile();
                    Thread.sleep(50);
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally{

            }
        }
    }
    private void threadStart(){
        thread.setDaemon(true);
        thread.start();
    }

    private void threadStop(){
        thread.interrupt();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void btnMain(View v){
        switch(v.getId()){
            case R.id.btnStart:
                threadStart();
                break;
            case R.id.btnStop:
                threadStop();
                break;
        }
    }
}
