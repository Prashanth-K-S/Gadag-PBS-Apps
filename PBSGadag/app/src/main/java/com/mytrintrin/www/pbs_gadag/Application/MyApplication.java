package com.mytrintrin.www.pbs_gadag.Application;

import android.app.Application;

import com.mytrintrin.www.pbs_gadag.Services.ConnectivityReceiver;

/**
 * Created by siteurl on 18/1/18.
 */

public class MyApplication extends Application {

    private static MyApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }
}
