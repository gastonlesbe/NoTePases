package com.gaston.lesbegueris.notepases;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.location.LocationRequest;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.gaston.lesbegueris.notepases.util.DistanceFormatter;
import com.gaston.lesbegueris.notepases.util.AppodealHelper;
import androidx.viewpager2.widget.ViewPager2;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton fab1;
    boolean trakingOn = false;
    private static final int LOCATION_REQUEST_CODE = 1;
    ActionBar actionBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MaterialToolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        myToolbar.inflateMenu(R.menu.menu);
        myToolbar.setTitleTextColor(getResources().getColor(R.color.colorOnPrimary));
        
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



        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new MainTabsAdapter(this));
        viewPager.setOffscreenPageLimit(2);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(R.string.ultimos);
            } else {
                tab.setText(R.string.favoritas);
            }
        }).attach();


        fab1 =(FloatingActionButton)findViewById(R.id.fab1);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findPlace();
            }
        });

    }


    public void findPlace() {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_location, null);
        dialogBuilder.setView(dialogView);
        AlertDialog dialog = dialogBuilder.create();

        View btnClose = dialogView.findViewById(R.id.btnCloseDialog);
        View btnSelectOnMap = dialogView.findViewById(R.id.btnSelectOnMap);
        View btnEnterAddress = dialogView.findViewById(R.id.btnEnterAddress);

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnSelectOnMap.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(MainActivity.this, MapsActivity2.class);
            startActivity(intent);
        });
        btnEnterAddress.setOnClickListener(v -> {
            dialog.dismiss();
            showAddressInputDialog();
        });

        dialog.show();
    }
    
    private void showAddressInputDialog() {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);

        TextInputLayout inputLayout = new TextInputLayout(this);
        inputLayout.setHint("Ingresa una dirección");
        inputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);

        final TextInputEditText edtAddress = new TextInputEditText(this);
        edtAddress.setPadding(50, 20, 50, 20);
        inputLayout.addView(edtAddress);

        dialogBuilder.setView(inputLayout);
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
        if (id == R.id.units) {
            showUnitsDialog();
            return true;
        }
        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item);
    }

    private void showUnitsDialog() {
        final String[] values = new String[] { "system", "metric", "imperial" };
        final String[] labels = new String[] {
                getString(R.string.units_system),
                getString(R.string.units_metric),
                getString(R.string.units_imperial)
        };

        String current = DistanceFormatter.getUnitsPreference(this);
        int checked = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(current)) {
                checked = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.units_title)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    DistanceFormatter.setUnitsPreference(this, values[which]);
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    // A place has been received; use requestCode to track the request.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO: Places API has been deprecated. This functionality needs to be reimplemented.
        // For now, this method is disabled.
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Mostrar el banner cada vez que la actividad se reanuda
        if (AppodealHelper.isInitialized()) {
            AppodealHelper.showBanner(this, R.id.adView);
        }
    }

    private void initAppodeal() {
        // Usar la clave de Appodeal desde strings.xml
        String appodealAppKey = getString(R.string.appodeal_app_key);
        AppodealHelper.initialize(this, appodealAppKey);
        AppodealHelper.showBanner(this, R.id.adView);
    }
}

