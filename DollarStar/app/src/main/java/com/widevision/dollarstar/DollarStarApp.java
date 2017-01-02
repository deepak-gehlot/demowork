package com.widevision.dollarstar;

import android.app.Application;

import com.facebook.FacebookSdk;

/**
 * Created by mercury-five on 17/06/16.
 */
public class DollarStarApp extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
    }
}
