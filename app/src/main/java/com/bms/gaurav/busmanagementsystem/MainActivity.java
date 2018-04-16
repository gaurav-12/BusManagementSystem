package com.bms.gaurav.busmanagementsystem;

import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    Button signIn_Button;
    Button signUp_Button;
    EditText challanNum;
    EditText password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signIn_Button = (Button)findViewById(R.id.signin);
        signUp_Button = (Button)findViewById(R.id.to_signup);
        challanNum = (EditText)findViewById(R.id.challan_num_SignIn);
        password = (EditText)findViewById(R.id.password_SignIn);

        registerButtonsClickListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in and update UI accordingly.
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
