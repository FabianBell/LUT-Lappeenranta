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
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class EditProfile extends AppCompatActivity {

    //Auth
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    //Fields
    private EditText mUsereMail;

    //Buttons
    private Button mLogOutButton;
    private Button mEditProfileButton;
    private Button mDeleteProfileButton;
    private TextView mChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //get Elements
        mUsereMail = (EditText) findViewById(R.id.usereMail);
        mChangePassword = (TextView) findViewById(R.id.changePassword);
        mEditProfileButton = (Button) findViewById(R.id.editProfileButton);
        mDeleteProfileButton = (Button) findViewById(R.id.deleteProfileButton);
        mLogOutButton = (Button) findViewById(R.id.logOutButton);

        mChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditProfile.this, ChangePassword.class));
            }
        });



        mEditProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String eMail = mUsereMail.getText().toString();
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
                mAuth.signOut();
                if(currentUser.isAnonymous()){
                    currentUser.delete();
                }
                startActivity(new Intent(EditProfile.this, LogIn.class));
            }
        });

    }
}
