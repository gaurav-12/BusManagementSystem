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

    // Adapter's  instance.
    BMS_DB_Adapter dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signIn_Button = (Button)findViewById(R.id.signin);
        signUp_Button = (Button)findViewById(R.id.to_signup);
        challanNum = (EditText)findViewById(R.id.challan_num_SignIn);
        password = (EditText)findViewById(R.id.password_SignIn);
        dbHelper = new BMS_DB_Adapter(this);

        registerButtonsClickListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in and update UI accordingly.
        dbHelper.open();
        String challanNum = dbHelper.isUserSignedIn();
        if (challanNum != null) {
            Intent i = new Intent(MainActivity.this, Activity_UserProfile.class);
            i.putExtra("ChallanNum", challanNum);
            startActivity(i);

            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        dbHelper.close();
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
                // Check for account's existence.
                boolean exists = dbHelper.checkIfPresent(challanNum.getText().toString());
                String chNum = challanNum.getText().toString();
                String passwordText = password.getText().toString();

                // ****Just to clear the table's data.
                if (chNum.equals("ADMIN123") && passwordText.equals("PASSWORD123")) {
                    dbHelper.clearData();

                    // Clear fields
                    challanNum.getText().clear();
                    password.getText().clear();

                    password.clearFocus();

                    Snackbar.make(relativeLayout, "Data deleted successfully!", Snackbar.LENGTH_LONG).show();
                }

                // Empty fields?
                else if (chNum.equals("") || passwordText.equals("")) {
                    Snackbar.make(relativeLayout, R.string.empty_fields, Snackbar.LENGTH_LONG)
                            .show();
                }

                // Oh! yess! it exists!
                else if (exists) {
                    // But....is the password correct?
                    if (dbHelper.checkPasswordMatch(chNum, passwordText)) {
                        //if Yes!....then....
                        dbHelper.userSignedIn(chNum, true);

                        Intent i = new Intent(MainActivity.this, Activity_UserProfile.class);
                        i.putExtra("ChallanNum", chNum);
                        startActivity(i);

                        finish();
                    }
                    else {
                        Snackbar.make(relativeLayout, R.string.wrong_password, Snackbar.LENGTH_LONG).show();
                    }
                }
                else {
                    Snackbar.make(relativeLayout, R.string.no_acc_exist, Snackbar.LENGTH_LONG).show();
                }

            }
        });
    }
}
