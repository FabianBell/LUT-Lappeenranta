package com.fabianbell.janinakeller.lut_lappeenranta.listener;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.firebase.crash.FirebaseCrash;

/**
 * Created by Fabian on 17.11.2017.
 */

public abstract class SimpleValueListener implements ValueEventListener {

    @Override
    public void onCancelled(FirebaseError firebaseError) {
        Log.d("data", "Cannot load data: " + firebaseError.getMessage());
        FirebaseCrash.report(firebaseError.toException());
    }
}
