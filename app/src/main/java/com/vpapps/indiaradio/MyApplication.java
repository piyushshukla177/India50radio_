package com.vpapps.indiaradio;

import android.app.Application;
import android.os.StrictMode;

import com.facebook.FacebookSdk;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.vpapps.utils.Constant;
import com.vpapps.utils.DBHelper;
import com.vpapps.utils.SharedPref;
import com.onesignal.OneSignal;
import com.vpapps.indiaradio.R;

import androidx.appcompat.app.AppCompatDelegate;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;

public class MyApplication extends Application {

    SharedPref sharedPref;

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPref = new SharedPref(getApplicationContext());

        FirebaseAnalytics.getInstance(getApplicationContext());

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/poppins_reg.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

        OneSignal.startInit(getApplicationContext())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .init();

        DBHelper dbHelper = new DBHelper(getApplicationContext());
        dbHelper.onCreate(dbHelper.getWritableDatabase());
        dbHelper.getAbout();

        MobileAds.initialize(getApplicationContext());

        FacebookSdk.sdkInitialize(getApplicationContext());

        AudienceNetworkAds.initialize(this);

        sharedPref = new SharedPref(this);

        String mode = sharedPref.getDarkMode();
        switch (mode) {
            case Constant.DARK_MODE_SYSTEM:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case Constant.DARK_MODE_OFF:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case Constant.DARK_MODE_ON:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
    }
}