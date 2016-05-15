package com.example.aly.indoornavigationapp;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AddPlace extends AppCompatActivity {
     ArrayAdapter<String>  placesAdapter;
     ListView placesList;
    DatabaseHelper helper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Button deleteBtn = (Button) findViewById(R.id.deletePlace);
       final ListView placesList = (ListView) findViewById(R.id.placesListView);
        placesAdapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1);
        placesList.setAdapter(placesAdapter);

        helper = new DatabaseHelper(getApplicationContext());
        Cursor cursor = helper.fetchAllPlaces();
        while(!cursor.isAfterLast())
        {
            placesAdapter.add(cursor.getString(0));
            cursor.moveToNext();
        }

        placesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, final View arg1, int arg2, long arg3) {
                arg1.setBackgroundColor(Color.parseColor("#3079ab") );
                deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        helper.deletePlace(String.valueOf(arg1));
                        Toast.makeText(getApplicationContext(),"deleting"+((TextView)arg1).getText().toString(),Toast.LENGTH_LONG).show();
                        String s =((TextView) arg1).getText().toString();
                        helper.deletePlace(s);

                    }
                });
            }
        });
        onRestart();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onRestart() {
        placesAdapter.clear();
        Cursor cursor = helper.fetchAllPlaces();
        while (!cursor.isAfterLast()){
            placesAdapter.add(cursor.getString(0));
            cursor.moveToNext();
        }
        super.onRestart();
    }
}
