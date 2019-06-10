package com.bms.gaurav.busmanagementsystem;

import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Activity_SignUp extends AppCompatActivity {

    private static final long ANIM_DUR = 400;
    private static final String
            STUDENT = "Student", FACULTY = "Faculty",
            REGION = "asia-northeast1", // Region for Cloud Functions.
            TAG = "BMS",
            // Firestore Documents Fields' name, used while getting and setting them.
            EN = "ENROLL_NUM", EMID = "EMPLOYEE_ID", EM = "EMAIL", CH = "CHAALAN_NUM", MO = "MOBILE_NUM", NAME = "NAME";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference
            FAC_ACCS = db.collection("FACULTY_ACCS"),
            STUD_ACCS = db.collection("STUDENT_ACCS"),
            DETAILS_LIST = db.collection("ALL_DETAILS_LIST");

    // enrollNum(Enrollment number) will also contain Employee Id. for faculty type account.
    private EditText Name, enrollNum, Email, Password, mobNum, chalNum;
    private TextView PasswordRules, signupStepText, WarningTextView;
    private Button signUp_Button, signupNext_Button, accType_Button;
    private String AccType;
    private AlertDialog.Builder accTypeDialog, progressDialogBuilder;
    private AlertDialog progressDialog;
    private ImageButton signupPrev_Button;
    private LinearLayout stepOneLayout, stepTwoLayout;
    private RelativeLayout relativeLayout;
//    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFunctions mFunctions;
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Name = findViewById(R.id.name_signup);
        enrollNum = findViewById(R.id.enroll_num_signup);
        Email = findViewById(R.id.email_signup);
        Password = findViewById(R.id.password_signup);
        mobNum = findViewById(R.id.mob_num_signup);
        chalNum = findViewById(R.id.chal_num_signup);

        PasswordRules = findViewById(R.id.password_rules_textview);
        WarningTextView = findViewById(R.id.warning_textview);

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
                                .setDuration(ANIM_DUR)
                                .start();
                    }
                    else {

                    }
                }
                else{
                    PasswordRules.animate()
                            .alpha(0f)
                            .setDuration(ANIM_DUR)
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
                            .setDuration(ANIM_DUR)
                            .start();
                }
                else {
                    PasswordRules.animate()
                            .alpha(0f)
                            .setDuration(ANIM_DUR)
                            .start();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mobNum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    // Regex in JAVA is Case Sensitive by default. Add (?i) at the beginning for case insensitivity.
                    if (!(mobNum.getText().toString().matches(
                            "^\\+91[6-9][0-9]{9}$")))
                    {
                        WarningTextView.animate()
                                .alpha(1f)
                                .setDuration(ANIM_DUR)
                                .start();
                    }
                }
                else{
                    WarningTextView.animate()
                            .alpha(0f)
                            .setDuration(ANIM_DUR)
                            .start();
                }
            }
        });

        mobNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!(s.toString().matches(
                        "^\\+91[6-9][0-9]{9}$")))
                {
                    WarningTextView.animate()
                            .alpha(1f)
                            .setDuration(ANIM_DUR)
                            .start();
                }
                else {
                    WarningTextView.animate()
                            .alpha(0f)
                            .setDuration(ANIM_DUR)
                            .start();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        initDialogs();
        changeStatusBarColor();

        accType_Button = findViewById(R.id.acc_type_button_signup);
        AccType = accType_Button.getText().toString();

        signUp_Button = findViewById(R.id.signup);
        signupNext_Button = findViewById(R.id.signup_next_step);
        signupPrev_Button = findViewById(R.id.signup_prev_step);
        signupStepText = findViewById(R.id.signup_step_text);
        signupStepText.setText("1/2");
        
        stepOneLayout = findViewById(R.id.step_one);
        stepTwoLayout = findViewById(R.id.step_two);

        relativeLayout = findViewById(R.id.parent_snackbar_signup);

        mAuth = FirebaseAuth.getInstance();
        mFunctions = FirebaseFunctions.getInstance(REGION);

        sharedPreferences = getSharedPreferences(getString(R.string.bms_preference_name), MODE_PRIVATE);

        registerButtonsClickListener();
    }

    private void changeStatusBarColor() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorAccentDark));
        }

    }

    private void initDialogs() {
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
                                AccType = FACULTY;
                                accType_Button.setText(FACULTY);
                                break;
                        }
                    }
                });

        progressDialogBuilder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog);
        progressDialogBuilder.setView(R.layout.progress_dialog)
        .setCancelable(false);
        progressDialog = progressDialogBuilder.create();
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
        accType_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accTypeDialog.show();
            }
        });

        signupNext_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signupPrev_Button.animate().alpha(1f).setDuration(ANIM_DUR).start();
                signupStepText.setText("2/2");

                stepTwoLayout.setVisibility(View.VISIBLE);
                stepTwoLayout.animate()
                        .scaleX(1f)
                        .setDuration(ANIM_DUR)
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        })
                        .start();

                stepOneLayout.animate()
                        .scaleX(0f)
                        .setDuration(ANIM_DUR)
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                stepOneLayout.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        })
                        .start();
            }
        });

        signupPrev_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signupPrev_Button.animate().alpha(0f).setDuration(ANIM_DUR).start();
                signupStepText.setText("1/2");

                stepOneLayout.setVisibility(View.VISIBLE);
                stepOneLayout.animate()
                        .scaleX(1f)
                        .setDuration(ANIM_DUR)
                        .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                        .start();

                stepTwoLayout.animate()
                        .scaleX(0f)
                        .setDuration(ANIM_DUR)
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                stepTwoLayout.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        })
                        .start();
            }
        });

        signUp_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "SignUp tapped.");

                // Check for network connectivity
                Boolean isConnected = checkNetworkConnectivity();

                if (!isConnected) {
                    Log.d(TAG, "Not connected to any network.");

                    showSnackbar(getString(R.string.network_error));
                }

                else if(fieldsValid()) {
                    Log.d(TAG, "Fields are valid.");

                    progressDialogBuilder.show();

                    ExistCheckTask checkTask = new ExistCheckTask(new CheckResponse() {
                        @Override
                        public void onCheckFinish(ArrayList<String> response) {
                            if (!response.contains("error")) {
                                if (!response.isEmpty()) {
                                    Log.d(TAG, response.toString() + " fields already exist.");
                                    progressDialog.dismiss();

                                    showSnackbar(response.toString() + " field(s) already exist. Please retry, or try Signing in");
                                } else {
                                    Log.d(TAG, "Checking successful, fields does not exist, Signing Up...");
                                    signupUser(Name.getText().toString());
                                }
                            } else {
                                Log.d(TAG, "Checking unsuccessful, some error occurred!");
                                progressDialog.dismiss();

                                showSnackbar(getString(R.string.sign_up_error));
                            }}
                    });
                    checkTask.execute();
                }
            }
        });
    }

    private void showSnackbar(String text) {
        Snackbar.make(relativeLayout, text, Snackbar.LENGTH_LONG).show();
    }

    private void makeToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void signupUser(final String name) {
        // Along with creating user, the method also Sign in this user.
        mAuth.createUserWithEmailAndPassword(Email.getText().toString(), Password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "Signup successful!");

                            // Update display name of the user.
                            final FirebaseUser user = task.getResult().getUser();

                            final UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            user.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Log.d(TAG, "Display name updated successfully!");
                                    }
                                    else {
                                        Log.d(TAG, "Name update error:" + task.getException().getMessage());
                                    }
                                }
                            });

                            // Create a new document for the user.
                            createDoc(user.getUid());
                        }
                        else {
                            Log.d(TAG, "Signup error:" + task.getException().getMessage());
                            showSnackbar(getString(R.string.sign_up_error));

                            progressDialog.dismiss();
                        }
                    }
                });
    }

    private void createDoc(String uid) {
        Log.d(TAG, "Creating " + AccType + " type document...");

        CollectionReference coll = AccType.equals(STUDENT)? STUD_ACCS : FAC_ACCS;
        // What to add, Enrollment num. or Employee Id.?
        String ENorEMID = AccType.equals(STUDENT)? EN : EMID;

        Map<String, String> data = new HashMap<>();
        data.put(ENorEMID, enrollNum.getText().toString());
        data.put(EM, Email.getText().toString());
        data.put(MO, mobNum.getText().toString());
        data.put(CH, chalNum.getText().toString());
        data.put(NAME, Name.getText().toString());

        coll.document(uid).set(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "New Document created successfully.");
                            // TODO: finish this Activity, which will return to SignIn activity to open UserProfile.
                        }else {
                            Log.d(TAG, "New Document error:" + task.getException().getMessage());
                            progressDialog.dismiss();
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
        if (TextUtils.isEmpty(Name.getText().toString()) ||
                TextUtils.isEmpty(enrollNum.getText().toString()) ||
                TextUtils.isEmpty(mobNum.getText().toString()) ||
                TextUtils.isEmpty(chalNum.getText().toString()) ||
                TextUtils.isEmpty(Email.getText().toString()) ||
                TextUtils.isEmpty(Password.getText().toString()))
        {
            Log.d(TAG, "Fields empty!");
            Snackbar.make(relativeLayout, R.string.invalid_fields, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        else if (!(Email.getText().toString().matches("^[A-Za-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$")) ||
                !(Password.getText().toString().matches(
                        "^(?=.*\\d)(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%&^+=\\-*,._?])(?!.*\\s).{6,}$")) ||
                !(mobNum.getText().toString().matches("^\\+91[6-9][0-9]{9}$")))
        {
            Log.d(TAG, "Email, Mobile number or Password invalid!");
            Snackbar.make(relativeLayout, R.string.invalid_fields, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        else return true;
    }

    private interface CheckResponse {
        void onCheckFinish(ArrayList<String> response);
    }

    private class ExistCheckTask extends AsyncTask<Void, Void, ArrayList<String>> {

        CheckResponse checkResponse = null;

        ExistCheckTask(CheckResponse checkRes) {
            this.checkResponse = checkRes;
        }

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            try {
                return FieldsExist();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> response) {
            checkResponse.onCheckFinish(response);
        }

        private ArrayList<String> FieldsExist() throws ExecutionException, InterruptedException {
            final ArrayList<String> fields = new ArrayList<>(); // Array of fields which exist, or else contain 'error'.

            Task<DocumentSnapshot> ENCheck = DETAILS_LIST.document(EN).get(Source.SERVER)
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                if (task.getResult().contains(enrollNum.getText().toString())) {
                                    Log.d(TAG, EN + " Check successful, field exist!");
                                    fields.add(EN);
                                }else Log.d(TAG, EN + " Check successful, field does not exist!");
                            }else {
                                Log.d(TAG, EN + " Error:" + task.getException().getMessage());
                                fields.add("error");
                            }
                        }
                    });
            Task<DocumentSnapshot> EMCheck = DETAILS_LIST.document(EM).get(Source.SERVER)
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                if (task.getResult().contains(Email.getText().toString())) {
                                    Log.d(TAG, EM + " Check successful, field exist!");
                                    fields.add(EM);
                                }else Log.d(TAG, EM + " Check successful, field does not exist!");
                            }else {
                                Log.d(TAG, EM + " Error:" + task.getException().getMessage());
                                fields.add("error");
                            }
                        }
                    });
            Task<DocumentSnapshot> CHCheck = DETAILS_LIST.document(CH).get(Source.SERVER)
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                if (task.getResult().contains(chalNum.getText().toString())) {
                                    Log.d(TAG, CH + " Check successful, field exist!");
                                    fields.add(CH);
                                }else Log.d(TAG, CH + " Check successful, field does not exist!");
                            }else {
                                Log.d(TAG, CH + " Error:" + task.getException().getMessage());
                                fields.add("error");
                            }
                        }
                    });
            Task<DocumentSnapshot> MOCheck = DETAILS_LIST.document(MO).get(Source.SERVER)
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                if (task.getResult().contains(mobNum.getText().toString())) {
                                    Log.d(TAG, MO + " Check successful, field exist!");
                                    fields.add(MO);
                                }else Log.d(TAG, MO + " Check successful, field does not exist!");
                            }else {
                                Log.d(TAG, MO + " Error:" + task.getException().getMessage());
                                fields.add("error");
                            }
                        }
                    });

            // Wait for all the Checking Tasks to complete.
            Tasks.await(Tasks.whenAllComplete(ENCheck, EMCheck, CHCheck, MOCheck));
            return fields;
        }
    }
}
