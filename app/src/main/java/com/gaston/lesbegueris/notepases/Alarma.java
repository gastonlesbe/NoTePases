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
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
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
    private MediaPlayer ringPlayer;
    private InterstitialAd interstitialAd;
    private boolean pendingInterstitial;
    private final Handler adHandler = new Handler(Looper.getMainLooper());
    private final Runnable adTimeout = new Runnable() {
        @Override
        public void run() {
            if (pendingInterstitial) {
                pendingInterstitial = false;
                goToMain();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarma);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        final Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        ringPlayer = MediaPlayer.create(Alarma.this, R.raw.ring);
        ringPlayer.start();

        MobileAds.initialize(this);
        loadInterstitial();

        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.shake);
        btnAlarma = findViewById(R.id.btnAlarma);
        btnAlarma.setAnimation(myAnim);
        btnAlarma.startAnimation(myAnim);

        btnAlarma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAlarm();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP) {
            dismissAlarm();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void dismissAlarm() {
        stopLocationUpdates();
        stopAlarmPlayback();
        stopTrackingSilently();
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
                        if (pendingInterstitial) {
                            showInterstitialThenReturnToMain();
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        interstitialAd = null;
                    }
                });
    }

    private void showInterstitialThenReturnToMain() {
        if (interstitialAd == null) {
            pendingInterstitial = true;
            adHandler.removeCallbacks(adTimeout);
            adHandler.postDelayed(adTimeout, 2000);
            loadInterstitial();
            return;
        }
        pendingInterstitial = false;
        adHandler.removeCallbacks(adTimeout);
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

    private void stopAlarmPlayback() {
        if (ringPlayer != null) {
            try {
                if (ringPlayer.isPlaying()) {
                    ringPlayer.stop();
                }
            } catch (IllegalStateException ignored) {
                // Ignore if player is not in a valid state.
            }
            ringPlayer.release();
            ringPlayer = null;
        }
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

    public void stopTraking(){
        stopTrackingSilently();
        goToMain();
    }

    private void stopTrackingSilently(){

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(333);
        Intent j = new Intent(Alarma.this, Traking.class);
        stopService(j);

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.commit();
        stopLocationUpdates();

    }

    private void goToMain() {
        Intent intent = new Intent(Alarma.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
