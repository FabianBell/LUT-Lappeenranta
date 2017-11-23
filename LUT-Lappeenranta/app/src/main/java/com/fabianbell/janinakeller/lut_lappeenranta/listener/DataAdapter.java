package com.fabianbell.janinakeller.lut_lappeenranta.listener;

/**
 * Created by Fabian on 21.11.2017.
 */

public abstract class DataAdapter<T> {

    public abstract void onLoad(T Data);
}
