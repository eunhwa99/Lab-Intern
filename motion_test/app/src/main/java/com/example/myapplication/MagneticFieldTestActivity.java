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

public class MagneticFieldTestActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor magneticFieldSensor;
    private LineChart lineChart;
    int DATA_RANGE = 1000;

    TextView txvCurrMagneticFieldX, txvCurrMagneticFieldY, txvCurrMagneticFieldZ;
    TextView txvMaxMagneticFieldX, txvMaxMagneticFieldY, txvMaxMagneticFieldZ;
    TextView txvMinMagneticFieldX, txvMinMagneticFieldY, txvMinMagneticFieldZ;

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
        setContentView(R.layout.activity_magnetic_field_test);

        txvCurrMagneticFieldX = (TextView) findViewById(R.id.txvCurrMagneticFieldX);
        txvCurrMagneticFieldY = (TextView) findViewById(R.id.txvCurrMagneticFieldY);
        txvCurrMagneticFieldZ = (TextView) findViewById(R.id.txvCurrMagneticFieldZ);
        txvMaxMagneticFieldX = (TextView) findViewById(R.id.txvMaxMagneticFieldX);
        txvMaxMagneticFieldY = (TextView) findViewById(R.id.txvMaxMagneticFieldY);
        txvMaxMagneticFieldZ = (TextView) findViewById(R.id.txvMaxMagneticFieldZ);
        txvMinMagneticFieldX = (TextView) findViewById(R.id.txvMinMagneticFieldX);
        txvMinMagneticFieldY = (TextView) findViewById(R.id.txvMinMagneticFieldY);
        txvMinMagneticFieldZ = (TextView) findViewById(R.id.txvMinMagneticFieldZ);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        lineChart = (LineChart) findViewById(R.id.magneticFieldChart);

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
        yLaxis.setAxisMaximum(600);
        yLaxis.setAxisMinimum(-600);

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
        MagneticFieldTestActivity.MyThread thread = new MagneticFieldTestActivity.MyThread();
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float MagneticFieldX = event.values[0];
        float MagneticFieldY = event.values[1];
        float MagneticFieldZ = event.values[2];

        double total = Math.sqrt(Math.pow(MagneticFieldX,2) + Math.pow(MagneticFieldY, 2) + Math.pow(MagneticFieldZ, 2));

        cx = MagneticFieldX;
        cy = MagneticFieldY;
        cz = MagneticFieldZ;
        ctotal = (float)total;


        if(maxx<cx) maxx=cx;
        if(maxy<cy) maxy=cy;
        if(maxz<cz) maxz=cz;

        if(minx>cx) minx=cx;
        if(miny>cy) miny=cy;
        if(minz>cz) minz=cz;

        txvCurrMagneticFieldX.setText("X : "+cx);
        txvCurrMagneticFieldY.setText("Y : "+cy);
        txvCurrMagneticFieldZ.setText("Z : "+cz);
        txvMaxMagneticFieldX.setText("X : "+maxx);
        txvMaxMagneticFieldY.setText("Y : "+maxy);
        txvMaxMagneticFieldZ.setText("Z : "+maxz);
        txvMinMagneticFieldX.setText("X : "+minx);
        txvMinMagneticFieldY.setText("Y : "+miny);
        txvMinMagneticFieldZ.setText("Z : "+minz);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
