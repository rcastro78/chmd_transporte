package mx.edu.transporte.chmd.servicios;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import mx.edu.transporte.chmd.AppTransporte;
import mx.edu.transporte.chmd.InicioActivity;
import mx.edu.transporte.chmd.R;
import mx.edu.transporte.chmd.modelosDB.AlumnoDB;

public class SincronizacionService extends Service {
    private static String TURNO_MAN="1",TURNO_TAR="2";
    static String METODO_ALUMNO_ASISTE="asistenciaAlumno.php";
    static String METODO_ALUMNO_SUBE_TARDE="asistenciaAlumnoTarde.php";
    static String METODO_ALUMNO_NO_ASISTE="noAsistenciaAlumno.php";
    static String METODO_ALUMNO_NO_ASISTE_TARDE="noAsistenciaAlumnoTarde.php";
    static String BASE_URL;
    static String PATH;

    Context context;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        BASE_URL = this.getString(R.string.BASE_URL);
        PATH = this.getString(R.string.PATH);

        List<AlumnoDB> alumnos = new Select().from(AlumnoDB.class).where("procesado=1").execute();
        for (AlumnoDB alumno:alumnos) {

            if(Integer.parseInt(alumno.ascenso)>0){
                //Aquí van los de la mañana
                //Procesar los alumnos que han subido
                if(Integer.parseInt(alumno.ascenso)<2)
                    registraAscenso(alumno.id_alumno,alumno.id_ruta_h,TURNO_MAN);
                else
                    registraInasistencia(alumno.id_alumno,alumno.id_ruta_h,TURNO_MAN);
                //actualizar alumnos, poner procesado a -1


                //Procesar los alumnos que no asistieron
           }


          if(Integer.parseInt(alumno.ascenso_t)>0){
              if(Integer.parseInt(alumno.ascenso_t)<2)
                  registraAscensoTarde(alumno.id_alumno,alumno.id_ruta_h,TURNO_TAR);
              else
                  registraInasistenciaTarde(alumno.id_alumno,alumno.id_ruta_h,TURNO_MAN);
          }


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
                                    .set("procesado=-1")
                                    .where("idAlumno=? AND id_ruta_h=?",alumno_id,ruta_id)
                                    .execute();
                        }

                        try {
                            for(int i=0; i<response.length(); i++){
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                //inasistencias  = jsonObject.getString("recuento");

                            }

                            if(turno.equalsIgnoreCase(TURNO_MAN)){


                            }
                            if(turno.equalsIgnoreCase(TURNO_TAR)){


                            }



                        }catch (JSONException e)
                        {
                            e.printStackTrace();

                        }

                        //Obtener ascensos, sumarlo con las inasistencias, comparar con el
                        //total de alumnos en la ruta



                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                VolleyLog.d("ERROR", "Error: " + error.getMessage());


            }
        });

        // Adding request to request queue
        AppTransporte.getInstance().addToRequestQueue(req);

    }
    public void registraInasistencia(final String alumno_id, final String ruta_id, final String turno){
        Log.d("PROCESAR",BASE_URL+PATH+METODO_ALUMNO_ASISTE+"?id_alumno="+alumno_id+"&id_ruta="+ruta_id);

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_ALUMNO_NO_ASISTE+"?id_alumno="+alumno_id+"&id_ruta="+ruta_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if(response.length()<=0){

                        }else {
                            new Update(AlumnoDB.class)
                                    .set("procesado=-1")
                                    .where("idAlumno=? AND id_ruta_h=?",alumno_id,ruta_id)
                                    .execute();
                        }

                        try {
                            for(int i=0; i<response.length(); i++){
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                //inasistencias  = jsonObject.getString("recuento");

                            }

                            if(turno.equalsIgnoreCase(TURNO_MAN)){


                            }
                            if(turno.equalsIgnoreCase(TURNO_TAR)){


                            }



                        }catch (JSONException e)
                        {
                            e.printStackTrace();

                        }

                        //Obtener ascensos, sumarlo con las inasistencias, comparar con el
                        //total de alumnos en la ruta



                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                VolleyLog.d("ERROR", "Error: " + error.getMessage());


            }
        });

        // Adding request to request queue
        AppTransporte.getInstance().addToRequestQueue(req);

    }
    public void registraInasistenciaTarde(final String alumno_id, final String ruta_id, final String turno){
        Log.d("PROCESAR",BASE_URL+PATH+METODO_ALUMNO_NO_ASISTE_TARDE+"?id_alumno="+alumno_id+"&id_ruta="+ruta_id);

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_ALUMNO_NO_ASISTE+"?id_alumno="+alumno_id+"&id_ruta="+ruta_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if(response.length()<=0){

                        }else {
                            new Update(AlumnoDB.class)
                                    .set("procesado=-1")
                                    .where("idAlumno=? AND id_ruta_h=?",alumno_id,ruta_id)
                                    .execute();
                        }

                        try {
                            for(int i=0; i<response.length(); i++){
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                //inasistencias  = jsonObject.getString("recuento");

                            }

                            if(turno.equalsIgnoreCase(TURNO_MAN)){


                            }
                            if(turno.equalsIgnoreCase(TURNO_TAR)){


                            }



                        }catch (JSONException e)
                        {
                            e.printStackTrace();

                        }

                        //Obtener ascensos, sumarlo con las inasistencias, comparar con el
                        //total de alumnos en la ruta



                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                VolleyLog.d("ERROR", "Error: " + error.getMessage());


            }
        });

        // Adding request to request queue
        AppTransporte.getInstance().addToRequestQueue(req);

    }
    public void registraAscensoTarde(final String alumno_id, final String ruta_id, final String turno){
        Log.d("PROCESAR",BASE_URL+PATH+METODO_ALUMNO_ASISTE+"?id_alumno="+alumno_id+"&id_ruta="+ruta_id);

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_ALUMNO_SUBE_TARDE+"?id_alumno="+alumno_id+"&id_ruta="+ruta_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if(response.length()<=0){

                        }else {
                            new Update(AlumnoDB.class)
                                    .set("procesado=-1")
                                    .where("idAlumno=? AND id_ruta_h=?",alumno_id,ruta_id)
                                    .execute();
                        }

                        try {
                            for(int i=0; i<response.length(); i++){
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                //inasistencias  = jsonObject.getString("recuento");

                            }

                            if(turno.equalsIgnoreCase(TURNO_MAN)){


                            }
                            if(turno.equalsIgnoreCase(TURNO_TAR)){


                            }



                        }catch (JSONException e)
                        {
                            e.printStackTrace();

                        }

                        //Obtener ascensos, sumarlo con las inasistencias, comparar con el
                        //total de alumnos en la ruta



                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                VolleyLog.d("ERROR", "Error: " + error.getMessage());


            }
        });

        // Adding request to request queue
        AppTransporte.getInstance().addToRequestQueue(req);

    }

}
