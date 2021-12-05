package mx.edu.transporte.chmd.servicios;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.legacy.content.WakefulBroadcastReceiver;

import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import mx.edu.transporte.chmd.AppTransporte;
import mx.edu.transporte.chmd.R;
import mx.edu.transporte.chmd.modelosDB.AlumnoDB;

public class NetworkChangeReceiver extends BroadcastReceiver {
    static String METODO_ALUMNO_ASISTE="asistenciaAlumno.php";
    static String BASE_URL;
    static String PATH;
    private static String TURNO_MAN="1",TURNO_TAR="2";


    @Override
    public void onReceive(final Context context, final Intent intent) {

        BASE_URL = context.getString(R.string.BASE_URL);
        PATH = context.getString(R.string.PATH);

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                Toast.makeText(context, "Trabajando con datos", Toast.LENGTH_LONG).show();


                Intent sincroService = new Intent(context, SincronizacionService.class);
                if (Build.VERSION.SDK_INT >= 26) {
                    context.startForegroundService(sincroService);
                }else{
                    context.startService(sincroService);
                }

            }




        } else {
            Toast.makeText(context, "Trabajando sin Internet", Toast.LENGTH_LONG).show();
            Intent sincroService = new Intent(context, SincronizacionService.class);
            context.stopService(sincroService);
        }
    }


    public void registraAscenso(final String alumno_id, final String ruta_id, final String turno){
        Log.d("PROCESAR",BASE_URL+PATH+METODO_ALUMNO_ASISTE+"?id_alumno="+alumno_id+"&id_ruta="+ruta_id);

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_ALUMNO_ASISTE+"?id_alumno="+alumno_id+"&id_ruta="+ruta_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if(response.length()<=0){

                        }else {
                            new Update(AlumnoDB.class)
                                    .set("procesado=2")
                                    .where("idAlumno=? AND id_ruta_h=?",alumno_id,ruta_id)
                                    .execute();
                        }

                        try {
                            for(int i=0; i<response.length(); i++){
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                //inasistencias  = jsonObject.getString("recuento");

                            }



                        }catch (JSONException e)
                        {
                            e.printStackTrace();

                        }

                        //Obtener ascensos, sumarlo con las inasistencias, comparar con el
                        //total de alumnos en la ruta



                    }
                }, error -> VolleyLog.d("ERROR", "Error: " + error.getMessage()));

        // Adding request to request queue
        AppTransporte.getInstance().addToRequestQueue(req);

    }


}
