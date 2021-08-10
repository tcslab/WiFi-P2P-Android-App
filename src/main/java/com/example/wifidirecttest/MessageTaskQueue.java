package com.example.wifidirecttest;


import android.os.Handler;
import android.util.Log;

import java.util.LinkedList;

import static com.example.wifidirecttest.MainActivity.TAG;
import static com.example.wifidirecttest.MainActivity.TAG2;
import static com.example.wifidirecttest.MainActivity.TAG3;


public class MessageTaskQueue {
    private static final int MAX_RETRANSMISSION_TRIES = 6;
    public int numTries=0;
    protected Handler handler;
    protected MainActivity mainActivity;
    public LinkedList<MainActivity.MessageTask> messageTaskList = new LinkedList<>();
    private boolean isRunning=false;

    public MessageTaskQueue(MainActivity mainActivity, Handler handler) {
        this.mainActivity = mainActivity;
        this.handler = handler;
    }

    public void run() {
        Log.d(TAG3, "SDTSKQ running in thread: " + Thread.currentThread().getName());
        if (!messageTaskList.isEmpty()) {
            Log.d(TAG2, "Advancing message queue");
            if (!(isRunning)) {
                isRunning=true;
                //messageTaskList.getFirst().msg.setSource(mainActivity.thisDeviceName);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        messageTaskList.getFirst().run();
                    }
                }, 3000);
            } else {
                Log.d(TAG2, "Message queue already running!");
            }
        } else {
            Log.d(TAG2, "Task queue run called but queue is empty!");
        }
    }
    public void sendTaskComplete(int result){
        if (result == MainActivity.SENT_SUCCESS) {
            messageTaskList.removeFirst();
            mainActivity.WhileSending=false;
            isRunning = false;
            Log.d(TAG2, "MessageTask successful, removing it from queue. \n Numbers of tasks in queue now: " + messageTaskList.size());
            numTries = 0;
            mainActivity.resetStatus(true); //True so to AdvanceQueue even in the case of the P2Pgroup already having been removed from the other party)
        } else if (result == MainActivity.SENT_FAILURE) {
            isRunning = false;
            if (numTries<MAX_RETRANSMISSION_TRIES) {
                Log.d(TAG, "Message send try " + numTries + " failed. Trying again");
                numTries++;
                mainActivity.resetStatus(true);
            } else {
                mainActivity.WhileSending = false;
                messageTaskList.removeFirst();
                Log.d(TAG, "Message send failed after " + numTries + " tries, removing it from queue. \n Numbers of tasks in queue now: " + messageTaskList.size());
                numTries = 0;
                handler.post(new MainActivity.AdvanceQueue());
            }
        }
    }

    public void addTask(MainActivity.MessageTask messageTask) {
        messageTaskList.add(messageTask);
        Log.d(TAG2, "added message task in queue of type " + messageTask.msg.getType() + " with recipent: " + messageTask.nextHop + "\n Numbers of tasks in queue: " + messageTaskList.size());
    }

    public boolean isEmpty(){
        return messageTaskList.isEmpty();
    }
}