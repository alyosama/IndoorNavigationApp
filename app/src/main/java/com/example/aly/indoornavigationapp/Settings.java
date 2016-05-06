package com.example.aly.indoornavigationapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
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
    }
}
