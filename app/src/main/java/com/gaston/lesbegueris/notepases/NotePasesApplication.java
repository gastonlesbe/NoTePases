package com.gaston.lesbegueris.notepases;

import android.app.Application;
import com.gaston.lesbegueris.notepases.util.ThemeHelper;

public class NotePasesApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThemeHelper.applyTheme(this);
    }
}
