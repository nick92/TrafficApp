package com.example.nick.fantraffic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nick on 16/10/15.
 */
class Traffic {
    String flow;
    String duration;

    Traffic(String flow, String duration){
        this.flow = flow;
        this.duration = duration;
    }

    private List<Traffic> trafficList;

    private void initializeData(){
        trafficList = new ArrayList<>();
        trafficList.add(new Traffic("Very Good", "10"));
        trafficList.add(new Traffic("Terrible", "60"));
        trafficList.add(new Traffic("Bad", "50"));
        trafficList.add(new Traffic("Good", "20"));
    }
}
