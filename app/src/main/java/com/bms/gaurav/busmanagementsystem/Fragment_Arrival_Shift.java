package com.bms.gaurav.busmanagementsystem;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Fragment_Arrival_Shift extends Fragment {
    private Context context;

    private RadioGroup shiftGroup;
    private int shiftI, shiftII;
    private AppCompatSpinner spinnerStops;
    private AppCompatButton lock_button;

    // Booleans to check if fields are changed, used when LOCK button is clicked, if false, no action will be taken, else updating of document will start.
    private Boolean isLocked, isShiftChanged, isStopChanged;

    private ArrayAdapter<CharSequence> stopsAdapter;

    // The value of shift and stop selected at present.
    private int Shift;
    private String Stop;

    private ImageView onLock_overlay;
    private ProgressBar progressBar;

    private String UID;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference USERS_ARR_COLL = db.collection("USERS_ARR");

    private SharedPreferences sharedPreferences;


    public Fragment_Arrival_Shift() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getContext();
        UID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        isShiftChanged = false;
        isStopChanged = true; // = true : As the setOnSelectedListener will 'not' it(!isStopChanged), and to avoid locking choices on start of fragment.
        isLocked = false;

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_arrival, container, false);

        shiftGroup = rootView.findViewById(R.id.radio_group_shift_arr);
        shiftI = R.id.shift_1_arr;
        shiftII = R.id.shift_2_arr;

        shiftGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d("FIELD CHANGE: ", "SHIFT CHANGED!");

                isShiftChanged = !isShiftChanged;
                Shift = checkedId;
            }
        });

        lock_button = rootView.findViewById(R.id.lock_button_arr);

        onLock_overlay = rootView.findViewById(R.id.onLock_overlay_arr);
        progressBar = rootView.findViewById(R.id.progressBar_arr);

        spinnerStops = rootView.findViewById(R.id.stops_spinner_arr); // NOTICE : rootView.findViewById

        spinnerStops.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

//            Callback method to be invoked when an item in this view has been selected. This callback is invoked only when the newly selected position is different from the previously selected position or if there was no selected item.

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("FIELD CHANGE: ", "STOP CHANGED!");
                isStopChanged = !isStopChanged;
                Stop = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Populate the spinner with Stops choices
        stopsAdapter = ArrayAdapter.createFromResource(getContext(), R.array.stops_list,android.R.layout.simple_spinner_item);
        stopsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStops.setAdapter(stopsAdapter);

        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.bms_preference_name), Context.MODE_PRIVATE);

        setOnClickListener();

        return rootView;
    }

    @Override
    public void onResume() {
        occupyFields();
        super.onResume();
    }

    private void occupyFields() {
        String stop = sharedPreferences.getString(getString(R.string.pref_stop_arr_key), null);
        String shift = sharedPreferences.getString(getString(R.string.pref_shift_arr_key), null);
        if ((stop != null) && (shift != null)){
            spinnerStops.setSelection(stopsAdapter.getPosition(stop));
            shiftGroup.check(shift.equals("I") ? shiftI : shiftII);
        }

        lockUnlockTransition("LOCK");
    }

    private void setOnClickListener() {
        lock_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkNetworkConnectivity()){
                    Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
                }
                else if (isLocked) {
                    Log.d("LOCK UNLOCK: ", "UNLOCKING NOW...");
                    lockUnlockTransition("UNLOCK");
                }
                else {
                    Log.d("LOCK UNLOCK: ", "LOCKING NOW...");
                    // TODO: Check if previous choices(prior to tapping LOCK button), were same as choices now, if so, then don't do anything(a toast, maybe), else update the USERS_ARR document of this user. While the doc is updating, show a progressbar and also disable LOCKED button(setEnable(false)).

                    // Fields changed, do something....
                    if (isShiftChanged || isStopChanged){
                        onLock_overlay.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.VISIBLE);
                        lock_button.setEnabled(false);

                        if (isShiftChanged && isStopChanged){
                            Log.d("UPDATING CHOICES: ", "SHIFT AND STOP!");

                            isStopChanged = false;
                            isShiftChanged = false;
                            updateStop(true);
                        }
                        else if (isShiftChanged){
                            Log.d("UPDATING CHOICES: ", "SHIFT!");

                            isShiftChanged = false;
                            updateShift();
                        }
                        else{
                            Log.d("UPDATING CHOICES: ", "STOP!");

                            isStopChanged = false;
                            updateStop(false);
                        }
                    }
                    else {
                        Log.d("NOT UPDATING CHOICES: ", "FIELDS UNCHANGED!");

                        Toast.makeText(getContext(), R.string.choices_unchanged, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @NonNull
    private Boolean checkNetworkConnectivity() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void updateStop(final Boolean UPDATE_SHIFT) {
        Map<String, Object> data = new HashMap<>();
        data.put("STOP", Stop);

        USERS_ARR_COLL.document(UID)
                .update(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d("STOP UPDATE: ", "SUCCESSFUL!");

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(getString(R.string.pref_stop_arr_key), Stop);
                            editor.apply();

                            Toast.makeText(context, R.string.updated_stop, Toast.LENGTH_SHORT).show();

                            if (UPDATE_SHIFT){
                                updateShift();
                            }
                            else {
                                progressBar.setVisibility(View.GONE);
                                lock_button.setEnabled(true);
                                lockUnlockTransition("LOCK");
                            }
                        }
                        else {
                            Log.d("STOP UPDATE: ", task.getException().getMessage());

                            Toast.makeText(context, R.string.stop_shift_update_error, Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void updateShift() {
        final String shift = Shift == shiftI ? "I" : "II";
        Map<String, Object> data = new HashMap<>();
        data.put("SHIFT", shift);

        USERS_ARR_COLL.document(UID)
                .update(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d("SHIFT UPDATE: ", "SUCCESSFUL!");

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(getString(R.string.pref_shift_arr_key), shift);
                            editor.apply();

                            progressBar.setVisibility(View.GONE);
                            lock_button.setEnabled(true);
                            lockUnlockTransition("LOCK");

                            Toast.makeText(context, R.string.updated_shift, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Log.d("SHIFT UPDATE: ", task.getException().getMessage());
                        }
                    }
                });
    }

    private void lockUnlockTransition(String LOCK_UNLOCK){
        if (LOCK_UNLOCK.equals("UNLOCK")) {
            // Background
            lock_button.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_lock_l));

            // Icon
            lock_button.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(),R.drawable.lock), null, null, null);
            lock_button.getCompoundDrawables()[0].setColorFilter(ContextCompat.getColor(getContext(), R.color.white), PorterDuff.Mode.SRC_IN);

            //Text
            lock_button.setText(R.string.choice_lock);
            lock_button.setTextColor(ContextCompat.getColor(getContext(), R.color.white));

            onLock_overlay.setVisibility(View.GONE);

            isLocked = false;
        }
        else if (LOCK_UNLOCK.equals("LOCK")){
            // Background
            lock_button.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_lock_ul));

            // Icon and its Color
            lock_button.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(),R.drawable.lock_open), null, null, null);
            lock_button.getCompoundDrawables()[0].setColorFilter(ContextCompat.getColor(getContext(), R.color.button_unlocked_bg), PorterDuff.Mode.SRC_IN);

            //Text
            lock_button.setText(R.string.choice_unlock);
            lock_button.setTextColor(ContextCompat.getColor(getContext(), R.color.button_unlocked_bg));

            onLock_overlay.setVisibility(View.VISIBLE);

            isLocked = true;
        }
    }
}
