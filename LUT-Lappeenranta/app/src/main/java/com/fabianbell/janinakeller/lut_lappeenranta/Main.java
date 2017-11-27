package com.fabianbell.janinakeller.lut_lappeenranta;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.fabianbell.janinakeller.lut_lappeenranta.listener.Callable;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.CallableForFirebase;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.CallableValueEventListener;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.Condition;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.Counter;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.SimpleChildListener;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.SimpleValueListener;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.Trigger;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
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
    private boolean brandChanged;
    private Map<String, ArrayList<String>> brandModelMap;
    private Map<String, String> modelMap;

    //autocomplete
    private ArrayList<String> brands;
    private ArrayList<String> modelOfBrand;
    private ArrayList<String> categories;
    private ArrayList<String> condition;

    //Adapter
    private ArrayAdapter<String> modelAdapter;
    private ArrayAdapter<String> categoryAdapter;
    private ArrayAdapter<String> conditionAdapter;
    private ArrayAdapter<String> brandAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRootRef = Utils.mRootRef;
        mAuth = FirebaseAuth.getInstance();

        Utils.data();

        host = (TabHost) findViewById(R.id.tabhost);
        host.setup();

        TabHost.TabSpec spec2 = host.newTabSpec("Statistics");
        spec2.setContent(R.id.statistics);
        spec2.setIndicator("Statistics");
        host.addTab(spec2);

        if (!mAuth.getCurrentUser().isAnonymous()) {
            TabHost.TabSpec spec1 = host.newTabSpec("Devices");
            spec1.setContent(R.id.devices);
            spec1.setIndicator("Devices");
            host.addTab(spec1);


            TabHost.TabSpec spec3 = host.newTabSpec("Profile");
            spec3.setContent(R.id.profile);
            spec3.setIndicator("Profile");
            host.addTab(spec3);

            String tag = getIntent().getStringExtra("TAG");
            if (tag != null) {
                host.setCurrentTabByTag(tag);
            } else {
                host.setCurrentTabByTag("Devices");
            }
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

        brands = new ArrayList<>();
        brandAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, brands);
        mStatisticsBrandsAutoComplete.setAdapter(brandAdapter);
        mStatisticsBrandsAutoComplete.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        modelOfBrand = new ArrayList<>();
        modelAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, modelOfBrand);
        mStatisticsModelsAutoComplete.setAdapter(modelAdapter);
        mStatisticsModelsAutoComplete.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        //get brands
        mRootRef.child("Brand").addChildEventListener(new SimpleChildListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("Brands", "Load Brand: " + dataSnapshot.getKey().toString());
                FirebaseCrash.log("Load Brand: " + dataSnapshot.getKey().toString());
                brands.add(dataSnapshot.getKey());
                brandAdapter.notifyDataSetChanged();
            }
        });

        //only update model if brand was changed
        mStatisticsBrandsAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                brandChanged = true;
                Log.d("Model", "Brand changed");
                FirebaseCrash.log("Brand changed");
            }
        });
        mStatisticsModelsAutoComplete.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (brandChanged) {
                    modelOfBrand.removeAll(modelOfBrand);
                    modelAdapter.notifyDataSetChanged();
                    modelMap = new HashMap<>();
                    Log.d("Model", "Models clear");
                    FirebaseCrash.log("Models clear");
                    String[] uncleanedBrandList = mStatisticsBrandsAutoComplete.getText().toString().split(",");
                    ArrayList<String> brandList = new ArrayList<>();
                    String cleanedBrand;
                    for (String brand : uncleanedBrandList) {
                        cleanedBrand = Utils.removeSpace(brand);
                        if (cleanedBrand != null) {
                            brandList.add(cleanedBrand);
                        }
                    }
                    brandModelMap = new HashMap<>();
                    for (String brand : brandList) {
                        mRootRef.child("Brand").child(brand).child("Model").addListenerForSingleValueEvent(new CallableValueEventListener<String>(brand, new CallableForFirebase<String>() {
                            @Override
                            public void call(String param, DataSnapshot data) {
                                ArrayList<String> models = new ArrayList<>();
                                for (DataSnapshot snapshot : data.getChildren()) {
                                    String value = snapshot.getValue().toString();
                                    modelOfBrand.add(value);
                                    models.add(value);
                                    modelAdapter.notifyDataSetChanged();
                                    modelMap.put(value, snapshot.getKey());
                                    Log.d("Model", "Load model: " + value);
                                    FirebaseCrash.log("Load model: " + value);
                                }
                                brandChanged = false;
                                brandModelMap.put(param, models);
                                Log.d("Model", "Model updated");
                                FirebaseCrash.log("Model updated");
                            }
                        }));
                    }
                }
            }
        });

        mStatisticsSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get models
                Map<String, String> brandModelParamMap = new HashMap<>(); //model - brand
                ArrayList<String> models = Utils.cleanFromTokenizer(mStatisticsModelsAutoComplete.getText().toString());
                ArrayList<String> brands = Utils.cleanFromTokenizer(mStatisticsBrandsAutoComplete.getText().toString());
                //check for statistics
                ArrayList<Object> callableParam = new ArrayList<>();
                callableParam.add(brands); //0
                callableParam.add(models); //1
                callableParam.add(brandModelParamMap); //2
                Counter triggerObserver = new Counter(0);

                Trigger<Counter> listTrigger = new Trigger<>(new Condition<Counter>(triggerObserver, models) {
                    @Override
                    public boolean isTrue() {
                        return this.getObserver().get() == ((ArrayList<String>) getExtra()).size();
                    }
                }, new Callable<Counter>(callableParam) {
                    @Override
                    public void call(Counter data) {
                        //find matches
                        ArrayList<String> brands = ((ArrayList<String>) ((ArrayList<Object>) getExtra()).get(0));
                        ArrayList<String> models = ((ArrayList<String>) ((ArrayList<Object>) getExtra()).get(1));
                        Map<String, String> brandModelParamMap = ((Map<String, String>) ((ArrayList<Object>) getExtra()).get(2));
                        for (String brand : brands) {
                            for (String model : models) {
                                if (brandModelMap.get(brand).contains(model)) {
                                    brandModelParamMap.put(model, brand);
                                }
                            }
                        }
                        //give parameters
                        Intent statisticIntent = new Intent(Main.this, Statistics.class);
                        String modelBase = "MODEL";
                        String brandBase = "BRAND";
                        int i = 1;
                        for (Map.Entry<String, String> entry : brandModelParamMap.entrySet()) {
                            statisticIntent.putExtra(modelBase + i, modelMap.get(entry.getKey()));
                            statisticIntent.putExtra(brandBase + i, entry.getValue());
                            i++;
                        }
                        Log.d("Search", "Start activity with map: " + brandModelParamMap.toString());
                        FirebaseCrash.log("Start activity with map: " + brandModelParamMap.toString());
                        startActivity(statisticIntent);
                    }
                });
                //check for statistics
                for (String model : models) {
                    ArrayList<Object> param = new ArrayList<>();
                    param.add(model); //0
                    param.add(listTrigger); //1
                    if (modelMap.get(model) == null) {
                        Toast.makeText(Main.this, "Cannot find model: " + model, Toast.LENGTH_SHORT).show();
                        break;
                    } else {
                        mRootRef.child("Statistics").child("Models").child(modelMap.get(model)).addListenerForSingleValueEvent(new CallableValueEventListener<ArrayList<Object>>(param, new CallableForFirebase<ArrayList<Object>>() {
                            @Override
                            public void call(ArrayList<Object> param, DataSnapshot data) {
                                Trigger<Counter> trigger = ((Trigger<Counter>) param.get(1));
                                if (data.getValue() != null) {
                                    trigger.getCondition().getObserver().add(1);
                                    trigger.onChange();
                                } else {
                                    Toast.makeText(Main.this, "There is no statistic for the model: " + ((String) param.get(0)), Toast.LENGTH_LONG).show();
                                }
                            }
                        }));
                    }
                }
            }
        });

        Button mStatisticsLogOutButton = findViewById(R.id.statisticsLogOutButton);

        ////////////////////////////////// Devices /////////////////////////////////////////
        if(!mAuth.getCurrentUser().isAnonymous()) {
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

            ((ViewGroup) mStatisticsLogOutButton.getParent()).removeView(mStatisticsLogOutButton);

            mEditProfileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Main.this, EditProfile.class));
                }
            });
        }else{
            LinearLayout mDevices = findViewById(R.id.devices);
            LinearLayout mProfile = findViewById(R.id.profile);
            ((ViewGroup) mDevices.getParent()).removeView(mDevices);
            ((ViewGroup) mProfile.getParent()).removeView(mProfile);

            mStatisticsLogOutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    user.delete();
                    startActivity(new Intent(Main.this, LogIn.class));
                }
            });
        }

    }
}