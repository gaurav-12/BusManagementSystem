package com.bms.gaurav.busmanagementsystem;


import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TypeEvaluator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.solver.widgets.Animator;
import android.support.transition.Transition;
import android.support.transition.TransitionSet;
import android.support.transition.TransitionValues;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.Toast;

public class Fragment_Departure_Shift extends Fragment {

    AppCompatButton lock_button;
    Boolean isLocked;
    ImageView onLock_overlay;
    //TransitionDrawable onLock_overlay_transition;

    public Fragment_Departure_Shift() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_departure, container, false);

        isLocked = false;
        lock_button = (AppCompatButton)rootView.findViewById(R.id.lock_button_dep);
        onLock_overlay = (ImageView)rootView.findViewById(R.id.onLock_overlay_dep);
        //onLock_overlay_transition = (TransitionDrawable)onLock_overlay.getDrawable();

        // Depending upon the users data, the lock button's icon's color should change, initially Button in Unlocked.
        lock_button.getCompoundDrawables()[0].setColorFilter(ContextCompat.getColor(getContext(), R.color.button_unlocked_bg), PorterDuff.Mode.SRC_IN);

        setOnClickListener();

        return rootView;
    }

    private void setOnClickListener() {
        lock_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLocked) {
                    // Background
                    lock_button.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_lock_ul));

                    // Icon and its Color
                    lock_button.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(),R.drawable.lock_open), null, null, null);
                    lock_button.getCompoundDrawables()[0].setColorFilter(ContextCompat.getColor(getContext(), R.color.button_unlocked_bg), PorterDuff.Mode.SRC_IN);

                    //Text
                    lock_button.setText(R.string.choice_unlocked);
                    lock_button.setTextColor(ContextCompat.getColor(getContext(), R.color.button_unlocked_bg));

                    onLock_overlay.setVisibility(View.INVISIBLE);
                    //onLock_overlay_transition.reverseTransition(100);       // Set the visibility for the overlay.
                    onLock_overlay.setClickable(false);     // Make it un-clickable

                    isLocked = false;
                }
                else {
                    // Background
                    lock_button.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_lock_l));

                    // Icon
                    lock_button.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(),R.drawable.lock), null, null, null);
                    lock_button.getCompoundDrawables()[0].setColorFilter(ContextCompat.getColor(getContext(), R.color.white), PorterDuff.Mode.SRC_IN);

                    //Text
                    lock_button.setText(R.string.choice_locked);
                    lock_button.setTextColor(ContextCompat.getColor(getContext(), R.color.white));

                    onLock_overlay.setVisibility(View.VISIBLE);
                    //onLock_overlay_transition.reverseTransition(100);     // Set the visibility for the overlay.
                    onLock_overlay.setClickable(true);     // Make it clickable

                    isLocked = true;
                }
            }
        });
    }
}
