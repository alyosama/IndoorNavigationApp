package com.example.aly.indoornavigationapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView mainText;
    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    StringBuilder sb = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button postBtn=(Button)findViewById(R.id.postBtn);
        mainText=(TextView)findViewById(R.id.textView);

        // Initiate wifi service manager
        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for wifi is disabled
                if (mainWifi.isWifiEnabled() == false)
                {
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
                mainText.setText("Starting Scan...");

            }
        });
    }

    class WifiReceiver extends BroadcastReceiver {

        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {

            sb = new StringBuilder();
            wifiList = mainWifi.getScanResults();
            sb.append("\n        Number Of Wifi connections :"+wifiList.size()+"\n\n");

            for(int i = 0; i < wifiList.size(); i++){

                sb.append(new Integer(i+1).toString() + ". "+wifiList.get(i).SSID+" : ");
                double distance=calculateDistance(wifiList.get(i).level,wifiList.get(i).frequency);
                sb.append(String.format("%.4f", distance));
                sb.append(" meters\n\n");
            }


            mainText.setText(sb);
        }

        // Calcuate Distance From WIFI Access Point
        public double calculateDistance(double signalLevelInDb,double freqInMHz){
            double exp=(27.55-(20*Math.log10(freqInMHz))+Math.abs(signalLevelInDb))/20.0;
            return Math.pow(10.0,exp);
        }

    }

}
