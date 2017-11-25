package com.fabianbell.janinakeller.lut_lappeenranta.listener;

/**
 * Created by Fabian on 23.11.2017.
 */

public class Trigger<T> {

    private Condition<T> condition;
    private Callable<T> callable;

    public Trigger(Condition<T> condition, Callable<T> callable){
        this.condition = condition;
        this.callable = callable;
    }

    public void onChange(){
        if (condition.isTrue()){
            callable.call(condition.getObserver());
        }
    }

    public Condition<T> getCondition() {
        return condition;
    }
}
