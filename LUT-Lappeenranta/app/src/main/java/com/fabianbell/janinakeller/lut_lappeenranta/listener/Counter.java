package com.fabianbell.janinakeller.lut_lappeenranta.listener;

/**
 * Created by Fabian on 25.11.2017.
 */

public class Counter {

    private int count;

    public Counter(int start){
        this.count = start;
    }

    public void add(int add){
        this.count = this.count + add;
    }

    public void set(int count) {
        this.count = count;
    }

    public int get() {
        return count;
    }
}
