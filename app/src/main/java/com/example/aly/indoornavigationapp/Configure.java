package com.example.aly.indoornavigationapp;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Configure extends AppCompatActivity {
    DatabaseHelper db;
    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    Spinner placesSpinner;
    List<ScanResult> wifiList;
    private static final String SERVER_URL = "http://indoor-balloonmail.rhcloud.com";

    boolean isRecord = false;
    ArrayList<Integer> featuresIDs;
    ArrayAdapter<String> dataAdapter;
    private ArrayAdapter<String> arrayAdapter;
    private ListView lv;

    @Override
    protected void onResume() {
        super.onResume();

        loadListViewData();
        loadSpinnerData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        db = new DatabaseHelper(getApplicationContext());
        placesSpinner = (Spinner) findViewById(R.id.placesSpinner);
        loadSpinnerData();

        //TODO send items to fill when wifichanged

        lv = (ListView) findViewById(R.id.listView);
        // lv.setItemsCanFocus(false);
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        loadListViewData();


        Button recordButton = (Button) findViewById(R.id.recordBtn);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Capture Record every 1 SEC
                if (mainWifi.isWifiEnabled() == false) {
                    // If wifi disabled then enable it
                    Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled",
                            Toast.LENGTH_LONG).show();

                    mainWifi.setWifiEnabled(true);
                }
                receiverWifi.need = true;
                mainWifi.startScan();
            }
        });


        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);


        Button exportButton = (Button) findViewById(R.id.exportBtn);
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SendDataSet().execute();

            }
        });


        Button createBtn = (Button) findViewById(R.id.createBtn);
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                featuresIDs = new ArrayList<Integer>();
                SparseBooleanArray checked = lv.getCheckedItemPositions();

                for (int i = 0; i < lv.getAdapter().getCount(); i++) {
                    if (checked.get(i)) {
                        String name = arrayAdapter.getItem(i).toString();
                        int pos = db.getWAPID(name);
                        featuresIDs.add(pos);
                    }
                }
                db.createDataSetTable(featuresIDs);
                Toast.makeText(getApplicationContext(), "Database Created", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if(receiverWifi != null)
            unregisterReceiver(receiverWifi);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        // wifi scaned value broadcast receiver
        receiverWifi = new WifiReceiver();
        // Register broadcast receiver
        // Broacast receiver will automatically call when number of wifi connections changed
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    protected void onPause()
    {
        super.onPause();

    }
    private void loadSpinnerData() {
        // Spinner Drop down elements
        // Creating adapter for spinner
        dataAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item);
        placesSpinner.setAdapter(dataAdapter);

        final Cursor cursor = db.fetchAllPlaces();
        if (cursor != null) {
            while (!cursor.isAfterLast()) {
                dataAdapter.add(cursor.getString(0));
                cursor.moveToNext();
            }
        }
    }

    private void loadListViewData() {
        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_multiple_choice);
        lv.setAdapter(arrayAdapter);


        final Cursor cursor = db.fetchAllWAPS();
        if (cursor != null) {
            while (!cursor.isAfterLast()) {
                arrayAdapter.add(cursor.getString(2));
                cursor.moveToNext();
            }
        }

    }

    class WifiReceiver extends BroadcastReceiver {

        boolean need = false;
        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {
            if(need)
            {
                wifiList = mainWifi.getScanResults();
                addFootPrint();
                need = false;
            }

        }


        private void addFootPrint() {
            String placeName = (String) placesSpinner.getSelectedItem();
            int place = db.getPlaceID(placeName);

            HashMap x = new HashMap();
            String[] Columns = db.getDataSetColumns();

            HashMap map = new HashMap();
            for (int j = 0; j < Columns.length - 1; j++) {
                int id = Integer.parseInt(Columns[j].substring(1));
                map.put(db.getWAPSSID(id), id);
            }
            for (ScanResult result : wifiList) {
                if (map.containsKey(result.SSID)) {
                    x.put(map.get(result.SSID), result.level);
                }
            }


            db.addFingerPrint(place, x);
            Toast.makeText(getApplicationContext(), "Footprint Added " + placeName, Toast.LENGTH_SHORT).show();

        }
    }

    class SendDataSet extends AsyncTask<Void, Void, Boolean> {
        URL url;
        HttpURLConnection connection;
        ProgressDialog dialog;
        @Override
        protected void onPreExecute (){
            dialog = new ProgressDialog(Configure.this);
            dialog.setIndeterminate(true);
            dialog.setMessage("Sending...");
            dialog.show();
        }

        @Override
        protected void onPostExecute (Boolean  result){
            dialog.hide();
            if(result)
            {
                Toast.makeText(getApplicationContext(), "Data sent to server",
                    Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Failed to send to server",
                        Toast.LENGTH_LONG).show();
            }
        }
        @Override
        protected Boolean doInBackground(Void ... f) {
            try {
                JSONObject data = getData();
                if(data == null)
                {
                    return false;
                }

                url = new URL(SERVER_URL + "/dataset");
                connection = (HttpURLConnection) url.openConnection();
                // set connection to allow output
                connection.setDoOutput(true);
                // set connection to allow input
                connection.setDoInput(true);
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
            }
            return false;

        }
        private JSONObject getData(){
            SQLiteDatabase db = Configure.this.db.getReadableDatabase();
            JSONObject data_json = null;
            JSONArray feature_json = new JSONArray();
            JSONArray classes_json = null;
            JSONObject places = new JSONObject();
            JSONObject waps = new JSONObject();
            ArrayList<Integer> classes = new ArrayList<>();
            try{
                String[] rowDetails = Configure.this.db.getDataSetColumns();
                Cursor c = db.query(Configure.this.db.DATASET_TABLE_NAME, rowDetails, null, null, null, null, null);
                if (c != null) {
                    if (c.moveToFirst()) {
                        do {
                            ArrayList<Integer> new_array = new ArrayList<>();
                            for (int j = 0; j < rowDetails.length-1; j++) {
                                new_array.add(j, c.getInt(j));
                            }
                            feature_json.put(new JSONArray(new_array));
                            classes.add(c.getInt(rowDetails.length-1));
                        }
                        while (c.moveToNext());

                        classes_json = new JSONArray(classes);


                    }
                    c.close();



                    if(c.getCount() > 0)
                    {
                        c = db.rawQuery("Select ID,X,Y,Name from Places;", null);
                        if (c == null || c.getCount() == 0) {
                            return null;
                        }
                        c.moveToFirst();
                        do {
                            JSONObject loc = new JSONObject();
                            loc.put("x", c.getFloat(1));
                            loc.put("y", c.getFloat(2));
                            loc.put("name", c.getString(3));
                            places.put(Integer.toString(c.getInt(0)), loc);
                        }
                        while (c.moveToNext());
                        c.close();
                        String[] Columns = Configure.this.db.getDataSetColumns();

                        for (int j = 0; j < Columns.length - 1; j++) {
                            int id = Integer.parseInt(Columns[j].substring(1));
                            waps.put(Configure.this.db.getWAPSSID(id), id);
                        }


                        data_json = new JSONObject();
                        data_json.put("features", feature_json);
                        data_json.put("classes", classes_json);
                        data_json.put("waps", waps);
                        data_json.put("places", places);
                        return data_json;
                    }
                    else
                    {
                        return null;
                    }

                }
            } catch (SQLiteException se) {
                Log.e(getClass().getSimpleName(), "Could not create or Open the database");
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                db.close();
            }
            return data_json;
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




}
