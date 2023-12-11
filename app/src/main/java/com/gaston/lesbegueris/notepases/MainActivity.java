package com.gaston.lesbegueris.notepases;

import android.app.LocalActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.LocationRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.widget.TabHost.OnTabChangeListener;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements OnTabChangeListener {

    FloatingActionButton fab1;
    boolean trakingOn = false;
    private static final int LOCATION_REQUEST_CODE = 1;
    private TabHost tabHost;

    SimpleAdapter adapter;
    private DbHelper helper;
    private SQLiteDatabase db;
    private DataBaseManager manager;
    private Cursor cursor;
    ActionBar actionBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        myToolbar.inflateMenu(R.menu.menu);
        myToolbar.setTitleTextColor(getResources().getColor(R.color.white));



        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Mostrar diálogo explicativo
            } else {
                // Solicitar permiso
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_REQUEST_CODE);
            }
        }



        String nombre = getIntent().getStringExtra("nombre");
        String address = getIntent().getStringExtra("address");
        tabHost =(TabHost) findViewById(R.id.tabHost);

        //

        Resources res = getResources();
        LocalActivityManager mlam = new LocalActivityManager(this, false);
        tabHost = (TabHost) findViewById(R.id.tabHost);
        mlam.dispatchCreate(savedInstanceState);
        tabHost.setup(mlam);

        TabHost.TabSpec spec = tabHost.newTabSpec("tab_creation");
        // text and image of tab
        Intent intent = new Intent().setClass(this, Tab1.class);
        spec.setIndicator(String.valueOf(R.string.ultimos)).setContent(intent);
        intent.putExtra("address", address);
        intent.putExtra("nombre", nombre);
        spec.setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);

        TabHost.TabSpec spec2 = tabHost.newTabSpec("tab_creation");
        // text and image of tab
        Intent e = new Intent().setClass(this, Tab2.class);
        spec2.setIndicator(String.valueOf(R.string.favoritas)).setContent(intent);


        // specify layout of tab
        e.putExtra("address", address);
        e.putExtra("nombre", nombre);
        spec2.setContent(e);
        // adding tab in TabHost
        tabHost.addTab(spec2);

        for(int i=0; i < tabHost.getTabWidget().getChildCount(); i++)
        {

            //tabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.shadow1);
            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title);
            //tv.setTextColor(Color.parseColor("#DDDED6"));
            tv.setTextSize(18);
            tv.setTypeface(null, Typeface.ITALIC);
            tv.setText(R.string.ultimos);

        }

        tabHost.getTabWidget().setCurrentTab(1);
        //tabHost.getTabWidget().getChildAt(1).setBackgroundResource(R.mipmap.botonazul);
        TextView tv = (TextView) tabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title);
        //tv.setTextColor(Color.parseColor("#000000"));
        tv.setTextSize(18);
        tv.setTypeface(null, Typeface.ITALIC);
        tv.setText(R.string.favoritas);


        fab1 =(FloatingActionButton)findViewById(R.id.fab1);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findPlace();
            }
        });

    }


    public void findPlace() {
        try {
            Intent intent =
                    new PlaceAutocomplete
                            .IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, 1);
            //finish();
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
   // @Override
    public void onTabChanged(String tabId) {
        // TODO Auto-generated method stub
        for(int i=0; i < tabHost.getTabWidget().getChildCount(); i++)

        {
            tabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor(
                    "#6674c4"));
        }

        tabHost.getTabWidget().getChildAt(tabHost.getCurrentTab()).setBackgroundColor(
                Color.parseColor("#6674c4"));
    }
    //@Override
    public void onPageSelected(int position) {
        for(int i=0; i < tabHost.getChildCount(); i++){
            TextView tv = (TextView) tabHost.getChildAt(i);
            if(i == position){
                tv.setBackgroundColor(Color.GRAY);
            } else {
                tv.setBackgroundColor(Color.WHITE);
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help:
                // User chose the "Settings" item, show the app settings UI...
                Intent e = new Intent(MainActivity.this, TutoActivity.class);
                startActivity(e);
                finish();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    // A place has been received; use requestCode to track the request.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.e("Tag", "Place: " + place.getAddress() + place.getPhoneNumber() + place.getLatLng().latitude);




                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("latitude2",place.getLatLng().latitude);
                intent.putExtra("longitude2",place.getLatLng().longitude);
                intent.putExtra("name",place.getName());
                intent.putExtra("address",place.getAddress());
               // intent.putExtra("trakingOn", trakingOn);
                startActivity(intent);
                finish();


//                        ((TextView) findViewById(R.id.searched_address)).setText(place.getName() + ",\n" +
//                        place.getAddress() + "\n" + place.getPhoneNumber());

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.e("Tag", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}

