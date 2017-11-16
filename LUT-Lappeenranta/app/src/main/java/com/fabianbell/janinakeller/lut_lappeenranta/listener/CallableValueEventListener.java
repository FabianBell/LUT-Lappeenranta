package com.fabianbell.janinakeller.lut_lappeenranta.listener;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.google.firebase.crash.FirebaseCrash;

/**
 * Created by Fabian on 15.11.2017.
 */

public class CallableValueEventListener<E> implements com.firebase.client.ValueEventListener {

    private CallableForFirebase<E> func;
    private E param;

    public CallableValueEventListener(E pram, CallableForFirebase<E> func){
        this.func = func;
        this.param = pram;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        func.call(param, dataSnapshot);
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {
        Log.d("Data", "Cannot load data: " + firebaseError.getMessage());
        FirebaseCrash.report(firebaseError.toException());
    }
}
