package com.example.wifidirecttest;

import android.util.Log;

import java.io.Serializable;
import java.util.LinkedList;

import static com.example.wifidirecttest.MainActivity.TAG;
import static com.example.wifidirecttest.MainActivity.TAG2;
import static com.example.wifidirecttest.MainActivity.thisDeviceName;

public class MyMessage implements Serializable {
    private String id;
    private String source;
    private String immediateSender;
    private String dest;
    private CrowdTask task;
    private String taskIdCollected;
    private Payload payload;  //TO FIX TO GENERIC TYPE (Byte?)
    private int type; // 1 = UNICAST, 2 = BROADCAST
    private LinkedList<String> piggyBackedRoute;
    byte[] data;

    public MyMessage(String id, String source, String dest, int type, CrowdTask task, LinkedList<String> sequence){
        this.id=id;
        this.source=source.substring(0,1);
        this.dest=dest.substring(0,1);
        this.type=type;
        this.task=task;
        this.piggyBackedRoute=sequence;
        this.taskIdCollected =null;
        this.immediateSender = thisDeviceName;
        //Log.d(TAG, "NEW MSG CREATED WITH SRC: " + source);
    }

    public MyMessage(String id, String source, String dest, int type, String taskIdCollected, LinkedList<String> sequence){
        this.id=id;
        this.source=source.substring(0,1);
        this.dest=dest.substring(0,1);
        this.type=type;
        this.task=null;
        this.piggyBackedRoute=sequence;
        this.taskIdCollected = taskIdCollected;
        this.immediateSender = thisDeviceName;
    }

    public MyMessage(String id, String source, String dest, int type, byte[] data, String taskIdCollected, LinkedList<String> sequence){
        this.id=id;
        this.source=source.substring(0,1);
        this.dest=dest.substring(0,1);
        this.type=type;
        this.task=null;
        this.piggyBackedRoute=sequence;
        this.data = data;
        this.taskIdCollected = taskIdCollected;
        this.immediateSender = thisDeviceName;
        //Log.d(TAG, "NEW MSG CREATED WITH SRC: " + source);
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public String getId() {
        return id;
    }
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public LinkedList<String> getPiggyBackedRoute() {
        return piggyBackedRoute;
    }
    public void addToPiggyBackedRoute(String node) {
        piggyBackedRoute.add(node);
        if (task!=null) {
            task.setPiggyPath(piggyBackedRoute);
        }
        Log.d(TAG2, "piggy route updated with " + node);
    }
    public void stripFromPiggyBackedRoute() {
        piggyBackedRoute.removeLast();
        if (task!=null){
            task.setPiggyPath(piggyBackedRoute);
        }
        Log.d(TAG2,"piggy route updated");
    }

    public String getTaskIdCollected() {
        return taskIdCollected;
    }

    public CrowdTask getTask() {
        return task;
    }

    public byte[] getData() {
        return data;
    }

    public String getImmediateSender() {
        return immediateSender;
    }

    public void setImmediateSender(String immediateSender) {
        this.immediateSender = immediateSender;
    }

    public class Payload {
        CrowdTask task;
        String response;
    }
    public void setPiggyBackedRoute(LinkedList<String> piggyBackedRoute) {
        this.piggyBackedRoute = (LinkedList<String>) piggyBackedRoute.clone();
    }
}
