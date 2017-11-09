package com.fabianbell.janinakeller.lut_lappeenranta;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.security.Permission;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class EditDevice extends AppCompatActivity {

    // TODO add device detail in EditText

    private Firebase mRootRef;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private UploadTask uploadTask;

    //fields
    private AutoCompleteTextView mDeviceModel;
    private AutoCompleteTextView mDeviceBrand;
    private EditText mDeviceIDNumber;
    private EditText mDevicePrice;
    private EditText mDeviceShop;
    private EditText mDeviceDateOfPurchase;
    private Spinner mDeviceCondition;

    private ImageView mReceipt;

    private Button mAddReceipt;
    private Button mSaveDeviceButton;
    private Button mDeleteDeviceButton;

    private ProgressBar mUploadReceiptProgressBar;

    //autocomplete
    private ArrayList<String> brands;
    private ArrayList<String> modelOfBrand;
    private ArrayList<String> categories;
    private ArrayList<String> condition;

    //Permission
    static final int PERMISSION_REQUEST_CODE_CAMERA = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_device);

        //get Elements
        mDeviceModel = (AutoCompleteTextView) findViewById(R.id.deviceModel);
        mDeviceBrand = (AutoCompleteTextView) findViewById(R.id.deviceBrand);
        mDeviceIDNumber = (EditText) findViewById(R.id.deviceidNumber);
        mDevicePrice = (EditText) findViewById(R.id.devicePrice);
        mDeviceShop = (EditText) findViewById(R.id.deviceShop);
        mDeviceDateOfPurchase = (EditText) findViewById(R.id.deviceDateOfPurchase);
        mDeviceCondition = (Spinner) findViewById(R.id.deviceCondition);

        mReceipt = (ImageView) findViewById(R.id.receipt);

        mAddReceipt = (Button) findViewById(R.id.addReceipt);
        mSaveDeviceButton = (Button) findViewById(R.id.addDeviceButton);
        mDeleteDeviceButton = (Button) findViewById(R.id.deleteDeviceButton);

        mUploadReceiptProgressBar = (ProgressBar) findViewById(R.id.uploadReceiptProgressBar);

        mRootRef = new Firebase("https://lut-lappeenranta.firebaseio.com/");
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference().child("User_receipt");


    }
}
