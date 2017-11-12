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
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.firebase.crash.FirebaseCrash;
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

public class AddDevice extends AppCompatActivity {

    private Firebase mRootRef;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private UploadTask uploadTask;

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

    private ProgressBar mUploadBar;

    //autocomplete
    private ArrayList<String> brands;
    private ArrayList<String> modelOfBrand;
    private ArrayList<String> categories;
    private ArrayList<String> condition;

    //test Data
    private Map<String, String> testBrands;

    //Permission
    static final int PERMISSION_REQUEST_CODE_CAMERA = 2;

    //image
    private String imagePath;

    //Adapter
    private ArrayAdapter<String> modelAdapter;
    private ArrayAdapter<String> categoryAdapter;
    private ArrayAdapter<String> conditionAdapter;
    private ArrayAdapter<String> brandAdapter;
    private boolean brandChanged;

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

        mUploadBar = (ProgressBar) findViewById(R.id.uploadBar);

        mRootRef = new Firebase("https://lut-lappeenranta.firebaseio.com/");
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference().child("User_receipt");

        if (savedInstanceState != null) {
            imagePath = savedInstanceState.getString("IMAGE_PATH");
            displayImage();
        }

        //createTestData(mRootRef);

        brands = new ArrayList<>();

        brandAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, brands);

        mDeviceBrand.setAdapter(brandAdapter);

        modelOfBrand = new ArrayList<>();

        modelAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, modelOfBrand);

        mDeviceModel.setAdapter(modelAdapter);

        categories = new ArrayList<>();

        categoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, categories);

        mDeviceCategory.setAdapter(categoryAdapter);

        condition = new ArrayList<>();

        conditionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, condition);

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

                Firebase deviceByNumber = mRootRef.child("Device").child(deviceNumber);
                //connect device to user
                mRootRef.child("User").child(mAuth.getCurrentUser().getUid()).child("Devices").child(deviceNumber).setValue("deviceNumber");
                Log.d("Device", "Start saving Device with Number: " + deviceNumber);
                FirebaseCrash.log("Start saving Device with Number: " + deviceNumber);
                deviceByNumber.child("category").setValue(category);
                deviceByNumber.child("brandName").setValue(brandName);
                deviceByNumber.child("modelName").setValue(modelName);
                deviceByNumber.child("price").setValue(price);
                deviceByNumber.child("shop").setValue(shop);
                deviceByNumber.child("date").setValue(date);
                deviceByNumber.child("condition").setValue(condition);
                Log.d("AddDevice - Device", "Device saved");
                FirebaseCrash.log("Device saved");

                //upload receipt
                if (imagePath != null) {
                    StorageReference imageRef = storageReference.child(mAuth.getCurrentUser().getUid() + "_" + deviceNumber + ".jpg");
                    File imageFile = new File(imagePath);
                    Uri imageUri = FileProvider.getUriForFile(AddDevice.this, "com.fabianbell.janinakeller.lut_lappeenranta.fileprovider", imageFile);
                    uploadTask = imageRef.putFile(imageUri);
                    uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            int progress;
                            if (taskSnapshot.getBytesTransferred() > 0) {
                                progress = (int) (taskSnapshot.getTotalByteCount() / taskSnapshot.getBytesTransferred() * 100.0f);
                            }else{
                                progress = 0;
                            }
                            mUploadBar.setProgress(progress);
                        }
                    });
                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()){
                                Log.d("Receipt", "Uploaded");
                                FirebaseCrash.log("Uploaded");
                                startActivity(new Intent(AddDevice.this, Main.class));
                            }else{
                                Log.d("Receipt", "Cannot upload Image > " + task.getException().getMessage());
                                FirebaseCrash.report(task.getException());
                            }
                        }
                    });
                }else {
                    Log.d("Receipt", "Upload without Receipt");
                    FirebaseCrash.log("Upload without Receipt");
                    startActivity(new Intent(AddDevice.this, Main.class));
                }
            }
        });

        mAddReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check for permission
                if(PermissionChecker.checkSelfPermission(AddDevice.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(AddDevice.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE_CAMERA);
                }else{
                    Log.d("Receipt", "Add Reciept");
                    FirebaseCrash.log("Add Reciept");
                    takePicture();
                }
            }
        });

        //get categories
        mRootRef.child("DeviceCategory").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("Category", "Load Category: " + dataSnapshot.getKey());
                FirebaseCrash.log("Load Category: " + dataSnapshot.getKey());
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
                FirebaseCrash.log("Load Condition: " + dataSnapshot.getKey());
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
                FirebaseCrash.log("Load Brand: " + dataSnapshot.getKey().toString());
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
                    modelOfBrand.removeAll(modelOfBrand);
                    modelAdapter.notifyDataSetChanged();
                    Log.d("Model", "Models clear");
                    FirebaseCrash.log("Models clear");
                    mRootRef.child("Brand").child(mDeviceBrand.getText().toString()).child("Model").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String value = snapshot.getValue().toString();
                                modelOfBrand.add(value);
                                modelAdapter.notifyDataSetChanged();
                                Log.d("Model", "Load model: " + value);
                                FirebaseCrash.log("Load model: " + value);
                            }
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
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("IMAGE_PATH", imagePath);
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

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
                Toast.makeText(AddDevice.this, "Without the permission you can not upload a picture", Toast.LENGTH_LONG);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //display picture
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d("Receipt", "Took Picture");
            FirebaseCrash.log("Took Picture");
            displayImage();
        }
    }

    private void displayImage(){
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inSampleSize= 4;
        Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            imageBitmap = rotateBitmap(imageBitmap, orientation);
        } catch (IOException e) {
            Log.d("Receipt", "Cannot find image file");
            FirebaseCrash.log("Cannot find image file");
        }
        mReceipt.setImageBitmap(imageBitmap);
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
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