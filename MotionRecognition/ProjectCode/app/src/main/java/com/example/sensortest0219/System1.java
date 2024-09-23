package com.example.sensortest0219;

import android.util.Log;

public class System1 extends RuleSystem{

    public int sumCnt;
    public double sumX,sumZ;

    public System1(){
        super();
        initSystem();
    }

    @Override
    public void initSystem() {
        super.initSystem();
        sumCnt=0;
        sumX=sumZ=0.0;
    }

    @Override
    public void startMotion() {
        super.startMotion();
        sumCnt=0;
        sumX=sumZ=0.0;
    }

    public void applyRule(float[] gyros, float[] accels, double dt){
        findError(gyros);
        addAbsSum(accels[AXIS_X],accels[AXIS_Z]);
        checkDirX(accels[AXIS_X],dt);
    }

    public void findError(float []gyros){
        if(gyros[AXIS_X] >5 || gyros[AXIS_Y] >5 || gyros[AXIS_Z] >5){
            isError = true;
        }
        if(gyros[AXIS_X] <-5 || gyros[AXIS_Y] <-5 || gyros[AXIS_Z] <-5){
            isError = true;
        }
    }

    public void addAbsSum(double accX, double accZ){
        sumX+=accX>0?accX:-accX;
        sumZ+=accZ>0?accZ:-accZ;
        sumCnt++;
    }

    public int findGesture(){
        int gesture = UNDEFINED;
        double diff = sumZ/sumCnt-sumX/sumCnt;
        xMoveDir=xMoveDir&7;
        if(isError){
            gesture = ERROR_FOUND;
        }
        else if(diff>0.7){
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

    public double getDiff(){
        return sumZ/sumCnt-sumX/sumCnt;
    }
}
