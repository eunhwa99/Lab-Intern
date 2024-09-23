package com.example.sensortest0219;

import android.util.Log;

public class System2 extends RuleSystem{

    public double []squareGyro = {0,0,0};
    public double []squareAccel = {0,0,0};

    public static int ARRAY_LENGTH = 3;
    public static int border = 3;

    public int xAccelDir, xAccelCnt;
    public int zGyroDir, zGyroCnt;

    public System2(){
        super();
        initSystem();
    }

    @Override
    public void initSystem() {
        super.initSystem();
        xAccelCnt=xAccelDir=0;
        zGyroCnt=zGyroDir=0;
        initSquareArray();
    }

    @Override
    public void startMotion() {
        super.startMotion();
        xAccelCnt=xAccelDir=0;
        zGyroCnt=zGyroDir=0;
        initSquareArray();
    }

    public void initSquareArray(){
        for(int i=0;i<ARRAY_LENGTH;i++){
            squareGyro[i]=squareAccel[i]=0.0;
        }
    }

    public void applyRule(float []gyros, float []accels, double dt){
        for(int i=0;i<ARRAY_LENGTH;i++){
            gyros[i]=filterNoise(gyros[i]);
            accels[i]=filterNoise(accels[i]);
        }
        sumSquare(gyros,accels);
        checkDirX(accels[AXIS_X],dt);
        checkAccelX(accels[AXIS_X]);
        checkgyroZ(gyros[AXIS_Z]);
        Log.e("log1","accelX : "+accels[AXIS_X]+", gyroZ : "+gyros[AXIS_Z]+", dirX : "+xAccelDir+", dirZ : "+zGyroDir+"");
    }

    public float filterNoise(float value){
        return (value <=border && value >=-border)? 0:value;
    }

    public void sumSquare(float [] gyro, float [] accel){
        for(int i=0;i<ARRAY_LENGTH;i++){
            squareAccel[i]+=accel[i]*accel[i];
            squareGyro[i]+=gyro[i]*gyro[i];
        }
    }

    public int findMaxSquare(double[] array){
        int maxIndex = 0;
        double maxValue = array[0];
        for(int i=0;i<ARRAY_LENGTH;i++){
            if(maxValue<array[i]){
                maxValue=array[i];
                maxIndex=i;
            }
        }
        return maxIndex;
    }

    public void checkAccelX(float accX){
        if(xAccelCnt<4){
            if(accX<-border&&(xAccelDir%2==1||xAccelCnt==0)){
                xAccelCnt++;
                xAccelDir=xAccelDir*2;
            }
            if(accX>border&&(xAccelDir%2==0||xAccelCnt==0)){
                xAccelCnt++;
                xAccelDir=xAccelDir*2+1;
            }
        }
    }

    public void checkgyroZ(float gyroZ){
        if(zGyroCnt<3){
            if(gyroZ<-border && (zGyroDir%2==1||zGyroCnt==0)){
                zGyroCnt++;
                zGyroDir=zGyroDir*2;
            }
            if(gyroZ>border && (zGyroDir%2==0||zGyroCnt==0)){
                zGyroCnt++;
                zGyroDir=zGyroDir*2+1;
            }
        }
    }

    public int findGesture(){
        int gesture = UNDEFINED;
        int maxIndexOfGyro = findMaxSquare(squareGyro);
        int maxIndexOfAccel = findMaxSquare(squareAccel);
        // xMoveDir=xMoveDir&7;
        zGyroDir=zGyroDir&7;
        xAccelDir=xAccelDir&15;
        if(maxIndexOfAccel==AXIS_Z && maxIndexOfGyro==AXIS_X){
            gesture = GESTURE_I;
        }
        else if(xAccelDir==5&&zGyroDir==5){
            gesture = GESTURE_S;
        }
        else if(xAccelDir==10&&zGyroDir==2){
            gesture = GESTURE_Z;
        }
        else{
            gesture = ERROR_FOUND;
        }
        totalTried++;
        detected[gesture]++;
        return gesture;
    }

    public int findGestureImproved(){
        int gesture = UNDEFINED;
        int maxIndexOfGyro = findMaxSquare(squareGyro);
        int maxIndexOfAccel = findMaxSquare(squareAccel);
        xMoveDir=xMoveDir&7;
        if(maxIndexOfAccel==AXIS_Z && maxIndexOfGyro==AXIS_X){
            gesture = GESTURE_I;
        }
        else if(xMoveDir==2){
            gesture = GESTURE_S;
        }
        else if(xMoveDir==5){
            gesture = GESTURE_Z;
        }
        else{
            gesture = ERROR_FOUND;
        }
        totalTried++;
        detected[gesture]++;
        return gesture;
    }
}
