package com.bms.gaurav.busmanagementsystem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

public class MainActivity extends AppCompatActivity {

    private static final int PLAY_SERVICES_REQUEST_CODE = 0;
    private static final int SIGN_UP_REQUEST_CODE = 1;
    private static final String STUDENT = "Student";
    private static final String FACULTY = "Faculty";

    private Button signIn_Button, signUp_Button, accType_Button;
    private String AccType;
    private EditText enrollNum, Password;
    private RelativeLayout relativeLayout;
    private ProgressBar progressBar;
    private AlertDialog.Builder accTypeDialog;

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference USERS_LIST = db.collection("USERS_LIST");

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signIn_Button = findViewById(R.id.signin);
        signUp_Button = findViewById(R.id.to_signup);
        AccType = STUDENT;
        accType_Button = findViewById(R.id.acc_type_button_signin);
        enrollNum = findViewById(R.id.enroll_num_signIn);
        Password = findViewById(R.id.password_signIn);
        relativeLayout = findViewById(R.id.parent_snackbar_signin);
        progressBar = findViewById(R.id.progressBar_signin);

        initDialog();

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {     // Signed In.
                    Log.d("AUTH STATE CHECK ", "SIGNED IN!");

                    Intent i = new Intent(MainActivity.this, Activity_UserProfile.class);
                    startActivity(i);
                    finish();
                }else Log.d("AUTH STATE CHECK ", "NOT SIGNED IN!");
            }
        };

        sharedPreferences = getSharedPreferences(getString(R.string.bms_preference_name), MODE_PRIVATE);

        registerButtonsClickListener();
    }

    private void initDialog() {
        accTypeDialog = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog);
        accTypeDialog.setTitle(R.string.acc_type);
        accTypeDialog.setItems(R.array.acc_type,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                AccType = STUDENT;
                                accType_Button.setText(STUDENT);
                                break;
                            case 1:
                                AccType = "FACULTY";
                                accType_Button.setText(FACULTY);
                                break;
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        // When some user is already Signed In, open the user's profile.
        mAuth.addAuthStateListener(mAuthStateListener);
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int status = googleAPI.isGooglePlayServicesAvailable(this);

        if (status != ConnectionResult.SUCCESS) {

            if (googleAPI.isUserResolvableError(status)) {
                Log.d("GOOGLE PLAY SERVICES: ", "SHOWING ERROR DIALOG....");

                googleAPI.getErrorDialog(this,
                                        status, // Error Code
                                        PLAY_SERVICES_REQUEST_CODE  // Request code for the dialog
                );
            }
            else {
                Log.d("GOOGLE PLAY SERVICES: ", "STATUS : PLAY SERVICES ERROR : STATUS : FAILURE!!");

                Toast.makeText(this, R.string.play_services_error, Toast.LENGTH_LONG).show();
                finish();
            }
        }
        else {
            Log.d("GOOGLE PLAY SERVICES: ", "STATUS : SUCCESS!!");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SIGN_UP_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Log.d("ACTIVITY RESULT", "USER SIGNED IN SUCCESSFULLY!");

                    // Start the profile's Activity of User.
                    Intent i = new Intent(MainActivity.this, Activity_UserProfile.class);
                    startActivity(i);
                    finish();
                }
                break;

            case PLAY_SERVICES_REQUEST_CODE:
                if (resultCode == RESULT_CANCELED) {
                    Log.d("GOOGLE PLAY SERVICES: ", "STATUS : DIALOG CANCELLED!!");

                    Toast.makeText(this, R.string.play_services_cancelled, Toast.LENGTH_SHORT).show();
                    finish();
                } else if (resultCode == RESULT_OK) {
                    Log.d("GOOGLE PLAY SERVICES: ", "STATUS : DIALOG SUCCESS!!");

                    Toast.makeText(this, "Welcome to BMS!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onStop() {
        if (mAuthStateListener != null){
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
        super.onStop();
    }

    private void registerButtonsClickListener() {
        accType_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accTypeDialog.show();
            }
        });
        signUp_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Activity_SignUp.class);
                startActivityForResult(i, SIGN_UP_REQUEST_CODE);
            }
        });

        signIn_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for network connectivity
                Boolean isConnected = checkNetworkConnectivity();

                if (!isConnected) {
                    Log.d("SIGN IN :", "NOT CONNECTED!!");

                    Snackbar.make(relativeLayout, R.string.network_error, Snackbar.LENGTH_LONG).show();
                }

                else if(fieldsValid()) {
                    Log.d("SIGN IN :", "FIELDS ARE VALID!!");

                    // Disable fields and ProgressBar should be visible now, in case the processing of getting the document takes time.
                    enableFields(false);
                    progressBar.setVisibility(View.VISIBLE);

                    // Look for input enroll Number's  document in Firestore's "users" collection (in SERVER and not in CACHE).
                    USERS_LIST.document(enrollNum.getText().toString()).get(Source.SERVER)

                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    Log.d("SIGN IN :", "TASK COMPLETE!!");

                                    if (task.isSuccessful()) {
                                        Log.d("SIGN IN :", "TASK SUCCESSFUL!!");

                                        DocumentSnapshot document = task.getResult();   // Our document(from users_list collection).

                                        // Document is present. But is it already Active?
                                        if (document.exists()) {
                                            Log.d("SIGN IN :", "DOCUMENT EXISTS!!");

                                            boolean AccountActive = document.getBoolean("ACCOUNT_ACTIVE");  // Is the account active?

                                            if (AccountActive) {    // There is already an Account for the input chaalan Number.
                                                Log.d("SIGN IN :", "ACCOUNT ALREADY ACTIVE!!");

                                                String userEmail = document.getString("EMAIL");
                                                Signin(userEmail);
                                            }

                                            // Ok lets create the account.
                                            else {
                                                Log.d("SIGN IN :", "ACCOUNT IS NOT ACTIVE!");

                                                // Disable fields and ProgressBar should be visible now, in case the processing of getting the document takes time.
                                                enableFields(true);
                                                progressBar.setVisibility(View.GONE);

                                                Snackbar.make(relativeLayout, R.string.no_acc_exist, Snackbar.LENGTH_LONG)
                                                        .show();
                                            }
                                        }

                                        // No document found.
                                        else {
                                            Log.d("SIGN IN :", "DOCUMENT DOES NOT EXIST!!");

                                            // Disable fields and ProgressBar should be visible now, in case the processing of getting the document takes time.
                                            enableFields(true);
                                            progressBar.setVisibility(View.GONE);

                                            Snackbar.make(relativeLayout, R.string.enroll_num_not_found, Snackbar.LENGTH_LONG).show();
                                        }
                                    }

                                    // Some problem occurred while getting Document.
                                    else {
                                        Log.d("SIGN IN :", "TASK NOT SUCCESSFUL: " + task.getException());

                                        // Disable fields and ProgressBar should be visible now, in case the processing of getting the document takes time.
                                        enableFields(true);
                                        progressBar.setVisibility(View.GONE);

                                        Snackbar.make(relativeLayout, R.string.firestore_doc_get_failure, Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    private void enableFields(boolean enable) {
        int newColor = enable? getResources().getColor(R.color.colorPrimary): getResources().getColor(R.color.disabled);
        accType_Button.setEnabled(enable);
        accType_Button.setTextColor(newColor);

        enrollNum.setEnabled(enable);
        Password.setEnabled(enable);
        signIn_Button.setEnabled(enable);
    }

    private void Signin(String userEmail) {
        mAuth.signInWithEmailAndPassword(userEmail, Password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(getString(R.string.pref_stop_arr_key), null);
                            editor.putString(getString(R.string.pref_shift_arr_key), null);
                            editor.putString(getString(R.string.pref_stop_dep_key), null);
                            editor.putString(getString(R.string.pref_shift_dep_key), null);
                            editor.apply();

//                            No need of Intent as AuthStateListener will do the work for us.
//                            Intent i = new Intent(MainActivity.this, Activity_UserProfile.class);
//                            startActivity(i);
//                            finish();
                        }
                        else {
                            Log.d("SIGN IN ERROR: ", task.getException().getMessage());

                            // Disable fields and ProgressBar should be visible now, in case the processing of getting the document takes time.
                            enableFields(true);
                            progressBar.setVisibility(View.GONE);

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                Snackbar.make(relativeLayout, R.string.wrong_Input, Snackbar.LENGTH_LONG).show();
                            }
                            else
                            Snackbar.make(relativeLayout, R.string.sign_in_error, Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @NonNull
    private Boolean checkNetworkConnectivity() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private boolean fieldsValid() {
        if (TextUtils.isEmpty(enrollNum.getText().toString()) ||
                TextUtils.isEmpty(Password.getText().toString()))
        {
            Log.d("FIELDS VALIDITY: ", "Fields empty!");
            Snackbar.make(relativeLayout, R.string.empty_fields, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        else return true;
    }
}
