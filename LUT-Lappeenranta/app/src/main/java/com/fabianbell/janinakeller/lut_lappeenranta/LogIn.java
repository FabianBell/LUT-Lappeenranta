package com.fabianbell.janinakeller.lut_lappeenranta;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.crash.FirebaseCrash;

public class LogIn extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private EditText mUsernameField;
    private EditText mPasswordField;

    private Button mLogiInButton;

    private TextView mSignUpLink;
    private TextView mForgotPasswordField;

    private static final String TAG = "SignInActivity";

    //Google Sign in
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private SignInButton googleSignInButton;

    //Anonymous Log in
    private Button anonymousSignInButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        String changedEmail = getIntent().getStringExtra("EMAIL_CHANGED");
        if (changedEmail != null){
            //changed Email
            Toast.makeText(LogIn.this, "Please log in again", Toast.LENGTH_LONG).show();
            Log.d("resaon", "Changed email adress");
            FirebaseCrash.log("Changed email adress");
        }

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    firebaseAuth.getCurrentUser().reload();
                    //debug
                    //todo remove debug login
                    if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                            startActivity(new Intent(LogIn.this, Main.class));
                        } else {
                        Log.d("EmailVerification", "User: " + firebaseAuth.getCurrentUser().getEmail());
                        Toast.makeText(LogIn.this, "Your email is not verified. Pleas verifiy your email.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        };

        mUsernameField = (EditText) findViewById(R.id.usernameField);
        mPasswordField = (EditText) findViewById(R.id.passwordField);

        mLogiInButton = (Button) findViewById(R.id.logiInButton);

        mForgotPasswordField = findViewById(R.id.forgotPasswordField);

        mLogiInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() != null) {
                    //debug
                    //todo remove debug login
                    if (!mAuth.getCurrentUser().isEmailVerified() && !mAuth.getCurrentUser().getEmail().equals("test.test@test.com")) {
                        if (mAuth.getCurrentUser().getEmail().equals(mUsernameField.getText().toString())) {
                            Toast.makeText(LogIn.this, "Your email is not verified. Pleas verifiy your email.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        startActivity(new Intent(LogIn.this, Main.class));
                    }
                }
                startSignIn();
            }
        });

        mSignUpLink = (TextView) findViewById(R.id.signUpLink);
        mSignUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogIn.this, SignUp.class));
            }
        });

        //Google Sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleSignInButton = findViewById(R.id.google_sign_in_button);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });

        //Anonymous Sign in
        anonymousSignInButton = (Button) findViewById(R.id.anonymousSignInButton);

        anonymousSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnonymousSignIn();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }
    }

    private void startSignIn() {
        String email = mUsernameField.getText().toString();
        String password = mPasswordField.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(LogIn.this, "Please fill all the fields", Toast.LENGTH_LONG).show();
        }else {
            mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    Toast.makeText(LogIn.this, "Sign in was not succsessful", Toast.LENGTH_LONG).show();
                    authResult.getUser().reload();
                    if(!authResult.getUser().isEmailVerified()){
                        Toast.makeText(LogIn.this, "Your email is not verified. Pleas verifiy your email.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LogIn.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        Log.d(TAG, "handleSignInResult:" + result.getStatus());
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            firebaseAuthWithGoogle(account);
        } else {
            Toast.makeText(LogIn.this, "Sign in was not succsessful", Toast.LENGTH_LONG).show();
        }
    }

    //gives the user lifetimeData to the normal authantification system
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(new Intent(LogIn.this, Main.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void startAnonymousSignIn(){
        String email = mUsernameField.getText().toString();
        String password = mPasswordField.getText().toString();

        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInAnonymously:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    startActivity(new Intent(LogIn.this, Main.class));
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInAnonymously:failure", task.getException());
                    Toast.makeText(LogIn.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        return;
    }
}
