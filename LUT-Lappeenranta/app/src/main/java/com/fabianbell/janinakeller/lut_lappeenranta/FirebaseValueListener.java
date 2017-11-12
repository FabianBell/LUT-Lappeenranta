package com.fabianbell.janinakeller.lut_lappeenranta;

import android.support.annotation.Nullable;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.ArrayList;
import com.fabianbell.janinakeller.lut_lappeenranta.CallableForFirebase;
import com.google.firebase.crash.FirebaseCrash;

/**
 * Created by Fabian on 09.11.2017.
 */

public class FirebaseValueListener<E> implements com.firebase.client.ChildEventListener{

    private CallableForFirebase<E> func;
    private E param;

    public FirebaseValueListener(E param, CallableForFirebase<E> func){
        this.func = func;
        this.param = param;
    }


    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        func.call(param, dataSnapshot);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {
        Log.d("Data", "Cannot load data: " + firebaseError.getMessage());
        FirebaseCrash.report(firebaseError.toException());
    }
}
