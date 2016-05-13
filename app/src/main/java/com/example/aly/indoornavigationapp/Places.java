package com.example.aly.indoornavigationapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class Places extends AppCompatActivity {
    //problems : no way to find what order will it be, so how to save in a known pattern
    // wrong values come out of database, must fix that

    float[] viewCoords = new float[2];
    int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places1);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DatabaseHelper helper = new DatabaseHelper(this);

        final ImageView map = (ImageView) findViewById(R.id.map);
        final ImageView cross = (ImageView) findViewById(R.id.cross);
        FloatingActionButton addPlaceBtn = (FloatingActionButton) findViewById(R.id.addLocationBtn);

        final EditText placeTxt = (EditText) findViewById(R.id.namePlaceTxt);
        Toast.makeText(Places.this, "press the rooms in the order they're printed to define the rooms in the correct order please", Toast.LENGTH_SHORT).show();


        addPlaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.addPlace(placeTxt.getText().toString(), ++count, viewCoords);
                Toast.makeText(Places.this, viewCoords[0] + "," + viewCoords[1] + " added", Toast.LENGTH_SHORT).show();

            }
        });
        final int[] mapCoords = new int[2];
        map.getLocationOnScreen(mapCoords);

        map.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {


                Log.v(" x = ", event.getX() + "");
                Log.v("y = ", event.getY() + "");
                int touchX = (int) event.getX();
                int touchY = (int) event.getY();

                viewCoords[0] = touchX - mapCoords[0];
                viewCoords[1] = touchY - mapCoords[1];

                cross.setX(viewCoords[0]);
                cross.setY(viewCoords[1]);

                cross.setVisibility(View.VISIBLE);

                return false;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_locations_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = null;
        switch (item.getItemId()) {
            case R.id.view:
                i = new Intent(this, Places.class);
                startActivity(i);
                return true;
        }
        return false;
    }
}
