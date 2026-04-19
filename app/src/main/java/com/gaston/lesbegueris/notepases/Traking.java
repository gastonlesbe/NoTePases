package com.gaston.lesbegueris.notepases;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.gaston.lesbegueris.notepases.util.DistanceFormatter;

public class Traking extends Service implements LocationListener {

    private static final String TAG = "Traking";
    private static final String CHANNEL_ID = "tracking_channel";
    private static final String ALARM_CHANNEL_ID = "alarm_channel_v2";
    private static final int NOTIFICATION_ID = 333;
    private static final int ALARM_NOTIFICATION_ID = 334;
    private static final String PREFS_NAME = "traking_state";
    private static final String PREF_LAT = "lat2";
    private static final String PREF_LNG = "lng2";
    private static final String PREF_ALERTA = "alerta";

    public static final String BROADCAST_ACTION = "com.gaston.lesbegueris.notepases.DATA";
    public static String str_receiver = "servicetutorial.service.receiver";

    // Statics so Alarma.java can stop sound and release wake lock on dismiss
    public static PowerManager.WakeLock alarmWakeLock;
    public static MediaPlayer alarmPlayer;

    public static void stopAlarmSound() {
        if (alarmPlayer != null) {
            try { alarmPlayer.stop(); } catch (Exception ignored) {}
            try { alarmPlayer.release(); } catch (Exception ignored) {}
            alarmPlayer = null;
        }
    }

    private double latitude2, longitude2;
    private int alerta;
    private LocationManager locationManager;
    private boolean isForegroundStarted = false;
    private long currentUpdateInterval = 60_000;
    private float lastKnownDistance = -1f;
    private boolean alarmFired = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (intent != null) {
            // Fresh start — read from intent and persist so we survive a restart
            latitude2  = intent.getDoubleExtra("latitude2", latitude2);
            longitude2 = intent.getDoubleExtra("longitude2", longitude2);
            alerta     = intent.getIntExtra("alerta", alerta);
            prefs.edit()
                    .putFloat(PREF_LAT,    (float) latitude2)
                    .putFloat(PREF_LNG,    (float) longitude2)
                    .putInt(PREF_ALERTA,   alerta)
                    .apply();
        } else {
            // Restarted by OS after being killed — recover saved destination
            latitude2  = prefs.getFloat(PREF_LAT, 0f);
            longitude2 = prefs.getFloat(PREF_LNG, 0f);
            alerta     = prefs.getInt(PREF_ALERTA, 300);
            Log.d(TAG, "Service restarted by OS — recovered destination from prefs");
        }

        if (!isForegroundStarted) {
            startForeground(NOTIFICATION_ID, buildTrackingNotification(null).build());
            isForegroundStarted = true;
        }

        startLocationUpdates();
        return START_REDELIVER_INTENT; // redelivers last intent on restart, better than START_STICKY
    }

    // ── Location registration ──────────────────────────────────────────────

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        locationManager = (LocationManager) getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);

        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!gpsEnabled && !networkEnabled) {
            Log.e(TAG, "No location providers available");
            return;
        }

        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException ignored) {}

        try {
            if (gpsEnabled) {
                // GPS is the primary provider — accurate, no jumps
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        currentUpdateInterval,
                        0f,
                        this
                );
                Log.d(TAG, "GPS registered, interval=" + currentUpdateInterval + "ms");
            }

            if (networkEnabled && !gpsEnabled) {
                // Network only as fallback when GPS is unavailable
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        currentUpdateInterval,
                        0f,
                        this
                );
                Log.d(TAG, "Network registered (GPS unavailable), interval=" + currentUpdateInterval + "ms");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Permission error registering location updates", e);
        }
    }

    // ── Location callback ─────────────────────────────────────────────────

    @Override
    public void onLocationChanged(Location location) {
        if (location == null || alarmFired) return;

        // Accuracy filter — reject noisy readings based on how close we are
        if (location.hasAccuracy()) {
            float accuracy = location.getAccuracy();
            float maxAccuracy = (lastKnownDistance >= 0 && lastKnownDistance < 300f) ? 50f : 200f;
            if (accuracy > maxAccuracy) {
                Log.d(TAG, "Skipping inaccurate fix: accuracy=" + accuracy
                        + "m, maxAllowed=" + maxAccuracy + "m");
                return;
            }
        }

        Location dest = new Location("");
        dest.setLatitude(latitude2);
        dest.setLongitude(longitude2);
        float distance = dest.distanceTo(location);
        lastKnownDistance = distance;

        Log.d(TAG, "Distance to destination: " + (int) distance + "m (alert at " + alerta + "m)");

        // Adjust update interval dynamically — re-register only when band changes
        long newInterval = calculateUpdateInterval(distance);
        if (Math.abs(newInterval - currentUpdateInterval) > 5_000) {
            currentUpdateInterval = newInterval;
            startLocationUpdates(); // re-register with new interval
        }

        if ((int) distance < alerta) {
            triggerAlarm();
            return;
        }

        updateTrackingNotification(distance);
    }

    // ── Interval calculation ──────────────────────────────────────────────

    private long calculateUpdateInterval(float distance) {
        if (distance > 3000f)  return 60_000;
        if (distance >= 2000f) return 30_000;
        if (distance >= 100f)  return 10_000;
        return 2_000; // Very close — 2s is enough without draining battery
    }

    // ── Alarm trigger ─────────────────────────────────────────────────────

    private void triggerAlarm() {
        if (alarmFired) return;
        alarmFired = true;

        Log.d(TAG, "Alarm triggered!");

        // Clear saved state so a service restart doesn't re-trigger the alarm
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply();

        try {
            if (locationManager != null) locationManager.removeUpdates(this);
        } catch (SecurityException ignored) {}

        // 1. Acquire WakeLock FIRST — screen must be on before anything else
        try {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                alarmWakeLock = pm.newWakeLock(
                        PowerManager.FULL_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE,
                        "GPSAlert:AlarmWakeLock"
                );
                alarmWakeLock.acquire(120_000); // 2 min max; released when user dismisses
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not acquire wake lock", e);
        }

        // 2. Play alarm sound directly from the service — user hears it immediately
        //    regardless of whether Alarma activity ever opens
        try {
            alarmPlayer = MediaPlayer.create(this, R.raw.ring);
            if (alarmPlayer != null) {
                alarmPlayer.setLooping(true);
                alarmPlayer.start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not start alarm sound", e);
        }

        Intent alarmIntent = new Intent(this, Alarma.class);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // 3. Try startActivity immediately after WakeLock turns screen on
        try {
            startActivity(alarmIntent);
            Log.d(TAG, "startActivity succeeded");
        } catch (Exception e) {
            Log.d(TAG, "startActivity blocked — relying on full-screen intent");
        }

        // 4. Post full-screen intent notification — this is the reliable path on
        //    locked/background devices. PRIORITY_MAX + CATEGORY_ALARM bypasses DnD.
        int piFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            piFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent fullScreenPI = PendingIntent.getActivity(this, 1, alarmIntent, piFlags);

        NotificationCompat.Builder alarmBuilder =
                new NotificationCompat.Builder(this, ALARM_CHANNEL_ID)
                        .setSmallIcon(R.drawable.noti)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.noTePases))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setContentIntent(fullScreenPI)   // tap → opens Alarma directly
                        .setFullScreenIntent(fullScreenPI, true)
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.cancel(NOTIFICATION_ID);
            nm.notify(ALARM_NOTIFICATION_ID, alarmBuilder.build());
        }

        // 5. Retry startActivity after 500ms — gives full-screen intent time to fire
        //    and covers the race where screen wasn't fully on yet
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                startActivity(alarmIntent);
                Log.d(TAG, "startActivity retry succeeded");
            } catch (Exception e) {
                Log.d(TAG, "startActivity retry also blocked — full-screen intent is handling it");
            }
        }, 500);
    }

    // ── Notifications ──────────────────────────────────────────────────────

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm == null) return;

            // Tracking channel — quiet, ongoing
            NotificationChannel trackingChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            trackingChannel.setDescription("Shows distance to destination while tracking");
            trackingChannel.enableVibration(false);
            trackingChannel.enableLights(false);
            trackingChannel.setSound(null, null);
            nm.createNotificationChannel(trackingChannel);

            // Alarm channel — IMPORTANCE_HIGH + sound required for fullScreenIntent to fire
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            AudioAttributes audioAttr = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            NotificationChannel alarmChannel = new NotificationChannel(
                    ALARM_CHANNEL_ID,
                    "GPS Alert Alarm",
                    NotificationManager.IMPORTANCE_HIGH
            );
            alarmChannel.setDescription("Fires when you reach your destination");
            alarmChannel.enableVibration(true);
            alarmChannel.setVibrationPattern(new long[]{0, 500, 200, 500});
            alarmChannel.setSound(alarmSound, audioAttr);
            alarmChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            alarmChannel.setBypassDnd(true);
            nm.createNotificationChannel(alarmChannel);
        }
    }

    private NotificationCompat.Builder buildTrackingNotification(Float distanceMeters) {
        Intent goApp = new Intent(this, MapsActivity.class);
        goApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        goApp.putExtra("latitude2", latitude2);
        goApp.putExtra("longitude2", longitude2);
        goApp.putExtra("trakingOn", true);
        goApp.putExtra("traking", true);
        goApp.setAction("com.gaston.lesbegueris.notepases");

        int piFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) piFlags |= PendingIntent.FLAG_IMMUTABLE;

        PendingIntent pIntent = PendingIntent.getActivity(this, NOTIFICATION_ID, goApp, piFlags);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.noti)
                        .setContentIntent(pIntent)
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setOnlyAlertOnce(true)
                        .setAutoCancel(false)
                        .setOngoing(true);

        if (distanceMeters == null) {
            builder.setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.recibiendo));
        } else {
            DistanceFormatter.Display display = DistanceFormatter.formatDistance(this, distanceMeters);
            builder.setContentTitle(getString(R.string.falta) + " " + display.value + " " + display.unit);
        }

        return builder;
    }

    private void updateTrackingNotification(float distanceMeters) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify(NOTIFICATION_ID, buildTrackingNotification(distanceMeters).build());
        }
    }

    // ── LocationListener stubs ────────────────────────────────────────────

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(String provider) {}
    @Override public void onProviderDisabled(String provider) {}

    // ── Lifecycle ─────────────────────────────────────────────────────────

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(this);
            } catch (SecurityException ignored) {}
        }
    }
}
