package com.fabianbell.janinakeller.lut_lappeenranta;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TabHost;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class Main extends AppCompatActivity {

    private Firebase mRootRef;
    private FirebaseAuth mAuth;

    private TabHost host;

    //Profile Elements
    private FloatingActionButton editProfileButton;

    //Device
    private ListView mDeviceList;

    private FloatingActionButton mAddDeviceButton;

    private ArrayList<String> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRootRef =  new Firebase("https://lut-lappeenranta.firebaseio.com/");
        mAuth = FirebaseAuth.getInstance();

        host = (TabHost) findViewById(R.id.tabhost);
        host.setup();

        TabHost.TabSpec spec1 = host.newTabSpec("Statistics");
        spec1.setContent(R.id.statistics);
        spec1.setIndicator("Statistics");
        host.addTab(spec1);

        TabHost.TabSpec spec2 = host.newTabSpec("Devices");
        spec2.setContent(R.id.devices);
        spec2.setIndicator("Devices");
        host.addTab(spec2);

        TabHost.TabSpec spec3 = host.newTabSpec("Profile");
        spec3.setContent(R.id.profile);
        spec3.setIndicator("Profile");
        host.addTab(spec3);

        ///////////////////////////////////// Profile //////////////////////////////////////
        editProfileButton = (FloatingActionButton) findViewById(R.id.editProfileButton);
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main.this, Test.class));
            }
        });

        ////////////////////////////////// Devices /////////////////////////////////////////
        mAddDeviceButton = (FloatingActionButton) findViewById(R.id.addDeviceButton);

        mAddDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main.this, AddDevice.class));
            }
        });

        mDeviceList = findViewById(R.id.deviceList);

        devices = new ArrayList<>();

        final ArrayAdapter<String> deviceListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, devices);

        mDeviceList.setAdapter(deviceListAdapter);

        mRootRef.child("User").child(mAuth.getCurrentUser().getUid()).child("Devices").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mRootRef.child("User").child(mAuth.getCurrentUser().getUid()).child("Devices").child(dataSnapshot.getKey()).child("modelName").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String device = dataSnapshot.getValue(String.class);
                        devices.add(device);
                        Log.d("DeviceList", "Load Device: " + device);
                        deviceListAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
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

            }
        });


    }
}
