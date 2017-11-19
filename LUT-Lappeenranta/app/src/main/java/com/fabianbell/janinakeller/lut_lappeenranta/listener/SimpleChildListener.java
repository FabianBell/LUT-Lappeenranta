package com.fabianbell.janinakeller.lut_lappeenranta.listener;

import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.google.firebase.crash.FirebaseCrash;

/**
 * Created by Fabian on 13.11.2017.
 */

public abstract class SimpleChildListener implements ChildEventListener {

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
