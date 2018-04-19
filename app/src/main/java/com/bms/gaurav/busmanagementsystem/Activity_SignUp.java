package com.bms.gaurav.busmanagementsystem;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Activity_SignUp extends AppCompatActivity {

    private EditText challanNum;
    private EditText password;
    private EditText mobileNum;
    private Button signIn_Button;
    private Button signUp_Button;
    private RelativeLayout relativeLayout;

    // Firebase Authorization
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    // Firebase Database
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        challanNum = findViewById(R.id.challan_num_signup);
        password = findViewById(R.id.password_signup);
        mobileNum = findViewById(R.id.mobile_num_signup);
        signIn_Button = findViewById(R.id.to_signin);
        signUp_Button = findViewById(R.id.signup);
        relativeLayout = findViewById(R.id.parent_SnackBar_signup);

        // Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // User Signed In
                }
                else {
                    // User Signed Out
                }
            }
        };

        // Firebase Database
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();    // Getting the reference to root

        // Method to register OnClick listener for all the buttons in the activity
        registerButtonsClickListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    // Attach the listener to your FirebaseAuth instance in the onStart() method and remove it on onStop()
    // [START]

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    // [END]

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    // Method called when Sign Up button is clicked
    private void createAccount(String challanNum) {
        String UserUID = "uid";
        DatabaseReference chNum = mRef.child(challanNum);


        if (chNum.child(UserUID) != null) {     // User's uid is already present! no need of creating a new account.
            // Do something
        }
        else {
            // Validate the form
            //...

            // Phone authentication [START]

        }
    }

    private void registerButtonsClickListener() {
        signIn_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        signUp_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(accAlreadyPresent()) {
                    showSnackBar(R.string.signUp_UnSuccess);
                }
                else {
                    // Empty fields?
                    if (challanNum.getText().toString().equals("") ||  mobileNum.getText().toString().equals("") || password.getText().toString().equals("")) {
                        showSnackBar(R.string.empty_fields);
                    }

                    // Everything is allright!
                    else {
                        // Adding the account in database.

                        clear_and_deFocus_EditTexts();

                        //Displaying a snackbar, if successfull.
                        showSnackBar(R.string.signUp_Success);
                    }
                }
            }
        });
    }

    // Checks if Challan Number (Account) is already present
    private boolean accAlreadyPresent() {
        return true; // Just for now ****
    }

    private void showSnackBar(int textStringID) {
        Snackbar.make(relativeLayout, textStringID, Snackbar.LENGTH_SHORT)
            .show();
    }

    private void clear_and_deFocus_EditTexts() {
        // Clearing the EditText views' text....
        challanNum.getText().clear();
        password.getText().clear();
        mobileNum.getText().clear();

        // Also clear focus form all the EditTexts....
        mobileNum.clearFocus();
        password.clearFocus();
        challanNum.clearFocus();
    }
}
