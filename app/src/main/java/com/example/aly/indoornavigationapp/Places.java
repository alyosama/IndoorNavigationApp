package com.example.aly.indoornavigationapp;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Hashtable;

public class Places extends AppCompatActivity {

    //problems : no way to find what order will it be, so how to save in a known pattern
    // wrong values come out of database, must fix that

    float[] viewCoords = new float[2];
    int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //final HashMap<Integer, float[]> keyLocations = new HashMap<Integer, float[]>();
        final DatabaseHelper helper = new DatabaseHelper(this);
        count = 0;

        //helper.clearDatabase();

        final ImageView map = (ImageView) findViewById(R.id.map);
        final ImageView cross = (ImageView) findViewById(R.id.cross);
        Button doneBtn = (Button) findViewById(R.id.addBtn);

        final EditText placeTxt = (EditText) findViewById(R.id.namePlaceTxt);
        Toast.makeText(Places.this, "press the rooms in the order they're printed to define the rooms in the correct order please", Toast.LENGTH_SHORT).show();

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //create a sqlite database to save the coordinates
                Cursor cursor = helper.fetchAllPlaces();
                while (!cursor.isAfterLast()) {
                    Log.v("x center", String.valueOf(cursor.getFloat(2)));
                    Log.v("y center", String.valueOf(cursor.getFloat(3)));
                    cursor.moveToNext();
                }
                //Log.v("main", keyLocations.toString());
            }
        });

        map.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                Log.v(" x = ", event.getX() + "");
                Log.v("y = ", event.getY() + "");
                viewCoords[0] = event.getX();
                viewCoords[1] = event.getY();

                cross.setX(viewCoords[0]);
                cross.setY(viewCoords[1]);

                //keyLocations.put(++count, viewCoords);

                helper.addPlace(placeTxt.getText().toString(), ++count, viewCoords);
                Toast.makeText(Places.this, viewCoords[0] + "," + viewCoords[1] + " added", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }
}
