package com.fabianbell.janinakeller.lut_lappeenranta.listener;

/**
 * Created by Fabian on 23.11.2017.
 */

public abstract class Condition<T> {

    private T observer;

    private Object extra;

    public Condition(T observer){
        this.observer = observer;
    }

    public Condition(T observer, Object extra){
        this.observer = observer;
        this.extra = extra;
    }

    public abstract boolean isTrue();

    public T getObserver() {
        return observer;
    }

    public Object getExtra() {
        return extra;
    }
}
