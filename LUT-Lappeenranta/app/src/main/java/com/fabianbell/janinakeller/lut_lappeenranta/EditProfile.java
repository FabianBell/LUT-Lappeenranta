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

import com.fabianbell.janinakeller.lut_lappeenranta.listener.SimpleValueListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.crash.FirebaseCrash;

import org.w3c.dom.Text;

public class EditProfile extends AppCompatActivity {

    //Auth
    private Firebase mRootRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    //Fields
    private EditText mUsereMail;

    //Buttons
    private Button mLogOutButton;
    private Button mEditProfileButton;
    private Button mDeleteProfileButton;
    private TextView mChangePassword;

    //request code
    private static final int DELETE_QUESTION = 10;
    private static final int USER_REFRESH = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mRootRef = new Firebase("https://lut-lappeenranta.firebaseio.com/");
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


        mUsereMail.setText(currentUser.getEmail());
        mEditProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean google = false;
                for (UserInfo user: FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
                    if (user.getProviderId().equals("google.com")) {
                        Log.d("Email", "User account is a google account > cannot change the email adress");
                        FirebaseCrash.log("User account is a google account > cannot change the email adress");
                        Toast.makeText(EditProfile.this, "Your account is a google account. We cannot change your email adress for you", Toast.LENGTH_LONG).show();
                        google = true;
                    }
                }
                if(!google){
                    String eMail = mUsereMail.getText().toString();
                    if (currentUser.getEmail().equals(eMail)) {
                        //do not have to change
                        Toast.makeText(EditProfile.this, "You did not change your email adress", Toast.LENGTH_SHORT).show();
                    } else {
                        if (eMail.equals("")) {
                            Toast.makeText(EditProfile.this, "Your email adress field is empty", Toast.LENGTH_SHORT).show();
                        } else {
                            //change email
                            currentUser.updateEmail(eMail).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(EditProfile.this, "cannot change your email adress: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    Log.d("Email", "Cannot change email adress: " + e.getMessage());
                                    FirebaseCrash.report(e);
                                }
                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("Debug", Boolean.toString(currentUser.isEmailVerified()) + " " + currentUser.getEmail());
                                    currentUser.sendEmailVerification().addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("Email", "Cannot send email verification");
                                            FirebaseCrash.report(e);
                                        }
                                    });
                                    Intent loginIntent = new Intent(EditProfile.this, LogIn.class);
                                    loginIntent.putExtra("EMAIL_CHANGED", "true");
                                    startActivity(loginIntent);
                                }
                            });
                        }
                    }
                }
            }
        });

        mDeleteProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent questionIntent = new Intent(EditProfile.this, Question.class);
                questionIntent.putExtra("QUESTION", "Do you really want to delete your account?");
                questionIntent.putExtra("ANSWER1", "Yes (delete)");
                questionIntent.putExtra("ANSWER2", "No (return)");
                startActivityForResult(questionIntent, DELETE_QUESTION);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DELETE_QUESTION && resultCode == RESULT_OK){
            String answer = data.getStringExtra("ANSWER");
            if(answer.equals("1")){
                Log.d("Profile", "delete account > refresh user credentials");
                FirebaseCrash.log("delete account > refresh user credentials");
                Intent loginIntent = new Intent(EditProfile.this, RefreshLogin.class);
                startActivityForResult(loginIntent, USER_REFRESH);
            }
        }
        if (requestCode == USER_REFRESH && resultCode == RESULT_OK){
            //delete User
            mRootRef.child("User").child(currentUser.getUid()).child("Devices").addListenerForSingleValueEvent(new SimpleValueListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Utils.deleteDevice(snapshot.getKey(), currentUser.getUid());
                    }
                    mRootRef.child("User").child(currentUser.getUid()).setValue(null);
                    //account deleted from database
                    Log.d("Profile", "user deleted from database > delete user from authentication system");
                    FirebaseCrash.log("user deleted from database > delete user from authentication system");
                    //logout
                    currentUser.delete().addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Profile", "Cannot delete user from authentication system: " + e.getMessage());
                            FirebaseCrash.report(e);
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Profile", "User is deleted completely");
                            FirebaseCrash.log("User is deleted completely");
                        }
                    });
                }
            });
            //return to login
            startActivity(new Intent(EditProfile.this, LogIn.class));
        }
    }
}
