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
//*****
//*****
//Servicio de Sincronización de base de datos hacia el server
//de los datos recolectados si la app registró subidas/bajadas sin conexión
//a internet.
//*****
//******
public class SincronizacionService extends Service {
    private static String TURNO_MAN="1",TURNO_TAR="2";
    static String METODO_ALUMNO_ASISTE="asistenciaAlumno.php";
    static String METODO_ALUMNO_SUBE_TARDE="asistenciaAlumnoTarde.php";
    static String METODO_ALUMNO_NO_ASISTE="noAsistenciaAlumno.php";
    static String METODO_ALUMNO_NO_ASISTE_TARDE="noAsistenciaAlumnoTarde.php";

    static String METODO_ALUMNO_REINICIA="reiniciaAsistenciaAlumno.php";
    static String METODO_ALUMNO_REINICIA_TARDE="reiniciaAsistenciaAlumnoTarde.php";


    //Descensos
    static String METODO_ALUMNO_DESC="descensoAlumno.php";
    static String METODO_ALUMNO_DESC_TARDE="descensoAlumnoTarde.php";
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


//Reinicio de asistencia en la mañana
        List<AlumnoDB> alumnosReinicioMan = new Select().from(AlumnoDB.class)
                .where("procesado=0 AND ascenso=0 AND descenso=0 AND ascenso_t=0 AND descenso_t=0")
                .execute();
        for (AlumnoDB alumno:alumnosReinicioMan) {
         reiniciaAsistencia(alumno.id_alumno,alumno.id_ruta_h);
        }

        //Reinicio de asistencia en la tarde
        List<AlumnoDB> alumnosReinicioTar = new Select().from(AlumnoDB.class)
                .where("procesado=0 AND ascenso=1 AND descenso=1 AND ascenso_t=0 AND descenso_t=0")
                .execute();
        for (AlumnoDB alumno:alumnosReinicioTar) {
            reiniciaAsistenciaTarde(alumno.id_alumno,alumno.id_ruta_h);
        }



        //Alumnos subiendo en la mañana
        List<AlumnoDB> alumnosAscensoMan = new Select().from(AlumnoDB.class)
                .where("procesado=1 AND ascenso=1 AND descenso=0 AND ascenso_t=0 AND descenso_t=0")
                .execute();
        for (AlumnoDB alumno:alumnosAscensoMan) {

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
        }
        List<AlumnoDB> alumnosDescensoMan = new Select().from(AlumnoDB.class)
                .where("procesado=1 AND ascenso=1 AND descenso=1 AND ascenso_t=0 AND descenso_t=0")
                .execute();
        for (AlumnoDB alumno:alumnosDescensoMan) {

            if(Integer.parseInt(alumno.ascenso)>0){
                //Aquí van los de la mañana
                //Procesar los alumnos que han subido
                if(Integer.parseInt(alumno.ascenso)<2)
                    registraDescenso(alumno.id_alumno,alumno.id_ruta_h,TURNO_MAN);

            }
        }






        //Alumnos subiendo en la tarde
        List<AlumnoDB> alumnosAscensoTar = new Select().from(AlumnoDB.class)
                .where("procesado=1 AND ascenso_t=1 AND descenso_t=0")
                .execute();
        for (AlumnoDB alumno:alumnosAscensoTar) {

            if(Integer.parseInt(alumno.ascenso_t)>0){
                //Aquí van los de la mañana
                //Procesar los alumnos que han subido
                if(Integer.parseInt(alumno.ascenso_t)<2)
                    registraAscensoTarde(alumno.id_alumno,alumno.id_ruta_h,TURNO_TAR);

                //actualizar alumnos, poner procesado a -1


                //Procesar los alumnos que no asistieron
            }
        }


        List<AlumnoDB> alumnosDescensoTar = new Select().from(AlumnoDB.class)
                .where("procesado=1 AND ascenso_t=1 AND descenso_t=1")
                .execute();
        for (AlumnoDB alumno:alumnosDescensoTar) {

            if(Integer.parseInt(alumno.descenso_t)>0){
                //Aquí van los de la mañana
                //Procesar los alumnos que han subido
                if(Integer.parseInt(alumno.descenso_t)<2)
                    registraDescensoTarde(alumno.id_alumno,alumno.id_ruta_h,TURNO_MAN);

            }
        }



          /*if(Integer.parseInt(alumno.ascenso_t)>0){
              if(Integer.parseInt(alumno.ascenso_t)<2)
                  registraAscensoTarde(alumno.id_alumno,alumno.id_ruta_h,TURNO_TAR);
              else
                  registraInasistenciaTarde(alumno.id_alumno,alumno.id_ruta_h,TURNO_MAN);
          }*/



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

    //reinicios de ascenso/descenso
    public void reiniciaAsistencia(String alumno_id, String ruta_id){
        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_ALUMNO_REINICIA+"?id_alumno="+alumno_id+"&id_ruta="+ruta_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if(response.length()<=0){

                        }

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
    public void reiniciaAsistenciaTarde(String alumno_id, String ruta_id){
        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_ALUMNO_REINICIA_TARDE+"?id_alumno="+alumno_id+"&id_ruta="+ruta_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if(response.length()<=0){

                        }



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



    //Registrar los descensos
    public void registraDescenso(final String alumno_id, final String ruta_id, final String turno){
        Log.d("PROCESAR",BASE_URL+PATH+METODO_ALUMNO_DESC+"?id_alumno="+alumno_id+"&id_ruta="+ruta_id);

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_ALUMNO_DESC+"?id_alumno="+alumno_id+"&id_ruta="+ruta_id,
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
    public void registraDescensoTarde(final String alumno_id, final String ruta_id, final String turno){
        Log.d("PROCESAR",BASE_URL+PATH+METODO_ALUMNO_DESC_TARDE+"?id_alumno="+alumno_id+"&id_ruta="+ruta_id);

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_ALUMNO_DESC_TARDE+"?id_alumno="+alumno_id+"&id_ruta="+ruta_id,
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
