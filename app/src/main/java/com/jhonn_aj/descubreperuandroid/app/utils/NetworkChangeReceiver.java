package com.jhonn_aj.descubreperuandroid.app.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by jhonn_aj on 14/04/2017.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {

    private boolean isConnect = false;
    public static receiverListener listener;

    @Override
    public void onReceive(Context context, Intent intent) {
        isNetworkAvailable(context);
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null){
            NetworkInfo[] infos = manager.getAllNetworkInfo();
            if (infos != null){
                for (int i = 0; i < infos.length; i++){
                    if (infos[i].getState() == NetworkInfo.State.CONNECTED){
                        if (!isConnect){
                           // Toast.makeText(context, " Conectado ", Toast.LENGTH_SHORT).show();
                            isConnect = true;
                            if (listener != null) {
                                listener.onNetworkConnectionChanged(isConnect);
                            }
                        }
                        return true;
                    }
                }
            }
        }
        isConnect = false;
        //Toast.makeText(context, "No hay conexión a internet", Toast.LENGTH_SHORT).show();
        if (listener != null) {
            listener.onNetworkConnectionChanged(isConnect);
        }
        return false;
    }

    public interface receiverListener{
        void onNetworkConnectionChanged(boolean isConnected);
    }

}
