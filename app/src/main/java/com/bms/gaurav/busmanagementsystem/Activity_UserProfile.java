package com.bms.gaurav.busmanagementsystem;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        mAuth = FirebaseAuth.getInstance();

        mDrawerLayout = findViewById(R.id.drawer_layout);
        SetNavigationItemSelectedListener();

        appBar = findViewById(R.id.appbar_profile);
        appBar_BG = (TransitionDrawable)appBar.getBackground();

        toolBar = findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolBar);     // ** It will set the Toolbar as the ActionBar, with the Default menu
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_nav_drawer);   // Adding NavigationDrawer icon on the AppBar.
        toolBar.setTitle(mAuth.getCurrentUser().getDisplayName());

        viewPager = findViewById(R.id.view_pager_profile); // Find the view pager that will allow the user to swipe between fragments

        // Create an adapter that knows which fragment should be shown on each page
        adapter = new FragmentPager_Adapter(this, getSupportFragmentManager());

        viewPager.setAdapter(adapter);  // Set the adapter onto the view pager
        tabLayout = findViewById(R.id.tabLayout_Profile);
        tabLayout.setupWithViewPager(viewPager, true);

        setOnTabSelectListener();
        setupTabIcon();
    }

    private void SetNavigationItemSelectedListener() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // close drawer when item is tapped
                mDrawerLayout.closeDrawers();

                // TODO : Add code here to update the UI based on the item selected

                return true;
            }
        });
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
