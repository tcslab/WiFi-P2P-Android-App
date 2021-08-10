package com.example.wifidirecttest;

import java.io.Serializable;

public class RouteInfoTuple implements Serializable {
    public String nextHop;
    public int numberOfHops;

    public RouteInfoTuple(String nextHop, int numberOfHops) {
        this.nextHop = nextHop;
        this.numberOfHops = numberOfHops;
    }
}
