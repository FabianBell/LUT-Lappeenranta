package com.fabianbell.janinakeller.lut_lappeenranta;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;

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
        mSaveButton = findViewById(R.id.saveButton);
        mOldPassword = findViewById(R.id.oldPassword);
        mNewPassword = findViewById(R.id.newPassword);
        mConfirmPassword = findViewById(R.id.confirmPassword);



        mSaveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String password = mNewPassword.getText().toString();
                    String passwordConfirmation = mConfirmPassword.getText().toString();
                    String oldPassword = mOldPassword.getText().toString();

                    if(oldPassword.equals("") || password.equals("") || passwordConfirmation.equals("")){
                        Toast.makeText(ChangePassword.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    }else {
                        if (password.equals(passwordConfirmation)) {
                            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPassword);
                            currentUser.reauthenticate(credential).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    if (e.getMessage().equals("The password is invalid or the user does not have a password.")) {
                                        Log.d("Password", "Cannot reauthenticate user: " + e.getMessage() + "(handled)");
                                        FirebaseCrash.log("Cannot reauthenticate user: " + e.getMessage() + "(handled)");
                                        Toast.makeText(ChangePassword.this, "The password is invalid", Toast.LENGTH_LONG).show();
                                    }else{
                                        Log.d("Password", "Cannot reauthenticate user: " + e.getMessage());
                                        FirebaseCrash.report(e);
                                    }
                                }
                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    currentUser.updatePassword(password).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            if(e.getMessage().equals("The given password is invalid. [ Password should be at least 6 characters ]")){
                                                Log.d("Password", "Cannot update password: " + e.getMessage() + "(handled)");
                                                FirebaseCrash.log("Cannot update password: " + e.getMessage() + "(handled)");
                                                Toast.makeText(ChangePassword.this, "Your password should be at least 6 characters", Toast.LENGTH_SHORT).show();
                                            }else {
                                                Log.d("Password", "Cannot update password: " + e.getMessage());
                                                FirebaseCrash.report(e);
                                            }
                                        }
                                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("Password", "Password updated");
                                            FirebaseCrash.log("Password updated");
                                            Intent mainIntent = new Intent(ChangePassword.this, Main.class);
                                            mainIntent.putExtra("TAG", "Profile");
                                            startActivity(mainIntent);
                                        }
                                    });
                                }
                            });
                        } else {
                            Toast.makeText(ChangePassword.this, "Passwords do not match", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

        }
    }

