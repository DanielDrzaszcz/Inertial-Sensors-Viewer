package com.dandrzas.inertialsensors.data;

import java.util.Observable;

public class InertialTrackingAlgorithm  extends Observable {
    private SensorData sensorAccelerometer;
    private final String TAG = InertialTrackingAlgorithm.class.getSimpleName();
    private float[] calculatedMovement = new float[3];
    private float[] linearAcceleration = new float[3];
    private float[] gravity = new float[3];
    private float[] linearAccelerationPrev = new float[3];
    private float[] calculatedVelocity = new float[3];
    private float[] calculatedVelocityPrev = new float[3];
    private long previousSampleTime;
    private long actualSampleTime;
    private boolean isRunning;
    boolean firstCalcDone;
    private float[] initialAcceleration = new float[3];
    private int calcCounter;
    private IFOrientationAlgorithm orientationAlgorithm;
    private float[] accelerationGlobal = new float[3];
    private final int GRAVITYBUFFSIZE = 100;
    private final long INITIALSAMPLES = 50000;
    private float[][] gravityBuffer = new float[3][GRAVITYBUFFSIZE];

    public InertialTrackingAlgorithm(SensorData sensorAccelerometer, IFOrientationAlgorithm orientationAlgorithm) {
        this.sensorAccelerometer = sensorAccelerometer;
        this.orientationAlgorithm = orientationAlgorithm;
    }

    public void calc()
    {
        actualSampleTime = sensorAccelerometer.getSampleTime();
        if(!firstCalcDone){
            previousSampleTime = actualSampleTime;
            linearAccelerationPrev[0]=linearAcceleration[0];
            linearAccelerationPrev[1]=linearAcceleration[1];
            linearAccelerationPrev[2]=linearAcceleration[2];
            calculatedVelocityPrev[0]= calculatedVelocity[0];
            calculatedVelocityPrev[1]= calculatedVelocity[1];
            calculatedVelocityPrev[2]= calculatedVelocity[2];
        }
        if(calcCounter==INITIALSAMPLES){
           /* initialAcceleration[0] = accelerationGlobal[0];
            initialAcceleration[1] = accelerationGlobal[1];
            initialAcceleration[2] = accelerationGlobal[2];
*/
            float sum = 0;
            for (float grav:gravityBuffer[0]) {
                sum += grav;
            }
            initialAcceleration[0] = sum/GRAVITYBUFFSIZE;

            sum = 0;
            for (float grav:gravityBuffer[1]) {
                sum += grav;
            }
            initialAcceleration[1] = sum/GRAVITYBUFFSIZE;

            sum = 0;
            for (float grav:gravityBuffer[2]) {
                sum += grav;
            }
            initialAcceleration[2] = sum/GRAVITYBUFFSIZE;

            /*Log.d("InrtialTrackingTestIni ", "initialAcceleration[0]: " + initialAcceleration[0]);
            Log.d("InrtialTrackingTestIni ", "initialAcceleration[1]: " + initialAcceleration[1]);
            Log.d("InrtialTrackingTestIni ", "initialAcceleration[2]: " + initialAcceleration[2]);*/
        }
        calcLinearGravity();
        calculatedVelocity = calcIntegral(linearAcceleration, linearAccelerationPrev, calculatedVelocity);
        /*Log.d("InrtialTrackingTest: ", "velocity: " + calculatedVelocity[0]);
        Log.d("InrtialTrackingTest: ", "velocity: " + calculatedVelocity[1]);
        Log.d("InrtialTrackingTest: ", "velocity: " + calculatedVelocity[2]);*/

        calculatedMovement = calcIntegral(calculatedVelocity, calculatedVelocityPrev, calculatedMovement);
        setChanged();
        notifyObservers(Constants.INERTIAL_TRACKER_ID);

        linearAccelerationPrev[0]=linearAcceleration[0];
        linearAccelerationPrev[1]=linearAcceleration[1];
        linearAccelerationPrev[2]=linearAcceleration[2];
        calculatedVelocityPrev[0]= calculatedVelocity[0];
        calculatedVelocityPrev[1]= calculatedVelocity[1];
        calculatedVelocityPrev[2]= calculatedVelocity[2];
        previousSampleTime = actualSampleTime;
        firstCalcDone = true;
        calcCounter++;
        //Log.d("InrtialTrackingTestIni", "calcCounter: " + calcCounter);
    }

    private void clearData()
    {
        calculatedMovement[0] = 0;
        calculatedMovement[1] = 0;
        calculatedMovement[2] = 0;
        calculatedVelocity[0] = 0;
        calculatedVelocity[1] = 0;
        calculatedVelocity[2] = 0;
        firstCalcDone = false;
        calcCounter = 0;
        initialAcceleration[0]=0;
        initialAcceleration[1]=0;
        initialAcceleration[2]=0;
    }

    public void startComputing() {
        clearData();
        isRunning = true;
    }

    public void stopComputing() {
        isRunning = false;
    }

    public double getPreviousSampleTime() {
        return previousSampleTime;
    }

    private void calcLinearGravity(){
        float[] acceleration = new float[3];
        acceleration[0] = sensorAccelerometer.getSampleValue()[0];
        acceleration[1] = sensorAccelerometer.getSampleValue()[1];
        acceleration[2] = sensorAccelerometer.getSampleValue()[2];

        // Convert acceleration to global coordinates
        accelerationGlobal[0] = acceleration[0] * orientationAlgorithm.getRotationMatrixRPYNWU()[0] + acceleration[1] * orientationAlgorithm.getRotationMatrixRPYNWU()[1] + acceleration[2] * orientationAlgorithm.getRotationMatrixRPYNWU()[2];
        accelerationGlobal[1] = acceleration[0] * orientationAlgorithm.getRotationMatrixRPYNWU()[3] + acceleration[1] * orientationAlgorithm.getRotationMatrixRPYNWU()[4] + acceleration[2] * orientationAlgorithm.getRotationMatrixRPYNWU()[5];
        accelerationGlobal[2] = acceleration[0] * orientationAlgorithm.getRotationMatrixRPYNWU()[6] + acceleration[1] * orientationAlgorithm.getRotationMatrixRPYNWU()[7] + acceleration[2] * orientationAlgorithm.getRotationMatrixRPYNWU()[8];

  /*      accelerationGlobal[0] = acceleration[0];
        accelerationGlobal[1] = acceleration[1];
        accelerationGlobal[2] = acceleration[2];
*/
        if(calcCounter<INITIALSAMPLES) {
            gravity[0] = 0;
            gravity[1] = 0;
            gravity[2] = 0;
            linearAcceleration[0] = 0;
            linearAcceleration[1] = 0;
            linearAcceleration[2] = 0;
            if(INITIALSAMPLES-calcCounter<=GRAVITYBUFFSIZE){
                int index = (int)(INITIALSAMPLES-calcCounter-1);
                gravityBuffer[0][index] = accelerationGlobal[0];
                gravityBuffer[1][index] = accelerationGlobal[1];
                gravityBuffer[2][index] = accelerationGlobal[2];
            }
        }
        else{
          /*  gravity[0] = parAccelerometerHPFGain * gravity[0] + (1 - parAccelerometerHPFGain) * acceleration[0];
            gravity[1] = parAccelerometerHPFGain * gravity[1] + (1 - parAccelerometerHPFGain) * acceleration[1];
            gravity[2] = parAccelerometerHPFGain * gravity[2] + (1 - parAccelerometerHPFGain) * acceleration[2];
*/
            gravity[0] = initialAcceleration[0];
            gravity[1] = initialAcceleration[1];
            gravity[2] = initialAcceleration[2];
            linearAcceleration[0] = accelerationGlobal[0]-gravity[0];
            linearAcceleration[1] = accelerationGlobal[1]-gravity[1];
            linearAcceleration[2] = accelerationGlobal[2]-gravity[2];
        }

        float totalGravity = (float) Math.sqrt(gravity[0]*gravity[0]+gravity[1]*gravity[1]+gravity[2]*gravity[2]);
/*
        Log.d("InrtialTrackingTest: ", "gravity: " + gravity[0]);
        Log.d("InrtialTrackingTest: ", "gravity: " + gravity[1]);
        Log.d("InrtialTrackingTest: ", "gravity: " + gravity[2]);
        Log.d("InrtialTrackingTest: ", "total gravity: " + totalGravity);*/

        float totalAcceleration = (float) Math.sqrt(linearAcceleration[0]*linearAcceleration[0]+linearAcceleration[1]*linearAcceleration[1]+linearAcceleration[2]*linearAcceleration[2]);
       /* Log.d("InrtialTrackingTest: ", "linearAcceleration: " + linearAcceleration[0]);
        Log.d("InrtialTrackingTest: ", "linearAcceleration: " + linearAcceleration[1]);
        Log.d("InrtialTrackingTest: ", "linearAcceleration: " + linearAcceleration[2]);
        Log.d("InrtialTrackingTest: ", "total linearAcceleration: " + totalAcceleration);*/
    }

    private float[] calcIntegral(float[] orgSignal, float[] orgSignalPrev, float[] actIntegratedValue){
        float NS2S = 1.0f / 1000000000.0f;
        float[] integratedSignal = new float[3];

        integratedSignal[0] = (float)(actIntegratedValue[0] + ((actualSampleTime-previousSampleTime)*NS2S)*((orgSignalPrev[0]+orgSignal[0])/2));
        integratedSignal[1] = (float)(actIntegratedValue[1] + ((actualSampleTime-previousSampleTime)*NS2S)*((orgSignalPrev[1]+orgSignal[1])/2));
        integratedSignal[2] = (float)(actIntegratedValue[2] + ((actualSampleTime-previousSampleTime)*NS2S)*((orgSignalPrev[2]+orgSignal[2])/2));

        return integratedSignal;
    }

    public float[] getCalculatedMovement() {
        return calculatedMovement;
    }

    public float[] getCalculatedVelocity() {
        return calculatedVelocity;
    }

    public float[] getLinearAcceleration() {
        return linearAcceleration;
    }

    public float[] getGravity() {
        return gravity;
    }

    public float[] getAccelerationGlobal() {
        return accelerationGlobal;
    }

    public IFOrientationAlgorithm getOrientationAlgorithm() {
        return orientationAlgorithm;
    }

    public long getActualSampleTime() {
        return actualSampleTime;
    }

    public void setOrientationAlgorithm(IFOrientationAlgorithm orientationAlgorithm) {
        this.orientationAlgorithm = orientationAlgorithm;
    }

}
