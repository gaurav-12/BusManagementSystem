package com.bms.gaurav.busmanagementsystem;

import android.content.Intent;
import android.graphics.Color;
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

public class Activity_SignUp extends AppCompatActivity {

    private BMS_DB_Adapter dbHelper;

    private EditText challanNum;
    private EditText password;
    private TextInputLayout re_Password_Layout;
    private EditText re_Password;
    private Button signIn_Button;
    private Button signUp_Button;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        dbHelper = new BMS_DB_Adapter(this);

        challanNum = (EditText)findViewById(R.id.challan_num_SignUp);
        password = (EditText)findViewById(R.id.password_SignUp);
        re_Password = (EditText)findViewById(R.id.re_password_SignUp);
        re_Password_Layout = (TextInputLayout)findViewById(R.id.re_Password_Layout);
        signIn_Button = (Button)findViewById(R.id.to_signin);
        signUp_Button = (Button)findViewById(R.id.signup);
        relativeLayout = (RelativeLayout)findViewById(R.id.parent_SnackBar_signup);

        // Method to register OnClick listener for all the buttons in the activity
        registerButtonsClickListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        dbHelper.open();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
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
                    // Account does not exist already, OK! now check if passwords matched....
                    if (!re_Password.getText().toString().equals(password.getText().toString())) {
                        showSnackBar(R.string.wrong_Input);
                    }

                    // Empty fields?
                    else if (challanNum.getText().toString().equals("") || password.getText().toString().equals("")) {
                        showSnackBar(R.string.empty_fields);
                    }

                    // Everything is allright!
                    else {
                        // Adding the account in database.
                        String cNum = challanNum.getText().toString();
                        String pass = password.getText().toString();
                        dbHelper.addAccount(cNum, pass);

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
        String chNum = challanNum.getText().toString();
        return dbHelper.checkIfPresent(chNum);
    }

    private void showSnackBar(int textStringID) {
        Snackbar.make(relativeLayout, textStringID, Snackbar.LENGTH_LONG)
            .show();
    }

    private void clear_and_deFocus_EditTexts() {
        // Clearing the EditText views' text....
        challanNum.getText().clear();
        password.getText().clear();
        re_Password.getText().clear();

        // Also clear focus form all the EditTexts....
        re_Password_Layout.clearFocus();
        password.clearFocus();
        challanNum.clearFocus();
    }
}
