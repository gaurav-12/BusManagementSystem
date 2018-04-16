package com.bms.gaurav.busmanagementsystem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by GAURAV on 30/03/2018.
 */

public class BMS_DB_Adapter {
    private SQLiteDatabase mDB;
    private BMS_DB_Helper mDBHelper;
    private final Context mContext;

    public BMS_DB_Adapter(Context ctxt) {
        this.mContext = ctxt;
    }

    public BMS_DB_Adapter open() throws SQLException {
        mDBHelper = new BMS_DB_Helper(mContext);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDBHelper.close();
    }

    public void addAccount(String challanNum, String password) {
        ContentValues info = new ContentValues(); // Inserting the information of the user in userInfo_Table.
        info.put(BMS_DB_Helper.CHALLAN_NUM, challanNum);
        info.put(BMS_DB_Helper.PASSWORD, password);
        info.put(BMS_DB_Helper.SIGNED_IN, 0); // Initially, user is not signed in so SIGNED_IN have its value 0
        long _id = mDB.insert(BMS_DB_Helper.USER_INFO_TABLE, null, info);

        ContentValues ad_info = new ContentValues(); // Also inserting a row in ArrivalDeparture_Table for the user.
        ad_info.put(BMS_DB_Helper.CHALLAN_NUM, challanNum);
        ad_info.put(BMS_DB_Helper.ARRIVAL_SHIFT, 0);
        ad_info.put(BMS_DB_Helper.DEPARTURE_SHIFT, 0);
        long _id_ad = mDB.insert(BMS_DB_Helper.ARR_DEP_INFO_TABLE, null, ad_info);
    }

    public void update_UsersChoices(String challanNum, int arr_shift, int dep_shift) {
        ContentValues cv = new ContentValues();
        cv.put(BMS_DB_Helper.ARRIVAL_SHIFT, arr_shift);
        cv.put(BMS_DB_Helper.DEPARTURE_SHIFT, dep_shift);
        String[] whereArgs = {challanNum};
        mDB.update(BMS_DB_Helper.ARR_DEP_INFO_TABLE, cv, BMS_DB_Helper.CHALLAN_NUM, whereArgs);
    }

    public boolean checkIfPresent(String chNum) {
        Cursor cursor = mDB.query(true,
                BMS_DB_Helper.USER_INFO_TABLE, // Table
                new String[] {BMS_DB_Helper.CHALLAN_NUM}, // Columns to display
                BMS_DB_Helper.CHALLAN_NUM + "=?", // Which column to look for ***** REMEMBER TO USE '=?' ******
                new String[] {chNum}, // What value to look for
                null, null, null, null);

        // If cursor have something, or it does not count to ZERO
        if (cursor.getCount() > 0) {
            cursor.close();
            return true; // Account already present
        }
        else {
            cursor.close();
            return false;
        }
    }

    public boolean checkPasswordMatch(String chNum, String inputPassword) {
        Cursor cursor = mDB.query(true,
                BMS_DB_Helper.USER_INFO_TABLE, // Table
                new String[] {BMS_DB_Helper.PASSWORD}, // Columns to display
                BMS_DB_Helper.CHALLAN_NUM + "=?", // Which column to look for ***** REMEMBER TO USE '=?' ******
                new String[] {chNum}, // What value to look for
                null, null, null, null);

        cursor.moveToFirst();

        if (cursor.getString(cursor.getColumnIndex(BMS_DB_Helper.PASSWORD)).equals(inputPassword)) {
            cursor.close();
            return true;
        }
        else {
            cursor.close();
            return false;
        }
    }

    // User is has either signedIn or signedOut, so here we set the SIGNED_IN's value according to it
    public void userSignedIn(String challanNum, boolean signedIn) {
        ContentValues contents = new ContentValues();
        if(signedIn) {  // signedIn = true
            contents.put(BMS_DB_Helper.SIGNED_IN, 1);
        }
        else contents.put(BMS_DB_Helper.SIGNED_IN, 0);  // signedIn = false

        int _id = mDB.update(BMS_DB_Helper.USER_INFO_TABLE, // Table
                contents,   // Content Values
                BMS_DB_Helper.CHALLAN_NUM + "=?",   // where CHALLAN_NUM =....
                new String[] {challanNum}    //....chNum
                );
    }

    public String isUserSignedIn() {
        Cursor cursor = mDB.query(BMS_DB_Helper.USER_INFO_TABLE,
                new String[] {BMS_DB_Helper.CHALLAN_NUM},
                BMS_DB_Helper.SIGNED_IN + "=?",
                new String[]{"1"},
                null, null, null, null);

        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            String challanNum = cursor.getString(cursor.getColumnIndex(BMS_DB_Helper.CHALLAN_NUM));
            cursor.close();
            return challanNum;
        }
        else {
            cursor.close();
            return null;
        }
    }

    public void clearData() {
        int rows = mDB.delete(BMS_DB_Helper.USER_INFO_TABLE, null, null);
        int rows1 = mDB.delete(BMS_DB_Helper.ARR_DEP_INFO_TABLE, null, null);
    }
}
