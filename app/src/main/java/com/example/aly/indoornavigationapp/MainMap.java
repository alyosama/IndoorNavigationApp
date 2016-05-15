package com.example.aly.indoornavigationapp;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Config;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
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
    ImageView cross;
    CoordinatorLayout mainlayout;
    Toolbar toolbar;

    Bitmap imageBitmap;
    public boolean isConfigured=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);
        floorMap = (ImageView) findViewById(R.id.map);
        imageBitmap= BitmapFactory.decodeResource(getResources(),R.drawable.floor_map);

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
            cross.setX(locationCoord[0]+floorMap.getX()-cross.getWidth());
            cross.setY(locationCoord[1]+toolbar.getHeight()-floorMap.getY());
           //Toast.makeText(MainMap.this,"Room: "+String.valueOf(location),Toast.LENGTH_SHORT).show();
        cross.setVisibility(View.VISIBLE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
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


//        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, 0x12345);
//        }else{
//            mainWifi.startScan();
//            //do something, permission was previously granted; or legacy device
//        }
        mainWifi.startScan();
    }
//TODO
    //Check permission for wifi access (newer versions (android m) of android need to allow permissions at runtime)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0x12345) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            mainWifi.startScan();
        }

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
            return n.nextInt(5) + 1;
        }
    }

    public int recieveLocationFromServer(){
        return 0;
    }
    public void sendToServer(HashMap featuresX){

    }

    @Override
    protected void onResume() {
        super.onResume();

        loadSpinnerData();
    }

    public void findPath() {

        String destination = String.valueOf(placesSpinner.getSelectedItem());
        int room = getLocation();
        float[] sourceCoord = helper.getPlaceLocation(room);
        Toast.makeText(MainMap.this,"Room: "+String.valueOf(room),Toast.LENGTH_SHORT).show();

        float[] destCoord = helper.getPlaceLocationByName(destination);
        float[] midCoord={(sourceCoord[0]+destCoord[0])/2.0f,(sourceCoord[1]+destCoord[1])/2.0f};
       // float[] midCoord={sourceCoord[0]+(sourceCoord[0] - destCoord[0])/2,sourceCoord[1]+(sourceCoord[1]-destCoord[1])/2};

        DrawLines(sourceCoord,midCoord,destCoord,Color.RED);
    }


    Canvas canvas;
    private void DrawLines(float[] sourceCoord,float[] midCoord,float[] destCoord,int color){
        Bitmap overlay=Bitmap.createBitmap(imageBitmap.getWidth(), imageBitmap.getHeight(),Bitmap.Config.ARGB_8888);
        canvas=new Canvas(overlay);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(imageBitmap, floorMap.getX(), floorMap.getY(),paint);



            DrawLine(sourceCoord[0],sourceCoord[1], midCoord[0],sourceCoord[1], Color.RED);
            DrawLine(midCoord[0],sourceCoord[1], midCoord[0],destCoord[1], Color.RED);
            DrawLine(midCoord[0],destCoord[1], destCoord[0],destCoord[1], Color.RED);

        floorMap.setImageBitmap(overlay);
    }

    private void DrawLine(float x,float y,float xend,float yend,int color){

        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setColor(color);
        paint.setStrokeWidth(12.0f);

        x+=floorMap.getX()-cross.getWidth();
        y+=toolbar.getHeight()-floorMap.getY();
        xend+=floorMap.getX()-cross.getWidth();
        yend+=toolbar.getHeight()-floorMap.getY();
        canvas.drawLine(x, y, xend, yend, paint);

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




