package com.example.wifidirecttest;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;


import static com.example.wifidirecttest.CrowdTask.TASK_COMPLETE;
import static com.example.wifidirecttest.MainActivity.TAG;
import static com.example.wifidirecttest.MainActivity.TAG4;



interface SerializableRunnable<T> extends Serializable, Runnable {}

public class TaskExecutable implements SerializableRunnable {
    public static final int SENSING_TASK = 1;
    public static final int LARGE_FILE_TASK = 2;
    transient MainActivity mActivity;
    private SensorManager mSensorManager;
    private Sensor mLightSensor;
    private float mLightQuantity;
    private SensorEventListener listener;
    private String taskId;
    private int taskType;

    public TaskExecutable(int taskType) {
        this.taskType = taskType;
    }

    public void run() {
        /*
        try {
            Thread.sleep(5000);//Simulate lag time
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        switch (taskType){
            case SENSING_TASK:
                mSensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
                mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
                listener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        mLightQuantity = event.values[0];
                        Log.d(TAG4, "LIGHT VALUE IS : " + mLightQuantity);
                        writeToFile(String.valueOf(mLightQuantity));
                        File data = new File(mActivity.getExternalFilesDir(null) + File.separator +  "data_" + taskId + ".txt");
                        if (!(data.isFile())) {
                            Log.d(TAG, "File not found in "+ mActivity.getExternalFilesDir(null) + File.separator +  "data_" + taskId + ".txt" + " RIGHT AFTER FUCKING CREATION");
                        }
                        finishSenseTask();
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    }
                };
                mSensorManager.registerListener(listener, mLightSensor, SensorManager.SENSOR_DELAY_UI);
                break;
            case LARGE_FILE_TASK:
                RandomAccessFile test_file = createTestFile(mActivity.getExternalFilesDir(null) + File.separator + "data_" + taskId + ".txt",1024*1024*10);
                MainActivity.finishTask(TASK_COMPLETE);
                break;
        }

    }

    public void finishSenseTask() {
        mSensorManager.unregisterListener(listener);
        MainActivity.finishTask(TASK_COMPLETE);
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    private void sendData() {

    }

    private void writeToFile(String content) {
        try {
            Log.d(TAG4, "Writing to file: " + mActivity.getExternalFilesDir(null) + File.separator + "data_" + taskId + ".txt");
            File file = new File(mActivity.getExternalFilesDir(null) + File.separator + "data_" + taskId + ".txt");

            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            writer.append(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d (TAG, "PROBLEM PROBLEM PROBLEM");
        }
    }

    public void setActivity(MainActivity mainActivity){
        this.mActivity = mainActivity;
    }

    public RandomAccessFile createTestFile(String path, int size){
        Log.d(TAG, "Creating test file of size " + size);
        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile(path, "rw");
            f.setLength(size);
        } catch (FileNotFoundException e) {
            Log.d(TAG, "CREATE TEST FILE NOT FOUND");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "CREATE TEST FILE IOExc");
            e.printStackTrace();
        }
        return f;
    }
}