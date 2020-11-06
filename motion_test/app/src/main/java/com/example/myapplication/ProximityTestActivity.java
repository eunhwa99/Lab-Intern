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

public class ProximityTestActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private LineChart lineChart;
    int DATA_RANGE = 1000;

    TextView txvCurrProximityData, txvMaxProximityData, txvMinProximityData;

    float cx;
    float maxdata, mindata;
    ArrayList<Entry> xVal;
    LineDataSet setXcomp;
    ArrayList<ILineDataSet> lineDataSets;
    LineData lineData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proximity_test);

        txvCurrProximityData = (TextView) findViewById(R.id.txvCurrProximityData);
        txvMaxProximityData = (TextView) findViewById(R.id.txvMaxProximityData);
        txvMinProximityData = (TextView) findViewById(R.id.txvMinProximityData);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        lineChart = (LineChart) findViewById(R.id.ProximityChart);

        chartInit();
        threadStart();
    }

    private void chartInit(){
        maxdata = -1000000; mindata = 1000000;
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
        yLaxis.setAxisMaximum(15);
        yLaxis.setAxisMinimum(0);

        // y축은 하나만 사용(yLaxis - 왼쪽 y축)
        yRaxis.setDrawLabels(false);
        yRaxis.setDrawAxisLine(false);
        yRaxis.setDrawGridLines(false);

        xVal = new ArrayList<Entry>();

        setXcomp = new LineDataSet(xVal, "X");
        setXcomp.setColor(Color.RED);
        setXcomp.setDrawValues(false);
        setXcomp.setDrawCircles(false);

        lineDataSets = new ArrayList<ILineDataSet>();
        lineDataSets.add(setXcomp);
        lineData = new LineData(lineDataSets);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    public void chartUpdate(){
        if(!xVal.isEmpty()){
            if(xVal.size() > DATA_RANGE){
                xVal.remove(0);
                for(int i=0;i<DATA_RANGE;i++){
                    xVal.get(i).setX(i);
                }
            }
        }
        xVal.add(new Entry(xVal.size(), cx));
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
        ProximityTestActivity.MyThread thread = new ProximityTestActivity.MyThread();
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        cx = event.values[0];
        if(maxdata<cx) maxdata=cx;
        if(mindata>cx) mindata=cx;

        txvCurrProximityData.setText("cm : "+cx);
        txvMaxProximityData.setText("cm : "+maxdata);
        txvMinProximityData.setText("cm : "+mindata);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
