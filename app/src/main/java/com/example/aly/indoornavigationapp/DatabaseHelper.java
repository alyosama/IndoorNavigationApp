package com.example.aly.indoornavigationapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by aly on 06/05/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "indoor.db";
    public static final int DATABASE_VERSION = 1;
    public static final String WAP_TABLE_NAME = "WAP";
    public static final String PLACES_TABLE_NAME = "Places";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql_wap = "CREATE TABLE " + WAP_TABLE_NAME +
                "( SSID text not null, name text not null, " +
                "level text not null, freq text not null );";

        String sql_places = "CREATE TABLE " + PLACES_TABLE_NAME + " ( name text not null );";

        db.execSQL(sql_wap);
        db.execSQL(sql_places);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
