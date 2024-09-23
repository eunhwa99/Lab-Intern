package com.example.sensortest0219;

public class RuleSystem {

    public int xMoveDir, xMoveCnt;
    public boolean isError;
    public double velX;
    public int totalTried = 0;
    public int[] detected = {0,0,0,0};

    // constants
    public static double border = 0.1;
    public static int ERROR_FOUND=3;
    public static int GESTURE_I = 0;
    public static int GESTURE_S = 1;
    public static int GESTURE_Z = 2;
    public static int UNDEFINED= -1;
    public static int AXIS_X = 0;
    public static int AXIS_Y = 1;
    public static int AXIS_Z = 2;

    public RuleSystem(){

    }

    public void initSystem(){
        xMoveDir=xMoveCnt=totalTried=0;
        isError=false;
        velX=0.0;
        initCntArray();
    }

    public void startMotion(){
        xMoveDir=xMoveCnt=0;
        isError=false;
        velX=0.0;
    }

    public void initCntArray(){
        totalTried=0;
        for(int i=0;i<detected.length;i++){
            detected[i]=0;
        }
    }

    public int[] getDetected(){
        return detected;
    }

    public int getTotalTried(){
        return totalTried;
    }

    public void checkDirX(float accX, double dt){
        velX=velX+accX*dt;
        if(xMoveCnt<3){
            if(velX<-border && (xMoveDir%2==1||xMoveCnt==0)){
                xMoveCnt++;
                xMoveDir=xMoveDir*2;
            }
            if(velX>border && (xMoveDir%2==0||xMoveCnt==0)){
                xMoveCnt++;
                xMoveDir=xMoveDir*2+1;
            }
        }
    }

    public int getxMoveDir(){
        return xMoveDir&7;
    }
}
