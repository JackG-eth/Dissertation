package com.example.epic.ui.Classes;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/*
    NetworkStatus checks the users current internet connection before performing specific tasks.
 */
public class NetworkStatus {

    private static NetworkStatus instance = new NetworkStatus();

    private static Context context;
    private ConnectivityManager connectivityManager;

    boolean connected = false;

    public static NetworkStatus getInstance(Context ctx) {
        context = ctx.getApplicationContext();
        return instance;
    }


    /*
        Check whether the user has an internet connection or not.
     */
    public boolean isOnline() {
        try {
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() &&
                    networkInfo.isConnected();
            return connected;


        } catch (Exception e) {
            System.out.println("CheckConnectivity Exception: " + e.getMessage());
        }
        return connected;
    }

}
