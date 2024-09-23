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

public class OrientationTestActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor orientationSensor;
    private LineChart lineChart;
    int DATA_RANGE = 1000;

    TextView txvCurrOrientationX, txvCurrOrientationY, txvCurrOrientationZ;
    TextView txvMaxOrientationX, txvMaxOrientationY, txvMaxOrientationZ;
    TextView txvMinOrientationX, txvMinOrientationY, txvMinOrientationZ;

    float cx, cy, cz, ctotal;
    float maxx = -1000000, maxy = -1000000, maxz = -1000000;
    float minx = 1000000, miny = 1000000, minz = 1000000;

    ArrayList<Entry> xVal, yVal, zVal;
    LineDataSet setXcomp, setYcomp, setZcomp;
    ArrayList<ILineDataSet> lineDataSets;
    LineData lineData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orientation_test);

        txvCurrOrientationX = (TextView) findViewById(R.id.txvCurrOrientationX);
        txvCurrOrientationY = (TextView) findViewById(R.id.txvCurrOrientationY);
        txvCurrOrientationZ = (TextView) findViewById(R.id.txvCurrOrientationZ);
        txvMaxOrientationX = (TextView) findViewById(R.id.txvMaxOrientationX);
        txvMaxOrientationY = (TextView) findViewById(R.id.txvMaxOrientationY);
        txvMaxOrientationZ = (TextView) findViewById(R.id.txvMaxOrientationZ);
        txvMinOrientationX = (TextView) findViewById(R.id.txvMinOrientationX);
        txvMinOrientationY = (TextView) findViewById(R.id.txvMinOrientationY);
        txvMinOrientationZ = (TextView) findViewById(R.id.txvMinOrientationZ);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        lineChart = (LineChart) findViewById(R.id.orientationChart);

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
        yLaxis.setAxisMaximum(360);
        yLaxis.setAxisMinimum(-360);

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
        OrientationTestActivity.MyThread thread = new OrientationTestActivity.MyThread();
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float OrientationX = event.values[0];
        float OrientationY = event.values[1];
        float OrientationZ = event.values[2];

        double total = Math.sqrt(Math.pow(OrientationX,2) + Math.pow(OrientationY, 2) + Math.pow(OrientationZ, 2));

        cx = OrientationX;
        cy = OrientationY;
        cz = OrientationZ;
        ctotal = (float)total;


        if(maxx<cx) maxx=cx;
        if(maxy<cy) maxy=cy;
        if(maxz<cz) maxz=cz;

        if(minx>cx) minx=cx;
        if(miny>cy) miny=cy;
        if(minz>cz) minz=cz;

        txvCurrOrientationX.setText("X : "+cx);
        txvCurrOrientationY.setText("Y : "+cy);
        txvCurrOrientationZ.setText("Z : "+cz);
        txvMaxOrientationX.setText("X : "+maxx);
        txvMaxOrientationY.setText("Y : "+maxy);
        txvMaxOrientationZ.setText("Z : "+maxz);
        txvMinOrientationX.setText("X : "+minx);
        txvMinOrientationY.setText("Y : "+miny);
        txvMinOrientationZ.setText("Z : "+minz);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
