package com.example.aly.indoornavigationapp;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class Places extends AppCompatActivity {
    DatabaseHelper db;
    ArrayAdapter<String> arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        final EditText namePlaceTxt = (EditText) findViewById(R.id.namePlaceTxt);
        Button addPlaceBtn = (Button) findViewById(R.id.addPlaceBtn);
        ListView placesListView = (ListView) findViewById(R.id.placesListView);

        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1);
        placesListView.setAdapter(arrayAdapter);

        db = new DatabaseHelper(getApplicationContext());

        final Cursor cursor = db.fetchAllPlaces();
        while (!cursor.isAfterLast()) {
            arrayAdapter.add(cursor.getString(0));
            cursor.moveToNext();
        }

        addPlaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String PlaceName = namePlaceTxt.getText().toString();
                db.addPlace(PlaceName);
                namePlaceTxt.getText().clear();
                arrayAdapter.add(PlaceName);
                Toast.makeText(getApplicationContext(), "Place Added", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
