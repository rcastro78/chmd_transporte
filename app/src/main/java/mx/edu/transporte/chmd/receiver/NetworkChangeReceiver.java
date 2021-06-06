package mx.edu.transporte.chmd.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.query.Select;

import java.util.List;

import mx.edu.transporte.chmd.InicioActivity;
import mx.edu.transporte.chmd.modelosDB.AlumnoDB;
import mx.edu.transporte.chmd.servicios.SincronizacionService;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (checkInternet(context)) {
            //Si hay internet, verificar las tablas de asistencia para enviarlas a la base de datos
            //del servidor.
            //tomar todos los registros de AlumnoDB

            Intent sincroService = new Intent(context, SincronizacionService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(sincroService);
            }else{
                context.startService(sincroService);
            }

        }else{
            Toast.makeText(context, "No hay red -> Trabajando en modo offline", Toast.LENGTH_LONG).show();
            Intent sincroService = new Intent(context, SincronizacionService.class);
            context.stopService(sincroService);
        }
    }

    boolean checkInternet(Context context) {
        ServiceManager serviceManager = new ServiceManager(context);
        if (serviceManager.isNetworkAvailable()) {
            return true;
        } else {
            return false;
        }
    }


    class ServiceManager {

        Context context;

        public ServiceManager(Context base) {
            context = base;
        }

        public boolean isNetworkAvailable() {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }
}
