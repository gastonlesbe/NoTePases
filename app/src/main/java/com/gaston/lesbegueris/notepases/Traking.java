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
import com.gaston.lesbegueris.notepases.util.DistanceFormatter;

import java.util.Timer;
import java.util.TimerTask;
import android.app.Notification;

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
    
    // Variables para almacenar datos de la ubicación
    private String name = "";
    private String address = "";
    private String str_id = "";
    private String nota = "";
    
    // Variables para actualización dinámica de ubicación
    private long currentUpdateInterval = 60000; // Intervalo actual en milisegundos (inicial: 60 segundos)
    private float lastDistance = -1; // Última distancia registrada
    private float minDistanceChange = 50; // Cambio mínimo de distancia para reconfigurar (metros)




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
            // Obtener datos adicionales si están disponibles
            name = intent.getStringExtra("nombre");
            if (name == null) name = "";
            address = intent.getStringExtra("address");
            if (address == null) address = "";
            str_id = intent.getStringExtra("str_id");
            if (str_id == null) str_id = "";
            nota = intent.getStringExtra("nota");
            if (nota == null) nota = "";
        }

        // Ensure the service stays alive with a foreground notification
        startForeground(333, buildNotification(-1));

        // Retornar START_STICKY para que el servicio se reinicie si se cierra
        return START_STICKY;
    }

    /**
     * Calcula el intervalo óptimo de actualización según la distancia al destino
     * Cuanto más cerca, más frecuente la actualización
     */
    private long calculateUpdateInterval(float distance) {
        if (distance > 2000) {
            // Muy lejos (>2km): actualizar cada 60 segundos
            return 60000;
        } else if (distance > 1000) {
            // Lejos (1-2km): actualizar cada 30 segundos
            return 30000;
        } else if (distance > 500) {
            // Medio (500m-1km): actualizar cada 15 segundos
            return 15000;
        } else if (distance > 200) {
            // Cerca (200-500m): actualizar cada 10 segundos
            return 10000;
        } else if (distance > 100) {
            // Muy cerca (100-200m): actualizar cada 5 segundos
            return 5000;
        } else if (distance > 50) {
            // Casi llegando (50-100m): actualizar cada 3 segundos
            return 3000;
        } else {
            // Muy cerca (<50m): actualizar cada 1 segundo para detectar llegada
            return 1000;
        }
    }
    
    /**
     * Reconfigura los location updates con el nuevo intervalo
     */
    @SuppressLint("MissingPermission")
    private void reconfigureLocationUpdates(long newInterval, float minDistance) {
        if (locationManager == null) {
            return;
        }
        
        try {
            // Remover updates actuales
            locationManager.removeUpdates(this);
            
            // Reconfigurar con nuevo intervalo
            if (isNetworkEnable) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 
                    newInterval, 
                    minDistance, 
                    this
                );
            }
            
            if (isGPSEnable) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 
                    newInterval, 
                    minDistance, 
                    this
                );
            }
            
            currentUpdateInterval = newInterval;
            Log.d(TAG, "Location updates reconfigured: interval=" + newInterval + "ms, minDistance=" + minDistance + "m");
        } catch (SecurityException e) {
            Log.e(TAG, "Error reconfiguring location updates", e);
        }
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

        // RECALCULAR la distancia cada vez que cambia la ubicación
        float distance = mDestination.distanceTo(location);
        int falta = (int) distance;
        DistanceFormatter.Display display = DistanceFormatter.formatDistance(this, distance);
        
        Log.d(TAG, "Location updated - Distance: " + falta + "m, Alert threshold: " + alerta + "m");
        
        // Actualizar dinámicamente el intervalo según la distancia
        long newInterval = calculateUpdateInterval(distance);
        float minDistance = Math.max(5, distance / 20); // Distancia mínima: 5m o 5% de la distancia
        
        // Solo reconfigurar si el intervalo cambió significativamente o la distancia cambió mucho
        boolean shouldReconfigure = false;
        if (Math.abs(newInterval - currentUpdateInterval) > 5000) { // Cambio de más de 5 segundos
            shouldReconfigure = true;
        } else if (lastDistance > 0 && Math.abs(distance - lastDistance) > minDistanceChange) {
            // Si la distancia cambió significativamente, verificar si necesitamos ajustar
            long recalculatedInterval = calculateUpdateInterval(distance);
            if (recalculatedInterval != currentUpdateInterval) {
                shouldReconfigure = true;
            }
        }
        
        if (shouldReconfigure) {
            reconfigureLocationUpdates(newInterval, minDistance);
        }
        lastDistance = distance; // Actualizar siempre la última distancia
        
        // VERIFICAR SI SE ALCANZÓ LA DISTANCIA DE ALARMA (recalculada con la nueva ubicación)
        if (falta < alerta) {
            Log.d(TAG, "Alarm triggered - Distance: " + falta + "m < Alert: " + alerta + "m");
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


        // ACTUALIZAR LA NOTIFICACIÓN con la nueva distancia calculada
        Intent goApp = new Intent(Traking.this, MapsActivity.class);
        goApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        goApp.putExtra("latitude2", latitude2);
        goApp.putExtra("longitude2", longitude2);
        goApp.putExtra("trakingOn", trakingOn);
        goApp.putExtra("traking", trakingOn); // También agregar como "traking" para compatibilidad
        goApp.putExtra("nombre", name); // Pasar el nombre
        goApp.putExtra("address", address); // Pasar la dirección
        goApp.putExtra("str_id", str_id); // Pasar el ID
        goApp.putExtra("nota", nota); // Pasar la nota
        goApp.setAction("com.gaston.lesbegueris.notepases");
        
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pIntent1 = PendingIntent.getActivity(
                Traking.this, 333, goApp, flags);

        sendBroadcast(goApp);

        // ACTUALIZAR LA NOTIFICACIÓN con la distancia recalculada
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(Traking.this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.noti)
                        .setContentTitle(display.value + " " + display.unit) // Distancia actualizada
                        .setContentIntent(pIntent1)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .setShowWhen(false); // No mostrar timestamp para que se vea más limpia

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(
                        Context.NOTIFICATION_SERVICE);
        // Usar el mismo ID (333) para actualizar la notificación existente
        mNotificationManager.notify(333, mBuilder.build());
        
        Log.d(TAG, "Notification updated with distance: " + falta + "m");



    }

    private Notification buildNotification(int falta) {
        Intent goApp = new Intent(Traking.this, MapsActivity.class);
        goApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pIntent = PendingIntent.getActivity(
                Traking.this, 333, goApp, flags);

        String title = (falta >= 0) ? (falta + " mts.") : getString(R.string.recibiendo);
        return new NotificationCompat.Builder(Traking.this, CHANNEL_ID)
                .setSmallIcon(R.drawable.noti)
                .setContentTitle(title)
                .setContentIntent(pIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .setOngoing(true)
                .setShowWhen(false)
                .build();
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
            return;
        }
        
        // Calcular distancia inicial para determinar el intervalo
        float initialDistance = -1;
        if (latitude1 != 0 && longitude1 != 0 && latitude2 != 0 && longitude2 != 0) {
            Location currentLoc = new Location("");
            currentLoc.setLatitude(latitude1);
            currentLoc.setLongitude(longitude1);
            Location destLoc = new Location("");
            destLoc.setLatitude(latitude2);
            destLoc.setLongitude(longitude2);
            initialDistance = currentLoc.distanceTo(destLoc);
        }
        
        // Si no tenemos distancia inicial, usar intervalo por defecto (60 segundos)
        if (initialDistance < 0) {
            initialDistance = 2000; // Asumir distancia grande inicialmente
        }
        
        // Calcular intervalo y distancia mínima iniciales
        long initialInterval = calculateUpdateInterval(initialDistance);
        float minDistance = Math.max(5, initialDistance / 20);
        
        // Actualizar variables globales
        currentUpdateInterval = initialInterval;
        lastDistance = initialDistance;

        // Remover updates anteriores si existen (solo una vez)
        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException e) {
            Log.e(TAG, "Error removing previous updates", e);
        }

        if (isNetworkEnable){
            location = null;
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                initialInterval,
                minDistance,
                this
            );
            
            if (locationManager != null){
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null){
                    Log.d(TAG, "Initial NETWORK location - lat: " + location.getLatitude() + ", lon: " + location.getLongitude());
                    latitude1 = location.getLatitude();
                    longitude1 = location.getLongitude();
                }
            }
        }

        if (isGPSEnable){
            location = null;
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                initialInterval,
                minDistance,
                this
            );
            
            if (locationManager != null){
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null){
                    Log.d(TAG, "Initial GPS location - lat: " + location.getLatitude() + ", lon: " + location.getLongitude());
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    // Si no teníamos ubicación de red, usar GPS
                    if (latitude1 == 0 && longitude1 == 0) {
                        latitude1 = latitude;
                        longitude1 = longitude;
                    }
                }
            }
        }
        
        Log.d(TAG, "Location updates started - interval: " + initialInterval + "ms, minDistance: " + minDistance + "m, distance: " + initialDistance + "m");
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