package com.example.sensortest0219;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItem;
import androidx.appcompat.view.menu.ActionMenuItemView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ActionMenuView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.annotation.Target;

import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Sensor gyroSensor;
    private Sensor accelSensor;
    private SensorManager sensorManager;

    private double timestamp;
    private double dt;
    private double RAD2DGR=180/Math.PI;
    private static final double NS2S = 1.0f/1000000000.0f;
    private float[] gyros={0,0,0}, accels={0,0,0};

    private System1 system1;
    private System2 system2;
    private RuleSystem system;
    private int detSystem=1;

    private Button btnStart,btnEnd,btnReset;
    private TextView textMessage,textI,textS,textZ,textNone,textTotal,textTitle;
    private LinearLayout table,mainLayout;

    private static String totalMessage = "You have tried ";
    private static String[] motions = {"I","S","Z","Nothing"};
    private static int INITIAL_OPEN = 1;
    private static int INITIAL_TRIAL = 1;
    private static int NOT_INITIAL_OPEN = 0;

    private boolean isMeasuring = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelSensor=sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        sensorManager.registerListener(this,gyroSensor,sensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this,accelSensor,sensorManager.SENSOR_DELAY_GAME);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnEnd = (Button) findViewById(R.id.btnEnd);
        btnReset= (Button) findViewById(R.id.btnReset);
        textI=findViewById(R.id.textCountI);
        textS=findViewById(R.id.textCountS);
        textZ=findViewById(R.id.textCountZ);
        textNone=findViewById(R.id.textCountUnknown);
        textTotal=findViewById(R.id.textCountTotal);
        textMessage = (TextView) findViewById(R.id.textMessage);
        textTitle = findViewById(R.id.textTitle);
        table=findViewById(R.id.table);
        mainLayout=findViewById(R.id.mainLayout);

        system1 = new System1();
        system2 = new System2();
        initSystem();
        setButtonEvent();

        SharedPreferences preferences = getSharedPreferences("sensorTest",MODE_PRIVATE);
        int initialOpen = preferences.getInt("initialOpen",INITIAL_OPEN);
        //int initialOpen = INITIAL_OPEN;

        if(initialOpen==INITIAL_OPEN){
            showGetStarted();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("initialOpen",NOT_INITIAL_OPEN);
            editor.commit();
        }
    }

    public void setButtonEvent(){
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnStart.setVisibility(View.GONE);
                btnEnd.setVisibility(View.VISIBLE);
                isMeasuring=true;
                startMotion();
            }
        });

        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnStart.setVisibility(View.VISIBLE);
                btnEnd.setVisibility(View.GONE);
                isMeasuring=false;
                findGesture();
                updateCount(system.getTotalTried(),system.getDetected());
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initCntArray();
                textMessage.setText("");
                updateCount(system.getTotalTried(),system.getDetected());
            }
        });
    }

    public void findGesture(){
        switch(detSystem){
            case 1:
                showToast("Found "+motions[system1.findGesture()]);
                showMessage("diff : "+system1.getDiff()+"\nxDet : "+system1.getxMoveDir());
                break;
            case 2:
                showToast("Found "+motions[system2.findGesture()]);
                showMessage("xDet : "+system2.getxMoveDir()+"\nxAccel : "+system2.xAccelDir+"\nzGyro: "+system2.zGyroDir);
                break;
            case 3:
                showToast("Found "+motions[system2.findGestureImproved()]);
                showMessage("xDet : "+system2.getxMoveDir());
                break;
        }
    }

    public void initCntArray(){
        switch(detSystem){
            case 1:
                system1.initCntArray();
                break;
            case 2:
            case 3:
                system2.initCntArray();
                break;
        }
        updateCount(system.getTotalTried(),system.getDetected());
    }

    public void initSystem(){
        switch(detSystem){
            case 1:
                system1.initSystem();
                system=system1;
                break;
            case 2:
            case 3:
                system2.initSystem();
                system=system2;
                break;
        }
        cleanScreen();
    }

    public void startMotion(){
        switch(detSystem){
            case 1:
                system1.startMotion();
                break;
            case 2:
            case 3:
                system2.startMotion();
                break;
        }
    }

    public void applyRule(float[] gyros, float[] accels, double dt){
        switch(detSystem){
            case 1:
                system1.applyRule(gyros,accels,dt);
                break;
            case 2:
            case 3:
                system2.applyRule(gyros,accels,dt);
                break;
        }
    }

    public void cleanScreen(){
        textTitle.setText("System "+detSystem);
        updateCount(system.getTotalTried(),system.getDetected());
        showMessage("");
    }

    public void showMessage(String message){
        textMessage.setText(message);
    }

    public void updateCount(int totalTried, int[] detected){
        textTotal.setText(totalMessage+totalTried+" times! ");
        textI.setText(""+detected[0]);
        textS.setText(""+detected[1]);
        textZ.setText(""+detected[2]);
        textNone.setText(""+detected[3]);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        dt=(event.timestamp-timestamp)*NS2S;
        timestamp=event.timestamp;

        if(event.sensor==gyroSensor){
            gyros=event.values;
        }

        if(event.sensor==accelSensor){
            accels=event.values;
        }

        if (dt - timestamp*NS2S != 0) {
            if(isMeasuring)
                applyRule(gyros,accels,dt);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this,gyroSensor,sensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this,accelSensor,sensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_rulebase1:
                detSystem=1;
                showToast("Move to System"+detSystem);
                break;
            case R.id.menu_rulebase2:
                detSystem=2;
                showToast("Move to System"+detSystem);
                break;
            case R.id.menu_rulebase3:
                detSystem=3;
                showToast("Move to Improved System");
                break;
        }
        initSystem();
        return super.onOptionsItemSelected(item);
    }

    public void showToast(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    private void showIntroDismissOnTarget(String title, String text, View view, int sequenceCnt){
        new GuideView.Builder(this)
                .setTitle(title)
                .setContentText(text)
                .setTargetView(view)
                .setContentTextSize(12)
                .setTitleTextSize(16)
                .setDismissType(DismissType.targetView)
                .setGuideListener(new GuideListener() {
                    @Override
                    public void onDismiss(View view) {
                        switch(sequenceCnt) {
                            case 6:
                                showIntroDismissOnTarget("Done?","Click right after the motion",btnEnd,sequenceCnt+1);
                                break;
                            case 7:
                                showIntro("Developer Log","Only for developers",textMessage,sequenceCnt+1);
                                break;
                        }
                    }
                })
                .build()
                .show();
    }

    private void showIntro(String title, String text, View view, int sequenceCnt){
        new GuideView.Builder(this)
                .setTitle(title)
                .setContentText(text)
                .setTargetView(view)
                .setContentTextSize(12)
                .setTitleTextSize(16)
                .setDismissType(DismissType.anywhere)
                .setGuideListener(new GuideListener() {
                    @SuppressLint("RestrictedApi")
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onDismiss(View view) {
                        switch(sequenceCnt) {
                            case 1:
                                showIntro("System","The selected system",textTitle,sequenceCnt+1);
                                break;
                            case 2:
                                showIntro("Total trials","For now 0 time!",textTotal,sequenceCnt+1);
                                break;
                            case 3:
                                showIntro("Reset","Initialize the total trials",btnReset,sequenceCnt+1);
                                break;
                            case 4:
                                showIntro("Table","Results of motion recognitions\n'?' stands for error",table,sequenceCnt+1);
                                break;
                            case 5:
                                showIntroDismissOnTarget("Start!","Click and draw 'I'\nPlease click here",btnStart,sequenceCnt+1);
                                break;
                        }
                    }
                })
                .build()
                .show();
    }

    public void showGetStarted(){
        int sequenceCnt=1;
        showIntro("Hello!","Rule-based System\nfor motion recognition",mainLayout,sequenceCnt);
    }
}