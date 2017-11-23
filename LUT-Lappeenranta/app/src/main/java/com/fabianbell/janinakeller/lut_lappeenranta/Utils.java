package com.fabianbell.janinakeller.lut_lappeenranta;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.annotation.NonNull;
import android.util.Log;

import com.fabianbell.janinakeller.lut_lappeenranta.listener.CallableForFirebase;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.CallableValueEventListener;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.DataAdapter;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.SimpleValueListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Fabian on 18.11.2017.
 */

public class Utils {

    private static Firebase mRootRef = new Firebase("https://lut-lappeenranta.firebaseio.com/");

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

    public static void setBrandAndModel(String modelName, String brandName, String deviceId){
        setBrandAndModel(modelName, brandName, deviceId, null, null);
    }

    public static void setBrandAndModel(String modelName, String brandName, String deviceId, Intent afterDataChange, Activity context){
        final Intent finalChange = afterDataChange;
        final Activity finalContext = context;
        Firebase device = mRootRef.child("Device").child(deviceId);

        device.child("brandName").setValue(brandName);
        Log.d("Device", "add brand name");
        FirebaseCrash.log("add brand name");
        ArrayList<Object> param = new ArrayList<>();
        param.add(mRootRef); //0
        param.add(device); //1
        param.add(modelName); //2
        param.add(brandName); //3
        //get known status
        mRootRef.child("Brand").child(brandName).addListenerForSingleValueEvent(new CallableValueEventListener<ArrayList<Object>>(param, new CallableForFirebase<ArrayList<Object>>() {
            @Override
            public void call(ArrayList<Object> param, DataSnapshot data) {
                boolean brandKnown = false;
                if(data.getValue() != null){
                    brandKnown = true;
                }
                param.add(brandKnown); //4
                ((Firebase) param.get(0)).child("Brand").child((String) param.get(3)).child("Model").addListenerForSingleValueEvent(new CallableValueEventListener<ArrayList<Object>>(param, new CallableForFirebase<ArrayList<Object>>() {
                    @Override
                    public void call(ArrayList<Object> param, DataSnapshot data) {
                        Map<String, String> models = data.getValue(Map.class);
                        boolean modelKnown = false;
                        if(models != null){
                            if(models.containsValue(param.get(2))) {
                                modelKnown = true;
                            }
                        }
                        //unpack param
                        Firebase mRootRef = (Firebase) param.get(0);
                        Firebase device = (Firebase) param.get(1);
                        String modelName = (String) param.get(2);
                        String brandName = (String) param.get(3);
                        boolean brandKnown = (boolean) param.get(4);

                        if (brandKnown && modelKnown){
                            //get Model Id
                            ArrayList<Object> simpleParam = new ArrayList<>();
                            simpleParam.add(device); //0
                            simpleParam.add(modelName); //1
                            Log.d("Data", "Search model in brand: " + brandName +  "with model name: " + modelName);
                            FirebaseCrash.log("Search model in brand: " + brandName +  "with model name: " + modelName);
                            mRootRef.child("Brand").child(brandName).child("Model").addListenerForSingleValueEvent(new CallableValueEventListener<ArrayList<Object>>(simpleParam, new CallableForFirebase<ArrayList<Object>>() {
                                @Override
                                public void call(ArrayList<Object> param, DataSnapshot data) {
                                    Map<String, String> models = data.getValue(Map.class);
                                    String modelName = (String) param.get(1);
                                    String modelId = null;
                                    Firebase device = (Firebase) param.get(0);
                                    for(Map.Entry model : models.entrySet()){
                                        if (model.getValue().toString().equals(modelName)){
                                            modelId = (String) model.getKey();
                                        }
                                    }
                                    if(modelId == null){
                                        FirebaseCrash.report(new Exception());
                                    }
                                    Log.d("Device","Found model key");
                                    FirebaseCrash.log("Found model key");
                                    device.child("modelName").setValue(modelId);
                                    device.child("unknownBrand").setValue(null);
                                    device.child("unknownModel").setValue(null);
                                    Log.d("Device", "add model id");
                                    FirebaseCrash.log("add model id");
                                    Log.d("Data", "Model and Brand upload finish");
                                    FirebaseCrash.log("Model and Brand upload finish");
                                    if (finalContext != null && finalChange != null) {
                                        finalContext.startActivity(finalChange);
                                    }else{
                                        if (finalContext == null && finalChange == null);
                                        else{
                                            throw new NullPointerException();
                                        }
                                    }
                                }
                            }));
                        }else{
                            Firebase brandRef = mRootRef.child("UnknownBrand_Model").child("Brand").child(brandName);
                            param.add(brandRef); //5
                            param.add(modelKnown); //6
                            //get models for brand
                            brandRef.child("Model").addListenerForSingleValueEvent(new CallableValueEventListener<ArrayList<Object>>(param, new CallableForFirebase<ArrayList<Object>>() {
                                @Override
                                public void call(ArrayList<Object> param, DataSnapshot data) {
                                    String newModelId = null;
                                    Firebase brandRef = (Firebase) param.get(5);
                                    Firebase deviceByNumber = (Firebase) param.get(1);
                                    boolean modelKnown = (boolean) param.get(6);
                                    boolean brandKnown = (boolean) param.get(4);
                                    //somehow the the value is casted like "Test" and cannot be compared with Test
                                    boolean test = "Test".startsWith("T");
                                    String modelName = (String) param.get(2);
                                    Firebase modelRef = null;
                                    //find Model
                                    findModelLoop:
                                    for (DataSnapshot snapshot : data.getChildren()){
                                        for(DataSnapshot entry : snapshot.getChildren()){
                                            String value = entry.getValue().toString();
                                            if(value.equals(modelName)){
                                                if(entry.getValue().toString().equals(param.get(2))){
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
                                    modelRef.child("Name").setValue(param.get(2));
                                    Log.d("Device", "added unknown model to database");
                                    FirebaseCrash.log("added unknown model to database");
                                    deviceByNumber.child("modelName").setValue(newModelId);
                                    //brand name is already added
                                    if(!modelKnown) {
                                        deviceByNumber.child("unknownModel").setValue("true");
                                    }else{
                                        deviceByNumber.child("unknownModel").setValue(null);
                                    }
                                    if(!brandKnown) {
                                        deviceByNumber.child("unknownBrand").setValue("true");
                                    }else{
                                        deviceByNumber.child("unknownBrand").setValue(null);
                                    }
                                    Log.d("Device", "Model marked as unknown");
                                    FirebaseCrash.log("Model marked as unknown");
                                    Log.d("Data", "Model and Brand upload finish");
                                    FirebaseCrash.log("Model and Brand upload finish");
                                    if (finalContext != null && finalChange != null) {
                                        finalContext.startActivity(finalChange);
                                    }else{
                                        if (finalContext == null && finalChange == null);
                                        else{
                                            throw new NullPointerException();
                                        }
                                    }
                                }
                            }));
                        }
                    }
                }));
            }
        }));
    }

    public static String removeSpace(String data){
        while (data.startsWith(" ")){
            data = data.substring(1);
        }
        while (data.endsWith(" ")){
            data.substring(data.length()-1);
        }
        return data;
    }

    public static void deleteDevice(String deviceId, String userId) {
        final Firebase mRootRef = new Firebase("https://lut-lappeenranta.firebaseio.com/");
        final StorageReference storage = FirebaseStorage.getInstance().getReference().child("User_receipt");
        final Firebase userDeviceRef = mRootRef.child("User").child(userId).child("Devices").child(deviceId);
        Log.d("DeleteDevice", "start deleting device");
        FirebaseCrash.log("start deleting device");
        mRootRef.child("Device").child(deviceId).setValue(null);
        userDeviceRef.setValue(null);
        //check for receipt
        storage.child(userId + "_" + deviceId + ".jpg").delete().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (!e.getMessage().equals("Object does not exist at location.")) {
                    Log.d("DeleteDevice", "Cannot delete receipt");
                    FirebaseCrash.report(e);
                }
            }
        });
        storage.child("thumb_" + userId + "_" + deviceId + ".jpg").delete().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (!e.getMessage().equals("Object does not exist at location.")) {
                    Log.d("DeleteDevice", "Cannot delete thumbnail");
                    FirebaseCrash.report(e);
                }
            }
        });
        Log.d("DeleteDevice", "Delete device completely");
        FirebaseCrash.log("Delete device completely");


    }

    public static void getDeviceData(String deviceId, final DataAdapter<Map<String, String>> dataAdapter){
        mRootRef.child("Device").child(deviceId).addListenerForSingleValueEvent(new SimpleValueListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Map<String, String> deviceData = dataSnapshot.getValue(Map.class);
                if(deviceData.containsKey("unknownBrand") || deviceData.containsKey("unknownModel")){
                    Log.d("data", "unknownModel or unknownBrand > serach for model name in unknown data");
                    FirebaseCrash.log("unknownModel or unknownBrand > serach for model name in unknown data");
                    mRootRef.child("UnknownBrand_Model").child("Brand").child(deviceData.get("brandName")).child("Model").child(deviceData.get("modelName")).child("Name").addListenerForSingleValueEvent(new SimpleValueListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String modelId = deviceData.get("modelName");
                            deviceData.put("modelName", dataSnapshot.getValue().toString());
                            deviceData.put("modelId", modelId);
                            dataAdapter.onLoad(deviceData);
                        }
                    });
                }else{
                    Log.d("data", "model known > search model name");
                    FirebaseCrash.log("model known > search model name");
                    mRootRef.child("Brand").child(deviceData.get("brandName")).child("Model").child(deviceData.get("modelName")).addListenerForSingleValueEvent(new SimpleValueListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String modelId = deviceData.get("modelName");
                            deviceData.put("modelName", dataSnapshot.getValue().toString());
                            deviceData.put("modelId", modelId);
                            dataAdapter.onLoad(deviceData);
                        }
                    });
                }
            }
        });
    }
}
