package com.gaston.lesbegueris.notepases;

import android.content.Intent;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * A styled map using JSON styles from a raw resource.
 */
public class MapsActivity2 extends AppCompatActivity
        implements OnMapReadyCallback {

    private static final String TAG = MapsActivity2.class.getSimpleName();
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LatLng selectedLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        // Get the SupportMapFragment and register for the callback
        // when the map is ready for use.
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        
        // Configurar botón de cerrar
        ImageButton btnClose = findViewById(R.id.btnClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

    }

    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready for use.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "Map is ready");
        
        // Habilitar controles del mapa
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false); // Deshabilitar para evitar conflictos
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        
        // NO aplicar estilo del mapa ya que puede estar bloqueando los eventos de click
        // Comentamos el estilo para que los clicks funcionen
        /*
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        */
        
        // Mostrar mensaje al usuario
        Toast.makeText(this, "Toca el mapa para seleccionar una ubicación", Toast.LENGTH_LONG).show();
        
        // Permitir al usuario tocar el mapa para seleccionar una ubicación
        // Usar un marcador arrastrable como alternativa más confiable
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                handleLocationSelection(latLng);
            }
        });
        
        // También permitir clic largo como alternativa
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                handleLocationSelection(latLng);
            }
        });
        
        // Centrar el mapa en la ubicación actual si está disponible
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<android.location.Location>() {
                    @Override
                    public void onComplete(@NonNull Task<android.location.Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            android.location.Location location = task.getResult();
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        } else {
                            // Si no se puede obtener la ubicación, centrar en una ubicación por defecto
                            // (puedes cambiar estas coordenadas a una ubicación por defecto)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-34.6037, -58.3816), 10));
                        }
                    }
                });
    }
    
    private void handleLocationSelection(LatLng latLng) {
        Log.d(TAG, "Map clicked at: " + latLng.latitude + ", " + latLng.longitude);
        Toast.makeText(MapsActivity2.this, "Ubicación seleccionada: " + latLng.latitude + ", " + latLng.longitude, Toast.LENGTH_SHORT).show();
        
        // Agregar marcador en la ubicación seleccionada
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title("Ubicación seleccionada"));
        
        // Guardar la ubicación seleccionada
        selectedLocation = latLng;
        
        // Mostrar mensaje de procesamiento
        Toast.makeText(MapsActivity2.this, "Obteniendo dirección...", Toast.LENGTH_SHORT).show();
        
        // Obtener dirección usando Geocoder
        Geocoder geocoder = new Geocoder(MapsActivity2.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String fullAddress = address.getAddressLine(0);
                if (fullAddress == null || fullAddress.isEmpty()) {
                    fullAddress = address.getAddressLine(1);
                }
                if (fullAddress == null || fullAddress.isEmpty()) {
                    fullAddress = latLng.latitude + ", " + latLng.longitude;
                }
                
                String placeName = address.getFeatureName();
                if (placeName == null || placeName.isEmpty()) {
                    placeName = address.getThoroughfare();
                }
                if (placeName == null || placeName.isEmpty()) {
                    placeName = address.getSubThoroughfare();
                }
                if (placeName == null || placeName.isEmpty()) {
                    placeName = "Ubicación";
                }
                
                Log.d(TAG, "Address found: " + fullAddress);
                
                // Abrir MapsActivity con la ubicación seleccionada
                Intent intent = new Intent(MapsActivity2.this, MapsActivity.class);
                intent.putExtra("latitude2", latLng.latitude);
                intent.putExtra("longitude2", latLng.longitude);
                intent.putExtra("name", placeName);
                intent.putExtra("address", fullAddress);
                startActivity(intent);
                finish();
            } else {
                Log.d(TAG, "No address found, using coordinates");
                // Si no se puede obtener la dirección, usar coordenadas
                Intent intent = new Intent(MapsActivity2.this, MapsActivity.class);
                intent.putExtra("latitude2", latLng.latitude);
                intent.putExtra("longitude2", latLng.longitude);
                intent.putExtra("name", "Ubicación");
                intent.putExtra("address", latLng.latitude + ", " + latLng.longitude);
                startActivity(intent);
                finish();
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoding error: ", e);
            e.printStackTrace();
            // Si hay error en geocodificación, usar coordenadas directamente
            Intent intent = new Intent(MapsActivity2.this, MapsActivity.class);
            intent.putExtra("latitude2", latLng.latitude);
            intent.putExtra("longitude2", latLng.longitude);
            intent.putExtra("name", "Ubicación");
            intent.putExtra("address", latLng.latitude + ", " + latLng.longitude);
            startActivity(intent);
            finish();
        }
    }


}