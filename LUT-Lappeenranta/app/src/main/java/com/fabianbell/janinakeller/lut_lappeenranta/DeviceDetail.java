package com.fabianbell.janinakeller.lut_lappeenranta;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fabianbell.janinakeller.lut_lappeenranta.listener.SimpleFirebaseListener;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DeviceDetail extends AppCompatActivity {

    //Firebase
    Firebase mRefRoot;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;

    //Permision
    private final int PERMISSION_REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 3;

    //Reciept
    private String imagePath;

    //Button
    private FloatingActionButton mEditDeviceButton;
    private Button mRecieptButton;
    private Button mDeviceFaultReportButton;

    //special
    private ProgressBar mDownloadBar;

    //device
    private String deviceId;
    private String deviceModel;

    private TextView mDeviceModel;
    private TextView mDeviceCategory;
    private TextView mDeviceBrand;
    private TextView mDeviceNumber;
    private TextView mDevicePrice;
    private TextView mDeviceShop;
    private TextView mDeviceDate;
    private TextView mDeviceCondition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);

        mRefRoot = new Firebase("https://lut-lappeenranta.firebaseio.com/");
        mAuth = FirebaseAuth.getInstance();

        deviceId = getIntent().getStringExtra("DeviceId");
        deviceModel = getIntent().getStringExtra("DeviceModel");
        storageReference = FirebaseStorage.getInstance().getReference().child("User_receipt").child(mAuth.getCurrentUser().getUid() + "_" + deviceId + ".jpg" );

        //get Elements
        mEditDeviceButton = (FloatingActionButton) findViewById(R.id.editDeviceButton);
        mRecieptButton = (Button) findViewById(R.id.recieptButton);

        mDownloadBar = (ProgressBar) findViewById(R.id.downloadProgress);

        mDeviceModel = (TextView) findViewById(R.id.DeviceDetailModel);
        mDeviceCategory = (TextView) findViewById(R.id.DeviceDetailCategory);
        mDeviceBrand = (TextView) findViewById(R.id.DeviceDetailBrand);
        mDeviceNumber = (TextView) findViewById(R.id.DeviceDetailNumber);
        mDevicePrice = (TextView) findViewById(R.id.DeviceDetailPrice);
        mDeviceShop = (TextView) findViewById(R.id.DeviceDetailShop);
        mDeviceDate = (TextView) findViewById(R.id.DeviceDetailDate);
        mDeviceCondition = (TextView) findViewById(R.id.DeviceDetailCondition);


        mDeviceFaultReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editDeviceIntent = new Intent(DeviceDetail.this, FaultReport.class);
                editDeviceIntent.putExtra("deviceId", deviceId);
                startActivity(editDeviceIntent);
            }
        });


        mEditDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editDeviceIntent = new Intent(DeviceDetail.this, EditDevice.class);
                editDeviceIntent.putExtra("deviceId", deviceId);
                startActivity(editDeviceIntent);
            }
        });

        mRecieptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(PermissionChecker.checkSelfPermission(DeviceDetail.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    Log.d("Reciept", "Do not have write permission");
                    FirebaseCrash.log("Do not have write permission");
                    ActivityCompat.requestPermissions(DeviceDetail.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
                }else{
                    Log.d("Reciept", "Have write Permission");
                    FirebaseCrash.log("Have write Permission");
                    Log.d("Reciept", "Start with download");
                    FirebaseCrash.log("Start with download");
                    downloadImage();
                }
            }
        });

        mRefRoot.child("Device").child(deviceId).addChildEventListener(new SimpleFirebaseListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                String value = dataSnapshot.getValue().toString();
                if (key.equals("brandName")){
                    mDeviceBrand.setText(value);
                }else{
                    if(key.equals("category")){
                        mDeviceCategory.setText(value);
                    }else{
                        if(key.equals("condition")){
                            mDeviceCondition.setText(value);
                        }else{
                            if(key.equals("date")){
                                mDeviceDate.setText(value);
                            }else{
                                if(key.equals("modelName")){
                                    mDeviceModel.setText(deviceModel);
                                }else{
                                    if(key.equals("price")){
                                        mDevicePrice.setText(value);
                                    }else{
                                        if(key.equals("shop")){
                                            mDeviceShop.setText(value);
                                        }else{
                                            if(key.equals("deviceNumber")){
                                                mDeviceNumber.setText(value);
                                            }else {
                                                //todo display unknown model or brand
                                                Log.e("deviceData", "Cannot categorize Data with key: " + key);
                                                FirebaseCrash.log("Cannot categorize Data with key: " + key);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });


    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File dcim = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
        File saveDir = new File(dcim,"XBrandtation");
        if (!saveDir.exists()) {
            boolean result = saveDir.mkdir();
            if(result){
                Log.d("Reciept", "created dir: " + saveDir.getAbsolutePath());
                FirebaseCrash.log("created dir: " + saveDir.getAbsolutePath());
            }else{
                Log.d("Reciept", "Cannot create dir: " + saveDir.getAbsolutePath());
                FirebaseCrash.log("Cannot create dir: " + saveDir.getAbsolutePath());
            }
        }
        File image = File.createTempFile(imageFileName, ".jpg", saveDir);
        Log.d("Reciept", "created image " + image.getAbsolutePath());
        FirebaseCrash.log("created image " + image.getAbsolutePath());
        imagePath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Receipt", "Persmission updated");
                FirebaseCrash.log("Persmission updated");
                Log.d("Reciept", "Start with download");
                FirebaseCrash.log("Start with download");
                downloadImage();
            }
            else {
                Log.d("Reciept", "Permission cannot be updated");
                FirebaseCrash.log("Permission cannot be updated");
                Toast.makeText(DeviceDetail.this, "Without the permission you can not download a picture", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void downloadImage(){
        try {
            final File image = createImageFile();
            FileDownloadTask downloadTask = storageReference.getFile(image);
            downloadTask.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.d("Reciept", "saved image at " + imagePath);
                    FirebaseCrash.log("saved image at " + imagePath);
                    Intent scannIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri imageUri = Uri.fromFile(image);
                    scannIntent.setData(imageUri);
                    sendBroadcast(scannIntent);
                    Log.d("Reciept","Image scanned for gallary");
                    FirebaseCrash.log("Image scanned for gallary");
                    Toast.makeText(DeviceDetail.this, "Reciept is saved in your gallary", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(DeviceDetail.this, "Cannot download reciept: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            downloadTask.addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    int progress = (int) (taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount() * 100.0f);
                    mDownloadBar.setProgress(progress);
                }
            });
        } catch (IOException e) {
            Log.d("Reciept", "Cannot create Image File: " + e.getMessage());
            FirebaseCrash.report(e);
            Toast.makeText(DeviceDetail.this, "Cannot create Image File: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("IMAGE_PATH", imagePath);
    }

    private void galleryAddpic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
}
