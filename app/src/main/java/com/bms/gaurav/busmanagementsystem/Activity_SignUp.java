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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Activity_SignUp extends AppCompatActivity {

    private EditText challanNum;
    private EditText mobileNum;
    private String MobileNumber;
    private Button signIn_Button;
    private Button signUp_Button;
    private RelativeLayout relativeLayout;
    private ProgressBar progressBarSignup;
    private View progressBarSignup_Overlay;

    private Button verify;
    private Button resend;
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

    private FirebaseFirestore db;
    private CollectionReference users_list_Reference;
    private CollectionReference users_Reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        challanNum = findViewById(R.id.challan_num_signup);

        mobileNum = findViewById(R.id.mobile_num_signup);
        MobileNumber = "+91" + mobileNum.getText().toString();  // Mobile Number text with code(+91).

        signIn_Button = findViewById(R.id.to_signin);
        signUp_Button = findViewById(R.id.signup);
        relativeLayout = findViewById(R.id.parent_SnackBar_signup);
        progressBarSignup = findViewById(R.id.progressBar_signup);
        progressBarSignup_Overlay = findViewById(R.id.progress_overlay_signup);

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
                resend.setEnabled(true);    // Now is the good time to enable Resend button.
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                mVerificationID = s;    // Used to create Credentials
                mResendToken = forceResendingToken; // Used when resending of code is required
                Log.d("ON_CODE_SENT :", s);

                // Since the processsing is completed, no need of overlay and progressBar.
                progressBarSignup_Overlay.setVisibility(View.GONE);
                progressBarSignup.setVisibility(View.GONE);

                // Code sent successfully, now is the time to show dialog box.
                mOTPDialog.show();
                resend.setEnabled(false);   // No need of Resend button initially.
            }

            // For the case when Verification is completed by AutoRetrievel.
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
                    Log.d("VERIFICATION FAILED : ", "FirebaseAuthInvalidCredentialsException : " + e.getMessage());

                    // Since the processsing is completed, no need of overlay and progressBar.
                    progressBarSignup_Overlay.setVisibility(View.GONE);
                    progressBarSignup.setVisibility(View.GONE);

                    Snackbar.make(relativeLayout, R.string.invalid_mobile_num, Snackbar.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Snackbar.make(relativeLayout, "An Error occurred on our side. Please retry later to resolve.", Snackbar.LENGTH_LONG).show();
                }
            }
        };
        UserVerified = false;

        // Method to register OnClick listener for all the buttons in the activity
        registerButtonsClickListener();

        db = FirebaseFirestore.getInstance();
        users_list_Reference = db.collection("users_list");
        users_Reference = db.collection("users");
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
                Log.d("SIGN UP :", "SIGN UP TAPPED!!!");

                // Check for network connectivity
                Boolean isConnected = checkNetworkConnectivity();

                if (!isConnected) {
                    Log.d("SIGN UP :", "NOT CONNECTED!!");

                    Snackbar.make(relativeLayout, R.string.network_error, Snackbar.LENGTH_LONG).show();
                }

                else if(fieldsValid()) {
                    Log.d("SIGN UP :", "FIELDS ARE VALID!!");

                    // ProgressBar and overlay should be visible now, in case the processing of getting the document takes time.
                    progressBarSignup_Overlay.setVisibility(View.VISIBLE);
                    progressBarSignup.setVisibility(View.VISIBLE);

                    // Look for input Challan Number's  document in Firestore's "users" collection
                    users_list_Reference.document(challanNum.getText().toString()).get()

                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    Log.d("SIGN UP :", "TASK COMPLETE!!");

                                    if (task.isSuccessful()) {
                                        Log.d("SIGN UP :", "TASK SUCCESSFUL!!");

                                        DocumentSnapshot document = task.getResult();   // Our document(from users_list collection).

                                        // Document is present. But is it already Active?
                                        if (document.exists()) {
                                            Log.d("SIGN UP :", "DOCUMENT EXISTS!!");

                                            boolean AccountActive = document.getBoolean("Account Active");  // Is the account active?

                                            if (AccountActive) {    // There is already an Account for the input Challan Number.
                                                Log.d("SIGN UP :", "ACCOUNT ALREADY ACTIVE!!");

                                                // Since the processsing is completed, no need of overlay and progressBar.
                                                progressBarSignup_Overlay.setVisibility(View.GONE);
                                                progressBarSignup.setVisibility(View.GONE);

                                                Snackbar.make(relativeLayout, R.string.acc_already_exist, Snackbar.LENGTH_LONG).show();
                                            }

                                            // Ok lets create the account.
                                            else {
                                                Log.d("SIGN UP :", "ACCOUNT IS NOT ACTIVE!!");

                                                // Query to get documents having their mobile number same as the input.
                                                Query query = users_list_Reference.whereEqualTo("Mobile Number", mobileNum.getText().toString());

                                                query.get()
                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    int docsContainingThisMobNum = task.getResult().getDocuments().size();

                                                                    boolean mobileNumNotExist = (docsContainingThisMobNum == 0);

                                                                    Log.d("SIGN UP :", Integer.toString(docsContainingThisMobNum) + " Documents found with the same Mobile Number.");

                                                                    // But, is the Mobile Number already in use?
                                                                    if (mobileNumNotExist) {
                                                                        Log.d("SIGN UP :", "SUCCESS : MOBILE NUMBER NOT IN USE!!");

                                                                        sendVerificationCode();
                                                                    }else {
                                                                        Log.d("SIGN UP :", "ERROR : THE MOBILE NUMBER ALREADY EXIST!!");

                                                                        // Since the processsing is completed, no need of overlay and progressBar.
                                                                        progressBarSignup_Overlay.setVisibility(View.GONE);
                                                                        progressBarSignup.setVisibility(View.GONE);

                                                                        Snackbar.make(relativeLayout, R.string.mobile_num_already_exist, Snackbar.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                                else {
                                                                    Log.d("SIGN UP :", "ERROR : WHILE CHECKING MOBILE NUMBER'S EXISTENCE!!");

                                                                    // Since the processsing is completed, no need of overlay and progressBar.
                                                                    progressBarSignup_Overlay.setVisibility(View.GONE);
                                                                    progressBarSignup.setVisibility(View.GONE);

                                                                    Snackbar.make(relativeLayout, R.string.mobile_num_exist_check_error, Snackbar.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.d("SIGN UP :", "ERROR : WHILE CHECKING MOBILE NUMBER'S EXISTENCE!!");

                                                                // Since the processsing is completed, no need of overlay and progressBar.
                                                                progressBarSignup_Overlay.setVisibility(View.GONE);
                                                                progressBarSignup.setVisibility(View.GONE);

                                                                Snackbar.make(relativeLayout, R.string.mobile_num_exist_check_error, Snackbar.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        }

                                        // No document found.
                                        else {
                                            Log.d("SIGN UP :", "DOCUMENT DOES NOT EXIST!!");

                                            // Since the processsing is completed, no need of overlay and progressBar.
                                            progressBarSignup_Overlay.setVisibility(View.GONE);
                                            progressBarSignup.setVisibility(View.GONE);

                                            Snackbar.make(relativeLayout, R.string.challan_num_not_found, Snackbar.LENGTH_LONG).show();
                                        }
                                    }

                                    // Some problem occured while getting Document.
                                    else {
                                        // TODO : Do something.
                                        Log.d("SIGN UP :", "TASK NOT SUCCESSFUL!!");

                                        // Since the processsing is completed, no need of overlay and progressBar.
                                        progressBarSignup_Overlay.setVisibility(View.GONE);
                                        progressBarSignup.setVisibility(View.GONE);

                                        Snackbar.make(relativeLayout, R.string.firestore_doc_get_failure, Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            })

                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("SIGN UP : ", "TASK FAILURE: " + e.getMessage());

                                    // Since the processsing is completed, no need of overlay and progressBar.
                                    progressBarSignup_Overlay.setVisibility(View.VISIBLE);
                                    progressBarSignup.setVisibility(View.VISIBLE);

                                    Snackbar.make(relativeLayout, R.string.firestore_doc_get_failure, Snackbar.LENGTH_LONG).show();
                                }
                            })
                    ;
                }
            }
        });
    }

    private Boolean checkNetworkConnectivity() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private boolean fieldsValid() {
        if (TextUtils.isEmpty(challanNum.getText().toString()) ||       // Challan number field empty..
                TextUtils.isEmpty(mobileNum.getText().toString())) {    // Mobile number field empty.

            Snackbar.make(relativeLayout, R.string.empty_fields, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        else return true;
    }

    private void sendVerificationCode() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + mobileNum.getText().toString(), // Mobile Number
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
        verify = mDialogView.findViewById(R.id.verify_button_otp_dialog);
        resend = mDialogView.findViewById(R.id.resend_button_otp_dialog);

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // In case verification takes time, a progress bar is displayed.
                mOTPDialogProgressBar.setVisibility(View.VISIBLE);
                mOTPDialogProgressOverlay.setVisibility(View.VISIBLE);

                verifyPhoneNumWithCode();
            }
        });

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

                    // TODO : To remove this line.
                    UserVerified = false;
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
                        Log.d("SIGN IN :", "SIGN IN COMPLETE!!");

                        mOTPDialogProgressOverlay.setVisibility(View.GONE);
                        mOTPDialogProgressBar.setVisibility(View.GONE);

                        // Now, since the SignIn is successful, lets put the user's UID in his Firestore Document, and mark him Active in users_list.
                        if (task.isSuccessful()) {

                            String UID = task.getResult().getUser().getUid();   // Getting the UID.
                            Log.d("SIGN IN :", "SIGN IN SUCCESSFULL!! " + "UID : " + UID);

                            // Update the User's document's UID.
                            users_Reference.document(challanNum.getText().toString()).update("UID", UID)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("SIGN IN DATA UPDATE : ", "DocumentSnapshot successfully updated!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("SIGN IN DATA UPDATE : ", "Error: ", e);
                                        }
                                    });


                            // Update the user's document's Account Active and Mobile Number fields.
                            users_list_Reference.document(challanNum.getText().toString()).update("Account Active", true)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("SIGN IN DATA UPDATE : ", "DocumentSnapshot successfully updated!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("SIGN IN DATA UPDATE : ", "Error: ", e);
                                        }
                                    });
                            users_list_Reference.document(challanNum.getText().toString()).update("Mobile Number", mobileNum.getText().toString())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("SIGN IN DATA UPDATE : ", "DocumentSnapshot successfully updated!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("SIGN IN DATA UPDATE : ", "Error: ", e);
                                        }
                                    });


                            // TODO : Below three lines to be removed, and User will be signed in just after the above process.
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
        mobileNum.getText().clear();

        // Also clear focus from all the EditTexts....
        mobileNum.clearFocus();
        challanNum.clearFocus();
    }
}
