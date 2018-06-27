package com.bms.gaurav.busmanagementsystem;

import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainActivity extends AppCompatActivity {

    private static final int PLAY_SERVICES_REQUEST_CODE = 0;

    Button signIn_Button;
    Button signUp_Button;
    EditText challanNum;
    EditText password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signIn_Button = findViewById(R.id.signin);
        signUp_Button = findViewById(R.id.to_signup);
        challanNum = findViewById(R.id.challan_num_SignIn);
        password = findViewById(R.id.password_SignIn);

        registerButtonsClickListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in and update UI accordingly.
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

            Toast.makeText(this, "Welcome to BMS!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PLAY_SERVICES_REQUEST_CODE :
                if (resultCode == RESULT_CANCELED) {
                    Log.d("GOOGLE PLAY SERVICES: ", "STATUS : DIALOG CANCELLED!!");

                    Toast.makeText(this, R.string.play_services_cancelled, Toast.LENGTH_SHORT).show();
                    finish();
                }else if (resultCode == RESULT_OK) {
                    Log.d("GOOGLE PLAY SERVICES: ", "STATUS : DIALOG SUCCESS!!");

                    Toast.makeText(this, "Welcome to BMS!", Toast.LENGTH_SHORT).show();
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    private void registerButtonsClickListener() {
        signUp_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Activity_SignUp.class);
                startActivity(i);
            }
        });

        signIn_Button.setOnClickListener(new View.OnClickListener() {
            RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.parent_SnackBar_signin);
            @Override
            public void onClick(View v) {
                // Will check for user's authentication.
            }
        });
    }
}
