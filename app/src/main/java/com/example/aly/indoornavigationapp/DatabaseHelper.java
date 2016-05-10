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
    public static final int DATABASE_VERSION = 3;
    public static final String WAP_TABLE_NAME = "WAP";
    public static final String PLACES_TABLE_NAME = "Places";
    private static final String DATASET_TABLE_NAME = "Dataset";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql_wap = "CREATE TABLE " + WAP_TABLE_NAME +
                "( ID INTEGER PRIMARY KEY AUTOINCREMENT not null, BSSID text not null, SSID text not null, Freq INTEGER);";

        String sql_places = "CREATE TABLE " + PLACES_TABLE_NAME +
                " ( ID INTEGER PRIMARY KEY AUTOINCREMENT not null, Name text not null, Number INTEGER not null" +
                " X FLOAT not null, Y FLOAT not null );";

        db.execSQL(sql_wap);
        db.execSQL(sql_places);

        //createDataSetTable(db);
    }


    public void createDataSetTable(ArrayList<Integer> featuresIDs) {
        SQLiteDatabase db = this.getWritableDatabase();
        String DATASET_TABLE_SQL = "CREATE TABLE " + DATASET_TABLE_NAME + " ( F" + featuresIDs.get(0) + " INTEGER";
        for (int i = 1; i < featuresIDs.size(); i++) {
            DATASET_TABLE_SQL += ", F" + featuresIDs.get(i) + " INTEGER";
        }
        DATASET_TABLE_SQL += ");";

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
        db.insert(DATASET_TABLE_NAME, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PLACES_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + WAP_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DATASET_TABLE_NAME);
        onCreate(db);

    }

    public void voidDropPlaces() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + PLACES_TABLE_NAME);

    }

    public void voidDropWAP() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + WAP_TABLE_NAME);

    }

    public void clearDatabase() {
        this.getWritableDatabase().execSQL("delete from Places");
        this.getWritableDatabase().close();
    }

    public void addPlace(String name, int number, float[] viewCoords) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Name", name); // Contact Name
        values.put("Number", number);
        values.put("X", viewCoords[0]);
        values.put("Y", viewCoords[1]);
        // Inserting Row
        db.insert(PLACES_TABLE_NAME, null, values);
    }

    public float[] getPlaceLocation(int number) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] conditions = {String.valueOf(number)};
        float[] coord = new float[2];
        Cursor c = db.rawQuery("Select X,Y from Places where Number like ?", conditions);
        coord[0] = c.getFloat(0);
        coord[1] = c.getFloat(1);

        db.close();
        return coord;
    }

    public Cursor fetchAllPlaces() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] rowDetails = {"Name", "Number", "X", "Y"};
        Cursor cursor = db.query(PLACES_TABLE_NAME, rowDetails, null, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        db.close();
        return cursor;
    }

    public void addWAP(int id, String bssid, String ssid, int freq) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("BSSID", bssid); // Contact Name
        values.put("SSID", ssid);
        values.put("Freq", freq);
        // Inserting Row
        db.insert(WAP_TABLE_NAME, null, values);
        db.close(); // Closing database connection
    }

    public int getWAPID(String ssid) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select ID from " + WAP_TABLE_NAME + " where SSID=\"" + ssid + "\"";
        Cursor cursor = db.rawQuery(query, null);

        int id;
        if (cursor != null && cursor.getCount() > 0) {
            id = cursor.getInt(0);
        } else {
            id = -1;
        }
        cursor.close();
        return id;
    }

    public Cursor fetchAllWAPS() {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] rowDetails = {"ID", "BSSID", "SSID", "Freq"};
        Cursor cursor = db.query(WAP_TABLE_NAME, rowDetails, null, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        db.close();
        return cursor;
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
}
