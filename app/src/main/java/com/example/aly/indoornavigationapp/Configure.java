package com.example.aly.indoornavigationapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Configure extends AppCompatActivity {
    DatabaseHelper db;
    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    Spinner placesSpinner;
    List<ScanResult> wifiList;

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

                // wifi scaned value broadcast receiver
                receiverWifi = new WifiReceiver();

                // Register broadcast receiver
                // Broacast receiver will automatically call when number of wifi connections changed
                registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                mainWifi.startScan();
            }
        });


        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);


        Button exportButton = (Button) findViewById(R.id.exportBtn);
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    db.exportTheDataSet();
                    Toast.makeText(getApplicationContext(), "Export Done", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

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
        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {
            wifiList = mainWifi.getScanResults();
            addFootPrint();

        }


        //TODO This Function Need to be Fixed
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
}
