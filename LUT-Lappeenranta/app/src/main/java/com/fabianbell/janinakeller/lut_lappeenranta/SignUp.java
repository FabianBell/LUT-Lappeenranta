package com.fabianbell.janinakeller.lut_lappeenranta;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUp extends AppCompatActivity {

    //Auth
    private FirebaseAuth mAuth;

    //Fields
    private EditText mSignUpEmailField;
    private EditText mSignUpPasswordField;
    private EditText mConfirmPasswordField;

    //Buttons
    private Button mSignUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        //get Elements
        mSignUpEmailField = (EditText) findViewById(R.id.signUpEmailField);
        mSignUpPasswordField = (EditText) findViewById(R.id.signUpPasswordField);
        mConfirmPasswordField = (EditText) findViewById(R.id.confirmPasswordField);
        mSignUpButton = (Button) findViewById(R.id.signUpButton);

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //compare Password
                String email = mSignUpEmailField.getText().toString();
                String password = mSignUpPasswordField.getText().toString();
                if(!password.equals(mConfirmPasswordField.getText().toString())){
                    Toast.makeText(SignUp.this, "Passwords do not match", Toast.LENGTH_LONG).show();
                }else {
                    if(password.length() < 6){
                        Toast.makeText(SignUp.this, "Password should be at least 6 characters", Toast.LENGTH_LONG).show();
                    }else {
                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(SignUp.this, "Sign Up was not succsessful: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                } else {
                                    //send Email Verification
                                    Log.d("EmailVerification", "Email is verified: " + task.getResult().getUser().isEmailVerified());
                                    task.getResult().getUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                Log.d("EmailVerification", "Sent Email Verification");
                                                startActivity(new Intent(SignUp.this, LogIn.class));
                                            }else{
                                                Log.d("EmailVerification", "Cannot sent Email Verification: " + task.getException().getMessage());
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(new Intent(SignUp.this, LogIn.class));
        return true;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SignUp.this, LogIn.class));
    }
}
