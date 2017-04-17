package com.jhonn_aj.descubreperuandroid;

import android.app.Application;

import com.jhonn_aj.descubreperuandroid.app.utils.NetworkChangeReceiver;

/**
 * Created by jhonn_aj on 14/04/2017.
 */

public class MyApp extends Application {

    private static MyApp mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;

    }

    public static synchronized MyApp getInstance(){
        return mInstance;
    }

    public void setConnectivityListener(NetworkChangeReceiver.receiverListener listener){
        NetworkChangeReceiver.listener = listener;
    }
}
