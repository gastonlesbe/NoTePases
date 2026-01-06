package com.gaston.lesbegueris.notepases;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;

/**
 * Created by gaston on 05/12/17.
 */

public class Alarma extends AppCompatActivity {

    ImageButton btnAlarma;
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "lastLocation" ;
    protected GoogleApiClient mGoogleApiClient;
    private MediaPlayer ring; // Variable de instancia para poder acceder desde otros métodos
    private Ringtone r; // Variable de instancia para el ringtone

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarma);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        ring = MediaPlayer.create(Alarma.this, R.raw.ring);
        
        // Configurar el MediaPlayer para reproducir en bucle y con volumen máximo
        if (ring != null) {
            ring.setLooping(true); // Reproducir en bucle
            ring.setVolume(1.0f, 1.0f); // Volumen máximo
            ring.start();
            Log.d("Alarma", "Alarm sound started");
        } else {
            Log.e("Alarma", "Failed to create MediaPlayer for alarm sound");
            // Intentar usar el ringtone por defecto como respaldo
            if (r != null) {
                r.play();
            }
        }

        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.shake);
        // Asegurar que la animación se repita infinitamente
        myAnim.setRepeatCount(Animation.INFINITE);
        myAnim.setRepeatMode(Animation.REVERSE);
        
        btnAlarma = findViewById(R.id.btnAlarma);
        btnAlarma.setAnimation(myAnim);
        btnAlarma.startAnimation(myAnim);

        btnAlarma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Detener la animación cuando se hace click
                if (btnAlarma != null) {
                    btnAlarma.clearAnimation();
                }
                stopTraking();
            }
        });
    }
    protected void stopLocationUpdates() {
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, (LocationListener) this);
        }

    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detener la animación
        if (btnAlarma != null) {
            btnAlarma.clearAnimation();
        }
        // Asegurarse de detener el sonido cuando la actividad se destruye
        if (ring != null) {
            try {
                ring.stop();
                ring.release();
                ring = null;
            } catch (Exception e) {
                Log.e("Alarma", "Error stopping alarm sound in onDestroy", e);
            }
        }
        if (r != null) {
            try {
                r.stop();
                r = null;
            } catch (Exception e) {
                Log.e("Alarma", "Error stopping ringtone in onDestroy", e);
            }
        }
    }

    public void stopTraking(){
        // Detener el sonido de la alarma
        if (ring != null) {
            try {
                ring.stop();
                ring.release();
                Log.d("Alarma", "Alarm sound stopped");
            } catch (Exception e) {
                Log.e("Alarma", "Error stopping alarm sound", e);
            }
        }
        if (r != null) {
            try {
                r.stop();
            } catch (Exception e) {
                Log.e("Alarma", "Error stopping ringtone", e);
            }
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(333);
        }
        
        // Detener el servicio de tracking
        Intent j = new Intent(Alarma.this, Traking.class);
        stopService(j);

        // Limpiar preferencias
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.commit();
        stopLocationUpdates();

        // Ir a MainActivity
        Intent intent = new Intent(Alarma.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

}
