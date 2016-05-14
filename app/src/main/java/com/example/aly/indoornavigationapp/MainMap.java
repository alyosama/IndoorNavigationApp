package com.example.aly.indoornavigationapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.jar.Manifest;

public class MainMap extends AppCompatActivity {
    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    DatabaseHelper db;

    FloatingActionButton findPathBtn;
    Spinner placesSpinner;
    DatabaseHelper helper;
    ArrayAdapter<String> dataAdapter;
    ImageView floorMap;
    CoordinatorLayout mainlayout;
    Toolbar toolbar;

    public boolean isConfigured=false;
     ImageView cross;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);
        floorMap = (ImageView) findViewById(R.id.map);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mainlayout = (CoordinatorLayout) findViewById(R.id.mainlayout);
        setSupportActionBar(toolbar);
        cross = (ImageView) findViewById(R.id.cross);

        helper = new DatabaseHelper(this);

        db = new DatabaseHelper(getApplicationContext());
        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        findPathBtn = (FloatingActionButton) findViewById(R.id.pathBtn);
        placesSpinner = (Spinner) findViewById(R.id.spinner);

        //Resize the marker
        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) cross.getLayoutParams();
        params.width = 50;
        params.height = 50;
        cross.setLayoutParams(params);
        loadSpinnerData();

        findPathBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findPath();
            }
        });
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 2000);
               int location = getLocation();
                markLocation(location);

            }
        };
        runnable.run();

        if(isConfigured){
            footprintsConfigured();

        }

    }

    public void markLocation(int location){
        float locationCoord[]= helper.getPlaceLocation(location);

        if(locationCoord.length !=0){
            //Toast.makeText(MainMap.this, locationCoord[0] + "," + locationCoord[1] + " mark coord", Toast.LENGTH_SHORT).show();
            cross.setX(locationCoord[0]+floorMap.getX()-cross.getWidth());
            cross.setY(locationCoord[1]+toolbar.getHeight()-floorMap.getY());
            Toast.makeText(MainMap.this,"Room: "+String.valueOf(location),Toast.LENGTH_SHORT).show();
        cross.setVisibility(View.VISIBLE);
        }
    }
    public void runWifi(){
        if (mainWifi.isWifiEnabled() == false) {
            // If wifi disabled then enable it
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled",
                    Toast.LENGTH_LONG).show();

            mainWifi.setWifiEnabled(true);
        }

        // wifi scaned value broadcast receiver
        receiverWifi = new WifiReceiver();
        // Register broadcast receiver
        // Broacast receiver will automatically call when number of wifi connections changed
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mainWifi.startScan();
    }
//TODO
    //Check permission for wifi access (newer versions (android m) of android need to allow permissions at runtime)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public int checkSelfPermission(String permission) {
        return super.checkSelfPermission(permission);
    }

    HashMap featuresMap;
    public void footprintsConfigured(){
        String[] Columns = db.getDataSetColumns();

        featuresMap= new HashMap();
        for (int j = 0; j < Columns.length - 1; j++) {
            int id = Integer.parseInt(Columns[j].substring(1));
            featuresMap.put(db.getWAPSSID(id), id);
        }
    }
    public int getLocation() {
        if(isConfigured){
            runWifi();
            ///RECIEVE FROM SERVER POSTION
            int location=recieveLocationFromServer();
            return location;
        }else{
            Random n = new Random();
            return n.nextInt(15) + 1;
        }
    }

    public int recieveLocationFromServer(){
        return 0;
    }
    public void sendToServer(HashMap featuresX){

    }

    public void findPath() {

        String destination = String.valueOf(placesSpinner.getSelectedItem());
        float sourceCoord[] = helper.getPlaceLocationByName("Room10");
        float cooridor1[] = helper.getPlaceLocationByName("corridor12");
        float destCoord[] = helper.getPlaceLocationByName(destination);
       // DrawLine(floorMap.getX()+sourceCoord[0],floorMap.getY()+sourceCoord[1] , floorMap.getX()+cooridor1[0],floorMap.getY()+sourceCoord[1], Color.BLUE);
        DrawLine(floorMap.getX()+sourceCoord[0],floorMap.getY()+sourceCoord[1] , floorMap.getX()+cooridor1[0],floorMap.getY()+sourceCoord[1], Color.RED);
        DrawLine(floorMap.getX()+cooridor1[0],floorMap.getY()+sourceCoord[1] , floorMap.getX()+cooridor1[0],floorMap.getY()+destCoord[1], Color.RED);
        DrawLine(floorMap.getX()+cooridor1[0],floorMap.getY()+destCoord[1] , floorMap.getX()+destCoord[0],floorMap.getY()+destCoord[1], Color.RED);
//        Toast.makeText(MainMap.this, floorMap.getX() + "," + floorMap.getY() + " floor coord", Toast.LENGTH_SHORT).show();

    }

    private void DrawLine(float x, float y, float xend, float yend, int color) {

        BitmapDrawable bmpDraw = (BitmapDrawable) floorMap.getDrawable();
        Bitmap bmp = bmpDraw.getBitmap().copy(Bitmap.Config.RGB_565, true);
        Canvas c = new Canvas(bmp);
        Paint p = new Paint();
        p.setColor(color);
        c.drawLine(x, y, xend, yend, p);
        floorMap.setImageBitmap(bmp);

    }

    private void loadSpinnerData() {
        // Spinner Drop down elements
        // Creating adapter for spinner
        dataAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        placesSpinner.setAdapter(dataAdapter);

        final Cursor cursor = helper.fetchAllPlaces();
        if (cursor != null) {
            while (!cursor.isAfterLast()) {
                dataAdapter.add(cursor.getString(0));
                cursor.moveToNext();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = null;
        switch (item.getItemId()) {
            case R.id.places:
                i = new Intent(this, Places.class);
                startActivity(i);
                return true;
            case R.id.wap:
                i = new Intent(this, WAP.class);
                startActivity(i);
                return true;
            case R.id.configure:
                i = new Intent(this, Configure.class);
                startActivity(i);
                return true;
        }
        return false;
    }

    class WifiReceiver extends BroadcastReceiver {
        public  List<ScanResult> wifiList;

        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {
            wifiList = mainWifi.getScanResults();

            HashMap x = new HashMap();
            for (ScanResult result : wifiList) {
                if (featuresMap.containsKey(result.SSID)) {
                    x.put(featuresMap.get(result.SSID), result.level);
                }
            }

            ////SEND X"Features Vector" to Server
            sendToServer(x);

        }
    }

    }

//Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//      .setAction("Action", null).show();




