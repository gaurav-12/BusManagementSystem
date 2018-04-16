package com.bms.gaurav.busmanagementsystem;

import android.content.Context;
import android.database.StaleDataException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by GAURAV on 30/03/2018.
 */

public class BMS_DB_Helper extends SQLiteOpenHelper {

    private static final String DB_NAME = "BMS_DB";
    private static final int DB_VERSION = 1;

    private static final String _ID = "_id";

    // First table to store basic information specific to this application.
    public static final String USER_INFO_TABLE = "User_Info";
    public static final String CHALLAN_NUM = "Challan_Num";
    public static final String PASSWORD = "Password";
    public static final String SIGNED_IN = "SignedIn";

    private static final String CREATE_USER_INFO_TABLE =
            "CREATE TABLE " + USER_INFO_TABLE + " ( " +
                    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    CHALLAN_NUM + " TEXT, " +
                    PASSWORD + " TEXT, " +
                    SIGNED_IN + " INTEGER " +
                    ");";

    //Second table to store information specific to the user.
    public static final String ARR_DEP_INFO_TABLE = "Arrival_Departure_Info";
    public static final String ARRIVAL_SHIFT = "Arrival_Shift"; // Arrival - to college.
    public static final String DEPARTURE_SHIFT = "Departure_Shift"; // Departure - from college.

    private static final String CREATE_ARR_DEP_TABLE =
            "CREATE TABLE " + ARR_DEP_INFO_TABLE + " ( " +
                    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    CHALLAN_NUM + " TEXT, " +
                    ARRIVAL_SHIFT + " INTEGER, " +
                    DEPARTURE_SHIFT + " INTEGER " +
                    ");";

    private final Context context;

    // Constructor
    public BMS_DB_Helper(Context ctxt) {
        super(ctxt, DB_NAME, null, DB_VERSION);
        this.context = ctxt;
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_INFO_TABLE);
        db.execSQL(CREATE_ARR_DEP_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
