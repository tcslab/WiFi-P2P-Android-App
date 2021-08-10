package com.example.wifidirecttest;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.LinkedList;

import static com.example.wifidirecttest.MainActivity.REQUEST;
import static com.example.wifidirecttest.MainActivity.RESPONSE;
import static com.example.wifidirecttest.MainActivity.TAG4;


public class CrowdTask implements Serializable, Parcelable {
    private static final int LIGHT_READ = 1;
    private static final int LARGE_FILE = 2;
    private final String id;
    private final String sourceNode;
    private final TaskExecutable task;
    private int status;
    private LinkedList<String> piggyPath = new LinkedList<>();

    public static final int TASK_SUBMITTED = 0;
    public static final int TASK_RUNNING = 1;
    public static final int TASK_COMPLETE = 2;
    public static final int TASK_ON_HOLD = 3;
    public static final int TASK_FAILED = -1;
    public static final String TAG = "LVL1";


    public CrowdTask(String sourceNode, int type, String responseText) {
        String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        String macAdress = MainActivity.wifiManager.getConnectionInfo().getMacAddress();
        this.id = macAdress + ":" + timestamp;
        this.sourceNode = sourceNode;
        this.status = TASK_SUBMITTED;
        this.piggyPath.add(sourceNode);
        TaskExecutable task = null;
        switch (type) {
            case REQUEST:
                task = new TaskExecutable(TaskExecutable.LARGE_FILE_TASK) {
                    @Override
                    public void run() {
                        mActivity.makeDialog(sourceNode, type, responseText);
                        super.run();
                    }
                };
                break;
            case RESPONSE: {
                task =  new TaskExecutable(TaskExecutable.LARGE_FILE_TASK) {
                    @Override
                    public void run() {
                        mActivity.makeDialog(sourceNode, type, responseText);
                        super.run();
                    }
                };
            }
        }
        this.task = task;
    }

    public CrowdTask(String sourceNode, int taskType){
        String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        String macAdress = MainActivity.wifiManager.getConnectionInfo().getMacAddress();
        this.id = macAdress + ":" + timestamp;
        TaskExecutable task=null;
        switch (taskType) {
            case LIGHT_READ:
                task = new TaskExecutable(TaskExecutable.SENSING_TASK);
                break;
            case LARGE_FILE:
                task = new TaskExecutable(TaskExecutable.LARGE_FILE_TASK);
                break;
        }
        task.setTaskId(this.id);
        this.task=task;
        this.sourceNode = sourceNode;
        this.piggyPath.add(sourceNode);
    }

    public void run(){
        Log.d(TAG, "initiating CrowdTask run");
        setStatus(TASK_RUNNING);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                task.run();
            }
        });
        t.start();

        /*ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<Integer> result = executor.submit(task);
        try {
            setStatus(result.get());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    public String getSourceNode() {
        return sourceNode;
    }

    public TaskExecutable getTask() {
        return task;
    }

    public int getStatus() {
        return status;
    }

    public String getId() {
        return id;
    }

    public void setStatus(int status) {
        this.status = status;
        if (status==TASK_COMPLETE){
            Log.d(TAG, "TASK COMPLETE");
        }
    }

    public LinkedList<String> getPiggyPath() {
        return piggyPath;
    }

    public void setPiggyPath(LinkedList<String> piggyPath) {
        this.piggyPath = (LinkedList<String>) piggyPath.clone();
    }

    public void setActivity(MainActivity mainActivity){
        task.setActivity(mainActivity);
    }

    //PARCEL SPECIFIC FUNCTIONS
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(sourceNode);
        dest.writeInt(status);
    }
    protected CrowdTask(Parcel in) {
        id = in.readString();
        sourceNode = in.readString();
        status = in.readInt();
        task = null;
    }
    public static final Creator<CrowdTask> CREATOR = new Creator<CrowdTask>() {
        @Override
        public CrowdTask createFromParcel(Parcel in) {
            return new CrowdTask(in);
        }

        @Override
        public CrowdTask[] newArray(int size) {
            return new CrowdTask[size];
        }
    };


}
