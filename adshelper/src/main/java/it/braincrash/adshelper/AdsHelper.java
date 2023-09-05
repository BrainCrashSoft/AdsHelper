package it.braincrash.adshelper;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.FrameLayout;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdsHelper {
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);

    public static final int TYPE_APP_OPEN = 1;                // Apertura app	            ca-app-pub-3940256099942544/3419835294
    public static final int TYPE_ADAPTIVE_BANNER = 2;         // Banner adattivo	        ca-app-pub-3940256099942544/9214589741
    public static final int TYPE_BANNER = 3;                  // Banner	                ca-app-pub-3940256099942544/6300978111
    public static final int TYPE_INTERSTITIAL = 4;            // Interstiziale	            ca-app-pub-3940256099942544/1033173712
    public static final int TYPE_INTERSTITIAL_VIDEO = 5;      // Video interstitial	    ca-app-pub-3940256099942544/8691691433
    public static final int TYPE_REWARDED = 6;                // Con premio	            ca-app-pub-3940256099942544/5224354917
    public static final int TYPE_REWARDED_INTERSTITIAL = 7;   // Interstitial con premio	ca-app-pub-3940256099942544/5354046379
    public static final int TYPE_NATIVE_ADVANCED = 8;         // Nativo avanzato	        ca-app-pub-3940256099942544/2247696110
    public static final int TYPE_NATIVE_ADVANCED_VIDEO = 9;   // Video nativo avanzato	    ca-app-pub-3940256099942544/1044960115

    private AdView adView;
    private com.google.android.ump.ConsentInformation consentInformation;

    private final String TAG = "dbug";

    private final Context context;
    private final String adUnitID;
    private final int viewID;
    private final int adFormat;
    private final boolean debug;
    private List<String> testDevices;

    public AdsHelper(final Context context, final String adUnitID, final int viewID, int adFormat, boolean debug) {
        this.context = context;
        this.viewID = viewID;
        this.adUnitID = adUnitID;
        this.adFormat = adFormat;
        this.debug = debug;
    }

    public void setTestDevices(List<String> testDevices) {
        this.testDevices = testDevices;
    }

    public void showAds() {
        ConsentRequestParameters params = new ConsentRequestParameters
                .Builder()
                .setTagForUnderAgeOfConsent(false)
                .build();

        consentInformation = UserMessagingPlatform.getConsentInformation(context);
        consentInformation.requestConsentInfoUpdate(
                (Activity) context,
                params,
                (com.google.android.ump.ConsentInformation.OnConsentInfoUpdateSuccessListener) () -> {
                    // TODO: Load and show the consent form.
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                            (Activity) context,
                            (ConsentForm.OnConsentFormDismissedListener) loadAndShowError -> {
                                if (loadAndShowError != null) {
                                    // Consent gathering failed.
                                    Log.w(TAG, String.format("%s: %s",
                                            loadAndShowError.getErrorCode(),
                                            loadAndShowError.getMessage()));
                                }

                                // Consent has been gathered.
                                if (consentInformation.canRequestAds()) {
                                    processAds();
                                }

                            }
                    );
                },
                requestConsentError -> {
                    // Consent gathering failed.
                    Log.w(TAG, String.format("%s: %s",
                            requestConsentError.getErrorCode(),
                            requestConsentError.getMessage()));
                });

        if (consentInformation.canRequestAds()) {
            processAds();
        }
    }


    private void processAds() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return;
        }
        //		Log.d(TAG, "Loading ADS");

        if(testDevices != null) {
            RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(testDevices).build();
            MobileAds.setRequestConfiguration(configuration);
        } else if (debug) {
            List<String> testDeviceIds = Arrays.asList(AdRequest.DEVICE_ID_EMULATOR);
            RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
            MobileAds.setRequestConfiguration(configuration);
        }

        MobileAds.initialize(context, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                switch (adFormat) {
                    case (TYPE_APP_OPEN) :
                        break;
                    case (TYPE_ADAPTIVE_BANNER) :
                        showAdaptiveBanner();
                        break;
                    case (TYPE_BANNER) :
                        break;
                    default:

                }

            }
        });

    }


    private void showAdaptiveBanner() {
        adView = new AdView(context);
        adView.setAdSize(getAdSize());
        if(debug) {
            adView.setAdUnitId("ca-app-pub-3940256099942544/9214589741");
        } else {
            adView.setAdUnitId(adUnitID);
        }

        FrameLayout adContainerView;
        adContainerView = ((Activity) context).findViewById(viewID);
        adContainerView.addView(adView);

        AdRequest adRequest = new AdRequest.Builder().build();

        // Start loading the ad in the background.
        adView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        // Step 1 - Determine the screen width (less decorations) to use for the ad width.
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 2 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
    }


    public void onResume() {
        if (adView != null) adView.resume();
    }

    public void onPause() {
        if (adView != null) adView.pause();
    }

    public void onDestroy() {
        if (adView != null) adView.destroy();
    }


}
