package com.fabianbell.janinakeller.lut_lappeenranta;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.fabianbell.janinakeller.lut_lappeenranta.listener.CallableForFirebase;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.CallableValueEventListener;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.FirebaseValueListener;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.SimpleFirebaseListener;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class Main extends AppCompatActivity {

    //Auth
    private FirebaseUser currentUser;
    private Firebase mRootRef;
    private FirebaseAuth mAuth;

    private TabHost host;

    //Profile Elements
    private TextView mProfileEmailTextView;
    private TextView mProfileNumberOfDevices;

    private Button mProfileLogOutButton;
    private FloatingActionButton mEditProfileButton;

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

        mProfileEmailTextView = (TextView) findViewById(R.id.profileEmailTextView);
        mProfileNumberOfDevices = (TextView) findViewById(R.id.profileNumberOfDevices);
        mProfileLogOutButton = (Button) findViewById(R.id.profileLougOutButton);
        mEditProfileButton = (FloatingActionButton) findViewById(R.id.editProfileButton);

        // TODO Fabian display user email and number of devices

        mProfileLogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                if(currentUser.isAnonymous()){
                    currentUser.delete();
                }
                startActivity(new Intent(Main.this, LogIn.class));
            }
        });

        mEditProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main.this, EditProfile.class));
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

        mRootRef.child("User").child(mAuth.getCurrentUser().getUid()).child("Devices").addChildEventListener(new SimpleFirebaseListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final Firebase device = mRootRef.child("Device").child(dataSnapshot.getKey());
                String display = dataSnapshot.getKey();
                //get model ID
                device.child("modelName").addListenerForSingleValueEvent(new CallableValueEventListener<String>(display, new CallableForFirebase<String>() {
                    @Override
                    public void call(String param, DataSnapshot data) {
                        final ArrayList<String> paramPack = new ArrayList<>();
                        String modelId = data.getValue().toString();
                        //param = display
                        paramPack.add(param);
                        paramPack.add(modelId);
                        //get Brand name
                        device.child("brandName").addListenerForSingleValueEvent(new CallableValueEventListener<ArrayList<String>>(paramPack, new CallableForFirebase<ArrayList<String>>() {
                            @Override
                            public void call(ArrayList<String> param, DataSnapshot data) {
                                String brandName = data.getValue().toString();
                                //get Model name with brand name and model ID
                                mRootRef.child("Brand").child(brandName).child("Model").child(paramPack.get(1)).addListenerForSingleValueEvent(new CallableValueEventListener<String>(paramPack.get(0), new CallableForFirebase<String>() {
                                    @Override
                                    public void call(String param, DataSnapshot data) {
                                        String modelName = data.getValue().toString();
                                        //param = display
                                        String display = modelName + " - " + param;
                                        Log.d("DeviceList", "Load Device: " + display);
                                        FirebaseCrash.log("Load Device: " + display);
                                        devices.add(display);
                                        deviceListAdapter.notifyDataSetChanged();
                                    }
                                }));
                            }
                        }));
                    }
                }));
            }
        });

        mDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(Main.this, DeviceDetail.class);
                String element = mDeviceList.getItemAtPosition(position).toString();
                String[] split = element.split(" - ");
                String deviceId = split[1];
                String deviceModel = split[0];
                Log.d("ListElement", "Number: " + deviceId);
                intent.putExtra("DeviceId", deviceId);
                intent.putExtra("DeviceModel", deviceModel);
                startActivity(intent);
            }
        });


    }
}
