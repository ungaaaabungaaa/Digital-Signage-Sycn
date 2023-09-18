package com.AmmasPastriesSycn;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {
    public static boolean isNetworkAvailable(Context context) {
        // Get the system service for connectivity
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Check if the connectivity manager is not null
        if (connectivityManager != null) {
            // Get the active network info
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            // Return true if networkInfo is not null and the device is connected
            return networkInfo != null && networkInfo.isConnected();
        }

        // Return false if connectivity manager is null
        return false;
    }
}
