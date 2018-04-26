package com.bms.gaurav.busmanagementsystem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.net.ConnectivityManagerCompat;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

public class Activity_SignUp extends AppCompatActivity {
    private NetworkInfo activeNetwork;

    private EditText challanNum;
    private EditText password;
    private EditText mobileNum;
    private Button signIn_Button;
    private Button signUp_Button;
    private RelativeLayout relativeLayout;

    private View mDialogView;
    private AlertDialog mOTPDialog;
    private ProgressBar mOTPDialogProgressBar;
    private View mOTPDialogProgressOverlay;
    private EditText mCodeText;

    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks onVerificationChangedCallback;
    private String mVerificationID;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private Boolean UserVerified;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ConnectivityManager connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = connectivityManager.getActiveNetworkInfo();



        challanNum = findViewById(R.id.challan_num_signup);
        password = findViewById(R.id.password_signup);

        mobileNum = findViewById(R.id.mobile_num_signup);
        mobileNum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mobileNum.setSelection(mobileNum.getText().length());   // Place the cursor at the end of edit text ie. after the +91 code
                }
            }
        });

        signIn_Button = findViewById(R.id.to_signin);
        signUp_Button = findViewById(R.id.signup);
        relativeLayout = findViewById(R.id.parent_SnackBar_signup);

        LayoutInflater inflater = this.getLayoutInflater();
        mDialogView = inflater.inflate(R.layout.otp_dialog_layout, null);
        mOTPDialogProgressBar = mDialogView.findViewById(R.id.progressBar_otp_dialog);
        mOTPDialogProgressOverlay = mDialogView.findViewById(R.id.progress_overlay_otp_dialog);
        mCodeText = mDialogView.findViewById(R.id.enter_otp_text);
        mOTPDialog = new AlertDialog.Builder(this)
                .setView(mDialogView)
                .create();
        registerDialogButtonsClickListener();

        mAuth = FirebaseAuth.getInstance();
        onVerificationChangedCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                mOTPDialogProgressOverlay.setVisibility(View.GONE);
                mOTPDialogProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                mVerificationID = s;    // Used to create Credentials
                mResendToken = forceResendingToken; // Used when resending of code is required
                Log.d("ON_CODE_SENT :", s);
            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Log.d("ON_VERIFICATION_: ", "COMPLETED");
                UserVerified = true;
                signInWithPhoneAuthCredential(phoneAuthCredential);
                mOTPDialog.dismiss();
                clear_and_deFocus_EditTexts();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    Log.d("VERIFICATION_FAILED : ", "FirebaseAuthInvalidCredentialsException : " + e.getMessage());
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Snackbar.make(relativeLayout, "Please retry the request later to resolve", Snackbar.LENGTH_SHORT).show();
                }
            }
        };
        UserVerified = false;

        // Method to register OnClick listener for all the buttons in the activity
        registerButtonsClickListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                // Check for network connectivity
                Boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

                if (!isConnected) {
                    Snackbar.make(relativeLayout, R.string.network_error, Snackbar.LENGTH_LONG).show();
                }
                else if(fieldsValid()) {
                    sendVerificationCode();
                    mOTPDialog.show();

                    // Initially the overlay and the loading bar should be visible, which will be removed if the auto retrieval is timed out
                    mOTPDialogProgressOverlay.setVisibility(View.VISIBLE);
                    mOTPDialogProgressBar.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private boolean fieldsValid() {
        if (TextUtils.isEmpty(challanNum.getText().toString()) ||       // Challan number field empty..
                TextUtils.isEmpty(password.getText().toString()) ||     // Password field empty..
                TextUtils.isEmpty(mobileNum.getText().toString()) ||    // Mobile number field empty..
                mobileNum.getText().toString().equals("+91")) {         // ..or..Mobile number field is still equal to country code ie. +91
            Snackbar.make(relativeLayout, R.string.empty_fields, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        else return true;
    }

    private void sendVerificationCode() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobileNum.getText().toString(), // Mobile Number
                10, // Setting time for Auto-Retrieval
                TimeUnit.SECONDS,   // Unit of time to consider for auto-retrieval
                this,   // Activity to bind the callbacks
                onVerificationChangedCallback   // The callback
        );
    }

    private void resendVerificationCode() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobileNum.getText().toString(), // Mobile Number
                10, // Setting time for Auto-Retrieval
                TimeUnit.SECONDS,   // Unit of time to consider for auto-retrieval
                this,   // Activity to bind the callbacks
                onVerificationChangedCallback,  // The callback
                mResendToken    // Token to resend the verification code
        );
    }

    private void registerDialogButtonsClickListener() {
        Button verify = mDialogView.findViewById(R.id.verify_button_otp_dialog);
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyPhoneNumWithCode();
            }
        });

        Button resend = mDialogView.findViewById(R.id.resend_button_otp_dialog);
        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerificationCode();
                Snackbar.make(mDialogView, R.string.code_resent_otp_dialog, Snackbar.LENGTH_SHORT).show();
            }
        });

        mOTPDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (UserVerified) {
                    mCodeText.getText().clear();
                    Snackbar.make(relativeLayout, "User Verified", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void verifyPhoneNumWithCode() {
        String code = mCodeText.getText().toString();
        if (TextUtils.isEmpty(code)) {
            Snackbar.make(mDialogView, R.string.empty_fields, Snackbar.LENGTH_SHORT).show();
        }
        else {
            mOTPDialogProgressOverlay.setVisibility(View.VISIBLE);
            mOTPDialogProgressBar.setVisibility(View.VISIBLE);
            PhoneAuthCredential mPhoneAuthCredential = PhoneAuthProvider.getCredential(mVerificationID, code);
            signInWithPhoneAuthCredential(mPhoneAuthCredential);
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        mAuth.signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mOTPDialogProgressOverlay.setVisibility(View.GONE);
                        mOTPDialogProgressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            // TODO More to be added.
                            mOTPDialog.dismiss();
                            UserVerified = true;
                            clear_and_deFocus_EditTexts();
                        }
                        else {
                            Log.w("SIGNIN : ERROR : ", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Snackbar.make(mDialogView, R.string.invalid_code_otp_dialog, Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }


    private void clear_and_deFocus_EditTexts() {
        // Clearing the EditText views' text....
        challanNum.getText().clear();
        password.getText().clear();
        mobileNum.getText().clear();

        mobileNum.setText(R.string.country_code);   // Set the country code (+91)

        // Also clear focus from all the EditTexts....
        mobileNum.clearFocus();
        password.clearFocus();
        challanNum.clearFocus();
    }
}
