package it.braincrash.adshelper;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;



public class MainActivity extends AppCompatActivity {

    private AdsHelper adshelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        adshelper = new AdsHelper(this, "", R.id.ad_view_container, AdsHelper.TYPE_ADAPTIVE_BANNER, true);
//        adshelper.setTestDevices(testDevices);
        adshelper.showAds();

    }

    @Override
    protected void onPause() {
        super.onPause();
        adshelper.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adshelper.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adshelper.onDestroy();
    }
}