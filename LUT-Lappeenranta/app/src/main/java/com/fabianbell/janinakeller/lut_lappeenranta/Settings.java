package com.fabianbell.janinakeller.lut_lappeenranta;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

public class Settings extends AppCompatActivity {

    //Switch
    private Switch mAccessLibrarySwitch;
    private Switch mAccessCameraSwitch;

    //TextView
    private TextView mChangeEmail;
    private TextView mChangePassword;

    //Button
    private Button mSaveButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAccessLibrarySwitch = (Switch) findViewById(R.id.accessLibrarySwitch);
        mAccessCameraSwitch = (Switch) findViewById(R.id.accessCameraSwitch);
        mChangeEmail = (TextView) findViewById(R.id.changeEmailTextView);
        mChangePassword = (TextView) findViewById(R.id.changePassword);
        mSaveButton = (Button) findViewById(R.id.saveButton);

        // TODO Access library, Access Camera permissions

        mChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity (new Intent(Settings.this, EditProfile.class));
            }
        });

        mChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity (new Intent(Settings.this, ChangePassword.class));
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity (new Intent(Settings.this, Main.class));
            }
        });


    }
}
