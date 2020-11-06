package com.jsy.dtwtestapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final int REQUEST_CODE=100;
    private Sensor linearAccSensor;
    private SensorManager sensorManager;
    private TextView txv_state, txv_I, txv_O, txv_S, txv_Z,txv_result;
    private EditText editName;

    private double accX,accY,accZ;
    private ArrayList<Double> timeSeriesX, timeSeriesY, timeSeriesZ;
    private ArrayList<Double> IdataX, IdataY, IdataZ, OdataX, OdataY, OdataZ, SdataX, SdataY, SdataZ, ZdataX, ZdataY, ZdataZ;
    private boolean nowReading = false;
    private double timestamp;
    private double dt;
    private static final double NS2S = 1.0f/1000000000.0f;
    private myThread thread;

    // 파일 입출력 허가
    private boolean fileWrite=false;
    private boolean fileRead=false;
    private String filename="";

    private boolean flag=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flag=false;
        thread = new myThread();

        timeSeriesX = new ArrayList<>();
        timeSeriesY = new ArrayList<>();
        timeSeriesZ = new ArrayList<>();

        IdataX = new ArrayList<>();
        IdataY = new ArrayList<>();
        IdataZ = new ArrayList<>();

        OdataX = new ArrayList<>();
        OdataY = new ArrayList<>();
        OdataZ = new ArrayList<>();

        SdataX = new ArrayList<>();
        SdataY = new ArrayList<>();
        SdataZ = new ArrayList<>();

        ZdataX = new ArrayList<>();
        ZdataY = new ArrayList<>();
        ZdataZ = new ArrayList<>();

        txv_I = (TextView)findViewById(R.id.txv_I);
        txv_O = (TextView)findViewById(R.id.txv_O);
        txv_S = (TextView)findViewById(R.id.txv_S);
        txv_Z = (TextView)findViewById(R.id.txv_Z);
        txv_state = (TextView)findViewById(R.id.txv_state);
        txv_result=(TextView)findViewById(R.id.txv_result);
        editName = (EditText) findViewById(R.id.editName);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        linearAccSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        readDataFromText();
    }

    public void readDataFromText(){
        int rawDataIds[] = {R.raw.data_i, R.raw.data_o, R.raw.data_s, R.raw.data_z};
        for(int i=0;i<4;i++){
            InputStream inputStream = getResources().openRawResource(rawDataIds[i]);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            try{
                while(true){
                    String temp = bufferedReader.readLine();
                    if(temp == null) break;
                    String[] tempArray = temp.split(",");
                    if(i==0){
                        IdataX.add(Double.parseDouble(tempArray[0]));
                        IdataY.add(Double.parseDouble(tempArray[1]));
                        IdataZ.add(Double.parseDouble(tempArray[2]));
                    }else if (i==1){
                        OdataX.add(Double.parseDouble(tempArray[0]));
                        OdataY.add(Double.parseDouble(tempArray[1]));
                        OdataZ.add(Double.parseDouble(tempArray[2]));
                    }else if(i==2){
                        SdataX.add(Double.parseDouble(tempArray[0]));
                        SdataY.add(Double.parseDouble(tempArray[1]));
                        SdataZ.add(Double.parseDouble(tempArray[2]));
                    }else {
                        ZdataX.add(Double.parseDouble(tempArray[0]));
                        ZdataY.add(Double.parseDouble(tempArray[1]));
                        ZdataZ.add(Double.parseDouble(tempArray[2]));
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        dt = (event.timestamp-timestamp)*NS2S;
        timestamp = event.timestamp;

        if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            accX = event.values[0];
            accY = event.values[1];
            accZ = event.values[2];
        }
    }

    public void readSensorData(){
        timeSeriesX.add(accX);
        timeSeriesY.add(accY);
        timeSeriesZ.add(accZ);
    }

    // 파일 입출력 확인
    public boolean checkFilePermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            fileRead=true;
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
            fileWrite=true;
        }
        return (fileRead&&fileWrite);
    }

    // 파일 입출력 요구
    public void requestFilePermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
    }

    // 권한 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_CODE&&grantResults.length>0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                fileRead = true;
            }
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                fileWrite = true;
            }
        }
    }

    public void showToast(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    public void checkFile(){
        if (!checkFilePermission()) {
            requestFilePermission();
        }
        else {
            filename = editName.getText().toString().trim();
            if (filename.equals("")) {
                showToast("파일 이름을 입력하세요");
            } else {
                txv_state.setText("기록 중");
                threadStart();
            }
        }
    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.btn_start:
                checkFile();
                // threadStart();
                break;
            case R.id.btn_end:
                threadStop();
                txv_state.setText("기록 종료. dtw유사도 출력");
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        flag=false;
        sensorManager.registerListener(this,linearAccSensor,sensorManager.SENSOR_DELAY_GAME);
    }

    // 잠시 종료하는 경우 (홈 버튼 누르는거 같이)
    @Override
    protected void onPause() {
        super.onPause();
        flag=true;
        threadStop();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        flag=true;
        threadStop();
        sensorManager.unregisterListener(this);
    }

    private void threadStart(){
        thread = new myThread();
        timeSeriesX.clear();
        timeSeriesY.clear();
        timeSeriesZ.clear();
        thread.setDaemon(true);
        thread.start();
    }

    private void threadStop(){
        thread.interrupt();
        DTW dtw = new DTW();
        Double[] tsX = new Double[timeSeriesX.size()];
        tsX = timeSeriesX.toArray(tsX);
        Double[] tsY = new Double[timeSeriesY.size()];
        tsY = timeSeriesY.toArray(tsY);
        Double[] tsZ = new Double[timeSeriesZ.size()];
        tsZ = timeSeriesZ.toArray(tsZ);
        Double[] IdX = new Double[IdataX.size()];
        IdX = IdataX.toArray(IdX);
        Double[] IdY = new Double[IdataY.size()];
        IdY = IdataX.toArray(IdY);
        Double[] IdZ = new Double[IdataZ.size()];
        IdZ = IdataX.toArray(IdZ);
        Double[] OdX = new Double[OdataX.size()];
        OdX = OdataX.toArray(OdX);
        Double[] OdY = new Double[OdataY.size()];
        OdY = OdataY.toArray(OdY);
        Double[] OdZ = new Double[OdataZ.size()];
        OdZ = OdataZ.toArray(OdZ);
        Double[] SdX = new Double[SdataX.size()];
        SdX = SdataX.toArray(SdX);
        Double[] SdY = new Double[SdataY.size()];
        SdY = SdataY.toArray(SdY);
        Double[] SdZ = new Double[SdataZ.size()];
        SdZ = SdataZ.toArray(SdZ);
        Double[] ZdX = new Double[ZdataX.size()];
        ZdX = ZdataX.toArray(ZdX);
        Double[] ZdY = new Double[ZdataY.size()];
        ZdY = ZdataY.toArray(ZdY);
        Double[] ZdZ = new Double[ZdataZ.size()];
        ZdZ = ZdataZ.toArray(ZdZ);

        double values[]=new double[4];
        values[0]=dtw.compute(tsX, IdX).getDistance()+dtw.compute(tsY, IdY).getDistance()+dtw.compute(tsZ, IdZ).getDistance();
        values[1]=dtw.compute(tsX, OdX).getDistance()+dtw.compute(tsY, OdY).getDistance()+dtw.compute(tsZ, OdZ).getDistance();
        values[2]=dtw.compute(tsX, SdX).getDistance()+dtw.compute(tsY, SdY).getDistance()+dtw.compute(tsZ, SdZ).getDistance();
        values[3]=dtw.compute(tsX, ZdX).getDistance()+dtw.compute(tsY, ZdY).getDistance()+dtw.compute(tsZ, ZdZ).getDistance();

        txv_I.setText(""+values[0]);
        txv_O.setText(""+values[1]);
        txv_S.setText(""+values[2]);
        txv_Z.setText(""+values[3]);

        String []det={"I","O","S","Z"};
        int idx=findMax(values);
        txv_result.setText(det[idx]);

        if(flag==false) writeFile(filename,values,det[idx]);


    }

    // 파일 쓰는 코드
    public void writeFile(String filename,double []values,String target){
        try{
            String dirpath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/myApp";
            File dir=new File(dirpath);
            if(!dir.exists()){
                dir.mkdir();
            }

            File file=new File(dir+"/"+filename+".txt");
            if(!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw=new FileWriter(file,true);
            fw.write(""+values[0]+","+values[1]+","+values[2]+","+values[3]+","+target+"\n");

            fw.flush();
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int findMax(double []values){
        int idx=0;
        double min=values[0];
        for(int i=1;i<4;i++){
            if(values[i]<min){
                min=values[i];
                idx=i;
            }
        }
        return idx;
    }

    class myThread extends Thread{

        @Override
        public void run() {
            try{
                while(!Thread.currentThread().isInterrupted()){
                    readSensorData();
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }
}
