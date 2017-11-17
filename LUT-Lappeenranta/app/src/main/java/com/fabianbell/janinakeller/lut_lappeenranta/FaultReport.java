package com.fabianbell.janinakeller.lut_lappeenranta;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

public class FaultReport extends AppCompatActivity {

    //Text
    private TextView mFaultReportDevice;
    private EditText mFaultReportBrokenPartsEditText;
    private EditText mFaultReportReasonEditText;

    //Button
    private Button mFaultReportSaveButton;

    //Spinner
    private Spinner mNewConditionSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fault_report);

        mFaultReportDevice = (TextView) findViewById(R.id.faultReportDevice);
        mFaultReportBrokenPartsEditText = (EditText) findViewById(R.id.faultReportBrokenPartsEditText);
        mFaultReportReasonEditText = (EditText) findViewById(R.id.faultReportReasonEditText);
        mFaultReportSaveButton = (Button) findViewById(R.id.faultReportSaveButton);
        mNewConditionSpinner = (Spinner) findViewById(R.id.newConditionSpinner);

        //TODO save new condition or delete device, and save fault report to database
        mFaultReportSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity (new Intent(FaultReport.this, Main.class));
            }
        });
    }
}
