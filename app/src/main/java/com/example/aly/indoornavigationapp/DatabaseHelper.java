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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by aly on 06/05/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "indoor.db";
    public static final int DATABASE_VERSION = 5;
    public static final String WAP_TABLE_NAME = "WAP";
    public static final String PLACES_TABLE_NAME = "Places";
    public static final String DATASET_TABLE_NAME = "Dataset";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql_wap = "CREATE TABLE " + WAP_TABLE_NAME +
                "( ID INTEGER PRIMARY KEY AUTOINCREMENT not null, BSSID text not null, SSID not null, Freq INTEGER);";

        String sql_places = "CREATE TABLE " + PLACES_TABLE_NAME +
                " ( ID INTEGER PRIMARY KEY AUTOINCREMENT not null, Name text not null, Number INTEGER not null," +
                " X FLOAT not null, Y FLOAT not null );";

        db.execSQL(sql_wap);
        db.execSQL(sql_places);

        //createDataSetTable(db);
    }


    public void createDataSetTable(ArrayList<Integer> featuresIDs) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + DATASET_TABLE_NAME);
        String DATASET_TABLE_SQL = "CREATE TABLE " + DATASET_TABLE_NAME + " ( F" + featuresIDs.get(0) + " INTEGER DEFAULT 0";
        for (int i = 1; i < featuresIDs.size(); i++) {
            DATASET_TABLE_SQL += ", F" + featuresIDs.get(i) + " INTEGER DEFAULT 0";
        }
        DATASET_TABLE_SQL += ", Class Integer not null);";

        db.execSQL(DATASET_TABLE_SQL);
        db.close();
    }

    /**
     * @param place    is a class Y
     * @param features is a X vector
     */
    public void addFingerPrint(int place, HashMap<Integer, Integer> features) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Class", place); // Contact Name
        for (Map.Entry<Integer, Integer> entry : features.entrySet()) {
            values.put("F" + entry.getKey(), entry.getValue());
        }
        // Inserting Row
        db.insert(DATASET_TABLE_NAME, null, values);
        db.close();
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
        db.close();
    }

    public void voidDropWAP() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + WAP_TABLE_NAME);
        db.close();
    }

    public void clearDatabase() {
        this.getWritableDatabase().execSQL("delete from Places");
        this.getWritableDatabase().close();
    }

    public void deletePlace(String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(PLACES_TABLE_NAME,"Name='" +name+"'",null);
        db.close();
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
        db.close();
    }

    public float[] getPlaceLocation(int number) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] conditions = {String.valueOf(number)};
        float[] coord = new float[2];
        Cursor c = db.rawQuery("Select X,Y from Places where Number like ?", conditions);
        c.moveToFirst();
        if(c.getCount()!=0){
        coord[0] = c.getFloat(0);
        coord[1] = c.getFloat(1);}
        db.close();
        return coord;
    }

    public float[] getPlaceLocationByName(String roomName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] conditions = {String.valueOf(roomName)};
        float[] coord = new float[2];
        Cursor c = db.rawQuery("Select X,Y from Places where Name like ?", conditions);
        c.moveToFirst();

        if(c.getCount()!=0){
        coord[0] = c.getFloat(0);
        coord[1] = c.getFloat(1);}

        db.close();
        return coord;
    }

    public int getPlaceNumber(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] conditions = {name};
        int id;
        Cursor c = db.rawQuery("Select Number from Places where Name like ?", conditions);
        c.moveToFirst();
        if(c.getCount()!=0){
        id = c.getInt(0);
        db.close();
        return id;}
        else return 0;
    }
    public  String getPlaceName(int id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] conditions = {String.valueOf(id)};
        String roomName;
        Cursor c = db.rawQuery("Select Number from Places where Name like ?", conditions);
        c.moveToFirst();
        roomName = c.getString(0);
        db.close();
        return roomName;
    }

    public int getPlaceID(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] conditions = {name};
        int id;
        Cursor c = db.rawQuery("Select ID from Places where Name like ?", conditions);
        c.moveToFirst();
        id = c.getInt(0);
        db.close();
        return id;
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

    public void addWAP(String bssid, String ssid, int freq) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("BSSID", bssid);
        values.put("SSID", ssid);
        values.put("Freq", freq);
        // Inserting Row
        db.insert(WAP_TABLE_NAME, null, values);
        db.close(); // Closing database connection
    }

    public int getWAPID(String ssid) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] conditions = {ssid};
        int id;
        Cursor c = db.rawQuery("Select ID from WAP where SSID like ?", conditions);
        c.moveToFirst();
        Log.d("Value", "Id is :" + String.valueOf(c.getInt(0)));
        id = c.getInt(0);
        c.close();
        db.close();
        return id;
    }

    public String getWAPSSID(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] conditions = {String.valueOf(id)};
        String ssid;
        Cursor c = db.rawQuery("Select SSID from WAP where ID like ?", conditions);
        c.moveToFirst();
        ssid = c.getString(0);
        c.close();
        db.close();
        return ssid;
    }

    public Cursor fetchAllWAPS() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] rowDetails = {"ID", "BSSID", "SSID", "Freq"};
        Cursor cursor = db.query(WAP_TABLE_NAME, rowDetails, null, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        db.close();
        return cursor;
    }


    public String[] getDataSetColumns() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor dbCursor = db.query(DATASET_TABLE_NAME, null, null, null, null, null, null);
        String[] Columns = dbCursor.getColumnNames();
        dbCursor.close();
        return Columns;

    }

    public void exportTheDataSet() throws IOException {
        SQLiteDatabase db = this.getReadableDatabase();

        File root = new File(Environment.getExternalStorageDirectory(), "Datasets");
        if (!root.exists()) {
            root.mkdirs();
        }

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
        String TimeStampDB = sdf.format(cal.getTime());

        try {

            File gpxfile = new File(root, TimeStampDB + ".csv");
            gpxfile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(gpxfile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);


            String[] rowDetails = getDataSetColumns();
            for (int j = 0; j < rowDetails.length; j++) {
                myOutWriter.append(rowDetails[j] + ";");
            }
            myOutWriter.append("\n");


            Cursor c = db.query(DATASET_TABLE_NAME, rowDetails, null, null, null, null, null);

            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        for (int j = 0; j < rowDetails.length; j++) {
                            myOutWriter.append(c.getString(j) + ";");
                        }
                        myOutWriter.append("\n");
                    }

                    while (c.moveToNext());
                }

                c.close();
                myOutWriter.close();
                fOut.close();

            }


        } catch (SQLiteException se) {
            Log.e(getClass().getSimpleName(), "Could not create or Open the database");
        } finally {
            db.close();
        }

    }
}
