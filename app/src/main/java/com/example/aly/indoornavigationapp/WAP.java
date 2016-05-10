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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WAP extends AppCompatActivity {
    DatabaseHelper db;

    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;


    ListView lv;
    ArrayAdapter<String> arrayAdapter;

    /* Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wap);

        db = new DatabaseHelper(getApplicationContext());

        lv = (ListView) findViewById(R.id.wapListView);

        Button scanBtn = (Button) findViewById(R.id.scanBtn);

        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_multiple_choice);
        lv.setAdapter(arrayAdapter);

        //TODO run this in another thread
/*
        final Cursor cursor=db.fetchAllWAPS();
        if(cursor!=null) {
            while (!cursor.isAfterLast()) {
                arrayAdapter.add(cursor.getString(1));
                cursor.moveToNext();
            }
        }
*/


        // Initiate wifi service manager
        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                arrayAdapter.clear();
                mainWifi.startScan();
                // mainText.setText("Starting Scan...");

            }
        });


    }

    class WifiReceiver extends BroadcastReceiver {
        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {
            wifiList = mainWifi.getScanResults();

            for (int i = 0; i < wifiList.size(); i++) {
                arrayAdapter.add(wifiList.get(i).SSID);
            }

        }
    }



}
