package com.example.aly.indoornavigationapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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


        Button addWAPs = (Button) findViewById(R.id.addWapBtn);
        addWAPs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray checked = lv.getCheckedItemPositions();

                for (int i = 0; i < lv.getAdapter().getCount(); i++) {
                    if (checked.get(i)) {
                        String name = arrayAdapter.getItem(i).toString();
                        for (ScanResult result : wifiList) {
                            if (result.SSID.equals(name)) {
                                db.addWAP(result.BSSID, result.SSID, result.frequency);
                            }
                        }
                    }
                }
                Toast.makeText(getApplicationContext(), "WAPS Added", Toast.LENGTH_SHORT).show();

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
