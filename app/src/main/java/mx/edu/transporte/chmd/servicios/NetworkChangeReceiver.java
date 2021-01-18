package mx.edu.transporte.chmd.servicios;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                //Toast.makeText(context, "Wifi enabled", Toast.LENGTH_LONG).show();

            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                //Toast.makeText(context, "Mobile data enabled", Toast.LENGTH_LONG).show();

            }
        } else {
            //Toast.makeText(context, "No internet is available", Toast.LENGTH_LONG).show();

        }
    }
}
