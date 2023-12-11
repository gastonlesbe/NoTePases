package com.gaston.lesbegueris.notepases;

/**
 * Created by gaston on 26/11/17.
 */
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.LocationCallback;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by deepshikha on 24/11/16.
 */

public class Traking extends Service implements LocationListener{

    private Location mDestination, mLocation;
    boolean trakingOn;
    double latitude2, longitude2;
    double latitude1, longitude1;
    private static final String TAG = "BroadcastService";
    public static final String BROADCAST_ACTION = "com.gaston.lesbegueris.notepases.DATA";
    private final Handler handler = new Handler();
    int counter = 0;
    int alerta;

    boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    double latitude,longitude;
    LocationManager locationManager;
    Location location;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    long notify_interval = 10000;
    public static String str_receiver = "servicetutorial.service.receiver";
    private Intent intent;
    private static final String CHANNEL_ID = "tracking_channel";




    public Traking() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        createNotificationChannel();

        mTimer = new Timer();
        mTimer.schedule(new TimerTaskToGetLocation(),20000,notify_interval);
        intent = new Intent(str_receiver);
        fn_getlocation();
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            latitude2 = intent.getDoubleExtra("latitude2", latitude2);
            longitude2 = intent.getDoubleExtra("longitude2", longitude2);
            alerta = intent.getIntExtra("alerta", alerta);
        }

        // Retornar START_STICKY para que el servicio se reinicie si se cierra
        return START_STICKY;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Log.e(TAG, "Received null location");
            return;
        }

        // Actualizar la ubicación actual
        latitude1 = location.getLatitude();
        longitude1 = location.getLongitude();

        mDestination = new Location("");
        mDestination.setLatitude(latitude2);
        mDestination.setLongitude(longitude2);

        float distance = mDestination.distanceTo(location);
        int falta = (int) distance;
        if (falta < alerta) {
            Intent alarmIntent = new Intent(Traking.this, Alarma.class);
            alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, alarmIntent, flags);

            if (locationManager != null) {
                try {
                    locationManager.removeUpdates(this);
                } catch (SecurityException e) {
                    Log.e(TAG, "Error removing location updates", e);
                }
            }
            startActivity(alarmIntent);
            return;
        }
        trakingOn = true; // Mantener trakingOn como true cuando se muestra la notificación


        Intent goApp = new Intent(Traking.this, MapsActivity.class);
        goApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        goApp.putExtra("latitude2", latitude2);
        goApp.putExtra("longitude2", longitude2);
        goApp.putExtra("trakingOn", trakingOn);
        goApp.putExtra("traking", trakingOn); // También agregar como "traking" para compatibilidad
        goApp.setAction("com.gaston.lesbegueris.notepases");
        
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pIntent1 = PendingIntent.getActivity(
                Traking.this, 333, goApp, flags);

        sendBroadcast(goApp);

        String txtFalta = getString(R.string.falta);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(Traking.this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.noti)
                        .setContentTitle(falta + " mts.")
                        .setContentIntent(pIntent1)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(false)
                        .setOngoing(false)
                //        .setContentText(falta + " mts.")
                ;

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(
                        Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(333, mBuilder.build());



    }



    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    @SuppressLint("MissingPermission")
    private void fn_getlocation(){
        locationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnable && !isNetworkEnable){

        }else {

            if (isNetworkEnable){
                location = null;
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,50000,10,this);
                if (locationManager!=null){
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location!=null){

                        Log.e("latitude1",location.getLatitude()+"");
                        Log.e("longitude1",location.getLongitude()+"");
                        Log.e("latitude2",latitude2+"");
                        Log.e("longitude2",longitude2+"");

                        latitude1 = location.getLatitude();
                        longitude1 = location.getLongitude();
                        //fn_update(location);
                    }
                }

            }


            if (isGPSEnable){
                location = null;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,50000,10,this);
                if (locationManager!=null){
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location!=null){
                        Log.e("latitude",location.getLatitude()+"");
                        Log.e("longitude",location.getLongitude()+"");
                        Log.e("latitude2",latitude2+"");
                        Log.e("longitude2",longitude2+"");
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                       // fn_update(location);
                    }
                }
            }


        }

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy <<");
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(this);
            } catch (SecurityException e) {
                Log.e(TAG, "Error removing location updates in onDestroy", e);
            }
        }
    }

    private class TimerTaskToGetLocation extends TimerTask{
        @Override
        public void run() {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    fn_getlocation();
                }
            });

        }
    }
/*

    private void fn_update(Location location){

        intent.putExtra("latutide1",location.getLatitude()+"");
        intent.putExtra("longitude1",location.getLongitude()+"");
        sendBroadcast(intent);
    }
*/


}