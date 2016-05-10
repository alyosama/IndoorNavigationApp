package com.example.aly.indoornavigationapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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
        setContentView(R.layout.activity_places);

        //final HashMap<Integer, float[]> keyLocations = new HashMap<Integer, float[]>();
        final DatabaseHelper helper = new DatabaseHelper(this);

        //helper.clearDatabase();

        final ImageView map = (ImageView) findViewById(R.id.map);
        final ImageView cross = (ImageView) findViewById(R.id.cross);
        Button addPlaceBtn = (Button) findViewById(R.id.addPlaceBtn);

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
                //keyLocations.put(++count, viewCoords);

                return false;
            }
        });
    }
}
