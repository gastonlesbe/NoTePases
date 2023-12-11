package com.gaston.lesbegueris.notepases;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ViewFlipper;

/**
 * Created by gaston on 14/12/17.
 */

public class TutoActivity extends AppCompatActivity {

    private ViewFlipper mViewFlipper;
    private Context mContext;
    private float initialX;
    ImageButton btnClose;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tuto);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.closetoolbar);
        setSupportActionBar(myToolbar);
        actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        myToolbar.inflateMenu(R.menu.menuclose);
        myToolbar.setTitleTextColor(getResources().getColor(R.color.white));


        mContext = this;
        mViewFlipper = (ViewFlipper) this.findViewById(R.id.viewFlipper);
        mViewFlipper.setAutoStart(true);
        mViewFlipper.setFlipInterval(4000);
        mViewFlipper.startFlipping();

        mViewFlipper.setInAnimation(this, android.R.anim.slide_in_left); //use either the default slide animation in sdk or create your own ones
        mViewFlipper.setOutAnimation(this, android.R.anim.slide_out_right);

        mViewFlipper.getInAnimation().setAnimationListener(new Animation.AnimationListener() {

            public void onAnimationStart(Animation animation) {}
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationEnd(Animation animation) {

                int displayedChild = mViewFlipper.getDisplayedChild();
                int childCount = mViewFlipper.getChildCount();

                if (displayedChild == childCount - 1) {
                    mViewFlipper.stopFlipping();
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menuclose, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuclose) {
            // User chose the "Settings" item, show the app settings UI...
            Intent e = new Intent(TutoActivity.this, MainActivity.class);
            startActivity(e);
            finish();
            return true;
        }
        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item);
    }

}
