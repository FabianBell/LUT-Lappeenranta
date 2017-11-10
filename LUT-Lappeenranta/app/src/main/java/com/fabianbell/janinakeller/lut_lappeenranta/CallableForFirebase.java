package com.fabianbell.janinakeller.lut_lappeenranta;

import com.firebase.client.DataSnapshot;

/**
 * Created by Fabian on 10.11.2017.
 */

public abstract class CallableForFirebase<E> {

    public abstract void call(E param, DataSnapshot data);
}
