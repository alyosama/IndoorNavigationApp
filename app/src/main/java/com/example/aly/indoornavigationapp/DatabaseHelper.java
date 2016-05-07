package com.example.aly.indoornavigationapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

/**
 * Created by aly on 06/05/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "indoor.db";
    public static final int DATABASE_VERSION = 2;
    public static final String WAP_TABLE_NAME = "WAP";
    public static final String PLACES_TABLE_NAME = "Places";


    public SQLiteDatabase placesDatabase;

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql_wap = "CREATE TABLE " + WAP_TABLE_NAME +
                "( BSSID text not null, SSID text not null);";

        String sql_places = "CREATE TABLE " + PLACES_TABLE_NAME + " ( ID INTEGER PRIMARY KEY, Name text not null );";

        db.execSQL(sql_wap);
        db.execSQL(sql_places);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void addPlace(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Name", name); // Contact Name
        // Inserting Row
        db.insert(PLACES_TABLE_NAME, null, values);
        db.close(); // Closing database connection
    }

    public void addWAP(String bssid, String ssid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("BSSID", bssid); // Contact Name
        values.put("SSID", ssid);

        // Inserting Row
        db.insert(WAP_TABLE_NAME, null, values);
        db.close(); // Closing database connection
    }

    public boolean searchWAP(String bssid) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from " + WAP_TABLE_NAME + " where BSSID=\"" + bssid + "\"";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.getCount() > 0) {
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public Cursor fetchAllWAPS() {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] rowDetails = {"BSSID", "SSID"};
        Cursor cursor = db.query(WAP_TABLE_NAME, rowDetails, null, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        db.close();
        return cursor;
    }

    public Cursor fetchAllPlaces() {
        placesDatabase = getReadableDatabase();
        String[] rowDetails = {"Name"};
        Cursor cursor = placesDatabase.query(PLACES_TABLE_NAME, rowDetails, null, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        placesDatabase.close();
        return cursor;
    }

   /* public void deleteMovie(String name){
        movieDatabase=getWritableDatabase();
        movieDatabase.delete("movie", "name='" + name + "'", null);
        movieDatabase.close();
    }
    public void updateMovie(String oldName,String name,String desc){
        movieDatabase=getWritableDatabase();
        ContentValues rows=new ContentValues();
        rows.put("name",name);
        rows.put("description",desc);
        movieDatabase.update("movie",rows,"name like ?",new String[]{oldName});
        movieDatabase.close();
    }*/

}
