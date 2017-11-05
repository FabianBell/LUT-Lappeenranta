package com.fabianbell.janinakeller.lut_lappeenranta;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by Fabian on 25.10.2017.
 */

public class LUT extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Firebase.setAndroidContext(this);
    }
}
