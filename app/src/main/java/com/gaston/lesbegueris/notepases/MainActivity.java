package com.gaston.lesbegueris.notepases;

import android.app.AlertDialog;
import android.app.LocalActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.ColorInt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.location.LocationRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gaston.lesbegueris.notepases.util.AppodealHelper;

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
        
        // Inicializar Appodeal (mismo método que en Caretemplate)
        initAppodeal();



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
        
        // Solicitar permiso de notificaciones para Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        LOCATION_REQUEST_CODE + 1);
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
        spec.setIndicator(getString(R.string.ultimos)).setContent(intent);
        intent.putExtra("address", address);
        intent.putExtra("nombre", nombre);
        spec.setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);

        TabHost.TabSpec spec2 = tabHost.newTabSpec("tab_creation");
        // text and image of tab
        Intent e = new Intent().setClass(this, Tab2.class);
        spec2.setIndicator(getString(R.string.favoritas)).setContent(intent);


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
        // Abrir diálogo para ingresar dirección o ir directamente al mapa
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Agregar Ubicación");
        dialogBuilder.setMessage("¿Cómo deseas agregar la ubicación?");
        
        dialogBuilder.setPositiveButton("Seleccionar en el mapa", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Abrir MapsActivity2 para seleccionar ubicación en el mapa
                Intent intent = new Intent(MainActivity.this, MapsActivity2.class);
                startActivity(intent);
            }
        });
        
        dialogBuilder.setNeutralButton("Ingresar dirección", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Mostrar diálogo para ingresar dirección
                showAddressInputDialog();
            }
        });
        
        dialogBuilder.setNegativeButton("Cancelar", null);
        dialogBuilder.show();
    }
    
    private void showAddressInputDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(android.R.layout.simple_list_item_1, null);
        
        final EditText edtAddress = new EditText(this);
        edtAddress.setHint("Ingresa una dirección");
        edtAddress.setPadding(50, 20, 50, 20);
        
        dialogBuilder.setView(edtAddress);
        dialogBuilder.setTitle("Ingresar Dirección");
        dialogBuilder.setPositiveButton("Buscar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String address = edtAddress.getText().toString();
                if (!address.isEmpty()) {
                    geocodeAddress(address);
                } else {
                    Toast.makeText(MainActivity.this, "Por favor ingresa una dirección", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialogBuilder.setNegativeButton("Cancelar", null);
        dialogBuilder.show();
    }
    
    private void geocodeAddress(String addressStr) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(addressStr, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                double latitude = address.getLatitude();
                double longitude = address.getLongitude();
                String fullAddress = address.getAddressLine(0);
                String placeName = address.getFeatureName();
                
                if (placeName == null || placeName.isEmpty()) {
                    placeName = address.getThoroughfare();
                }
                if (placeName == null || placeName.isEmpty()) {
                    placeName = "Ubicación";
                }
                
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("latitude2", latitude);
                intent.putExtra("longitude2", longitude);
                intent.putExtra("name", placeName);
                intent.putExtra("address", fullAddress);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No se encontró la dirección. Intenta con otra dirección o selecciona en el mapa.", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al buscar la dirección. Intenta seleccionar en el mapa.", Toast.LENGTH_LONG).show();
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
        int id = item.getItemId();
        if (id == R.id.help) {
            // User chose the "Settings" item, show the app settings UI...
            Intent e = new Intent(MainActivity.this, TutoActivity.class);
            startActivity(e);
            finish();
            return true;
        }
        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item);
    }

    // A place has been received; use requestCode to track the request.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO: Places API has been deprecated. This functionality needs to be reimplemented.
        // For now, this method is disabled.
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initAppodeal() {
        // Usar la clave de Appodeal desde strings.xml
        String appodealAppKey = getString(R.string.appodeal_app_key);
        AppodealHelper.initialize(this, appodealAppKey);
        AppodealHelper.showBanner(this, R.id.adView);
    }
}

