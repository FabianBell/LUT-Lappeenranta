package com.fabianbell.janinakeller.lut_lappeenranta.listener;

/**
 * Created by Fabian on 23.11.2017.
 */

public abstract class Callable<T> {

    private Object extra;

    public Callable(){}
    public Callable(Object extra){
        this.extra = extra;
    }
    public abstract void call(T data);

    public Object getExtra() {
        return extra;
    }
}
