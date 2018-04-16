package com.bms.gaurav.busmanagementsystem;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by GAURAV on 07/04/2018.
 */

public class
FragmentPager_Adapter extends FragmentPagerAdapter {
    private Context mCtxt;

    public FragmentPager_Adapter(Context context, FragmentManager fm) {
        super(fm);
        mCtxt = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new Fragment_Arrival_Shift();
        }
        else return new Fragment_Departure_Shift();
    }


    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0 :
                return mCtxt.getString(R.string.select_arrival);
            case 1 :
                return mCtxt.getString(R.string.select_departure);
            default:
                return null;
        }
    }
}

