package com.bms.gaurav.busmanagementsystem;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.ColorInt;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

public class Activity_UserProfile extends AppCompatActivity {
    BMS_DB_Adapter dbHelper;
    String challanNum;

    ViewPager viewPager;
    FragmentPager_Adapter adapter;
    TabLayout tabLayout;

    AppBarLayout appBar;
    TransitionDrawable appBar_BG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);
        dbHelper = new BMS_DB_Adapter(this);

        appBar = (AppBarLayout)findViewById(R.id.appbar_profile);
        appBar_BG = (TransitionDrawable)appBar.getBackground();

        challanNum = getIntent().getExtras().getString("ChallanNum");   // Getting the challan number sent by the login activity's intent's extras

        setSupportActionBar((Toolbar)findViewById(R.id.toolbar_profile));     // ** It will set the Toolbar as the ActionBar, with the Default menu
        //setStandAloneToolbar();

        viewPager = (ViewPager)findViewById(R.id.view_pager_profile); // Find the view pager that will allow the user to swipe between fragments

        // Create an adapter that knows which fragment should be shown on each page
        adapter = new FragmentPager_Adapter(this, getSupportFragmentManager());

        viewPager.setAdapter(adapter);  // Set the adapter onto the view pager
        tabLayout = (TabLayout)findViewById(R.id.tabLayout_Profile);
        tabLayout.setupWithViewPager(viewPager, true);

        setOnTabSelectListener();
        setupTabIcon();
    }

    private void setupTabIcon() {
        tabLayout.getTabAt(0).setIcon(R.drawable.college_arrival);

        tabLayout.getTabAt(1).setIcon(R.drawable.home_departure);
        // ****Setting the home icon's color to offwhite as it wont be selected initially****
        tabLayout.getTabAt(1).getIcon().setColorFilter(ContextCompat.getColor(this, R.color.offwhite), PorterDuff.Mode.SRC_IN);
    }


    private void setOnTabSelectListener() {

        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {

            // ** Colors **
            int primaryDark = ContextCompat.getColor(Activity_UserProfile.this, R.color.colorPrimaryDark);
            int accentDark = ContextCompat.getColor(Activity_UserProfile.this, R.color.colorAccentDark);
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

    private void setStandAloneToolbar() {
    // *** Here we define a Standalone Toolbar, then its Menu's Item's click listener, and finally we inflate the menu on the Toolbar.
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_profile);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.logout :
                        dbHelper.userSignedIn(challanNum, false);   // User signed out, thus signedIn parameter's value is given false

                        Intent i = new Intent(Activity_UserProfile.this, MainActivity.class);
                        startActivity(i);

                        finish();
                        return true;
                    default:
                        return false;
                }
            }
        });

        toolbar.inflateMenu(R.menu.options_menu_profile);
    // ***
    }

    @Override
    protected void onResume() {
        super.onResume();
        dbHelper.open();
    }

    @Override
    protected void onStop() {
        super.onStop();
        dbHelper.close();
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
            case R.id.logout :
                dbHelper.userSignedIn(challanNum, false);   // User signed out, thus signedIn parameter's value is given false

                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);

                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
