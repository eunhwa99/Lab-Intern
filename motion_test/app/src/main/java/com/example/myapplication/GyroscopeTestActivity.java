package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class GyroscopeTestActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private LineChart lineChart;
    int DATA_RANGE = 1000;

    TextView txvCurrGyroX, txvCurrGyroY, txvCurrGyroZ;
    TextView txvMaxGyroX, txvMaxGyroY, txvMaxGyroZ;
    TextView txvMinGyroX, txvMinGyroY, txvMinGyroZ;

    float cx, cy, cz, ctotal;
    float maxx, maxy, maxz ;
    float minx, miny, minz;

    ArrayList<Entry> xVal, yVal, zVal;
    LineDataSet setXcomp, setYcomp, setZcomp;
    ArrayList<ILineDataSet> lineDataSets;
    LineData lineData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyroscope_test);

        txvCurrGyroX = (TextView) findViewById(R.id.txvCurrGyroX);
        txvCurrGyroY = (TextView) findViewById(R.id.txvCurrGyroY);
        txvCurrGyroZ = (TextView) findViewById(R.id.txvCurrGyroZ);
        txvMaxGyroX = (TextView) findViewById(R.id.txvMaxGyroX);
        txvMaxGyroY = (TextView) findViewById(R.id.txvMaxGyroY);
        txvMaxGyroZ = (TextView) findViewById(R.id.txvMaxGyroZ);
        txvMinGyroX = (TextView) findViewById(R.id.txvMinGyroX);
        txvMinGyroY = (TextView) findViewById(R.id.txvMinGyroY);
        txvMinGyroZ = (TextView) findViewById(R.id.txvMinGyroZ);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        lineChart = (LineChart) findViewById(R.id.gyroscopeChart);

        chartInit();
        threadStart();
    }

    private void chartInit(){
        maxx = -1000000; maxy = -1000000; maxz = -1000000;
        minx = 1000000; miny = 1000000; minz = 1000000;
        //lineChart.setAutoScaleMinMaxEnabled(true);

        XAxis xaxis = lineChart.getXAxis();
        YAxis yLaxis = lineChart.getAxisLeft();
        YAxis yRaxis = lineChart.getAxisRight();

        // x축은 Label표시 없음. DATA_RANGE을 최대값으로 설정, x축 위치는 아래쪽
        xaxis.setDrawLabels(false);
        xaxis.setAxisMaximum(DATA_RANGE+10);
        xaxis.setAxisMinimum(0);
        xaxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // y축은 검은색으로 표시, 최대값 20, 최소값 -20까지 표현
        yLaxis.setTextColor(Color.WHITE);
        yLaxis.setAxisMaximum(20);
        yLaxis.setAxisMinimum(-20);

        // y축은 하나만 사용(yLaxis - 왼쪽 y축)
        yRaxis.setDrawLabels(false);
        yRaxis.setDrawAxisLine(false);
        yRaxis.setDrawGridLines(false);

        xVal = new ArrayList<Entry>();

        setXcomp = new LineDataSet(xVal, "X");
        setXcomp.setColor(Color.RED);
        setXcomp.setDrawValues(false);
        setXcomp.setDrawCircles(false);

        yVal = new ArrayList<Entry>();

        setYcomp = new LineDataSet(yVal, "Y");
        setYcomp.setColor(Color.BLUE);
        setYcomp.setDrawValues(false);
        setYcomp.setDrawCircles(false);

        zVal = new ArrayList<Entry>();

        setZcomp = new LineDataSet(zVal, "Z");
        setZcomp.setColor(Color.GREEN);
        setZcomp.setDrawValues(false);
        setZcomp.setDrawCircles(false);


        lineDataSets = new ArrayList<ILineDataSet>();
        lineDataSets.add(setXcomp);
        lineDataSets.add(setYcomp);
        lineDataSets.add(setZcomp);
        lineData = new LineData(lineDataSets);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    public void chartUpdate(){
        if(!xVal.isEmpty()){
            if(xVal.size() > DATA_RANGE){
                xVal.remove(0);
                yVal.remove(0);
                zVal.remove(0);
                for(int i=0;i<DATA_RANGE;i++){
                    xVal.get(i).setX(i);
                    yVal.get(i).setX(i);
                    zVal.get(i).setX(i);
                }
            }
        }
        xVal.add(new Entry(xVal.size(), cx));
        yVal.add(new Entry(yVal.size(), cy));
        zVal.add(new Entry(zVal.size(), cz));
        setXcomp.notifyDataSetChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    class MyThread extends Thread{
        @Override
        public void run() {
            while(true){
                chartUpdate();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void threadStart() {
        GyroscopeTestActivity.MyThread thread = new GyroscopeTestActivity.MyThread();
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float gyroX = event.values[0];
        float gyroY = event.values[1];
        float gyroZ = event.values[2];

        double total = Math.sqrt(Math.pow(gyroX,2) + Math.pow(gyroY, 2) + Math.pow(gyroZ, 2));

        cx = gyroX;
        cy = gyroY;
        cz = gyroZ;
        ctotal = (float)total;

        if(maxx<cx) maxx=cx;
        if(maxy<cy) maxy=cy;
        if(maxz<cz) maxz=cz;

        if(minx>cx) minx=cx;
        if(miny>cy) miny=cy;
        if(minz>cz) minz=cz;

        txvCurrGyroX.setText("X : "+cx);
        txvCurrGyroY.setText("Y : "+cy);
        txvCurrGyroZ.setText("Z : "+cz);
        txvMaxGyroX.setText("X : "+maxx);
        txvMaxGyroY.setText("Y : "+maxy);
        txvMaxGyroZ.setText("Z : "+maxz);
        txvMinGyroX.setText("X : "+minx);
        txvMinGyroY.setText("Y : "+miny);
        txvMinGyroZ.setText("Z : "+minz);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
