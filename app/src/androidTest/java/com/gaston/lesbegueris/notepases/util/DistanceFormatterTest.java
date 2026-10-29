package com.gaston.lesbegueris.notepases.util;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Regression test for the Android 6.0 (API 23) crash: with units on "system"
 * (the default) the formatter read the locale through an API 24-only method.
 * Run it on an API 23 emulator to cover the old code path.
 */
@RunWith(AndroidJUnit4.class)
public class DistanceFormatterTest {
    private Context context;
    private String savedUnits;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        savedUnits = DistanceFormatter.getUnitsPreference(context);
        DistanceFormatter.setUnitsPreference(context, "system");
    }

    @After
    public void tearDown() {
        DistanceFormatter.setUnitsPreference(context, savedUnits);
    }

    @Test
    public void systemUnits_doesNotCrash() {
        // Before the fix this threw NoSuchMethodError on API 23.
        DistanceFormatter.isImperial(context);
    }

    @Test
    public void systemUnits_formatsTrackingDistance() {
        DistanceFormatter.Display display = DistanceFormatter.formatDistance(context, 1500f);
        assertNotNull(display.value);
        assertFalse(display.unit.isEmpty());
    }

    @Test
    public void explicitUnits_overrideTheLocale() {
        DistanceFormatter.setUnitsPreference(context, "imperial");
        assertTrue(DistanceFormatter.isImperial(context));
        DistanceFormatter.setUnitsPreference(context, "metric");
        assertFalse(DistanceFormatter.isImperial(context));
    }
}
