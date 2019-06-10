package com.bms.gaurav.busmanagementsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

public class Activity_UserProfile extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private DrawerLayout mDrawerLayout;
    private ViewPager viewPager;
    private FragmentPager_Adapter adapter;
    private TabLayout tabLayout;

    private Toolbar toolBar;
    private AppBarLayout appBar;
    private TransitionDrawable appBar_BG;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        sharedPreferences = getSharedPreferences(getString(R.string.bms_preference_name), MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();

        mDrawerLayout = findViewById(R.id.drawer_layout);
        setSupportActionBar(toolBar);     // ** It will set the Toolbar as the ActionBar, with the Default menu
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_nav_drawer);   // Adding NavigationDrawer icon on the AppBar.
    }

    private void setupTabIcon() {
        tabLayout.getTabAt(0).setIcon(R.drawable.college_arrival);

        tabLayout.getTabAt(1).setIcon(R.drawable.home_departure);
        // ****Setting the home icon's color to offwhite as it wont be selected initially****
        tabLayout.getTabAt(1).getIcon().setColorFilter(ContextCompat.getColor(this, R.color.offwhite), PorterDuff.Mode.SRC_IN);
    }


    private void setOnTabSelectListener() {

        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {

            // ****

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    appBar_BG.reverseTransition(100);
                    //appBar.setBackgroundColor(ContextCompat.getColor(Activity_UserProfile.this, R.color.colorPrimary));
                }
                else {
                    appBar_BG.reverseTransition(100);
                    //appBar.setBackgroundColor(ContextCompat.getColor(Activity_UserProfile.this, R.color.colorAccent));
                }
                tab.getIcon().setColorFilter(ContextCompat.getColor(Activity_UserProfile.this, R.color.white), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(ContextCompat.getColor(Activity_UserProfile.this, R.color.offwhite), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

//  ** Here we set the default menu for the activity to be shown in Toolbar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.options_menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout :  // Logout option on Toolbar menu.
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                mAuth.signOut();

                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);

                finish();
                return true;

            case android.R.id.home :    // Navigation Drawer icon on Toolbar.
                mDrawerLayout.openDrawer(GravityCompat.START, true);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
