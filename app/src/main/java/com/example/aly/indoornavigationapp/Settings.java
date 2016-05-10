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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

import android.os.HandlerThread;

public class Settings extends AppCompatActivity {
    DatabaseHelper db;
    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    Spinner placesSpinner;
    List<ScanResult> wifiList;
    boolean isRecord = false;
    private ArrayAdapter<String> arrayAdapter;
    private TextView debugView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        db = new DatabaseHelper(getApplicationContext());
        placesSpinner = (Spinner) findViewById(R.id.placesSpinner);
        loadSpinnerData();


/*
        Button wapBtn = (Button) findViewById(R.id.wapBtn);
        wapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.this, WAP.class);
                startActivity(intent);
            }
        });

        Button placesBtn = (Button) findViewById(R.id.placesBtn);
        placesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.this, Places.class);
                startActivity(intent);
            }
        });

        Button configureButton = (Button) findViewById(R.id.configureBtn);
        configureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.createDataSetTable();
            }
        });
*/

        //TODO send items to fill when wifichanged


        final ListView lv = (ListView) findViewById(R.id.listView);
        // lv.setItemsCanFocus(false);
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_multiple_choice);
        lv.setAdapter(arrayAdapter);

        Button recordButton = (Button) findViewById(R.id.recordBtn);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecord = true;
                //TODO Capture Record every 1 SEC
                int place = placesSpinner.getSelectedItemPosition();
                // receiverWifi.setPlace(place);
                mainWifi.startScan();
            }
        });

        debugView = (TextView) findViewById(R.id.textView4);
        Button configureButton = (Button) findViewById(R.id.confBtn);

        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        configureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainWifi.isWifiEnabled() == false) {
                    // If wifi disabled then enable it
                    Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled",
                            Toast.LENGTH_LONG).show();

                    mainWifi.setWifiEnabled(true);
                }

                isRecord = false;
                // wifi scaned value broadcast receiver
                receiverWifi = new WifiReceiver();

                // Register broadcast receiver
                // Broacast receiver will automatically call when number of wifi connections changed
                registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                arrayAdapter.clear();
                mainWifi.startScan();
                debugView.setText("Starting Scan...");

            }
        });

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

        Button placeConfButton = (Button) findViewById(R.id.placesConf);
        placeConfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.this, Places.class);
                startActivity(i);
            }
        });


        Button createBtn = (Button) findViewById(R.id.createBtn);
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Integer> featuresIDs = new ArrayList<Integer>();
                SparseBooleanArray checked = lv.getCheckedItemPositions();

                for (int i = 0; i < lv.getAdapter().getCount(); i++) {
                    if (checked.get(i)) {
                        String name = arrayAdapter.getItem(i).toString();
                        int pos = 5;
                        featuresIDs.add(pos);
                    }
                }
                db.createDataSetTable(featuresIDs);
            }
        });



    }

    private void loadSpinnerData() {
        // Spinner Drop down elements
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item);
        placesSpinner.setAdapter(dataAdapter);

        final Cursor cursor = db.fetchAllPlaces();
        while (!cursor.isAfterLast()) {
            dataAdapter.add(cursor.getString(0));
            cursor.moveToNext();
        }

    }

    class WifiReceiver extends BroadcastReceiver {
        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {
            wifiList = mainWifi.getScanResults();
            if (isRecord) {

                //TODO be dynamic by selecting WIFI AccessPoints from list  view
                int place = placesSpinner.getSelectedItemPosition();
                int[] x = new int[2];
                for (ScanResult result : wifiList) {
                    if (result.SSID.equals("Osama")) { //OSAMA
                        x[0] = result.level;
                    } else if (result.SSID.equals("TE-Data")) { //TE-DATA
                        x[1] = result.level;
                    }

                }
                db.addFingerPrint(place, x);
                Toast.makeText(getApplicationContext(), "Footprint Added", Toast.LENGTH_SHORT).show();

            } else {
                for (int i = 0; i < wifiList.size(); i++) {
                    arrayAdapter.add(wifiList.get(i).SSID);
                }
                debugView.setText("WAP found is" + wifiList.size());
            }
        }
    }
}
