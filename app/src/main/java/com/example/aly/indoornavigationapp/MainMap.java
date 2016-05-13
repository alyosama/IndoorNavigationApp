package com.example.aly.indoornavigationapp;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import java.util.Random;

public class MainMap extends AppCompatActivity {
    FloatingActionButton findPathBtn;
    Spinner placesSpinner;
    DatabaseHelper helper;
    ArrayAdapter<String> dataAdapter;
    ImageView floorMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);
        floorMap = (ImageView) findViewById(R.id.map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        helper = new DatabaseHelper(this);


        findPathBtn = (FloatingActionButton) findViewById(R.id.pathBtn);
        placesSpinner = (Spinner) findViewById(R.id.spinner);
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
                getLocation();
                //TODO Mark this Location
            }
        };
        runnable.run();
    }

    public int getLocation() {
        Random n = new Random();
        return n.nextInt(15) + 1;
    }

    public void findPath() {

        String destination = String.valueOf(placesSpinner.getSelectedItem());
        float sourceCoord[] = helper.getPlaceLocationByName("Room10");
        float cooridor1[] = helper.getPlaceLocationByName("corridor1");
        float destCoord[] = helper.getPlaceLocationByName(destination);
        DrawLine(floorMap.getPivotX()+sourceCoord[0],floorMap.getPivotY()+sourceCoord[1] , floorMap.getPivotX()+cooridor1[0],floorMap.getPivotY()+sourceCoord[1], Color.RED);


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

}

//Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//      .setAction("Action", null).show();




