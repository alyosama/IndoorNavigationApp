package com.example.aly.indoornavigationapp;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
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
    boolean paused;
    HashMap<String, Integer> config = null;
    private static final String SERVER_URL = "http://172.20.10.2:3000";

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

//        findPathBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                findPath();
//            }
//        });
//        final Handler handler = new Handler();
//        final Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                handler.postDelayed(this, 2000);
//               int location = getLocation();
//                markLocation(location);
//
//            }
//        };
//        runnable.run();

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
    public void _markLocation(float x, float y, String name){
        cross.setX(x+floorMap.getX()-cross.getWidth());
        cross.setY(y + toolbar.getHeight() - floorMap.getY());
        Toast.makeText(MainMap.this,"Room: "+name,Toast.LENGTH_SHORT).show();
        cross.setVisibility(View.VISIBLE);
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
    @Override
    protected void onPause(){
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        paused = false;
        if(config == null)
        {
            new GetWaps().execute();
        }
        else
        {
            runWifi();
        }

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

            ArrayList<Integer> features = new ArrayList<>(config.size());
            for(int i = 0; i < config.size();i++)
            {
                features.add(i, 0);
            }
            for (ScanResult result : wifiList) {
                if (config.containsKey(result.SSID)) {
                    features.set(config.get(result.SSID)-1, result.level);
                }
            }
            unregisterReceiver(receiverWifi);
            new GetLoc().execute(features);

        }
    }


    class GetWaps extends AsyncTask<Void, Void, Boolean> {
        URL url;
        HttpURLConnection connection;
        ProgressDialog dialog;
        @Override
        protected void onPreExecute (){
            dialog = new ProgressDialog(MainMap.this);
            dialog.setIndeterminate(true);
            dialog.setMessage("Trying to fetch config...");
            dialog.show();
        }

        @Override
        protected void onPostExecute (Boolean  result){
            dialog.hide();
            if(result)
            {
                if(!paused) {
                    runWifi();
                }
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Failed to send to server, retrying in 5 sec.",
                        Toast.LENGTH_LONG).show();

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if(!paused)
                        {
                            new GetWaps().execute();
                        }
                    }
                }, 5000);
            }


        }
        @Override
        protected Boolean doInBackground(Void ... f) {
            try {
                url = new URL(SERVER_URL + "/wap");
                connection = (HttpURLConnection) url.openConnection();
                // set connection to allow input
                connection.setDoInput(true);
                // set the request method to POST
                connection.setRequestMethod("GET");
                // set content-type property
                connection.setRequestProperty("Content-Type", "application/json");
                // set charset property to utf-8
                connection.setRequestProperty("charset", "utf-8");
                // set accept property
                connection.setRequestProperty("Accept", "application/json");
                // connect to server
                connection.connect();
                // receive the response from server
                return getResponseFromServer(connection);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;

        }
        private boolean getResponseFromServer(HttpURLConnection connection){
            // create StringBuilder object to append the input stream in
            StringBuilder sb = new StringBuilder();
            String line;
            try{
                // get input stream
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                // append stream in a the StringBuilder object
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                reader.close();
                // convert StringBuilder object to string and store it in a variable
                String JSONResponse = sb.toString();
                // convert response to JSONObject
                JSONObject response = new JSONObject(JSONResponse);

                // checks if an error is in the response
                if (response.has("error")) {
                    Log.d(Configure.class.getSimpleName(), JSONResponse);
                    return false;
                }

                MainMap.this.config = new HashMap<>();
                Iterator<String> iter = response.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    Integer value = response.getInt(key);
                    MainMap.this.config.put(key,value);
                }
                return true;
            }

            catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return false;
        }
    }

    static class LocResponse{
        public float x,y;
        public boolean result;
        public String name;

        public LocResponse(float x, float y, boolean result) {
            this.x = x;
            this.y = y;
            this.result = result;
        }

        public LocResponse(float x, float y, String name, boolean result) {
            this.x = x;
            this.y = y;
            this.result = result;
            this.name = name;
        }
    }

    class GetLoc extends AsyncTask<ArrayList<Integer>, Void, LocResponse> {
        URL url;
        HttpURLConnection connection;

        @Override
        protected void onPostExecute (LocResponse  result){
            if(result.result)
            {
                if(!paused) {
                    _markLocation(result.x, result.y, result.name);
                }
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Failed to send to server, retrying in 4 sec.",
                        Toast.LENGTH_SHORT).show();
            }

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if(!paused)
                    {
                        runWifi();
                    }
                }
            }, 4000);

        }
        @Override
        protected LocResponse doInBackground(ArrayList<Integer> ... f) {
            try {
                url = new URL(SERVER_URL + "/location");
                connection = (HttpURLConnection) url.openConnection();
                // set connection to allow input
                connection.setDoInput(true);
                connection.setDoOutput(true);
                // set the request method to POST
                connection.setRequestMethod("POST");
                // set content-type property
                connection.setRequestProperty("Content-Type", "application/json");
                // set charset property to utf-8
                connection.setRequestProperty("charset", "utf-8");
                // set accept property
                connection.setRequestProperty("Accept", "application/json");
                // connect to server
                connection.connect();

                JSONObject data = new JSONObject();
                data.put("features", new JSONArray(f[0]));

                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                // write JSON body to the output stream
                outputStream.write(data.toString().getBytes("utf-8"));
                // flush to ensure all data in the stream is sent
                outputStream.flush();
                // close stream
                outputStream.close();
                // receive the response from server
                return getResponseFromServer(connection);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new LocResponse(0,0,false);

        }
        private LocResponse getResponseFromServer(HttpURLConnection connection){
            // create StringBuilder object to append the input stream in
            StringBuilder sb = new StringBuilder();
            String line;
            try{
                // get input stream
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                // append stream in a the StringBuilder object
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                reader.close();
                // convert StringBuilder object to string and store it in a variable
                String JSONResponse = sb.toString();
                // convert response to JSONObject
                JSONObject response = new JSONObject(JSONResponse);

                // checks if an error is in the response
                if (response.has("error")) {
                    Log.d(Configure.class.getSimpleName(), JSONResponse);
                    return new LocResponse(0,0,false);
                }


                return new LocResponse((float)response.getDouble("x") ,
                        (float)response.getDouble("y"),response.getString("name"), true);
            }

            catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return new LocResponse(0,0,false);
        }
    }

}





