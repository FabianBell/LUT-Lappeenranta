package com.fabianbell.janinakeller.lut_lappeenranta;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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
    private DatePicker mDeviceDateOfPurchase;
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
    private boolean saveImage;

    //Adapter
    private ArrayAdapter<String> modelAdapter;
    private Map<String, String> modelMap;
    private ArrayAdapter<String> categoryAdapter;
    private ArrayAdapter<String> conditionAdapter;
    private ArrayAdapter<String> brandAdapter;
    private boolean brandChanged;

    //Request Code
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private final int ADD_UNKNOWN_MODEL_QUESTION = 2;
    private final int ADD_UNKNOWN_BRAND_QUESTION = 3;
    private static final int QUESTION_SAVE_PICTURE = 6;

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
        mDeviceDateOfPurchase = (DatePicker) findViewById(R.id.deviceDateOfPurchase);
        mDeviceCondition = (Spinner) findViewById(R.id.deviceCondition);

        mReceipt = (ImageView) findViewById(R.id.receipt);

        mAddReceipt = (Button) findViewById(R.id.addReceipt);
        mAddDeviceButton = (Button) findViewById(R.id.addDeviceButton);

        mRootRef = new Firebase("https://lut-lappeenranta.firebaseio.com/");
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference().child("User_receipt");

        if (savedInstanceState != null) {
            imagePath = savedInstanceState.getString("IMAGE_PATH");
            if (imagePath != null) {
                displayImage();
            }
        }

        //createTestData(mRootRef);

        //set Date
        Calendar currentDate = Calendar.getInstance();
        mDeviceDateOfPurchase.updateDate(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH));


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
                String brandName = Utils.removeSpace(mDeviceBrand.getText().toString());
                String modelName = Utils.removeSpace(mDeviceModel.getText().toString());

                //Brand and model upload
                ArrayList<Object> param = new ArrayList<>();
                param.add(brandName);
                param.add(modelName);
                mRootRef.child("Brand").addListenerForSingleValueEvent(new CallableValueEventListener<ArrayList<Object>>(param, new CallableForFirebase<ArrayList<Object>>() {
                    @Override
                    public void call(ArrayList<Object> param, DataSnapshot data) {
                        //unpack param
                        String brandName = (String) param.get(0);
                        String modelName = (String) param.get(1);

                        if(data.child(brandName).exists()){
                            //iterate over models of brandName
                            boolean foundModel = false;
                            for(DataSnapshot childSnapshot : data.child(brandName).child("Model").getChildren()){
                                //search for matching modelName --> model not in the database with the given brand
                                if(childSnapshot.getValue().toString().equals(modelName)){
                                    String key = childSnapshot.getKey();
                                    String deviceId = mRootRef.child("Device").push().getKey();
                                    //add model and brand normal
                                    addNormalDeviceData(deviceId);
                                    Intent mainIntent = new Intent(AddDevice.this, Main.class);
                                    Utils.setBrandAndModel(modelName, brandName, deviceId, mainIntent, AddDevice.this);
                                    //addBrandModel(true, true, key, deviceId);
                                    uploadReceipt(deviceId);
                                    return;
                                }
                            }
                            //Model name not in the database
                            Log.d("Device", "Model is unknown");
                            FirebaseCrash.log("Model is unknown");
                            Intent questionIntent = new Intent(AddDevice.this, Question.class);
                            questionIntent.putExtra("QUESTION", "The given model is unknown. Are you sure you want to add this model?");
                            questionIntent.putExtra("ANSWER1", "Yes (continue)");
                            questionIntent.putExtra("ANSWER2", "No (return)");
                            startActivityForResult(questionIntent, ADD_UNKNOWN_MODEL_QUESTION);
                        }else{
                            //Brand name not in the database
                            Log.d("Device", "Brand is unknown");
                            FirebaseCrash.log("Brand is unknown");
                            Intent questionIntent = new Intent(AddDevice.this, Question.class);
                            questionIntent.putExtra("QUESTION", "The given brand is unknown. Are you sure you want to add this brand?");
                            questionIntent.putExtra("ANSWER1", "Yes (continue)");
                            questionIntent.putExtra("ANSWER2", "No (return)");
                            startActivityForResult(questionIntent, ADD_UNKNOWN_BRAND_QUESTION);
                        }
                    }
                }));
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
        mRootRef.child("DeviceCategory").addChildEventListener(new SimpleChildListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("Category", "Load Category: " + dataSnapshot.getKey());
                FirebaseCrash.log("Load Category: " + dataSnapshot.getKey());
                categories.add(dataSnapshot.getKey());
                categoryAdapter.notifyDataSetChanged();
            }
        });

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
                    modelMap = new HashMap<>();
                    Log.d("Model", "Models clear");
                    FirebaseCrash.log("Models clear");
                    mRootRef.child("Brand").child(mDeviceBrand.getText().toString()).child("Model").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String value = snapshot.getValue().toString();
                                modelOfBrand.add(value);
                                modelAdapter.notifyDataSetChanged();
                                modelMap.put(value, dataSnapshot.getKey());
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

    private void addNormalDeviceData(String deviceId){
        String category = mDeviceCategory.getSelectedItem().toString();
        String deviceNumber = mDeviceidNumber.getText().toString();
        String price = mDevicePrice.getText().toString();
        String shop = mDeviceShop.getText().toString();
        String date = mDeviceDateOfPurchase.getDayOfMonth() + "." + (mDeviceDateOfPurchase.getMonth() + 1) + "." + mDeviceDateOfPurchase.getYear();
        String condition = mDeviceCondition.getSelectedItem().toString();

        Firebase deviceById = mRootRef.child("Device").child(deviceId);

        mRootRef.child("User").child(mAuth.getCurrentUser().getUid()).child("Devices").child(deviceId).setValue("deviceId");
        Log.d("Device", "Start saving Device with Id: " + deviceId);
        FirebaseCrash.log("Start saving Device with Id: " + deviceId);
        deviceById.child("category").setValue(category);
        deviceById.child("price").setValue(price);
        deviceById.child("shop").setValue(shop);
        deviceById.child("date").setValue(date);
        deviceById.child("condition").setValue(condition);
        deviceById.child("deviceNumber").setValue(deviceNumber);
    }

    private void uploadReceipt(String deviceId){

        //upload receipt
        if (imagePath != null) {
            StorageReference imageRef = storageReference.child(mAuth.getCurrentUser().getUid() + "_" + deviceId + ".jpg");
            File imageFile = new File(imagePath);
            Uri imageUri = FileProvider.getUriForFile(AddDevice.this, "com.fabianbell.janinakeller.lut_lappeenranta.fileprovider", imageFile);
            uploadTask = imageRef.putFile(imageUri);
            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        Log.d("Receipt", "Uploaded");
                        FirebaseCrash.log("Uploaded");
                        exit();
                    }else{
                        Log.d("Receipt", "Cannot upload Image > " + task.getException().getMessage());
                        FirebaseCrash.report(task.getException());
                        exit();
                    }
                }
            });
        }else {
            Log.d("Receipt", "Upload without Receipt");
            FirebaseCrash.log("Upload without Receipt");
            exit();
        }
    }

    private void addBrandModel(boolean brandKnown, boolean modelKnown, String modelId, String deviceId){
        String brandName = mDeviceBrand.getText().toString();
        String modelName = mDeviceModel.getText().toString();
        Firebase deviceByNumber = mRootRef.child("Device").child(deviceId);

        deviceByNumber.child("brandName").setValue(brandName);
        Log.d("Device", "add brand name");
        FirebaseCrash.log("add brand name");

        if (brandKnown && modelKnown){
            Log.d("Device","Found model key");
            FirebaseCrash.log("Found model key");
            deviceByNumber.child("modelName").setValue(modelId);
            Log.d("Device", "add model id");
            FirebaseCrash.log("add model id");
        }else{
            if (brandKnown){
                Firebase brandRef = mRootRef.child("UnknownBrand_Model").child("Brand").child(brandName);
                ArrayList<Object> param = new ArrayList<>();
                param.add(brandRef); //0
                param.add(modelName); //1
                param.add(deviceByNumber); //2
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
                param.add(deviceByNumber); //2
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
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("IMAGE_PATH", imagePath);
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
            Intent questionIntent = new Intent(AddDevice.this, Question.class);
            questionIntent.putExtra("QUESTION", "Do you want to save this picture on your device as well?");
            questionIntent.putExtra("ANSWER1", "Yes");
            questionIntent.putExtra("ANSWER2", "No");
            startActivityForResult(questionIntent, QUESTION_SAVE_PICTURE);
        }
        if (requestCode == ADD_UNKNOWN_MODEL_QUESTION && resultCode == RESULT_OK) {
            String answer = data.getStringExtra("ANSWER");
            Log.d("Device", "Got answer: Answer Nr. " + answer);
            if (answer.equals("1")){
                String deviceId = mRootRef.child("Device").push().getKey();
                addNormalDeviceData(deviceId);
                String modelName = mDeviceModel.getText().toString();
                String brandName = mDeviceBrand.getText().toString();
                Intent mainIntent = new Intent(AddDevice.this, Main.class);
                Utils.setBrandAndModel(modelName, brandName, deviceId, mainIntent, AddDevice.this);
                //addBrandModel(true, false, null, deviceId);
                uploadReceipt(deviceId);
                Log.d("Device", "Saved Device");
                FirebaseCrash.log("Saved Device");
            }else{
                Toast.makeText(AddDevice.this, "Please correct your model", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == ADD_UNKNOWN_BRAND_QUESTION && resultCode == RESULT_OK){
            String answer = data.getStringExtra("ANSWER");
            Log.d("Device", "Got answer: Answer Nr. " + answer);
            if (answer.equals("1")){
                String deviceId = mRootRef.child("Device").push().getKey();
                addNormalDeviceData(deviceId);
                String modelName = mDeviceModel.getText().toString();
                String brandName = mDeviceBrand.getText().toString();
                Intent mainIntent = new Intent(AddDevice.this, Main.class);
                Utils.setBrandAndModel(modelName, brandName, deviceId, mainIntent, AddDevice.this);
                //addBrandModel(false, false, null, deviceId);
                uploadReceipt(deviceId);
                Log.d("Device", "Saved Device");
                FirebaseCrash.log("Saved Device");
            }else{
                Toast.makeText(AddDevice.this, "Please correct your Brand and Model", Toast.LENGTH_LONG).show();
            }
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
    }

    private void displayImage(){
        Log.d("Reciept", "Display Reciept");
        FirebaseCrash.log("Display Reciept");
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inSampleSize= 4;
        Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            imageBitmap = Utils.rotateBitmap(imageBitmap, orientation);
        } catch (IOException e) {
            Log.d("Receipt", "Cannot find image file");
            FirebaseCrash.report(e);
        }
        mReceipt.setImageBitmap(imageBitmap);
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