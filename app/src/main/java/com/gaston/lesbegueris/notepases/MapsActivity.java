package com.gaston.lesbegueris.notepases;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.*;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.ads.MobileAds;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import static java.lang.Integer.parseInt;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnSuccessListener;


public class MapsActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener, View.OnClickListener {

    private static final int LOCATION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    String address, txtlatitude2, txtlongitude2, destino, name, str_id, nota;
    double latitude2, longitude2, latitude1, longitude1;
    TextView txtBuscar, txtDistancia, txtAddress, txtNombre;
    ImageButton btnBuscar, btnEdit, btnClose;
    protected GoogleApiClient mGoogleApiClient;

    private InterstitialAd mInterstitialAd;
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
    private AdView mAdView;

    private FusedLocationProviderClient mFusedLocationClient;

    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;


    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    private LatLng myLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        mFusedLocationClient = getFusedLocationProviderClient(this);
        startLocationUpdates();

        fn_getlocation();
        AdView adView = new AdView(this);


        adView.setAdSize(AdSize.BANNER);
        //test
        //adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");

        //verdadero
        adView.setAdUnitId("ca-app-pub-9841764898906750/1703228093");

        AdRequest adRequest = new AdRequest.Builder().build();


        mAdView = findViewById(R.id.adView);

        mAdView.loadAd(adRequest);
        mInterstitialAd = new InterstitialAd(this);
        //TEST
        //mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        //REAL
        mInterstitialAd.setAdUnitId("ca-app-pub-9841764898906750/8552541624");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                // Call displayInterstitial() function
                displayInterstitial();
            }
        });


        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);


        latitude2 = Double.parseDouble(sharedpreferences.getString("txtlatitude2", String.valueOf(latitude2)));
        longitude2 = Double.parseDouble(sharedpreferences.getString("txtlongitude2", String.valueOf(longitude2)));
        address = sharedpreferences.getString("address", address);
        trakingOn = sharedpreferences.getBoolean("trakingOn", trakingOn);


        Intent intent = getIntent();
        latitude2 = intent.getDoubleExtra("latitude2", latitude2);
        longitude2 = intent.getDoubleExtra("longitude2", longitude2);
        name = intent.getStringExtra("nombre");
        trakingOn = intent.getBooleanExtra("traking", trakingOn);
        str_id = intent.getStringExtra("str_id");
        nota = intent.getStringExtra("nota");
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);


        txtBuscar = (TextView) findViewById(R.id.txtDireccion);
        txtDistancia = (TextView) findViewById(R.id.txtDistancia);
        // txtDistancia.setFocusable(false);
        txtAddress = (TextView) findViewById(R.id.txtaddress);
        txtNombre = (TextView) findViewById(R.id.txtnombre);
        txtNombre.setText(R.string.nombre);
        btnBuscar = (ImageButton) findViewById(R.id.btnBuscar);
        btnEdit = (ImageButton) findViewById(R.id.btnEdit);

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editarNombre();
            }
        });
        btnClose = (ImageButton) findViewById(R.id.btnClose);
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
            btnBuscar.setImageResource(R.mipmap.start);
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
                    btnBuscar.setImageResource(R.mipmap.stop);
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }
            });
        } else {
            btnBuscar.setImageResource(R.mipmap.stop);
            btnBuscar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    stopTraking();

                }
            });


        }
    }

    private void cerrar() {
        Intent intent = new Intent(MapsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void editarNombre() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.edit_name, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = (EditText) dialogView.findViewById(R.id.edTextNombre);
        edt.setText(name);
        final ImageButton btnFavorito = (ImageButton) dialogView.findViewById(R.id.btnFavorito);
        if (nota == null) {
            btnFavorito.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnFavorito.setImageResource(android.R.drawable.btn_star_big_on);
                    nota = "fav";
                }
            });
        } else {
            btnFavorito.setImageResource(android.R.drawable.btn_star_big_on);
            btnFavorito.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnFavorito.setImageResource(android.R.drawable.btn_star_big_off);
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

        txtNombre.setText(name);

        manager = new DataBaseManager(MapsActivity.this);
        String txtlatitude2 = String.valueOf(latitude2);
        String txtlongitude2 = String.valueOf(longitude2);


        manager = new DataBaseManager(this);
        cursor = manager.verRepetidos(txtlatitude2, txtlongitude2);

        if (cursor.getCount() != 0) {

            manager.modificarUbicacion(str_id, name, txtlatitude2, txtlongitude2, address, null, nota);
        } else {

            manager.insertar(name, txtlatitude2, txtlongitude2, address, null, nota);

        }


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help:
                // User chose the "Settings" item, show the app settings UI...
                Intent e = new Intent(MapsActivity.this, TutoActivity.class);
                startActivity(e);

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //code

            //startLocationUpdates();
            mLocation = location;
            myLocation = new LatLng(latitude1, longitude1);
            mMap.addMarker(new MarkerOptions()
                    .position(myLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            );

            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
            Log.d("location", "Latitude:" + mLocation.getLatitude() + "\n" + "Longitude:" + mLocation.getLongitude());
            distancia();

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

            mLocation = location;
            myLocation = new LatLng(latitude1, longitude1);
            markerYo = mMap.addMarker(new MarkerOptions()
                    .position(myLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            );

            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
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
                            CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(latitude1, longitude1));
                            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
                            mMap.moveCamera(center);
                            mMap.animateCamera(zoom);

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
        mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(latitude2, longitude2));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
        mMap.moveCamera(center);
        mMap.animateCamera(zoom);
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
            address = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        destino = address.substring(0, address.indexOf(","));
        txtAddress.setText(destino);
        txtNombre.setText(name);
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
                LatLng markerLocation = markerDestino.getPosition();
                //Toast.makeText(MapsActivity.this, markerLocation.toString(), Toast.LENGTH_LONG).show();
                //  Log.d("Marker", "finished");

                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(
                            markerDestino.getPosition().latitude, markerDestino.getPosition().longitude, 1);
                    address = addresses.get(0).getAddressLine(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                destino = address.substring(0, address.indexOf(","));

                txtAddress.setText(destino);
                latitude2 = markerDestino.getPosition().latitude;
                longitude2 = markerDestino.getPosition().longitude;
                Toast.makeText(MapsActivity.this, address,
                        Toast.LENGTH_SHORT).show();
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
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                        distancia();
                    }
                },
                Looper.myLooper());
    }

    public void starttraking(){

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(markerDestino.getPosition().latitude, markerDestino.getPosition().longitude, 1);
            address = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }


       // trakingOn = true;

        CharSequence charAlerta = txtDistancia.getText();
        alerta = parseInt(charAlerta.toString());
        String destination = String.valueOf(markerDestino.getPosition());
        Intent intent = new Intent(this, Traking.class);
        //intent.putExtra("marker", marker);
        txtlatitude2 = String.valueOf( markerDestino.getPosition().latitude);
        txtlongitude2 = String.valueOf( markerDestino.getPosition().longitude);


        intent.putExtra("latitude2",  markerDestino.getPosition().latitude);
        intent.putExtra("longitude2",  markerDestino.getPosition().longitude);
       // intent.putExtra("address", address);
        //intent.putExtra("distancia", b);
        intent.putExtra("trakingOn", true);
        intent.putExtra("alerta", alerta);

        manager = new DataBaseManager(this);
       cursor = manager.verRepetidos(txtlatitude2, txtlongitude2);


        if (cursor.getCount() != 0) {
            startService(intent);
        }else {

            manager.insertar(name, txtlatitude2, txtlongitude2, address, null, null);

            startService(intent);
            finish();
        }


    }

    public void guardarTemp(){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("txtlongitude2", txtlongitude2);
        editor.putString("txtlatitude2", txtlatitude2);
        editor.putString("alerta", String.valueOf(alerta));
        editor.putBoolean("trakingOn", true);
        editor.putString("name", txtNombre.getText().toString());
        editor.apply();
        editor.commit();

    }

    public void distancia(){

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
        dist = temp.distanceTo(location);
        distance = results[0];
        b = (int) dist;

        if(location == null) {
            txtBuscar.setText(R.string.recibiendo);
        }

        txtBuscar.setText(b + " Mts");


    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
        }

    }
    public void stopTraking(){
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(333);
        Intent intent = new Intent(this, MainActivity.class); startActivity(intent);
        finish();
        startActivity(intent);
        Intent j = new Intent(this, Traking.class);
        stopService(j);

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.commit();
        stopLocationUpdates();
        trakingOn=false;



        if (mInterstitialAd.isLoaded()) {
           // mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }

    }
    public void displayInterstitial(){
        // If Ads are loaded, show Interstitial else show nothing.
        if (mInterstitialAd.isLoaded()) {

        }
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
        int falta = (int)distance;
        trakingOn = true;
        Intent goApp = new Intent (this, MapsActivity.class);
        goApp.putExtra("trakingOn", trakingOn);
        goApp.putExtra("latitude2",  markerDestino.getPosition().latitude);
        goApp.putExtra("longitude2", markerDestino.getPosition().longitude);
        PendingIntent pIntent1 = PendingIntent.getActivity(
                this, (int) System.currentTimeMillis(), goApp, 0);

        String txtFalta = String.valueOf(R.string.falta);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(MapsActivity.this)
                        .setSmallIcon(R.drawable.noti)
                        .setContentTitle(falta+" mts.")
                        .setContentIntent(pIntent1)
                        //.setContentText(falta+" mts.")
                //.setPriority(1)
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