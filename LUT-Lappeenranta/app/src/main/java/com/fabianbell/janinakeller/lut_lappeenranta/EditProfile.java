package com.fabianbell.janinakeller.lut_lappeenranta;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EditProfile extends AppCompatActivity {

    //Auth
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser currentUser;

    //Fields
    private EditText mUsereMail;
    private EditText mUserPassword;
    private EditText mUserPasswordConfirmation;

    //Buttons
    private Button mLogOutButton;
    private Button mEditProfileButton;
    private Button mDeleteProfileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mFirebaseAuth = FirebaseAuth.getInstance();
        currentUser = mFirebaseAuth.getCurrentUser();

        //get Elements
        mUsereMail = (EditText) findViewById(R.id.usereMail);
        mUserPassword = (EditText) findViewById(R.id.userPassword);
        mUserPasswordConfirmation = (EditText) findViewById(R.id.userPasswordConfirmation);
        mEditProfileButton = (Button) findViewById(R.id.editProfileButton);
        mDeleteProfileButton = (Button) findViewById(R.id.deleteProfileButton);
        mLogOutButton = (Button) findViewById(R.id.logOutButton);



        mEditProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String eMail = mUsereMail.getText().toString();
                String password = mUserPassword.getText().toString();
                String passwordConfirmation = mUserPasswordConfirmation.getText().toString();

                if (password.equals(passwordConfirmation)) {
                    // TODO Mehtod beennden :)
                } else {
                    Toast.makeText(EditProfile.this, "Passwords do not match", Toast.LENGTH_LONG);
                }
            }
        });

        mDeleteProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mLogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirebaseAuth.signOut();
                if(currentUser.isAnonymous()){
                    currentUser.delete();
                }
                startActivity(new Intent(EditProfile.this, LogIn.class));
            }
        });

    }
}
