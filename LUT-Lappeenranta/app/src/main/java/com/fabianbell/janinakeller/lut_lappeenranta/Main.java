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
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import com.fabianbell.janinakeller.lut_lappeenranta.listener.CallableForFirebase;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.CallableValueEventListener;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.SimpleChildListener;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.SimpleValueListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main extends AppCompatActivity {

    //Auth
    private FirebaseUser currentUser;
    private Firebase mRootRef;
    private FirebaseAuth mAuth;

    private TabHost host;

    //Profile Elements
    private TextView mProfileEmailTextView;
    private TextView mProfileNumberOfDevices;
    private TextView mDevicesText;

    private Button mProfileLogOutButton;
    private FloatingActionButton mEditProfileButton;

    //Device
    private ListView mDeviceList;

    private FloatingActionButton mAddDeviceButton;
    private ArrayList<String> devices;
    private ArrayAdapter<String> deviceListAdapter;

    private Map<String, String> deviceIdMap;

    //Statistic
    private Spinner mStatisticsCategorySpinner;
    private MultiAutoCompleteTextView mStatisticsBrandsAutoComplete;
    private MultiAutoCompleteTextView mStatisticsModelsAutoComplete;
    private Button mStatisticsSearchButton;

    //autocomplete
    private ArrayList<String> brands;
    private ArrayList<String> modelOfBrand;
    private ArrayList<String> categories;
    private ArrayList<String> condition;

    //Adapter
    private ArrayAdapter<String> modelAdapter;
    private Map<String, String> modelMap;
    private ArrayAdapter<String> categoryAdapter;
    private ArrayAdapter<String> conditionAdapter;
    private ArrayAdapter<String> brandAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRootRef = new Firebase("https://lut-lappeenranta.firebaseio.com/");
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

        String tag = getIntent().getStringExtra("TAG");
        if (tag != null) {
            host.setCurrentTabByTag(tag);
        }


        ////////////////////////////////////// Statistics ///////////////////////////////////


        mStatisticsCategorySpinner = (Spinner) findViewById(R.id.statisticsCategorySpinner);
        mStatisticsBrandsAutoComplete = (MultiAutoCompleteTextView) findViewById(R.id.statisticsBrandsAutoComplete);
        mStatisticsModelsAutoComplete = (MultiAutoCompleteTextView) findViewById(R.id.statisticsModelsAutoComplete);
        mStatisticsSearchButton = (Button) findViewById(R.id.statisticsSearchButton);
        categories = new ArrayList<>();
        condition = new ArrayList<>();
        categoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        conditionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, condition);
        mStatisticsCategorySpinner.setAdapter(conditionAdapter);

        //get categories
        mRootRef.child("DeviceCategory").addChildEventListener(new SimpleChildListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("Category", "Load Category: " + dataSnapshot.getKey());
                FirebaseCrash.log("Load Category: " + dataSnapshot.getKey());
                categories.add(dataSnapshot.getKey());
                categoryAdapter.notifyDataSetChanged();

                //TODO AutoComplete Models, Brands,Category
                mStatisticsSearchButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Main.this, Statistics.class);
                        intent.putExtra("Category", "mStatisticsCategorySpinner");
                        intent.putExtra("Brands", "mStatisticsBrandsAutoComplete");
                        intent.putExtra("Models", "mStatisticsModelsAutoComplete");

                    }
                });

                ///////////////////////////////////// Profile //////////////////////////////////////

                mProfileEmailTextView = (TextView) findViewById(R.id.profileEmailTextView);
                mProfileNumberOfDevices = (TextView) findViewById(R.id.profileNumberOfDevices);
                mDevicesText = findViewById(R.id.DevicesTextView);
                mProfileLogOutButton = (Button) findViewById(R.id.profileLougOutButton);
                mEditProfileButton = (FloatingActionButton) findViewById(R.id.editProfileButton);

                //set fields
                mProfileEmailTextView.setText(mAuth.getCurrentUser().getEmail());
                mRootRef.child("User").child(mAuth.getCurrentUser().getUid()).child("Devices").addListenerForSingleValueEvent(new SimpleValueListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long count = dataSnapshot.getChildrenCount();
                        if (count == 1) {
                            mDevicesText.setText("Device");
                        }
                        mProfileNumberOfDevices.setText(Long.toString(count));
                    }
                });

                currentUser = mAuth.getCurrentUser();

                mProfileLogOutButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAuth.signOut();
                        if (currentUser.isAnonymous()) {
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

                deviceListAdapter = new ArrayAdapter<String>(Main.this, android.R.layout.simple_list_item_1, devices);

                mDeviceList.setAdapter(deviceListAdapter);

                deviceIdMap = new HashMap<>();
                mRootRef.child("User").child(mAuth.getCurrentUser().getUid()).child("Devices").addChildEventListener(new SimpleChildListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        final Firebase device = mRootRef.child("Device").child(dataSnapshot.getKey());
                        String deviceId = dataSnapshot.getKey();
                        //get model ID
                        device.child("modelName").addListenerForSingleValueEvent(new CallableValueEventListener<String>(deviceId, new CallableForFirebase<String>() {
                            @Override
                            public void call(String param, DataSnapshot data) {
                                if (data.getValue() != null) {
                                    ArrayList<String> paramPack = new ArrayList<>();
                                    if (data.getValue() == null) {
                                        FirebaseCrash.report(new Exception());
                                    }
                                    String modelId = data.getValue().toString();
                                    //param = deviceId
                                    paramPack.add(param); //0
                                    paramPack.add(modelId); //1
                                    //get Brand name
                                    device.child("brandName").addListenerForSingleValueEvent(new CallableValueEventListener<ArrayList<String>>(paramPack, new CallableForFirebase<ArrayList<String>>() {
                                        @Override
                                        public void call(ArrayList<String> param, DataSnapshot data) {
                                            if (data.getValue() != null) {
                                                String brandName = data.getValue().toString();
                                                param.add(brandName); //2
                                                //get Model name with brand name and model ID
                                                //check if unknown model
                                                device.child("unknownModel").addListenerForSingleValueEvent(new CallableValueEventListener<ArrayList<String>>(param, new CallableForFirebase<ArrayList<String>>() {
                                                    @Override
                                                    public void call(ArrayList<String> param, DataSnapshot data) {
                                                        if (data.getValue() != null) {
                                                            //unknown model
                                                            mRootRef.child("UnknownBrand_Model").child("Brand").child(param.get(2)).child("Model").child(param.get(1)).child("Name").addListenerForSingleValueEvent(new CallableValueEventListener<String>(param.get(0), new CallableForFirebase<String>() {
                                                                @Override
                                                                public void call(String param, DataSnapshot data) {
                                                                    String modelName = data.getValue().toString();
                                                                    //param = deviceId
                                                                    deviceIdMap.put(modelName, param);
                                                                    Log.d("DeviceList", "Load Device: " + modelName);
                                                                    FirebaseCrash.log("Load Device: " + modelName);
                                                                    devices.add(modelName);
                                                                    deviceListAdapter.notifyDataSetChanged();
                                                                }
                                                            }));
                                                        } else {
                                                            //known brand and model
                                                            mRootRef.child("Brand").child(param.get(2)).child("Model").child(param.get(1)).addListenerForSingleValueEvent(new CallableValueEventListener<String>(param.get(0), new CallableForFirebase<String>() {
                                                                @Override
                                                                public void call(String param, DataSnapshot data) {
                                                                    String modelName = data.getValue().toString();
                                                                    //param = deviceId
                                                                    deviceIdMap.put(modelName, param);
                                                                    Log.d("DeviceList", "Load Device: " + modelName);
                                                                    FirebaseCrash.log("Load Device: " + modelName);
                                                                    devices.add(modelName);
                                                                    deviceListAdapter.notifyDataSetChanged();
                                                                }
                                                            }));
                                                        }
                                                    }
                                                }));
                                            }
                                        }
                                    }));
                                }
                            }
                        }));
                    }
                });

                mDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(Main.this, DeviceDetail.class);
                        String deviceModel = mDeviceList.getItemAtPosition(position).toString();
                        String deviceId = deviceIdMap.get(deviceModel);
                        Log.d("ListElement", "Number: " + deviceId);
                        intent.putExtra("DeviceId", deviceId);
                        startActivity(intent);
                    }
                });


            }
        });
    }
}