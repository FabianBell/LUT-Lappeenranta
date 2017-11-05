package com.fabianbell.janinakeller.lut_lappeenranta;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Map;

public class AddDevice extends AppCompatActivity {

    private Firebase mRootRef;
    private FirebaseAuth mAuth;

    //fields
    private Spinner mDeviceCategory;
    private AutoCompleteTextView mDeviceModel;
    private AutoCompleteTextView mDeviceBrand;
    private EditText mDeviceidNumber;
    private EditText mDevicePrice;
    private EditText mDeviceShop;
    private EditText mDeviceDateOfPurchase;
    private Spinner mDeviceCondition;

    private ImageView mReceipt;

    private Button mAddReceipt;
    private Button mAddDeviceButton;

    //autocomplete
    private ArrayList<String> brands;
    private ArrayList<String> modelOfBrand;
    private ArrayList<String> categories;
    private ArrayList<String> condition;

    //test Data
    private Map<String, String> testBrands;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        //get Elements
        mDeviceCategory = (Spinner) findViewById(R.id.deviceCategory);
        mDeviceModel = (AutoCompleteTextView) findViewById(R.id.deviceModel);
        mDeviceBrand = (AutoCompleteTextView) findViewById(R.id.deviceBrand);
        mDeviceidNumber = (EditText) findViewById(R.id.deviceidNumber);
        mDevicePrice = (EditText) findViewById(R.id.devicePrice);
        mDeviceShop = (EditText) findViewById(R.id.deviceShop);
        mDeviceDateOfPurchase = (EditText) findViewById(R.id.deviceDateOfPurchase);
        mDeviceCondition = (Spinner) findViewById(R.id.deviceCondition);

        mReceipt = (ImageView) findViewById(R.id.receipt);

        mAddReceipt = (Button) findViewById(R.id.addReceipt);
        mAddDeviceButton = (Button) findViewById(R.id.addDeviceButton);

        mRootRef = new Firebase("https://lut-lappeenranta.firebaseio.com/");
        mAuth = FirebaseAuth.getInstance();

        //createTestData(mRootRef);

        brands = new ArrayList<>();

        final ArrayAdapter<String> brandAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, brands);

        mDeviceBrand.setAdapter(brandAdapter);

        modelOfBrand = new ArrayList<>();

        final ArrayAdapter<String> modelAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, modelOfBrand);

        mDeviceModel.setAdapter(modelAdapter);

        categories = new ArrayList<>();

        final ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, categories);

        mDeviceCategory.setAdapter(categoryAdapter);

        condition = new ArrayList<>();

        final  ArrayAdapter<String> conditionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, condition);

        mDeviceCondition.setAdapter(conditionAdapter);

        //add Device

        mAddDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String category = mDeviceCategory.getSelectedItem().toString();
                String brandName = mDeviceBrand.getText().toString();
                String modelName = mDeviceModel.getText().toString();
                String deviceNumber = mDeviceidNumber.getText().toString();
                String price = mDevicePrice.getText().toString();
                String shop = mDeviceShop.getText().toString();
                String date = mDeviceDateOfPurchase.getText().toString();
                String condition = mDeviceCondition.getSelectedItem().toString();

                Firebase deviceByNumber = mRootRef.child("User").child(mAuth.getCurrentUser().getUid()).child("Devices").child(deviceNumber);
                Log.d("Device", "Start saving Device with Number: " + deviceNumber);
                deviceByNumber.child("category").setValue(category);
                deviceByNumber.child("brandName").setValue(brandName);
                deviceByNumber.child("modelName").setValue(modelName);
                deviceByNumber.child("price").setValue(price);
                deviceByNumber.child("shop").setValue(shop);
                deviceByNumber.child("date").setValue(date);
                deviceByNumber.child("condition").setValue(condition);
                Log.d("Device", "Device saved");

                startActivity(new Intent(AddDevice.this, Main.class));
            }
        });

        //get categories
        mRootRef.child("DeviceCategory").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("Category", "Load Category: " + dataSnapshot.getKey());
                categories.add(dataSnapshot.getKey());
                categoryAdapter.notifyDataSetChanged();
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

        //get Conditions
        mRootRef.child("DeviceCondition").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("Condition", "Load Condition: " + dataSnapshot.getKey());
                condition.add(dataSnapshot.getKey());
                conditionAdapter.notifyDataSetChanged();
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

        //get brands
        mRootRef.child("Brand").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("Brands", "Load Brand: " + dataSnapshot.getKey().toString());
                brands.add(dataSnapshot.getKey());
                brandAdapter.notifyDataSetChanged();
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

        mDeviceBrand.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mRootRef.child("Brand").child(mDeviceBrand.getText().toString()).child("Model").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() != null) {
                            Map<String, String> model = dataSnapshot.getValue(Map.class);
                            Log.d("Model", "Models clear");
                            modelOfBrand = new ArrayList<>();
                            for (String i : model.keySet()) {
                                Log.d("Model", "Load Model: " + model.get(i));
                                modelOfBrand.add(model.get(i));
                            }
                            Log.d("Model", "Models complete: " + modelOfBrand);
                            modelAdapter.notifyDataSetChanged();
                        }
                    }
                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }
        });
    }

    private void createTestData(final Firebase mRootRef){
        Firebase brand = mRootRef.child("Brand");
        brand.child("Apple").child("Model").push().setValue("iPhone 1");
        brand.child("Apple").child("Model").push().setValue("iPhone 2");
        brand.child("Apple").child("Model").push().setValue("iPhone 3");
        brand.child("Apple").child("Model").push().setValue("iPhone 4");
        brand.child("Apple").child("Model").push().setValue("iPhone 5");
        brand.child("Samsung").child("Model").push().setValue("Samsung Galaxy S1");
        brand.child("Samsung").child("Model").push().setValue("Samsung Galaxy S2");
        brand.child("Samsung").child("Model").push().setValue("Samsung Galaxy S3");
        brand.child("Samsung").child("Model").push().setValue("Samsung Galaxy S4");
        brand.child("Samsung").child("Model").push().setValue("Samsung Galaxy S5");
        brand.child("Huawaii").setValue("Empty");
        brand.child("Philips").setValue("Empty");
        brand.child("HTC").setValue("Empty");
        brand.child("Nokia").setValue("Empty");

        Firebase categories = mRootRef.child("DeviceCategory");
        categories.child("Laptop").setValue("Empty");
        categories.child("Smartphone").setValue("Empty");
        categories.child("Speaker").setValue("Empty");
        categories.child("PC").setValue("Empty");
        categories.child("Powerbank").setValue("Empty");

        Firebase codition = mRootRef.child("DeviceCondition");
        codition.child("Second Hand").setValue("Empty");
        codition.child("Refurbished").setValue("Empty");
        codition.child("New").setValue("Empty");
    }
}
