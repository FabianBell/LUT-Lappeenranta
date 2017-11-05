package com.fabianbell.janinakeller.lut_lappeenranta;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DebugUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Test extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;

    private FirebaseUser currentUser;

    private Button mLogOutButton;

    private Button mAddDevice;

    private TabHost host;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mFirebaseAuth = FirebaseAuth.getInstance();

        currentUser = mFirebaseAuth.getCurrentUser();

        mLogOutButton = (Button) findViewById(R.id.logOutButton);

        mAddDevice = (Button) findViewById(R.id.testaddDevice);

        mAddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Test.this, AddDevice.class));
            }
        });



        //generate Logs
        /*
        Log.d("User", "UserIsNul: " + (currentUser == null));
        Log.d("User", "UserName: " + currentUser.getEmail());
        Log.d("User", "UserIsAnonymous: " + currentUser.isAnonymous());
        Log.d("User", "UserPhoneNumber: " + currentUser.getPhoneNumber());
        Log.d("User", "UserPhotoUrl: " + currentUser.getPhotoUrl());*/










        //log out
        mLogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirebaseAuth.signOut();
                if(currentUser.isAnonymous()){
                    currentUser.delete();
                }
                startActivity(new Intent(Test.this, LogIn.class));
            }
        });

        host = (TabHost) findViewById(R.id.tabhost);
        host.setup();

        TabHost.TabSpec spec = host.newTabSpec("logout")
                .setContent(R.id.logout)
                .setIndicator("Log out");
        host.addTab(spec);

        spec = host.newTabSpec("display")
                .setContent(R.id.display)
                .setIndicator("Display");
        host.addTab(spec);
    }
}
