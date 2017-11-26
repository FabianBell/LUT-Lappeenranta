package com.fabianbell.janinakeller.lut_lappeenranta;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.fabianbell.janinakeller.lut_lappeenranta.listener.CallableForFirebase;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.CallableValueEventListener;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.SimpleChildListener;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.SimpleValueListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditDevice extends AppCompatActivity {

    private Firebase mRootRef;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;
    private UploadTask uploadTask;

    //fields
    private AutoCompleteTextView mDeviceModel;
    private AutoCompleteTextView mDeviceBrand;
    private EditText mDeviceIDNumber;
    private EditText mDevicePrice;
    private EditText mDeviceShop;
    private DatePicker mDeviceDateOfPurchase;
    private Spinner mDeviceCondition;

    //adapter
    private ArrayAdapter<String> conditionAdapter;
    private ArrayAdapter<String> brandAdapter;
    private ArrayAdapter<String> modelAdapter;
    private Map<String, String> modelMap;

    private ImageView mReceiptThumbnail;

    private Button mAddReceipt;
    private Button mSaveDeviceButton;
    private Button mDeleteDeviceButton;
    Button mPickFromGalleryButton;

    //private ProgressBar mUploadReceiptProgressBar;

    //autocomplete
    private ArrayList<String> brands;
    private ArrayList<String> modelOfBrand;
    private boolean brandChanged;
    private ArrayList<String> condition;

    //Permission
    private static final int PERMISSION_REQUEST_CODE_CAMERA = 2;
    static final int READ_EXTERNAL_STORAGE = 3;

    //request code
    private static final int QUESTION_DELETE = 4;
    private static final int QUESTION_FAULTREPORT = 5;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int QUESTION_SAVE_PICTURE = 6;
    private static final int PICK_IMAGE = 8;

    //ID
    private String deviceId;

    //lifetimeData
    private Map<String, String> data;
    private String thumbnailPath;
    private String imagePath;
    private boolean saveImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_device);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null){
            thumbnailPath = savedInstanceState.getString("THUMBNAIL_PATH");
            imagePath = savedInstanceState.getString("IMAGE_PATH");
            String saveImageString = savedInstanceState.getString("IMAGE_SAVE");
            if (saveImageString != null){
                saveImage = Boolean.valueOf(saveImageString);
            }
            if (imagePath != null || thumbnailPath != null){
                displayImage();
            }
        }

        mRootRef = new Firebase("https://lut-lappeenranta.firebaseio.com/");
        mAuth = FirebaseAuth.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference().child("User_receipt");

        deviceId = getIntent().getStringExtra("deviceId");

        //get Elements
        mDeviceModel = findViewById(R.id.deviceModel);
        mDeviceBrand = findViewById(R.id.deviceBrand);
        mDeviceIDNumber = findViewById(R.id.deviceIDNumber);
        mDevicePrice = findViewById(R.id.devicePrice);
        mDeviceShop = findViewById(R.id.deviceShop);
        mDeviceDateOfPurchase = findViewById(R.id.deviceDateOfPurchase);
        mDeviceCondition = findViewById(R.id.deviceCondition);
        mPickFromGalleryButton = findViewById(R.id.pickFromGalaryButton);

        brands = new ArrayList<>();
        brandAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, brands);
        mDeviceBrand.setAdapter(brandAdapter);

        modelOfBrand = new ArrayList<>();
        modelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, modelOfBrand);
        mDeviceModel.setAdapter(modelAdapter);

        condition = new ArrayList<>();
        conditionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, condition);
        mDeviceCondition.setAdapter(conditionAdapter);

        //get brands
        mRootRef.child("Brand").addChildEventListener(new SimpleChildListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("Brands", "Load Brand: " + dataSnapshot.getKey());
                FirebaseCrash.log("Load Brand: " + dataSnapshot.getKey());
                brands.add(dataSnapshot.getKey());
                brandAdapter.notifyDataSetChanged();
            }
        });
        //only update model if brand was changed
        mDeviceBrand.addTextChangedListener(new TextWatcher() {
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
        mDeviceModel.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (brandChanged) {
                    Log.d("Debug", modelAdapter.getContext().toString());
                    modelMap = new HashMap<>();
                    Log.d("Model", "Models clear");
                    FirebaseCrash.log("Models clear");
                    mRootRef.child("Brand").child(mDeviceBrand.getText().toString()).child("Model").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            modelOfBrand = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String value = snapshot.getValue().toString();
                                Log.d("Model", "Load model: " + value);
                                FirebaseCrash.log("Load model: " + value);
                                modelOfBrand.add(value);
                            }
                            modelAdapter = new ArrayAdapter<String>(EditDevice.this, android.R.layout.simple_list_item_1, modelOfBrand);
                            mDeviceModel.setAdapter(modelAdapter);
                            brandChanged = false;
                            Log.d("Model", "Model updated");
                            FirebaseCrash.log("Model updated");
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            Log.d("Model", firebaseError.getMessage());
                            FirebaseCrash.report(firebaseError.toException());
                        }
                    });
                }
            }
        });

        mReceiptThumbnail = findViewById(R.id.recieptThumbnail);

        mAddReceipt = findViewById(R.id.addReceipt);
        mSaveDeviceButton = findViewById(R.id.saveDeviceButton);
        mDeleteDeviceButton = findViewById(R.id.deleteDeviceButton);

        mDeleteDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent questionIntent = new Intent(EditDevice.this, Question.class);
                questionIntent.putExtra("QUESTION", "Do you really want to delete this device?");
                questionIntent.putExtra("ANSWER1", "Yes");
                questionIntent.putExtra("ANSWER2", "No");
                startActivityForResult(questionIntent, QUESTION_DELETE);
            }
        });

        setData();

        mSaveDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get lifetimeData
                String brandName = Utils.removeSpace(mDeviceBrand.getText().toString());
                String condition = mDeviceCondition.getSelectedItem().toString();
                String date = mDeviceDateOfPurchase.getDayOfMonth() + "." + (mDeviceDateOfPurchase.getMonth() + 1)  + "." + mDeviceDateOfPurchase.getYear();
                String deviceNumber = mDeviceIDNumber.getText().toString();
                String modelName = Utils.removeSpace(mDeviceModel.getText().toString());
                String price = mDevicePrice.getText().toString();
                String shop = Utils.removeSpace(mDeviceShop.getText().toString());

                Firebase device = mRootRef.child("Device").child(deviceId);

                //check for update
                boolean modelChanged = false;
                for (Map.Entry entry : data.entrySet()){
                    if (entry.getKey().equals("shop") && !entry.getValue().equals(shop)) {
                        device.child("shop").setValue(shop);
                    }else {
                        if(entry.getKey().equals("condition") && !entry.getValue().equals(condition)){
                            device.child("condition").setValue(condition);
                        }else {
                            if(entry.getKey().equals("date") && !entry.getValue().equals(date)){
                                device.child("date").setValue(date);
                            }else{
                                if(entry.getKey().equals("deviceNumber") && !entry.getValue().equals(deviceNumber)){
                                    device.child("deviceNumber").setValue(deviceNumber);
                                }else {
                                    if (entry.getKey().equals("price") && !entry.getValue().equals(price)) {
                                        device.child("price").setValue(price);
                                    } else {
                                        if (entry.getKey().equals("brandName") && !entry.getValue().equals(brandName)) {
                                            Log.d("Save", "Brand changed > model changed as well");
                                            FirebaseCrash.log("Brand changed > model changed as well");
                                            modelChanged = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Intent deviceDetailIntent = new Intent(EditDevice.this, DeviceDetail.class);
                deviceDetailIntent.putExtra("DeviceId", deviceId);
                deviceDetailIntent.putExtra("DeviceModel", modelName);

                //upload image
                if (imagePath != null){
                    StorageReference imageRef = storageReference.child(mAuth.getCurrentUser().getUid() + "_" + deviceId + ".jpg");
                    StorageReference thumbnailRef = storageReference.child("thumb_" + mAuth.getCurrentUser().getUid() + "_" + deviceId + ".jpg");
                    thumbnailRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Receipt", "delete old thumbnail");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Reciept", "Cannot delete old thumbnail");
                            FirebaseCrash.report(e);
                        }
                    });
                    File imageFile = new File(imagePath);
                    Uri imageUri = FileProvider.getUriForFile(EditDevice.this, "com.fabianbell.janinakeller.lut_lappeenranta.fileprovider", imageFile);
                    uploadTask = imageRef.putFile(imageUri);
                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            exit();
                        }
                    });
                }else {
                    exit();
                }
                if (modelChanged || !data.get("modelName").equals(modelName)){
                    //model chnaged thwo brand change but model in field was changed
                    Utils.setBrandAndModel(modelName, brandName, deviceId, deviceDetailIntent, EditDevice.this);
                }else {
                    startActivity(deviceDetailIntent);
                }
            }
        });

        mAddReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check for permission
                if(PermissionChecker.checkSelfPermission(EditDevice.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(EditDevice.this, new String[]{android.Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE_CAMERA);
                }else{
                    Log.d("Receipt", "Add Reciept");
                    FirebaseCrash.log("Add Reciept");
                    takePicture();
                }
            }
        });

        mPickFromGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check for permission
                if(PermissionChecker.checkSelfPermission(EditDevice.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(EditDevice.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
                }else{
                    EditDevice.this.loadImageFromGallery();
                }
            }
        });
    }

    private void changeModel(boolean unknownBrand, String brandName, String modelName, Firebase device){
        Log.d("Save", "Model changed > search for id");
        FirebaseCrash.log("Model changed > search for id");
        if (!unknownBrand){
            Firebase brandRef = mRootRef.child("UnknownBrand_Model").child("Brand").child(brandName);
            ArrayList<Object> param = new ArrayList<>();
            param.add(brandRef); //0
            param.add(modelName); //1
            param.add(device); //2
            //get models for brand
            brandRef.child("Model").addListenerForSingleValueEvent(new CallableValueEventListener<ArrayList<Object>>(param, new CallableForFirebase<ArrayList<Object>>() {
                @Override
                public void call(ArrayList<Object> param, DataSnapshot data) {
                    String newModelId = null;
                    Firebase brandRef = (Firebase) param.get(0);
                    Firebase deviceByNumber = (Firebase) param.get(2);
                    Firebase modelRef = null;
                    //find Model
                    findModelLoop:
                    for (DataSnapshot snapshot : data.getChildren()){
                        for(DataSnapshot entry : snapshot.getChildren()){
                            if(entry.getKey().equals("Name")){
                                if(entry.getValue().toString().equals(param.get(1))){
                                    //known model > get Key
                                    Log.d("Save", "Model is known > insert id");
                                    FirebaseCrash.log("Model is known > insert id");
                                    newModelId = snapshot.getKey();
                                    Log.d("Save", "Increase Model usage");
                                    FirebaseCrash.log("Increase Model usage");
                                    modelRef = brandRef.child("Model").child(newModelId);
                                    //increase usage
                                    brandRef.child("Model").child(newModelId).child("usage").addListenerForSingleValueEvent(new CallableValueEventListener<Firebase>(modelRef, new CallableForFirebase<Firebase>() {
                                        @Override
                                        public void call(Firebase param, DataSnapshot data) {
                                            if(data.getValue() == null){
                                                param.child("usage").setValue("1");
                                            }else{
                                                String increment = Integer.toString(Integer.parseInt(data.getValue().toString()) + 1);
                                                param.child("usage").setValue(increment);
                                            }
                                        }
                                    }));
                                    break findModelLoop;
                                }
                            }
                        }
                    }
                    if (newModelId == null){
                        //new Model
                        Log.d("Save", "Model is unknown > insern new model");
                        FirebaseCrash.log("Model is unknown > insern new model");
                        newModelId = brandRef.child("Model").push().getKey();
                        modelRef = brandRef.child("Model").child(newModelId);
                        Firebase usage = modelRef.child("usage");
                        //set usage to 1
                        usage.setValue("1");
                    }
                    modelRef.child("Name").setValue(param.get(1));
                    Log.d("Device", "added unknown model to database");
                    FirebaseCrash.log("added unknown model to database");
                    deviceByNumber.child("modelName").setValue(newModelId);
                    deviceByNumber.child("unknownModel").setValue("true");
                    Log.d("Device", "Model marked as unknown");
                    FirebaseCrash.log("Model marked as unknown");
                }
            }));
        }else {
            //Model is unknown
            Firebase brandRef = mRootRef.child("UnknownBrand_Model").child("Brand").child(brandName);
            ArrayList<Object> param = new ArrayList<>();
            param.add(brandRef); //0
            param.add(modelName); //1
            param.add(device); //2
            param.add(brandName); //3
            brandRef.child("Model").addListenerForSingleValueEvent(new CallableValueEventListener<ArrayList<Object>>(param, new CallableForFirebase<ArrayList<Object>>() {
                @Override
                public void call(ArrayList<Object> param, DataSnapshot data) {
                    String newModelId = null;
                    Firebase brandRef = (Firebase) param.get(0);
                    Firebase deviceByNumber = (Firebase) param.get(2);
                    Firebase modelRef = null;
                    findModelLoop:
                    for (DataSnapshot snapshot : data.getChildren()) {
                        for (DataSnapshot entry : snapshot.getChildren()) {
                            if (entry.getKey().equals("Name")) {
                                if (entry.getValue().toString().equals(param.get(1))) {
                                    //known model > get Key
                                    Log.d("Save", "Model is known > insert id");
                                    FirebaseCrash.log("Model is known > insert id");
                                    newModelId = snapshot.getKey();
                                    Log.d("Save", "Increase Model usage");
                                    FirebaseCrash.log("Increase Model usage");
                                    modelRef = brandRef.child("Model").child(newModelId);
                                    //increase usage
                                    brandRef.child("Model").child(newModelId).child("usage").addListenerForSingleValueEvent(new CallableValueEventListener<Firebase>(modelRef, new CallableForFirebase<Firebase>() {
                                        @Override
                                        public void call(Firebase param, DataSnapshot data) {
                                            if (data.getValue() == null) {
                                                param.child("usage").setValue("1");
                                            } else {
                                                String increment = Integer.toString(Integer.parseInt(data.getValue().toString()) + 1);
                                                param.child("usage").setValue(increment);
                                            }
                                        }
                                    }));
                                    break findModelLoop;
                                }
                            }
                        }
                    }
                    if (newModelId == null) {
                        //new Model
                        Log.d("Save", "Model is unknown > insern new model");
                        FirebaseCrash.log("Model is unknown > insern new model");
                        newModelId = brandRef.child("Model").push().getKey();
                        modelRef = brandRef.child("Model").child(newModelId);
                        Firebase usage = modelRef.child("usage");
                        //set usage to 1
                        usage.setValue("1");
                    }
                    modelRef.child("Name").setValue(param.get(1));
                    Log.d("Device", "added unknown brand and model to database");
                    FirebaseCrash.log("added unknown brand and model to database");
                    deviceByNumber.child("brandName").setValue(param.get(3));
                    deviceByNumber.child("unknownBrand").setValue("true");
                    deviceByNumber.child("modelName").setValue(newModelId);
                    deviceByNumber.child("unknownModel").setValue("true");
                    Log.d("Device", "Brand and model marked as unknown");
                    FirebaseCrash.log("Brand and model marked as unknown");
                }
            }));
        }
    }

    private void setData() {
        mRootRef.child("Device").child(deviceId).addListenerForSingleValueEvent(new SimpleValueListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                data = dataSnapshot.getValue(Map.class);
                final String modelId = data.get("modelName");
                String brandName = data.get("brandName");
                //check for unknown model
                if (!data.containsKey("unknownModel")) {
                    //known Model
                    Log.d("Data", "Known Model");
                    FirebaseCrash.log("Known Model");
                    mRootRef.child("Brand").child(brandName).child("Model").addListenerForSingleValueEvent(new SimpleValueListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot.getKey().equals(modelId)) {
                                    String modelName = snapshot.getValue().toString();
                                    data.put("modelName", modelName);

                                    //lifetimeData set complete
                                    Log.d("Data", "Data loaded successfully");
                                    FirebaseCrash.log("Data loaded successfully");

                                    mDeviceModel.setText(data.get("modelName"));
                                    mDeviceBrand.setText(data.get("brandName"));
                                    mDeviceIDNumber.setText(data.get("deviceNumber"));
                                    mDevicePrice.setText(data.get("price"));
                                    mDeviceShop.setText(data.get("shop"));
                                    String[] date = data.get("date").split("\\.");
                                    mDeviceDateOfPurchase.updateDate(Integer.parseInt(date[2]), Integer.parseInt(date[1]) - 1, Integer.parseInt(date[0]));
                                    //get Conditions
                                    mRootRef.child("DeviceCondition").addChildEventListener(new SimpleChildListener() {
                                        @Override
                                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                            Log.d("Condition", "Load Condition: " + dataSnapshot.getKey());
                                            FirebaseCrash.log("Load Condition: " + dataSnapshot.getKey());
                                            condition.add(dataSnapshot.getKey());
                                            conditionAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                                modelOfBrand.add(snapshot.getValue().toString());
                                modelAdapter.notifyDataSetChanged();
                                Log.d("Model", "First model update > Load model: " + snapshot.getValue().toString());
                                FirebaseCrash.log("First model update > Load model: " + snapshot.getValue().toString());
                            }
                        }
                    });
                }else{
                    //unknown model
                    Log.d("Data", "Unknown Model");
                    FirebaseCrash.log("Unknown Model");
                    mRootRef.child("UnknownBrand_Model").child("Brand").child(brandName).child("Model").child(modelId).child("Name").addListenerForSingleValueEvent(new SimpleValueListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String modelName = dataSnapshot.getValue().toString();
                            data.put("modelName", modelName);

                            //lifetimeData set complete
                            Log.d("Data", "Data loaded successfully");
                            FirebaseCrash.log("Data loaded successfully");

                            mDeviceModel.setText(data.get("modelName"));
                            mDeviceBrand.setText(data.get("brandName"));
                            mDeviceIDNumber.setText(data.get("deviceNumber"));
                            mDevicePrice.setText(data.get("price"));
                            mDeviceShop.setText(data.get("shop"));
                            String[] date = data.get("date").split("\\.");
                            Log.d("debug", Integer.parseInt(date[2]) + "," + Integer.parseInt(date[1]) + "," + Integer.parseInt(date[0]));
                            mDeviceDateOfPurchase.updateDate(Integer.parseInt(date[2]), Integer.parseInt(date[1]) - 1, Integer.parseInt(date[0]));

                            //get Conditions
                            mRootRef.child("DeviceCondition").addChildEventListener(new SimpleChildListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                    Log.d("Condition", "Load Condition: " + dataSnapshot.getKey());
                                    FirebaseCrash.log("Load Condition: " + dataSnapshot.getKey());
                                    condition.add(dataSnapshot.getKey());
                                    conditionAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    });
                    if(!data.containsKey("unknownBrand")){
                        modelMap = new HashMap<>();
                        mRootRef.child("Brand").child(brandName).child("Model").addChildEventListener(new SimpleChildListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                modelOfBrand.add(dataSnapshot.getValue().toString());
                                modelAdapter.notifyDataSetChanged();
                                modelMap.put(dataSnapshot.getValue().toString(), dataSnapshot.getKey());
                                Log.d("Model", "First model update > Load model: " + dataSnapshot.getValue().toString());
                                FirebaseCrash.log("First model update > Load model: " + dataSnapshot.getValue().toString());
                            }
                        });
                    }
                }
            }
        });

        //download thumbnail
        if (thumbnailPath == null && imagePath == null) {
            StorageReference thumbnailRef = storageReference.child("thumb_" + mAuth.getCurrentUser().getUid() + "_" + deviceId + ".jpg");
            try {
                File thumbnail = getImageFile();
                Log.d("thumbnail", "Created the thumbnail file at: " + thumbnail.getAbsolutePath());
                FirebaseCrash.log("Created the thumbnail file");
                thumbnailRef.getFile(thumbnail).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Log.d("thumbnail", "thumbnail downloaded successfully");
                        FirebaseCrash.log("thumbnail downloaded successfully");
                        mAddReceipt.setText("CHANGE RECEIPT");
                        mPickFromGalleryButton.setText("CHANGE RECEIPT FROM GALLERY");
                        displayImage();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mAddReceipt.setText("ADD RECEIPT");
                        mPickFromGalleryButton.setText("ADD RECEIPT FROM GALLERY");
                    }
                });
            } catch (IOException e) {
                Log.d("thumbnail", "cannot create image file");
                FirebaseCrash.report(e);
            }
        }
    }

    private File getImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getFilesDir();
        File pictureDir = new File(storageDir + File.separator + "thumbnails");
        if (!pictureDir.exists()){
            pictureDir.mkdir();
            Log.d("thumbnail", "Pictures directory does not exist. Created a new one.");
            FirebaseCrash.log("Pictures directory does not exist. Created a new one.");
        }
        File image = new File(pictureDir + File.separator + imageFileName + ".jpg");
        //File image = File.createTempFile(imageFileName,".jpg", pictureDir);
        thumbnailPath = image.getAbsolutePath();
        return image;
    }

    private void displayImage(){
        String path = null;
        if(imagePath == null){
            path = thumbnailPath;
        }else{
            path = imagePath;
        }
        Bitmap thumbBitmap = BitmapFactory.decodeFile(path);
        ExifInterface exif = null;
        try {
            //todo bugfix: rotate bitmap
            //todo bugfix: thumbnail creation
            exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            thumbBitmap = Utils.rotateBitmap(thumbBitmap, orientation);
        } catch (IOException e) {
            Log.d("Receipt", "Cannot find image file");
            FirebaseCrash.report(e);
        }
        mReceiptThumbnail.setImageBitmap(thumbBitmap);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("THUMBNAIL_PATH", thumbnailPath);
        savedInstanceState.putString("IMAGE_PATH", imagePath);
        savedInstanceState.putString("IMAGE_SAVE", Boolean.toString(saveImage));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QUESTION_DELETE && resultCode == RESULT_OK){
            String answer = data.getStringExtra("ANSWER");
            if (answer.equals("1")){
                Intent questionIntent = new Intent(EditDevice.this, Question.class);
                questionIntent.putExtra("QUESTION", "Do you want to make a fault report before deleting the device?");
                questionIntent.putExtra("ANSWER1", "Yes");
                questionIntent.putExtra("ANSWER2", "No (delete)");
                startActivityForResult(questionIntent, QUESTION_FAULTREPORT);
            }
        }
        if (requestCode == QUESTION_FAULTREPORT && resultCode == RESULT_OK){
            String answer = data.getStringExtra("ANSWER");
            if (answer.equals("1")){
                Log.d("question", "FaultReport");
                //todo connect to fault report
            }else{
                Utils.deleteDevice(deviceId, mAuth.getCurrentUser().getUid());
                Intent mainIntent = new Intent(EditDevice.this, Main.class);
                exit();
                startActivity(mainIntent);
            }
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d("Receipt", "Took Picture");
            FirebaseCrash.log("Took Picture");
            displayImage();
            Intent questionIntent = new Intent(EditDevice.this, Question.class);
            questionIntent.putExtra("QUESTION", "Do you want to save this picture on your device as well?");
            questionIntent.putExtra("ANSWER1", "Yes");
            questionIntent.putExtra("ANSWER2", "No");
            startActivityForResult(questionIntent, QUESTION_SAVE_PICTURE);
        }
        if (requestCode == QUESTION_SAVE_PICTURE && resultCode == RESULT_OK) {
            String answer = data.getStringExtra("ANSWER");
            if (answer.equals("1")) {
                Log.d("Reciept", "Save image on device");
                FirebaseCrash.log("Save image on device");
                saveImage = true;
            }else{
                Log.d("Reciept", "Will not save image on device");
                FirebaseCrash.log("Will not save image on device");
                saveImage = false;
            }
        }
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK){
            Uri uri = data.getData();
            String wholeID = DocumentsContract.getDocumentId(uri);

            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];

            String[] column = { MediaStore.Images.Media.DATA };

            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";

            Cursor cursor = getContentResolver().
                    query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            column, sel, new String[]{ id }, null);

            int columnIndex = cursor.getColumnIndex(column[0]);
            if (cursor.moveToFirst()) {
                imagePath = cursor.getString(columnIndex);
            }
            cursor.close();
            displayImage();
        }
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File imageFile = null;
            try{
                imageFile = createImageFile();
            }catch (IOException e){
                Log.d("Receipt", "Cannot create image file");
                FirebaseCrash.log("Cannot create image file");
            }
            if (imageFile != null) {

                Uri imageUri = FileProvider.getUriForFile(this,"com.fabianbell.janinakeller.lut_lappeenranta.fileprovider", imageFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg", storageDir);
        imagePath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Receipt", "Add Reciept");
                FirebaseCrash.log("Add Reciept");
                takePicture();
            }
            else {
                Toast.makeText(EditDevice.this, "Without the permission you can not upload a picture", Toast.LENGTH_LONG);
            }
        }
        if (requestCode == READ_EXTERNAL_STORAGE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                loadImageFromGallery();
            }else{
                Toast.makeText(EditDevice.this, "Without the permission you can not upload a picture", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (thumbnailPath != null) {
            File thumbnail = new File(thumbnailPath);
            boolean isDeleted = thumbnail.delete();
            if(isDeleted){
                Log.d("thumbnail", "delete thumbnail from internal storage");
                FirebaseCrash.log("delete thumbnail from internal storage");
            }else{
                Log.d("thumbnail", "Cannot delete thumbnail from internal storage");
                FirebaseCrash.report(new Exception());
            }
        }
        if (!saveImage) {
            if (imagePath != null) {
                File image = new File(imagePath);
                boolean isDeletedImage = image.delete();
                if (isDeletedImage) {
                    Log.d("receipt", "Deleted receipt from external storage");
                    FirebaseCrash.log("Deleted receipt from external storage");
                }
            } else {
                Log.d("lifetimeData", "ImagePath is null but saveImage is not null");
                FirebaseCrash.report(new Exception("ImagePath is null but saveImage is not null"));
            }
        }
    }

    private void exit(){
        if (!saveImage) {
            if (imagePath != null) {
                File image = new File(imagePath);
                boolean isDeletedImage = image.delete();
                if (isDeletedImage) {
                    Log.d("receipt", "Deleted receipt from external storage");
                    FirebaseCrash.log("Deleted receipt from external storage");
                }
            } else {
                Log.d("lifetimeData", "ImagePath is null but saveImage is not null");
                FirebaseCrash.report(new Exception("ImagePath is null but saveImage is not null"));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent deviceDetailIntent = new Intent(EditDevice.this, DeviceDetail.class);
        deviceDetailIntent.putExtra("DeviceId", deviceId);
        startActivity(deviceDetailIntent);
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent deviceDetailIntent = new Intent(EditDevice.this, DeviceDetail.class);
        deviceDetailIntent.putExtra("DeviceId", deviceId);
        startActivity(deviceDetailIntent);
    }

    private void loadImageFromGallery(){
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        //Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        //chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }
}