package com.fabianbell.janinakeller.lut_lappeenranta;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class DeviceDetail extends AppCompatActivity {

    private FloatingActionButton mEditDeviceButton;

    //TODO add device detail to devices

    //device

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);

        mEditDeviceButton = (FloatingActionButton) findViewById(R.id.editDeviceButton);

        mEditDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DeviceDetail.this, EditDevice.class));
            }
        });


    }
}
