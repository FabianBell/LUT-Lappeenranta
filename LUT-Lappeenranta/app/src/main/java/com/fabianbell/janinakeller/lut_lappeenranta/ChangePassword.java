package com.fabianbell.janinakeller.lut_lappeenranta;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {

    //Auth
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    //Fields
    private EditText mOldPassword;
    private EditText mNewPassword;
    private EditText mConfirmPassword;

    //Buttons
    private Button mSaveButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);


        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();


        //get Elements
        mSaveButton = (Button) findViewById(R.id.saveButton);
        mOldPassword = (EditText) findViewById(R.id.oldPassword);
        mNewPassword = (EditText) findViewById(R.id.newPassword);
        mConfirmPassword = (EditText) findViewById(R.id.confirmPassword);


        mSaveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String password = mNewPassword.getText().toString();
                    String passwordConfirmation = mConfirmPassword.getText().toString();


                    if (password.equals(passwordConfirmation)) {
                        // TODO Fabian old password mit aktuellem password vergleichen, sonst einfavh löschen
                    } else {
                        Toast.makeText(ChangePassword.this, "Passwords do not match", Toast.LENGTH_LONG);
                    }
                }
            });

        }


    }

