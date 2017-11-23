package com.fabianbell.janinakeller.lut_lappeenranta;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fabianbell.janinakeller.lut_lappeenranta.listener.BetterDay;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.DataAdapter;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.SimpleChildListener;
import com.fabianbell.janinakeller.lut_lappeenranta.listener.SimpleValueListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

public class FaultReport extends AppCompatActivity {

    //Firebase
    Firebase mRootRef;
    FirebaseAuth mAuth;

    //Text
    private TextView mFaultReportDevice;
    private EditText mFaultReportBrokenPartsEditText;
    private EditText mFaultReportReasonEditText;
    private EditText mFaultReportGarantyEditText;
    private DatePicker mDayOfFault;

    //Button
    private Button mFaultReportSaveButton;
    private Button mFaultReportSaveAndDeleteButton;

    //Spinner
    private Spinner mCondition;
    private ArrayList<String> conditions;
    private ArrayAdapter<String> conditionsAdapter;

    //data
    private Map<String, String> deviceData;
    private String deviceId;

    //request code
    private static int SAVE_REQUEST = 1;
    private static int SAVE_AND_DELTE_REQUEST = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fault_report);

        if (savedInstanceState != null){
            deviceId = savedInstanceState.getString("deviceId");
        }
        if (deviceId == null) {
            deviceId = getIntent().getStringExtra("deviceId");
        }

        mRootRef = new Firebase("https://lut-lappeenranta.firebaseio.com/");
        mAuth = FirebaseAuth.getInstance();

        mFaultReportDevice = findViewById(R.id.faultReportDevice);
        mFaultReportBrokenPartsEditText = findViewById(R.id.faultReportBrokenPartsEditText);
        mFaultReportReasonEditText = findViewById(R.id.faultReportReasonEditText);
        mFaultReportGarantyEditText = findViewById(R.id.faultReportGarantyEditText);
        mFaultReportSaveButton = findViewById(R.id.faultReportSaveButton);
        mFaultReportSaveAndDeleteButton = findViewById(R.id.faultReportSaveAndDeleteButton);
        mDayOfFault = findViewById(R.id.deviceDateOfFault);
        mCondition = findViewById(R.id.condition);

        conditions = new ArrayList<>();
        conditionsAdapter = new ArrayAdapter<String>(FaultReport.this, android.R.layout.simple_spinner_dropdown_item, conditions);
        mCondition.setAdapter(conditionsAdapter);


        Utils.getDeviceData(deviceId, new DataAdapter<Map<String, String>>() {
            @Override
            public void onLoad(Map<String, String> Data) {
                deviceData = Data;
                mFaultReportDevice.setText(deviceData.get("modelName"));
                mRootRef.child("DeviceCondition").addChildEventListener(new SimpleChildListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        conditions.add(dataSnapshot.getKey());
                        conditionsAdapter.notifyDataSetChanged();
                        int position = conditionsAdapter.getPosition(deviceData.get("condition"));
                        mCondition.setSelection(position);
                    }
                });
            }
        });

        mFaultReportSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent questionIntent = new Intent(FaultReport.this, Question.class);
                questionIntent.putExtra("QUESTION", "Do you want to save the report? You cannot edit the report after that.");
                questionIntent.putExtra("ANSWER1", "Yes (save)");
                questionIntent.putExtra("ANSWER2", "No (return)");
                questionIntent.putExtra("EXTRA1", deviceId);
                questionIntent.putExtra("EXTRA2", deviceData.get("modelName"));
                startActivityForResult(questionIntent, SAVE_REQUEST);
            }
        });

        mFaultReportSaveAndDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent questionIntent = new Intent(FaultReport.this, Question.class);
                questionIntent.putExtra("QUESTION", "Do you want to save the report and delete the device? You cannot edit the report after that and you cannot undo the delete.");
                questionIntent.putExtra("ANSWER1", "Yes (save and delete)");
                questionIntent.putExtra("ANSWER2", "No (return)");
                questionIntent.putExtra("EXTRA1", deviceId);
                questionIntent.putExtra("EXTRA2", deviceData.get("modelName"));
                startActivityForResult(questionIntent, SAVE_AND_DELTE_REQUEST);
            }
        });

        Calendar currentDate = Calendar.getInstance();
        mDayOfFault.updateDate(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH));


    }

    private String saveFaultReport() {
        String brokenParts = mFaultReportBrokenPartsEditText.getText().toString();
        String reason = mFaultReportReasonEditText.getText().toString();
        String guaranty = mFaultReportGarantyEditText.getText().toString();
        String reportId = mRootRef.child("FaultReport").child(deviceData.get("modelId")).push().getKey();
        //calculate lifeTime
        BetterDay dateOfPurchase = BetterDay.parse(deviceData.get("date"));
        BetterDay dateOfFault = new BetterDay(mDayOfFault.getDayOfMonth(), mDayOfFault.getMonth()+1, mDayOfFault.getYear());
        if (dateOfFault.before(dateOfPurchase)){
            Toast.makeText(FaultReport.this, "You bougth this device after the given day of the fault", Toast.LENGTH_LONG).show();
        }else{
            String lifetime = dateOfFault.diff(dateOfPurchase).print();

            //insertData
            Firebase reportRef = mRootRef.child("FaultReport").child(deviceData.get("modelId")).child(reportId);
            reportRef.child("BrokenParts").setValue(brokenParts);
            reportRef.child("Reason").setValue(reason);
            reportRef.child("Garanty").setValue(guaranty);
            reportRef.child("Lifetime").setValue(lifetime);
            Log.d("Save", "Saved FaultReport");
            FirebaseCrash.log("Saved FaultReport");
            return reportId;
        }
        return null;
    }

    private void changeCondition(){
        String condition = mCondition.getSelectedItem().toString();
        mRootRef.child("Device").child(deviceId).child("condition").setValue(condition);
        Log.d("Save", "Changed condition");
        FirebaseCrash.log("Changed condition");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("deviceId", deviceId);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SAVE_REQUEST && resultCode == RESULT_OK){
            String answer = data.getStringExtra("ANSWER");
            deviceId = data.getStringExtra("EXTRA1");
            if (answer.equals("1")){
                String faultId = saveFaultReport();
                if (faultId != null) {
                    Intent deviceDetailIntent = new Intent(FaultReport.this, DeviceDetail.class);
                    deviceDetailIntent.putExtra("DeviceId", deviceId);
                    deviceDetailIntent.putExtra("DeviceModel", data.getStringExtra("EXTRA1"));
                    startActivity(deviceDetailIntent);
                }
            }
        }
        if(requestCode == SAVE_AND_DELTE_REQUEST && resultCode == RESULT_OK){
            String answer = data.getStringExtra("ANSWER");
            deviceId = data.getStringExtra("EXTRA1");
            if (answer.equals("1")){
                String faultId = saveFaultReport();
                if (faultId != null) {
                    Intent mainIntent = new Intent(FaultReport.this, Main.class);
                    mainIntent.putExtra("TAG", "Devices");
                    startActivity(mainIntent);
                    Utils.deleteDevice(deviceId, mAuth.getCurrentUser().getUid());
                }

            }
        }
    }
}
