package com.gaston.lesbegueris.notepases.util;

import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;
import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerView;
import com.appodeal.ads.BannerCallbacks;

/**
 * Helper class for Appodeal banner ads integration
 */
public class AppodealHelper {
    private static final String TAG = "AppodealHelper";
    private static boolean isInitialized = false;
    private static String initializedAppKey = null;

    /**
     * Initialize Appodeal SDK with app key
     */
    public static void initialize(Activity activity, String appKey) {
        if (isInitialized && initializedAppKey != null && initializedAppKey.equals(appKey)) {
            Log.d(TAG, "Appodeal already initialized with same app key");
            return;
        }
        
        if (isInitialized && initializedAppKey != null && !initializedAppKey.equals(appKey)) {
            Log.d(TAG, "Appodeal initialized with different app key, re-initializing...");
            isInitialized = false;
            initializedAppKey = null;
        }

        try {
            Log.d(TAG, "Initializing Appodeal with app key: " + (appKey != null ? appKey.substring(0, Math.min(10, appKey.length())) + "..." : "null"));
            
            // Set banner callbacks before initialization
            Appodeal.setBannerCallbacks(new BannerCallbacks() {
                @Override
                public void onBannerLoaded(int height, boolean isPrecache) {
                    Log.d(TAG, "Banner loaded successfully, height: " + height);
                    try {
                        Appodeal.show(activity, Appodeal.BANNER);
                    } catch (Exception e) {
                        Log.e(TAG, "Error showing banner after load", e);
                    }
                }

                @Override
                public void onBannerFailedToLoad() {
                    Log.w(TAG, "Banner failed to load");
                    activity.getWindow().getDecorView().postDelayed(() -> {
                        try {
                            Appodeal.cache(activity, Appodeal.BANNER);
                        } catch (Exception e) {
                            Log.e(TAG, "Error retrying banner cache", e);
                        }
                    }, 5000);
                }

                @Override
                public void onBannerShown() {
                    Log.d(TAG, "Banner shown");
                }

                @Override
                public void onBannerShowFailed() {
                    Log.w(TAG, "Banner show failed");
                }

                @Override
                public void onBannerClicked() {
                    Log.d(TAG, "Banner clicked");
                }

                @Override
                public void onBannerExpired() {
                    Log.d(TAG, "Banner expired");
                }
            });
            
            // Initialize Appodeal with BANNER
            Appodeal.initialize(activity, appKey, Appodeal.BANNER);
            isInitialized = true;
            initializedAppKey = appKey;
            Log.d(TAG, "Appodeal initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Appodeal", e);
        }
    }

    /**
     * Show banner ad in the specified container
     */
    public static void showBanner(Activity activity, int containerId) {
        if (!isInitialized) {
            Log.w(TAG, "Appodeal not initialized. Call initialize() first.");
            return;
        }

        try {
            ViewGroup container = activity.findViewById(containerId);
            if (container == null) {
                Log.e(TAG, "Container not found with ID: " + containerId);
                return;
            }

            // Remove any existing banner
            removeBanner(container);

            // Try to get banner view and add to container
            BannerView bannerView = Appodeal.getBannerView(activity);
            if (bannerView != null) {
                container.addView(bannerView);
                Appodeal.show(activity, Appodeal.BANNER);
            } else {
                // Even if bannerView is null, Appodeal.show() will load and show the banner automatically
                Appodeal.show(activity, Appodeal.BANNER);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing banner", e);
        }
    }

    /**
     * Remove banner from container
     */
    private static void removeBanner(ViewGroup container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            if (container.getChildAt(i) instanceof BannerView) {
                container.removeViewAt(i);
                break;
            }
        }
    }

    /**
     * Hide banner ad
     */
    public static void hideBanner(Activity activity) {
        try {
            Appodeal.hide(activity, Appodeal.BANNER);
        } catch (Exception e) {
            Log.e(TAG, "Error hiding banner", e);
        }
    }

    /**
     * Check if Appodeal is initialized
     */
    public static boolean isInitialized() {
        return isInitialized;
    }
}

