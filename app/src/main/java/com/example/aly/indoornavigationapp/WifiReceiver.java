package com.example.aly.indoornavigationapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aly on 09/05/16.
 */
public class WifiReceiver extends BroadcastReceiver {
    WifiManager mainWifi;
    ArrayAdapter<String> arrayAdapterLV;
    TextView textViewDebug;
    List<ScanResult> wifiList;
    StringBuilder sb;
    DatabaseHelper db;
    int place;

    public TextView getTextViewDebug() {
        return textViewDebug;
    }

    public void setTextViewDebug(TextView textViewDebug) {
        this.textViewDebug = textViewDebug;
    }

    public ArrayAdapter<String> getLVArrayAdapter() {
        return arrayAdapterLV;
    }

    public void setLVArrayAdapter(ArrayAdapter<String> arrayAdapter) {
        this.arrayAdapterLV = arrayAdapter;
    }

    // This method call when number of wifi connections changed
    public void onReceive(Context c, Intent intent) {
        wifiList = mainWifi.getScanResults();
        sb.append("\n        Number Of Wifi connections :" + wifiList.size() + "\n\n");
        textViewDebug.setText(sb);
        for (int i = 0; i < wifiList.size(); i++) {
            arrayAdapterLV.add(wifiList.get(i).SSID);
        }


        //doSomething();
        //recordFootPrint();
        //Toast.makeText(c,"Foot Print Added",Toast.LENGTH_SHORT).show();
        //   fillArrayAdapter();
    }

    public void recordFootPrint() {
        int[] x = new int[2];
        for (ScanResult result : wifiList) {
            if (result.BSSID.equals("EC:8A:4C:98:F3:15")) { //OSAMA
                x[0] = result.level;
            } else if (result.BSSID.equals("14:CC:20:AD:01:CA")) { //Ahmed
                x[1] = result.level;
            }

        }
        sb.append(x[0] + " " + x[1] + " : " + place);
        db.addFingerPrint(place, x);
        textViewDebug.setText(sb);

    }

    /*
        public void addWAPsToDatabase(DatabaseHelper db) {
            for (int i = 0; i < wifiList.size(); i++) {
                db.addWAP(wifiList.get(i).BSSID, wifiList.get(i).SSID);
            }

        }
    */
    public void fillArrayAdapter() {
        for (int i = 0; i < wifiList.size(); i++) {
            arrayAdapterLV.add(wifiList.get(i).SSID);
        }
    }

    // Calcuate Distance From WIFI Access Point
    public double calculateDistance(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }


    public StringBuilder getSb() {
        return sb;
    }

    public void setMainWifi(WifiManager mainWifi) {
        this.mainWifi = mainWifi;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }


}
