package com.vpapps.indiaradio;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.vpapps.interfaces.AdConsentListener;
import com.vpapps.utils.AdConsent;
import com.vpapps.utils.Constant;
import com.vpapps.utils.Methods;
import com.vpapps.utils.SharedPref;
import com.vpapps.indiaradio.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.viewpager.widget.ViewPager;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class SettingActivity extends AppCompatActivity {

    Toolbar toolbar;
    SharedPref sharedPref;
    Methods methods;
    AdConsent adConsent;
    ConstraintLayout ll_theme;
    LinearLayout ll_consent, ll_adView;
    SwitchCompat switch_consent, switch_noti;
    Boolean isNoti = true;
    ImageView iv_theme;
    TextView tv_privacy, tv_about, tv_moreapp, tv_rateapp, tv_shareapp, tv_theme;
    String them_mode = "";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        sharedPref = new SharedPref(this);
        methods = new Methods(this);
        methods.forceRTLIfSupported(getWindow());
        methods.setStatusColor(getWindow());

        isNoti = sharedPref.getIsNotification();
        them_mode = methods.getDarkMode();

        toolbar = this.findViewById(R.id.toolbar_setting);
        toolbar.setTitle(getString(R.string.settings));
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adConsent = new AdConsent(this, new AdConsentListener() {
            @Override
            public void onConsentUpdate() {
                setConsentSwitch();
            }
        });

        ll_theme = findViewById(R.id.ll_theme);
        ll_consent = findViewById(R.id.ll_consent);
        switch_noti = findViewById(R.id.switch_noti);
        switch_consent = findViewById(R.id.switch_consent);
        iv_theme = findViewById(R.id.iv_theme);
        tv_theme = findViewById(R.id.tv_theme);
        tv_rateapp = findViewById(R.id.tv_rateapp);
        tv_shareapp = findViewById(R.id.tv_shareapp);
        tv_moreapp = findViewById(R.id.tv_moreapp);
        tv_about = findViewById(R.id.tv_about);
        tv_privacy = findViewById(R.id.tv_privacy);
        ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);

        if (adConsent.isUserFromEEA()) {
            setConsentSwitch();
        } else {
            ll_consent.setVisibility(View.GONE);
        }
        if (isNoti) {
            switch_noti.setChecked(true);
        } else {
            switch_noti.setChecked(false);
        }

        if (methods.isDarkMode()) {
            iv_theme.setImageResource(R.mipmap.mode_dark);
        } else {
            iv_theme.setImageResource(R.mipmap.mode_icon);
        }

        String mode = methods.getDarkMode();
        switch (mode) {
            case Constant.DARK_MODE_SYSTEM:
                tv_theme.setText(getString(R.string.system_default));
                break;
            case Constant.DARK_MODE_OFF:
                tv_theme.setText(getString(R.string.light));
                break;
            case Constant.DARK_MODE_ON:
                tv_theme.setText(getString(R.string.dark));
                break;
        }

        ll_theme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openThemeDialog();
            }
        });

        switch_noti.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                OneSignal.setSubscription(isChecked);
                sharedPref.setIsNotification(isChecked);
            }
        });

        switch_consent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ConsentInformation.getInstance(SettingActivity.this).setConsentStatus(ConsentStatus.PERSONALIZED);
                } else {
                    ConsentInformation.getInstance(SettingActivity.this).setConsentStatus(ConsentStatus.NON_PERSONALIZED);
                }
            }
        });

        tv_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        tv_rateapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String appName = getPackageName();//your application package name i.e play store application url
                try {
//                    startActivity(new Intent(Intent.ACTION_VIEW,
//                            Uri.parse("market://details?id="
//                                    + appName)));
                } catch (android.content.ActivityNotFoundException anfe) {
//                    startActivity(new Intent(
//                            Intent.ACTION_VIEW,
//                            Uri.parse("http://play.google.com/store/apps/details?id="
//                                    + appName)));
                }
            }
        });

        tv_shareapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ishare = new Intent(Intent.ACTION_SEND);
                ishare.setType("text/plain");
                ishare.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.app_name) + " - http://play.google.com/store/apps/details?id=" + getPackageName());
                //ishare.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.app_name) + " - http://play.google.com/store/apps/details?id=" + "com.india50.plusradio");
                startActivity(ishare);
            }
        });

        tv_privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPrivacyDialog();
            }
        });

        tv_moreapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_more_apps))));
            }
        });

        ll_consent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adConsent.requestConsent();
            }
        });

        changeThemeColor();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private void openThemeDialog() {
        final Dialog dialog = new Dialog(SettingActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_theme);
        if (getString(R.string.isRTL).equals("true")) {
            dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        dialog.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        RadioGroup radioGroup = dialog.findViewById(R.id.radioGroup_them);
        MaterialButton btn_ok = dialog.findViewById(R.id.textView_ok_them);
        MaterialButton btn_cancel = dialog.findViewById(R.id.textView_cancel_them);

        String mode = methods.getDarkMode();
        assert mode != null;
        switch (mode) {
            case Constant.DARK_MODE_SYSTEM:
                radioGroup.check(radioGroup.getChildAt(0).getId());
                break;
            case Constant.DARK_MODE_OFF:
                radioGroup.check(radioGroup.getChildAt(1).getId());
                break;
            case Constant.DARK_MODE_ON:
                radioGroup.check(radioGroup.getChildAt(2).getId());
                break;
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                MaterialRadioButton rb = group.findViewById(checkedId);
                if (null != rb && checkedId > -1) {
                    switch (checkedId) {
                        case R.id.radioButton_system_them:
                            them_mode = Constant.DARK_MODE_SYSTEM;
                            break;
                        case R.id.radioButton_light_them:
                            them_mode = Constant.DARK_MODE_OFF;
                            break;
                        case R.id.radioButton_dark_them:
                            them_mode = Constant.DARK_MODE_ON;
                            break;
                        default:
                            break;
                    }
                }
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPref.setDarkMode(them_mode);
                switch (them_mode) {
                    case Constant.DARK_MODE_SYSTEM:
                        tv_theme.setText(getResources().getString(R.string.system_default));
                        break;
                    case Constant.DARK_MODE_OFF:
                        tv_theme.setText(getResources().getString(R.string.light));
                        break;
                    case Constant.DARK_MODE_ON:
                        tv_theme.setText(getResources().getString(R.string.dark));
                        break;
                    default:
                        break;
                }
                dialog.dismiss();

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
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void setConsentSwitch() {
        if (ConsentInformation.getInstance(this).getConsentStatus() == ConsentStatus.PERSONALIZED) {
            switch_consent.setChecked(true);
        } else {
            switch_consent.setChecked(false);
        }
    }

    public void openPrivacyDialog() {
        Dialog dialog;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog = new Dialog(SettingActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            dialog = new Dialog(SettingActivity.this);
        }

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_privacy);

        WebView webview = dialog.findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        String mimeType = "text/html;charset=UTF-8";
        String encoding = "utf-8";

        if (Constant.itemAbout != null) {
            String text = "<html><head>"
                    + "<style> body{color: #000 !important;text-align:left}"
                    + "</style></head>"
                    + "<body>"
                    + Constant.itemAbout.getPrivacy()
                    + "</body></html>";

            webview.setBackgroundColor(Color.TRANSPARENT);
            webview.loadDataWithBaseURL("blarg://ignored", text, mimeType, encoding, "");
        }

        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void changeThemeColor() {

        int[][] states = new int[][]{
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_checked},
        };

        int[] thumbColors = new int[]{
                ContextCompat.getColor(SettingActivity.this, R.color.switch_thumb_disable),
                ContextCompat.getColor(SettingActivity.this, R.color.primary),
        };

        int[] trackColors = new int[]{
                ContextCompat.getColor(SettingActivity.this, R.color.black40_dark),
                ContextCompat.getColor(SettingActivity.this, R.color.black40_dark),
        };
        DrawableCompat.setTintList(DrawableCompat.wrap(switch_noti.getThumbDrawable()), new ColorStateList(states, thumbColors));
        DrawableCompat.setTintList(DrawableCompat.wrap(switch_noti.getTrackDrawable()), new ColorStateList(states, trackColors));
        DrawableCompat.setTintList(DrawableCompat.wrap(switch_consent.getThumbDrawable()), new ColorStateList(states, thumbColors));
        DrawableCompat.setTintList(DrawableCompat.wrap(switch_consent.getTrackDrawable()), new ColorStateList(states, trackColors));
    }
}