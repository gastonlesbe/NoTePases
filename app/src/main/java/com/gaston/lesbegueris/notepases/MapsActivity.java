package com.gaston.lesbegueris.notepases;

import android.annotation.SuppressLint;
import androidx.appcompat.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.os.Build;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.gaston.lesbegueris.notepases.util.DistanceFormatter;

import com.google.android.gms.location.*;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import static java.lang.Integer.parseInt;

import com.google.android.gms.tasks.OnSuccessListener;


public class MapsActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener, View.OnClickListener {

    private static final int LOCATION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    String address, txtlatitude2, txtlongitude2, destino, name, str_id, nota;
    double latitude2, longitude2, latitude1, longitude1;
    TextView txtBuscar, txtAddress, txtStart, txtAlertUnit;
    TextInputEditText txtDistancia;
    MaterialButton btnBuscar, btnEdit, btnClose;
    protected GoogleApiClient mGoogleApiClient;

    boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    long notify_interval = 10000;
    public static String str_receiver = "servicetutorial.service.receiver";


    LatLng position, myPosition;
    double dist, yo;
    int b;
    float distance;

    private Location mLastLocation;
    public LocationManager mLocationManager;
    LocationManager locationManager;

    private Location mLocation, location;

    Marker markerYo, markerDestino;
    boolean trakingOn = false;
    private static final String TAG = "BroadcastTest";
    private Intent intent;
    int alerta;

    public static final String MyPREFERENCES = "lastLocation";
    SharedPreferences sharedpreferences;

    SimpleCursorAdapter adapter;
    private DbHelper helper;
    private SQLiteDatabase db;
    private DataBaseManager manager;
    private Cursor cursor;

    private FusedLocationProviderClient mFusedLocationClient;
    private InterstitialAd interstitialAd;

    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;


    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    private LatLng myLocation;
    private static final String CHANNEL_ID = "tracking_channel";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        MobileAds.initialize(this);
        loadInterstitial();

        mFusedLocationClient = getFusedLocationProviderClient(this);
        startLocationUpdates();
        
        createNotificationChannel();

        fn_getlocation();

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);


        latitude2 = Double.parseDouble(sharedpreferences.getString("txtlatitude2", String.valueOf(latitude2)));
        longitude2 = Double.parseDouble(sharedpreferences.getString("txtlongitude2", String.valueOf(longitude2)));
        address = sharedpreferences.getString("address", address);
        trakingOn = sharedpreferences.getBoolean("trakingOn", trakingOn);


        Intent intent = getIntent();
        if (intent != null) {
            // Obtener datos del Intent, usando los valores por defecto si no están presentes
            double intentLat = intent.getDoubleExtra("latitude2", Double.NaN);
            double intentLon = intent.getDoubleExtra("longitude2", Double.NaN);
            if (!Double.isNaN(intentLat) && !Double.isNaN(intentLon)) {
                latitude2 = intentLat;
                longitude2 = intentLon;
            }
            String intentName = intent.getStringExtra("nombre");
            if (intentName != null) {
                name = intentName;
            }
            boolean intentTraking = intent.getBooleanExtra("traking", trakingOn);
            trakingOn = intent.getBooleanExtra("trakingOn", intentTraking);
            String intentStrId = intent.getStringExtra("str_id");
            if (intentStrId != null) {
                str_id = intentStrId;
            }
            String intentNota = intent.getStringExtra("nota");
            if (intentNota != null) {
                nota = intentNota;
            }
            if (intent.getBooleanExtra("fromSelection", false)) {
                trakingOn = false;
                sharedpreferences.edit().putBoolean("trakingOn", false).apply();
            }
        }
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);


        txtBuscar = (TextView) findViewById(R.id.txtDireccion);
        txtDistancia = (TextInputEditText) findViewById(R.id.txtDistancia);
        // txtDistancia.setFocusable(false);
        txtAddress = (TextView) findViewById(R.id.txtaddress);
        txtAlertUnit = (TextView) findViewById(R.id.textView12);
        if (txtAlertUnit != null) {
            txtAlertUnit.setText(DistanceFormatter.getAlertUnit(this));
        }
        // Nombre oculto en la UI; no se muestra en este layout
        btnBuscar = (MaterialButton) findViewById(R.id.btnBuscar);
        txtStart = (TextView) findViewById(R.id.txtStart);
        btnEdit = (MaterialButton) findViewById(R.id.btnEdit);

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editarNombre();
            }
        });
        btnClose = (MaterialButton) findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrar();
            }
        });


        int LOCATION_REFRESH_TIME = 1000;
        int LOCATION_REFRESH_DISTANCE = 5;

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            return;
        }


        if (trakingOn == false) {
            btnBuscar.setIconResource(android.R.drawable.ic_media_play);
            if (txtStart != null) {
                txtStart.setText(R.string.inicia);
            }
            btnBuscar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    distancia();
                    starttraking();
                    guardarTemp();
                    startNotification();
                    mTimer = new Timer();
                    //mTimer.schedule(new TimerTaskToGetLocation(),50000,notify_interval);
                    //  intent = new Intent(str_receiver);
                    //fn_getlocation();

                    trakingOn = true;
                    // Cambiar el botón a "stop" inmediatamente
                    btnBuscar.setIconResource(android.R.drawable.ic_media_pause);
                    if (txtStart != null) {
                        txtStart.setText(R.string.detener);
                    }
                    // Actualizar el listener para que ahora detenga el tracking
                    btnBuscar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            stopTraking();
                        }
                    });
                    // Reiniciar la actividad para reflejar el cambio de estado
                    Intent intent = new Intent(MapsActivity.this, MapsActivity.class);
                    intent.putExtra("trakingOn", true);
                    intent.putExtra("traking", true);
                    intent.putExtra("fromSelection", false);
                    intent.putExtra("latitude2", latitude2);
                    intent.putExtra("longitude2", longitude2);
                    if (name != null) {
                        intent.putExtra("nombre", name);
                    }
                    if (address != null) {
                        intent.putExtra("address", address);
                    }
                    if (str_id != null) {
                        intent.putExtra("str_id", str_id);
                    }
                    if (nota != null) {
                        intent.putExtra("nota", nota);
                    }
                    finish();
                    startActivity(intent);
                }
            });
        } else {
            btnBuscar.setIconResource(android.R.drawable.ic_media_pause);
            if (txtStart != null) {
                txtStart.setText(R.string.detener);
            }
            btnBuscar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopTraking();
                }
            });
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Tracking Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Channel for tracking notifications");
            channel.enableVibration(false);
            channel.enableLights(false);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    private void cerrar() {
        showInterstitialThenReturnToMain();
    }

    private void loadInterstitial() {
        AdRequest request = new AdRequest.Builder().build();
        InterstitialAd.load(
                this,
                getString(R.string.admob_interstitial_id),
                request,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(InterstitialAd ad) {
                        interstitialAd = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        interstitialAd = null;
                    }
                });
    }

    private void showInterstitialThenReturnToMain() {
        if (interstitialAd == null) {
            goToMain();
            return;
        }
        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                interstitialAd = null;
                loadInterstitial();
                goToMain();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                interstitialAd = null;
                goToMain();
            }
        });
        interstitialAd.show(this);
    }

    private void goToMain() {
        Intent intent = new Intent(MapsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateCameraToBounds(boolean animate) {
        if (mMap == null || markerDestino == null) {
            return;
        }
        if (markerYo != null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(markerYo.getPosition());
            builder.include(markerDestino.getPosition());
            LatLngBounds bounds = builder.build();
            int padding = 120; // padding en pixels
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            if (animate) {
                mMap.animateCamera(cameraUpdate);
            } else {
                mMap.moveCamera(cameraUpdate);
            }
            mMap.setOnMapLoadedCallback(() -> mMap.animateCamera(cameraUpdate));
        } else {
            CameraUpdate center = CameraUpdateFactory.newLatLng(markerDestino.getPosition());
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
            mMap.moveCamera(center);
            if (animate) {
                mMap.animateCamera(zoom);
            }
        }
    }

    private void editarNombre() {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.edit_name, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = (EditText) dialogView.findViewById(R.id.edTextNombre);
        edt.setText(name);
        final MaterialButton btnFavorito = (MaterialButton) dialogView.findViewById(R.id.btnFavorito);
        if (nota == null) {
            btnFavorito.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnFavorito.setIconResource(android.R.drawable.btn_star_big_on);
                    nota = "fav";
                }
            });
        } else {
            btnFavorito.setIconResource(android.R.drawable.btn_star_big_on);
            btnFavorito.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnFavorito.setIconResource(android.R.drawable.btn_star_big_off);
                    nota = null;
                }
            });
        }
        dialogBuilder.setTitle(R.string.modificanombre);
        //dialogBuilder.setMessage("Enter text below");
        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with
                name = edt.getText().toString();
                favorito();
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void favorito() {

        // No mostrar nombre en la UI

        manager = new DataBaseManager(MapsActivity.this);
        String txtlatitude2 = String.valueOf(latitude2);
        String txtlongitude2 = String.valueOf(longitude2);


        manager = new DataBaseManager(this);
        cursor = manager.verRepetidos(txtlatitude2, txtlongitude2);

        if (cursor.getCount() != 0) {
            // Si encuentra una ubicación repetida, obtener el str_id del cursor
            cursor.moveToFirst();
            String existingId = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
            manager.modificarUbicacion(existingId, name, txtlatitude2, txtlongitude2, address, null, nota);
        } else {
            // Si no existe, insertar nueva ubicación
            manager.insertar(name, txtlatitude2, txtlongitude2, address, null, nota);
        }

        // Cerrar el cursor
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.help) {
            // User chose the "Settings" item, show the app settings UI...
            Intent e = new Intent(MapsActivity.this, TutoActivity.class);
            startActivity(e);
            return true;
        }
        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item);
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //code

            //startLocationUpdates();
            mLocation = location;
            latitude1 = location.getLatitude();
            longitude1 = location.getLongitude();
            myLocation = new LatLng(latitude1, longitude1);
            if (markerYo == null) {
                markerYo = mMap.addMarker(new MarkerOptions()
                        .position(myLocation)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                );
            } else {
                markerYo.setPosition(myLocation);
            }
            updateCameraToBounds(true);
            Log.d("location", "Latitude:" + mLocation.getLatitude() + "\n" + "Longitude:" + mLocation.getLongitude());
            distancia();

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

            mLocation = location;
            if (location != null) {
                latitude1 = location.getLatitude();
                longitude1 = location.getLongitude();
                myLocation = new LatLng(latitude1, longitude1);
                if (markerYo == null) {
                    markerYo = mMap.addMarker(new MarkerOptions()
                            .position(myLocation)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    );
                } else {
                    markerYo.setPosition(myLocation);
                }
                updateCameraToBounds(true);
            }
            Log.d("location", "Latitude:" + mLocation.getLatitude() + "\n" + "Longitude:" + mLocation.getLongitude());
            distancia();
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

            // txtBuscar.setText(R.string.recibiendo);
        }

    };


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        // Controles UI
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

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

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location == null) {
                            // Logic to handle location object
                            // txtBuscar.setText(R.string.recibiendo);
                        }


                        mMap.getUiSettings().setZoomControlsEnabled(true);

                        // Getting LocationManager object from System Service LOCATION_SERVICE
                        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                        // Creating a criteria object to retrieve provider
                        Criteria criteria = new Criteria();

                        // Getting the name of the best provider
                        String provider = locationManager.getBestProvider(criteria, true);

                        // Getting Current Location
                        if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        try {
                            // Customise the styling of the base map using a JSON object defined
                            // in a raw resource file.
                            boolean success = googleMap.setMapStyle(
                                    MapStyleOptions.loadRawResourceStyle(
                                            MapsActivity.this, R.raw.style_json));

                            if (!success) {
                                Log.e(TAG, "Style parsing failed.");
                            }
                        } catch (Resources.NotFoundException e) {
                            Log.e(TAG, "Can't find style. Error: ", e);
                        }

                        location = locationManager.getLastKnownLocation(provider);

                        if (location != null) {
                            // Getting latitude of the current location
                            latitude1 = location.getLatitude();

                            // Getting longitude of the current location
                            longitude1 = location.getLongitude();

                            // Creating a LatLng object for the current location
                            LatLng latLng = new LatLng(latitude1, longitude1);

                            myPosition = new LatLng(latitude1, longitude1);
                            markerYo = googleMap.addMarker(new MarkerOptions()
                                    .position(myPosition)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            );
                            updateCameraToBounds(true);

                            distancia();
                        }

                    }
                });


        // Marcadores
        position = new LatLng(latitude2, longitude2);
        markerDestino = googleMap.addMarker(new MarkerOptions()
                .snippet(address)
                .position(position)
                .title("Destino")
                .icon(BitmapDescriptorFactory.defaultMarker())
                .draggable(true));
        updateCameraToBounds(false);
        CircleOptions circleOptions = new CircleOptions()
                .center(position)
                .radius(Double.parseDouble(txtDistancia.getText().toString()))
                .fillColor(0x40ff0000)
                .strokeColor(0x40ff0000);
        final Circle circle = mMap.addCircle(circleOptions);
        // distancia();
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(
                    markerDestino.getPosition().latitude, markerDestino.getPosition().longitude, 1);
            // Verificar que la lista no esté vacía antes de acceder al índice 0
            if (addresses != null && !addresses.isEmpty() && addresses.get(0) != null) {
                address = addresses.get(0).getAddressLine(0);
            } else {
                Log.w(TAG, "No addresses found for location");
                address = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoding error", e);
            e.printStackTrace();
            address = null;
        }
        if (address != null) {
            destino = address;
        } else {
            destino = "";
        }
        if (txtAddress != null) {
            txtAddress.setText(destino);
        }
        // No mostrar nombre en la UI
        distancia();
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDrag(Marker arg0) {
                // TODO Auto-generated method stub
                Log.d("Marker", "Dragging");
            }

            @Override
            public void onMarkerDragEnd(Marker arg0) {
                // TODO Auto-generated method stub
                if (markerDestino == null) {
                    Log.e(TAG, "markerDestino is null in onMarkerDragEnd");
                    return;
                }
                LatLng markerLocation = markerDestino.getPosition();
                //Toast.makeText(MapsActivity.this, markerLocation.toString(), Toast.LENGTH_LONG).show();
                //  Log.d("Marker", "finished");

                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(
                            markerDestino.getPosition().latitude, markerDestino.getPosition().longitude, 1);
                    // Verificar que la lista no esté vacía antes de acceder al índice 0
                    if (addresses != null && !addresses.isEmpty() && addresses.get(0) != null) {
                        address = addresses.get(0).getAddressLine(0);
                    } else {
                        Log.w(TAG, "No addresses found in onMarkerDragEnd");
                        address = markerDestino.getPosition().latitude + ", " + markerDestino.getPosition().longitude;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Geocoding error in onMarkerDragEnd", e);
                    e.printStackTrace();
                    address = markerDestino.getPosition().latitude + ", " + markerDestino.getPosition().longitude;
                }
                
                // Procesar la dirección de forma segura
                if (address != null) {
                    destino = address;
                } else {
                    destino = "";
                }

                if (txtAddress != null) {
                    txtAddress.setText(destino);
                }
                latitude2 = markerDestino.getPosition().latitude;
                longitude2 = markerDestino.getPosition().longitude;
                if (address != null) {
                    Toast.makeText(MapsActivity.this, address,
                            Toast.LENGTH_SHORT).show();
                }
                CircleOptions circleOptions = new CircleOptions()
                        .center(markerLocation)
                        .radius(Double.parseDouble(txtDistancia.getText().toString()))
                        .fillColor(0x40ff0000)
                        .strokeColor(0x40ff0000);

                Circle circle = mMap.addCircle(circleOptions);

                 distancia();
            }

            @Override
            public void onMarkerDragStart(Marker arg0) {
                // TODO Auto-generated method stub
                circle.remove();
                Log.d("Marker", "Started");
                txtBuscar.setText("Calculando Distancia ...");


            }
        });


    }


    @SuppressLint("MissingPermission")

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            // ¿Permisos asignados?
            if (permissions.length > 0 &&
                    permissions[0].equals(android.Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                Toast.makeText(this, "Error de permisos", Toast.LENGTH_LONG).show();
            }

        }
    }


    @SuppressLint("MissingPermission")
    private void fn_getlocation() {
        locationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        
        // Remover listeners anteriores antes de registrar nuevos para evitar duplicados
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(this);
            } catch (SecurityException e) {
                Log.e(TAG, "Error removing previous location updates", e);
            }
        }
        
        if (!isGPSEnable && !isNetworkEnable) {

        } else {

            if (isNetworkEnable) {
                location = null;
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,50000,10,this);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {

                        Log.e("latitude1", location.getLatitude() + "");
                        Log.e("longitude1", location.getLongitude() + "");
                        Log.e("latitude2", latitude2 + "");
                        Log.e("longitude2", longitude2 + "");

                        latitude1 = location.getLatitude();
                        longitude1 = location.getLongitude();

                        //distancia();
                    }
                }

            }


            if (isGPSEnable) {
                location = null;
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50000, 10,this);
                if (locationManager!=null){
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location!=null){
                        Log.e("latitude",location.getLatitude()+"");
                        Log.e("longitude",location.getLongitude()+"");
                        Log.e("latitude2",latitude2+"");
                        Log.e("longitude2",longitude2+"");
                        latitude1 = location.getLatitude();
                        longitude1 = location.getLongitude();
                        //fn_update(location);
                        //distancia();
                    }
                }
            }



        }

    }


    //@Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

    }



    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        // Guardar la referencia del callback para poder removerlo después
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // do work here
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    onLocationChanged(locationResult.getLastLocation());
                    // Solo calcular distancia si markerDestino está inicializado
                    if (markerDestino != null) {
                        distancia();
                    }
                }
            }
        };
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, mLocationCallback,
                Looper.myLooper());
    }

    public void starttraking(){
        // Verificar que markerDestino no sea null
        if (markerDestino == null) {
            Log.e(TAG, "markerDestino is null, cannot start tracking");
            Toast.makeText(this, "Error: No se puede iniciar el seguimiento", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(markerDestino.getPosition().latitude, markerDestino.getPosition().longitude, 1);
            // Verificar que la lista no esté vacía antes de acceder al índice 0
            if (addresses != null && !addresses.isEmpty() && addresses.get(0) != null) {
                address = addresses.get(0).getAddressLine(0);
            } else {
                Log.w(TAG, "No addresses found for location in starttraking");
                // Usar coordenadas como dirección si no se encuentra
                address = markerDestino.getPosition().latitude + ", " + markerDestino.getPosition().longitude;
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoding error in starttraking", e);
            e.printStackTrace();
            // Usar coordenadas como dirección si hay error
            address = markerDestino.getPosition().latitude + ", " + markerDestino.getPosition().longitude;
        }


       // trakingOn = true;

        // Verificar que markerDestino no sea null (ya se verificó al inicio del método)
        CharSequence charAlerta = txtDistancia.getText();
        alerta = DistanceFormatter.toAlertMeters(this, charAlerta.toString());
        String destination = String.valueOf(markerDestino.getPosition());
        Intent intent = new Intent(this, Traking.class);
        //intent.putExtra("marker", marker);
        txtlatitude2 = String.valueOf( markerDestino.getPosition().latitude);
        txtlongitude2 = String.valueOf( markerDestino.getPosition().longitude);


        intent.putExtra("latitude2",  markerDestino.getPosition().latitude);
        intent.putExtra("longitude2",  markerDestino.getPosition().longitude);
        intent.putExtra("address", address); // Pasar la dirección
        intent.putExtra("nombre", name); // Pasar el nombre
        intent.putExtra("str_id", str_id); // Pasar el ID si existe
        intent.putExtra("nota", nota); // Pasar la nota (favorito)
        intent.putExtra("trakingOn", true);
        intent.putExtra("alerta", alerta);

        manager = new DataBaseManager(this);
        cursor = manager.verRepetidos(txtlatitude2, txtlongitude2);

        try {
            if (cursor.getCount() != 0) {
                startTrackingService(intent);
            } else {
                // Insertar nueva ubicación con el estado de favorito si existe
                manager.insertar(name, txtlatitude2, txtlongitude2, address, null, nota);
                startTrackingService(intent);
                finish();
            }
        } finally {
            // Cerrar el cursor
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }


    }

    public void guardarTemp(){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("txtlongitude2", txtlongitude2);
        editor.putString("txtlatitude2", txtlatitude2);
        editor.putString("alerta", String.valueOf(alerta));
        editor.putBoolean("trakingOn", true);
        editor.putString("name", name != null ? name : "");
        editor.apply();
        editor.commit();

    }

    private void startTrackingService(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, intent);
        } else {
            startService(intent);
        }
    }

    public void distancia(){
        // Verificar que markerDestino no sea null
        if (markerDestino == null) {
            Log.e(TAG, "markerDestino is null, cannot calculate distance");
            if (txtBuscar != null) {
                txtBuscar.setText(getString(R.string.recibiendo));
            }
            return;
        }

        Location temp = new Location("");
        double marcadorLat = markerDestino.getPosition().latitude;
        double marcadorLon = markerDestino.getPosition().longitude;
        temp.setLatitude(marcadorLat);
        temp.setLongitude(marcadorLon);

        Location yo = new Location("");
        yo.setLatitude(latitude1);
        yo.setLongitude(longitude1);

        float[] results = new float[1];
        Location.distanceBetween( markerDestino.getPosition().latitude,  markerDestino.getPosition().longitude,
                latitude1, longitude1, results);
        
        // Verificar que location no sea null antes de usarlo
        if (location != null) {
            dist = temp.distanceTo(location);
        } else {
            dist = results[0];
        }
        distance = results[0];
        b = (int) dist;

        if(location == null) {
            txtBuscar.setText(R.string.recibiendo);
        }

        DistanceFormatter.Display display = DistanceFormatter.formatDistance(this, distance);
        txtBuscar.setText(display.value + " " + display.unit);


    }

    protected void stopLocationUpdates() {
        // Remover LocationCallback del FusedLocationProviderClient
        if (mFusedLocationClient != null && mLocationCallback != null) {
            try {
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                Log.d(TAG, "FusedLocationProviderClient location updates removed");
            } catch (SecurityException e) {
                Log.e(TAG, "Error removing FusedLocationProviderClient updates", e);
            }
        }
        
        // Remover LocationManager listeners
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(this);
                Log.d(TAG, "LocationManager location updates removed");
            } catch (SecurityException e) {
                Log.e(TAG, "Error removing LocationManager updates", e);
            }
        }
        
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Restaurar listeners si el tracking está activo
        if (trakingOn && mLocationRequest != null) {
            startLocationUpdates();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Remover listeners cuando la actividad se pausa para evitar consumo innecesario
        // No remover si el tracking está activo (el servicio Traking se encarga de eso)
        if (!trakingOn) {
            stopLocationUpdates();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Siempre remover todos los listeners cuando la actividad se destruye
        stopLocationUpdates();
    }
    public void stopTraking(){
        showInterstitialThenStopTracking();
    }

    private void showInterstitialThenStopTracking() {
        if (interstitialAd == null) {
            stopTrackingSilently();
            goToMain();
            return;
        }
        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                interstitialAd = null;
                loadInterstitial();
                stopTrackingSilently();
                goToMain();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                interstitialAd = null;
                stopTrackingSilently();
                goToMain();
            }
        });
        interstitialAd.show(MapsActivity.this);
    }

    private void stopTrackingSilently() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(333);
        Intent j = new Intent(this, Traking.class);
        stopService(j);

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.commit();
        stopLocationUpdates();
        trakingOn=false;

    }

    @SuppressLint("MissingPermission")
   // @Override
    public void onLocationChanged(Location location) {

        Location mLocation = new Location("");
        mLocation.setLatitude(latitude1);
        mLocation.setLongitude(longitude1);


        Location mDestination = new Location("");
        mDestination.setLatitude(latitude2);
        mDestination.setLongitude(longitude2);


        //distancia();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public void startNotification(){

       // String distance = String.valueOf(b);
        DistanceFormatter.Display display = DistanceFormatter.formatDistance(this, distance);
        trakingOn = true;
        Intent goApp = new Intent (this, MapsActivity.class);
        goApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        goApp.putExtra("trakingOn", trakingOn);
        goApp.putExtra("traking", trakingOn); // También agregar como "traking" para compatibilidad
        // Verificar que markerDestino no sea null antes de usar
        if (markerDestino != null) {
            goApp.putExtra("latitude2",  markerDestino.getPosition().latitude);
            goApp.putExtra("longitude2", markerDestino.getPosition().longitude);
        } else {
            // Usar las coordenadas guardadas si markerDestino es null
            goApp.putExtra("latitude2", latitude2);
            goApp.putExtra("longitude2", longitude2);
        }
        if (name != null) {
            goApp.putExtra("nombre", name);
        }
        if (str_id != null) {
            goApp.putExtra("str_id", str_id);
        }
        if (nota != null) {
            goApp.putExtra("nota", nota);
        }
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pIntent1 = PendingIntent.getActivity(
                this, 333, goApp, flags);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(MapsActivity.this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.noti)
                        .setContentTitle(display.value + " " + display.unit)
                        .setContentIntent(pIntent1)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(false)
                        .setOngoing(false)
                        //.setContentText(falta+" mts.")
                ;

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(
                        Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(333, mBuilder.build());

    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        // Retrieve the data from the marker.
        Integer clickCount = (Integer) marker.getTag();

        // Check if a click count was set, then display the click count.
        if (clickCount != null) {
            clickCount = clickCount + 1;
            marker.setTag(clickCount);
            Toast.makeText(this,
                    marker.getTitle() +
                            " has been clicked " + clickCount + " times.",
                    Toast.LENGTH_SHORT).show();
            /*
            latitude2 = marker.getPosition().latitude;
            longitude2 = marker.getPosition().longitude;
            //address = addresses.get(0).getAddressLine(0);
*/
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());
            try {
                addresses = geocoder.getFromLocation(
                        marker.getPosition().latitude, marker.getPosition().longitude, 1);
                address = addresses.get(0).getAddressLine(0);
            } catch (IOException e) {
                e.printStackTrace();
            }

            latitude2 = marker.getPosition().latitude;
            longitude2 = marker.getPosition().longitude;
            Toast.makeText(this,
                    /*"lat: "+latitude2+" long: "+longitude2+" "+*/address,
                    Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onClick(View v) {

    }
}