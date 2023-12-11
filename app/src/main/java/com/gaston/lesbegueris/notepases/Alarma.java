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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
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
    private InterstitialAd mInterstitialAd;
    protected GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarma);

        mInterstitialAd = new InterstitialAd(this);
        //TEST
        //mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        //REAL
        mInterstitialAd.setAdUnitId("ca-app-pub-9841764898906750/8552541624");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());


        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        final Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        final MediaPlayer ring = MediaPlayer.create(Alarma.this, R.raw.ring);
        ring.start();

        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.shake);
        btnAlarma = findViewById(R.id.btnAlarma);
        btnAlarma.setAnimation(myAnim);
        btnAlarma.startAnimation(myAnim);

        btnAlarma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTraking();
                stopLocationUpdates();

                btnAlarma = findViewById(R.id.btnAlarma);
                btnAlarma.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stopTraking();
                        ring.stop();
                        ring.release();
                        displayInterstitial();
                    }
                });


            }


        });
    }
    protected void stopLocationUpdates() {
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, (LocationListener) this);
        }

    }
    public void displayInterstitial(){
        // If Ads are loaded, show Interstitial else show nothing.
        if (mInterstitialAd.isLoaded()) {

        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    public void stopTraking(){

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(333);
        Intent intent = new Intent(Alarma.this, MainActivity.class); startActivity(intent);
        finish();
        startActivity(intent);
        Intent j = new Intent(Alarma.this, Traking.class);
        stopService(j);

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.commit();
        stopLocationUpdates();



        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }

    }

}
