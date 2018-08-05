package com.bms.gaurav.busmanagementsystem;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.HashMap;
import java.util.Map;

public class Activity_SignUp extends AppCompatActivity {

    private EditText enrollNum, Email, Password;
    private TextView PasswordRules;
    private Button signIn_Button, signUp_Button;
    private RelativeLayout relativeLayout;
    private ProgressBar progressBarSignup;
    private View progressBarSignup_Overlay;

    private FirebaseAuth mAuth;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference USERS_LIST = db.collection("USERS_LIST");
//    private final CollectionReference USERS = db.collection("USERS");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        enrollNum = findViewById(R.id.enroll_num_signup);
        Email = findViewById(R.id.email_signup);
        Password = findViewById(R.id.password_signup);
        PasswordRules = findViewById(R.id.password_rules_textview);

        Password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    // Regex in JAVA is Case Sensitive by default. Add (?i) at the beginning for case insensitivity.
                    if (!(Password.getText().toString().matches(
                            "^(?=.*\\d)(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%&^+=\\-*,._?])(?!.*\\s).{6,}$")))
                    {
                        PasswordRules.animate()
                                .alpha(1f)
                                .setDuration(250)
                                .start();
                    }
                    else {
                    }
                }
                else{
                    PasswordRules.animate()
                            .alpha(0f)
                            .setDuration(250)
                            .start();
                }
            }
        });

        Password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!(s.toString().matches(
                        "^(?=.*\\d)(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%&^+=\\-*,._?])(?!.*\\s).{6,}$")))
                {
                    PasswordRules.animate()
                            .alpha(1f)
                            .setDuration(250)
                            .start();
                }
                else {
                    PasswordRules.animate()
                            .alpha(0f)
                            .setDuration(250)
                            .start();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

//        mobileNum = findViewById(R.id.mobile_num_signup);
//        mobileNum.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                /*
//                Regular Expression for Indian Mobile Number.
//                    1. ^ asserts position at start of a line
//                    2. Match a single character present in the list [6-9]
//                        6-9 a single character in the range between 6 (index 54) and 9 (index 57) (case sensitive)
//                    3. Match a single character present in the list [0-9]{9}
//                        {9} Quantifier — Matches exactly 9 times
//                        0-9 a single character in the range between 0 (index 48) and 9 (index 57) (case sensitive)
//                    4. $ asserts position at the end of a line
//                */
//                if (!(s.toString().matches("^[6-9][0-9]{9}$"))) {
//                    mobileNum.setError("Invalid Mobile number");
//                }else {
//                    Log.d("MOBILE NUMBER : ", "VALID MOBILE NUMBER!");
//                    mobileNum.setError(null);
//                }
//            }
//        });

        signIn_Button = findViewById(R.id.to_signin);
        signUp_Button = findViewById(R.id.signup);
        relativeLayout = findViewById(R.id.parent_SnackBar_signup);
        progressBarSignup = findViewById(R.id.progressBar_signup);
        progressBarSignup_Overlay = findViewById(R.id.progress_overlay_signup);

        mAuth = FirebaseAuth.getInstance();

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

                    // Look for input enroll Number's  document in Firestore's "users" collection (in SERVER and not in CACHE).
                    USERS_LIST.document(enrollNum.getText().toString()).get(Source.SERVER)

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

                                            boolean AccountActive = document.getBoolean("ACCOUNT_ACTIVE");  // Is the account active?

                                            if (AccountActive) {    // There is already an Account for the input chaalan Number.
                                                Log.d("SIGN UP :", "ACCOUNT ALREADY ACTIVE!!");

                                                // Since the processing is completed, no need of overlay and progressBar.
                                                progressBarSignup_Overlay.setVisibility(View.GONE);
                                                progressBarSignup.setVisibility(View.GONE);

                                                Snackbar.make(relativeLayout, R.string.acc_already_exist, Snackbar.LENGTH_LONG).show();
                                            }

                                            // Ok lets create the account.
                                            else {
                                                Log.d("SIGN UP :", "ACCOUNT IS NOT ACTIVE!");

                                                String userEmail = document.getString("EMAIL");
                                                String userName = document.getString("NAME");

                                                boolean fieldsMatch = userEmail.equals(Email.getText().toString());

                                                if (fieldsMatch){
                                                    Log.d("SIGN UP :", "FIELDS MATCH: SUCCESSFUL!");

                                                    signupUser(userName);
                                                }
                                                else {
                                                    Log.d("SIGN UP :", "FIELDS MATCH: UNSUCCESSFUL!");

                                                    // Since the processing is completed, no need of overlay and progressBar.
                                                    progressBarSignup_Overlay.setVisibility(View.GONE);
                                                    progressBarSignup.setVisibility(View.GONE);

                                                    Snackbar.make(relativeLayout, R.string.signup_fields_mismatch, Snackbar.LENGTH_LONG).show();
                                                }
                                            }
                                        }

                                        // No document found.
                                        else {
                                            Log.d("SIGN UP :", "DOCUMENT DOES NOT EXIST!!");

                                            // Since the processing is completed, no need of overlay and progressBar.
                                            progressBarSignup_Overlay.setVisibility(View.GONE);
                                            progressBarSignup.setVisibility(View.GONE);

                                            Snackbar.make(relativeLayout, R.string.enroll_num_not_found, Snackbar.LENGTH_LONG).show();
                                        }
                                    }

                                    // Some problem occurred while getting Document.
                                    else {
                                        Log.d("SIGN UP :", "TASK NOT SUCCESSFUL: " + task.getException());

                                        // Since the processing is completed, no need of overlay and progressBar.
                                        progressBarSignup_Overlay.setVisibility(View.GONE);
                                        progressBarSignup.setVisibility(View.GONE);

                                        Snackbar.make(relativeLayout, R.string.firestore_doc_get_failure, Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    private void signupUser(final String name) {
        mAuth.createUserWithEmailAndPassword(Email.getText().toString(), Password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Log.d("CREATE USER: ", "SUCCESSFUL!");

                            final FirebaseUser user = task.getResult().getUser();

                            final UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            Map<String, Object> data = new HashMap<>();
                            data.put("UID", user.getUid());
                            data.put("ACCOUNT_ACTIVE", true);

                            USERS_LIST.document(enrollNum.getText().toString())
                                    .update(data)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Log.d("DOC UPDATE: ", "SUCCESSFUL!");

                                                updateProfile(user, profileUpdates);

                                                // TODO: Also, update phone number.
                                            }
                                            else{
                                                Log.d("DOC UPDATE: ERROR", task.getException().getMessage());

                                                Snackbar.make(relativeLayout, R.string.sign_up_error, Snackbar.LENGTH_LONG).show();

                                                // Since the processing is completed, no need of overlay and progressBar.
                                                progressBarSignup_Overlay.setVisibility(View.GONE);
                                                progressBarSignup.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                        }
                        else {
                            Log.d("CREATE USER: ERROR: ", task.getException().getMessage());

                            Snackbar.make(relativeLayout, R.string.sign_up_error, Snackbar.LENGTH_LONG).show();

                            // Since the processing is completed, no need of overlay and progressBar.
                            progressBarSignup_Overlay.setVisibility(View.GONE);
                            progressBarSignup.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void updateProfile(FirebaseUser user, UserProfileChangeRequest profileUpdates) {
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d("USER UPDATE: ", "Profile updated!");

                            // TODO: Finish this activity with OK message(if its the last operation of SignUp).
                            setResult(RESULT_OK);
                            finish();
                        }
                        else {
                            Log.d("PROFILE UPDATE ERROR: ", task.getException().getMessage());

                            Snackbar.make(relativeLayout, R.string.sign_up_error, Snackbar.LENGTH_LONG).show();

                            // Since the processing is completed, no need of overlay and progressBar.
                            progressBarSignup_Overlay.setVisibility(View.GONE);
                            progressBarSignup.setVisibility(View.GONE);
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

//    Email Regex explanation:

//         ^ asserts position at start of the string
//         Match a single character present in the list below [A-Z0-9._%+-]+
//              + Quantifier — Matches between one and unlimited times, as many times as possible, giving back as needed
//              A-Z a single character in the range between A (index 65) and Z (index 90) (case insensitive)
//              0-9 a single character in the range between 0 (index 48) and 9 (index 57) (case insensitive)
//              ._%+- matches a single character in the list ._%+- (case insensitive).
//         @ matches the character @ literally (case insensitive).
//         Match a single character present in the list below [A-Z]{2,4}
//              {2,4} Quantifier — Matches between 2 and 4 times, as many times as possible, giving back as needed
//              A-Z a single character in the range between A (index 65) and Z (index 90) (case insensitive)
//         $ asserts position at the end of the string
    private boolean fieldsValid() {
        if (TextUtils.isEmpty(enrollNum.getText().toString()) ||
                TextUtils.isEmpty(Email.getText().toString()) ||
                TextUtils.isEmpty(Password.getText().toString()))
        {
            Log.d("FIELDS VALIDITY: ", "Fields empty!");
            Snackbar.make(relativeLayout, R.string.invalid_fields, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        else if (!(Email.getText().toString().matches("^[A-Za-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$")) ||
                !(Password.getText().toString().matches(
                        "^(?=.*\\d)(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%&^+=\\-*,._?])(?!.*\\s).{6,}$")))
        {
            Log.d("FIELDS VALIDITY: ", "Email or Password invalid!");
            Snackbar.make(relativeLayout, R.string.invalid_fields, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        else return true;
    }
}
