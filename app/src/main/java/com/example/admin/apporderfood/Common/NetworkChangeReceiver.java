package com.example.admin.apporderfood.Common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkChangeReceiver extends BroadcastReceiver {

    public static final String NETWORK_CHANGE_ACTION = "com.androiderstack.broadcastreceiverdemo.NetworkChangeReceiver";

    @Override
    public void onReceive(final Context context, final Intent intent) {

        if (isOnline(context))
        {
            sendInternalBroadcast(context, "Internet Connected");
        }
        else
        {
            sendInternalBroadcast(context, "Internet Not Connected");
        }
    }

    /**
     * This method is responsible to send status by internal broadcast
     *
     * @param context
     * @param status
     * */
    private void sendInternalBroadcast(Context context, String status)
    {
        try
        {
            Intent intent = new Intent();
            intent.putExtra("status", status);
            intent.setAction(NETWORK_CHANGE_ACTION);
            context.sendBroadcast(intent);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Check if network available or not
     *
     * @param context
     * */
    public static boolean isOnline(Context context)
    {
        boolean isOnline = false;
        try
        {
            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = connManager.getActiveNetworkInfo();

            isOnline = (netInfo != null && netInfo.isConnected());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return isOnline;
    }

}
