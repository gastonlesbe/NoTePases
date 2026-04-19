package com.gaston.lesbegueris.notepases;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class Alarma extends AppCompatActivity {

    ImageButton btnAlarma;
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "lastLocation";
    private MediaPlayer ring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ── Wake screen and show over lock screen ──────────────────────────
        // Must be called BEFORE super.onCreate() and setContentView()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (km != null) km.requestDismissKeyguard(this, null);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            );
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarma);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        // Use the sound already started by Traking service if available,
        // otherwise start our own (covers the case where Alarma opens independently)
        if (Traking.alarmPlayer == null) {
            ring = MediaPlayer.create(this, R.raw.ring);
            if (ring != null) {
                ring.setLooping(true);
                ring.start();
            }
        }

        btnAlarma = findViewById(R.id.btnAlarma);

        // Shake animation
        final Animation shakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake);
        btnAlarma.startAnimation(shakeAnim);

        // Pulsing sonar rings
        startRingPulse(findViewById(R.id.ring1), 0);
        startRingPulse(findViewById(R.id.ring2), 700);
        startRingPulse(findViewById(R.id.ring3), 1400);

        btnAlarma.setOnClickListener(v -> dismiss());
    }

    private void dismiss() {
        // Stop whichever player is running
        Traking.stopAlarmSound();
        if (ring != null) {
            try { ring.stop(); ring.release(); } catch (Exception ignored) {}
            ring = null;
        }
        releaseWakeLock();
        stopTraking();
    }

    private void releaseWakeLock() {
        if (Traking.alarmWakeLock != null && Traking.alarmWakeLock.isHeld()) {
            Traking.alarmWakeLock.release();
            Traking.alarmWakeLock = null;
        }
    }

    private void startRingPulse(View ring, long delay) {
        if (ring == null) return;
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ring, "scaleX", 0.15f, 1.6f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ring, "scaleY", 0.15f, 1.6f);
        ObjectAnimator alpha  = ObjectAnimator.ofFloat(ring, "alpha",  0.85f, 0f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(2100);
        set.setStartDelay(delay);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                ring.setScaleX(0.15f);
                ring.setScaleY(0.15f);
                set.setStartDelay(0);
                set.start();
            }
        });
        set.start();
    }

    public void stopTraking() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.cancel(333);
            nm.cancel(334); // also cancel alarm notification
        }
        stopService(new Intent(this, Traking.class));
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.commit();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Traking.stopAlarmSound();
        if (ring != null) {
            try { ring.release(); } catch (Exception ignored) {}
            ring = null;
        }
        releaseWakeLock();
    }
}
