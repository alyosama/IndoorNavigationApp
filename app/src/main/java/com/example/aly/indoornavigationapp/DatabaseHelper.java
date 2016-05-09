package com.example.aly.indoornavigationapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by aly on 06/05/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "indoor.db";
    public static final int DATABASE_VERSION = 2;
    public static final String WAP_TABLE_NAME = "WAP";
    public static final String PLACES_TABLE_NAME = "Places";

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


        //TODO Remove these two lines
        String DATASET_TABLE_SQL = "CREATE TABLE Dataset ( F0 INTEGER, F1 INTEGER, Class INTEGER);";
        db.execSQL(DATASET_TABLE_SQL);

    }


    public void createDataSetTable(ArrayList<String> featuresNames) {
        SQLiteDatabase db = this.getWritableDatabase();
        String DATASET_TABLE_SQL = "CREATE TABLE Dataset ( F0 INTEGER";
        for (int i = 1; i < featuresNames.size(); i++) {
            DATASET_TABLE_SQL += ", F" + i + " INTEGER";
        }
        DATASET_TABLE_SQL += ");";

        db.execSQL(DATASET_TABLE_SQL);
        db.close();
    }


    public void createDataSetTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        String DATASET_TABLE_SQL = "CREATE TABLE Dataset ( F0 INTEGER, F1 INTEGER, Class INTEGER);";
        db.execSQL(DATASET_TABLE_SQL);
        db.close();
    }

    /**
     * @param place    is a class Y
     * @param features is a X vector
     */
    public void addFingerPrint(int place, int[] features) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Class", place); // Contact Name
        for (int i = 0; i < features.length; i++) {
            values.put("F" + String.valueOf(i), features[i]);
        }
        // Inserting Row
        db.insert("Dataset", null, values);
        db.close(); // Closing database connection
    }

    public void exportTheDataSet() throws IOException {

        File root = new File(Environment.getExternalStorageDirectory(), "Datasets");
        if (!root.exists()) {
            root.mkdirs();
        }

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
        String TimeStampDB = sdf.format(cal.getTime());

        SQLiteDatabase db;
        try {

            File gpxfile = new File(root, TimeStampDB + ".csv");
            gpxfile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(gpxfile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append("F0;F1;Class");
            myOutWriter.append("\n");

            db = this.getWritableDatabase();
            String[] rowDetails = {"F0", "F1", "Class"};
            Cursor c = db.query("Dataset", rowDetails, null, null, null, null, null);

            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        myOutWriter.append(c.getString(0) + ";" + c.getString(1) + ";" + c.getString(2));
                        myOutWriter.append("\n");
                    }

                    while (c.moveToNext());
                }

                c.close();
                myOutWriter.close();
                fOut.close();

            }

            db.close();

        } catch (SQLiteException se) {
            Log.e(getClass().getSimpleName(), "Could not create or Open the database");
        }

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void voidDropPlaces() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + PLACES_TABLE_NAME);
        db.close();
    }

    public void voidDropWAP() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + WAP_TABLE_NAME);
        db.close();
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
        SQLiteDatabase db = this.getWritableDatabase();
        String[] rowDetails = {"Name"};
        Cursor cursor = db.query(PLACES_TABLE_NAME, rowDetails, null, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        db.close();
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
