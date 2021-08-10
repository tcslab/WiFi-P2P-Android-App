package com.example.wifidirecttest;

import java.util.LinkedList;

public class DataHolder {
    final LinkedList<CrowdTask> savedTasklist = new LinkedList<>();
    private static DataHolder instance;

    private DataHolder(){
    }

    static DataHolder getInstance() {
        if (instance==null){
            instance = new DataHolder();
        }
        return instance;
    }

}
