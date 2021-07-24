package mx.edu.transporte.chmd.fragmentos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import mx.edu.transporte.chmd.AppTransporte;
import mx.edu.transporte.chmd.R;
import mx.edu.transporte.chmd.SeleccionRutaActivity;
import mx.edu.transporte.chmd.adapter.AsistenciaAdapter;
import mx.edu.transporte.chmd.modelos.Asistencia;
import mx.edu.transporte.chmd.modelosDB.AlumnoDB;

public class HomeFragment extends Fragment {

    TextView lblRuta,lblTotalInasist,lblTotales,lblAscDesc,lblInasist;
    ListView lstAlumnos;
    String rsp,hexadecimal;
    boolean todosSubidos;
    Button btnCerrarRegistro;
    int totalAscensos = 0;
    int totalInasistencias = 0;
    int totalAlumnos = 0;
    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;
    Typeface tf;
    String recuento,ascDesc,inasistencias;
    ArrayList<Asistencia> items = new ArrayList<>();
    static String BASE_URL;
    static String PATH;
    static String METODO_ALUMNOS_MAT="getAlumnosRutaMat.php";
    static String METODO_ALUMNOS_TAR="getAlumnosRutaTar.php";
    //Metodos POST
    static String METODO_COMENTAR="registraComentario.php";

    static String METODO_ALUMNO_ASISTE="asistenciaAlumno.php";
    static String METODO_ALUMNO_SUBE_TARDE="asistenciaAlumnoTarde.php";
    static String METODO_ALUMNO_DESC="descensoAlumno.php";
    static String METODO_ALUMNO_DESC_TARDE="descensoAlumnoTarde.php";
    static String METODO_ALUMNO_NO_ASISTE="noAsistenciaAlumno.php";
    static String METODO_ALUMNO_NO_ASISTE_TARDE="noAsistenciaAlumnoTarde.php";
    static String METODO_ALUMNO_REINICIA="reiniciaAsistenciaAlumno.php";
    static String METODO_ALUMNO_REINICIA_TARDE="reiniciaAsistenciaAlumnoTarde.php";
    static String METODO_CERRAR_RUTA="cerrarRuta.php";
    static String METODO_CERRAR_RUTA_TARDE="cerrarRutaTarde.php";
    AsistenciaAdapter adapter;
    SharedPreferences sharedPreferences;
    String idRuta,turno;
    int estatus=-1;
    int posicion=-1;
    //Estados para las rutas de la mañana o de la tarde
    static int ASCENSO=0;
    static int DESCENSO=1;
    private static String TURNO_MAN="1",TURNO_TAR="2";
    boolean isChecked;


    private void notifyAdapter()  {
        getActivity().runOnUiThread(new Runnable()  {
            public void run() {
                lstAlumnos.setAdapter(null);
                if(adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        BASE_URL = this.getString(R.string.BASE_URL);
        PATH = this.getString(R.string.PATH);
        tf = Typeface.createFromAsset(getActivity().getAssets(),"fonts/GothamRoundedMedium_21022.ttf");
        isChecked = sharedPreferences.getBoolean("habilitarNFC",true);
        sharedPreferences = getActivity().getSharedPreferences(this.getString(R.string.SHARED_PREF), 0);
        searchView = v.findViewById(R.id.searchView);
        lblRuta = v.findViewById(R.id.lblRuta);
        lblAscDesc = v.findViewById(R.id.lblAscDesc);
        lblTotalInasist = v.findViewById(R.id.lblTotalInasist);
        lblInasist = v.findViewById(R.id.lblInasist);
        lblTotales = v.findViewById(R.id.lblTotales);
        lstAlumnos = v.findViewById(android.R.id.list);
        btnCerrarRegistro = v.findViewById(R.id.btnCerrarRegistro);
        lblRuta.setTypeface(tf);
        lblAscDesc.setTypeface(tf);
        lblTotalInasist.setTypeface(tf);
        lblInasist.setTypeface(tf);
        lblTotales.setTypeface(tf);
        btnCerrarRegistro.setTypeface(tf);

        idRuta = sharedPreferences.getString("idRuta","");
        //cerrarRuta(idRuta,0);
        turno = sharedPreferences.getString("turno","");
        estatus = sharedPreferences.getInt("estatus",0);
        //Toast.makeText(getActivity().getApplicationContext(),""+estatus,Toast.LENGTH_LONG).show();
        if(turno.equals("2")){
            lblRuta.setText(sharedPreferences.getString("nomRuta","")+" (tarde)");
        }else{
            lblRuta.setText(sharedPreferences.getString("nomRuta","")+" (mañana)");
        }


        if(turno.equalsIgnoreCase(TURNO_MAN)){
            if(!hayConexion())
                getAsistenciaDB(idRuta);
            else{
                if(estatus<2){
                    getAsistencia(idRuta,METODO_ALUMNOS_MAT);
                }else{
                    Toast.makeText(getActivity().getApplicationContext(),"Esta ruta ya fue cerrada",Toast.LENGTH_LONG).show();
                }
            }

            //getTotal(idRuta,METODO_ALUMNOS_TOTL_MAT);
            //getTotalAscensos(idRuta,METODO_ALUMNOS_ASC_MAT);
            //getTotalInasist(idRuta,METODO_ALUMNOS_INASIST_MAT);
        }
        if(turno.equalsIgnoreCase(TURNO_TAR)){
            if(!hayConexion())
                getAsistenciaDB(idRuta);
            else
            if(estatus<2){
                getAsistencia(idRuta,METODO_ALUMNOS_TAR);
            }else{
                Toast.makeText(getActivity().getApplicationContext(),"Esta ruta ya fue cerrada",Toast.LENGTH_LONG).show();
            }
            //getTotal(idRuta,METODO_ALUMNOS_TOTL_TAR);
            //getTotalAscensos(idRuta,METODO_ALUMNOS_ASC_TAR);
            //getTotalInasist(idRuta,METODO_ALUMNOS_INASIST_TAR);
        }

        if(adapter!=null)
            adapter.refrescar(items);

        btnCerrarRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("CHMD - Transporte")
                        .setMessage("¿Deseas cerrar esta ruta?")

                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if(turno.equalsIgnoreCase(TURNO_MAN)) {
                                    //El cierre de la ruta solo se hará cuando los que han subido y los que no asisten
                                    //sea igual al total de alumnos.
                                    //enviarNotificacion("fIlaNjGQBVk:APA91bFfr0nTafsAjh8PXAJ8oOD1W7AcqvAGDOn3Y1hAN-WCO2ukAwIYStXuTS39ELBvR1DxRw-iG_h6Pvy_pcXphb4VJ8wW6KjXKJUuVdSTJvsMgkhcoGM17m0Q8Aa0Y369Qpj-lU45","Aviso de transporte","CHMD");
                                    if (totalAscensos + totalInasistencias == totalAlumnos && totalAlumnos>0) {
                                        estatus = sharedPreferences.getInt("estatus",0);
                                        if(estatus==ASCENSO)
                                            cerrarRuta(METODO_CERRAR_RUTA,idRuta,ASCENSO);
                                        if(estatus==DESCENSO)
                                            cerrarRuta(METODO_CERRAR_RUTA,idRuta,DESCENSO);
                                    } else {
                                        Toast.makeText(getActivity().getApplicationContext(), "No se puede cerrar todavía", Toast.LENGTH_LONG).show();
                                    }
                                }

                                if(turno.equalsIgnoreCase(TURNO_TAR)) {
                                    //El cierre de la ruta solo se hará cuando los que han subido y los que no asisten
                                    //sea igual al total de alumnos.
                                    //enviarNotificacion("fIlaNjGQBVk:APA91bFfr0nTafsAjh8PXAJ8oOD1W7AcqvAGDOn3Y1hAN-WCO2ukAwIYStXuTS39ELBvR1DxRw-iG_h6Pvy_pcXphb4VJ8wW6KjXKJUuVdSTJvsMgkhcoGM17m0Q8Aa0Y369Qpj-lU45","Aviso de transporte","CHMD");
                                    if (totalAscensos + totalInasistencias == totalAlumnos && totalAlumnos>0) {
                                        estatus = sharedPreferences.getInt("estatus",0);
                                        if(estatus==ASCENSO)
                                            cerrarRuta(METODO_CERRAR_RUTA_TARDE,idRuta,ASCENSO);
                                        if(estatus==DESCENSO)
                                            cerrarRuta(METODO_CERRAR_RUTA_TARDE,idRuta,DESCENSO);
                                    } else {
                                        Toast.makeText(getActivity().getApplicationContext(), "No se puede cerrar todavía", Toast.LENGTH_LONG).show();
                                    }
                                }


                            }
                        })

                        .setNegativeButton("Cancelar", null)
                        .setIcon(R.mipmap.ic_launcher)
                        .show();
            }
        });


        lstAlumnos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Asistencia asistencia = (Asistencia) lstAlumnos.getItemAtPosition(position);
                //guardar la posición
                posicion = position;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("posicion", posicion);
                editor.apply();
                if (estatus == ASCENSO) {
                    //La ruta no está cerrada -> METODOS DE ASCENSOS
                    if (turno.equalsIgnoreCase(TURNO_MAN)) {
                        trabajarTurnoMan(asistencia);
                    }
                    if (turno.equalsIgnoreCase(TURNO_TAR)) {
                        trabajarTurnoTar(asistencia);
                    }
                }

            }
        });
        return v;
    }

    public void registraAscensoDB(String ruta_id, String idAlumno){
        //Ascenso 1
        //Descenso 0
        new Update(AlumnoDB.class)
                .set("ascenso=1, descenso=0")
                .where("id_ruta_h="+ruta_id+" AND id_alumno="+idAlumno)
                .execute();

        notifyAdapter();

    }

    public void registraInasistenciaTardeDB(String ruta_id, String idAlumno){
        //Ascenso 1
        //Descenso 0
        new Update(AlumnoDB.class)
                .set("ascenso_t=2, descenso_t=2")
                .where("id_ruta_h_s="+ruta_id+" AND id_alumno="+idAlumno)
                .execute();

        notifyAdapter();

    }

    public void registraAscensoTardeDB(String ruta_id, String idAlumno){
        //Ascensot 1
        //Descensot 0
        new Update(AlumnoDB.class)
                .set("ascenso_t=1, descenso_t=0, salida=0")
                .where("id_ruta_h_s="+ruta_id+" AND id_alumno="+idAlumno)
                .execute();

        notifyAdapter();

    }

    public void registraInasistenciaDB(String ruta_id, String idAlumno){
        //Ascenso 2
        //Descenso 2
        new Update(AlumnoDB.class)
                .set("ascenso=2, descenso=2")
                .where("id_ruta_h="+ruta_id+" AND id_alumno="+idAlumno)
                .execute();

        notifyAdapter();

    }
    public void reiniciaRegistroDB(String ruta_id, String idAlumno){
        //Ascenso 0
        //Descenso 0
        new Update(AlumnoDB.class)
                .set("ascenso=0, descenso=0")
                .where("id_ruta_h="+ruta_id+" AND id_alumno="+idAlumno)
                .execute();

        notifyAdapter();

    }

    public void reiniciaRegistroTardeDB(String ruta_id, String idAlumno){
        //Ascenso 0
        //Descenso 0
        new Update(AlumnoDB.class)
                .set("ascenso_t=0, descenso_t=0")
                .where("id_ruta_h_s="+ruta_id+" AND id_alumno="+idAlumno)
                .execute();

        notifyAdapter();

    }

    public void getAsistenciaDB(String ruta_id){
        items.clear();
        ArrayList<AlumnoDB> dbAlumno = new ArrayList<>();
        List<AlumnoDB> list = new Select().from(AlumnoDB.class).where("id_ruta_h=?",idRuta).execute();
        dbAlumno.addAll(list);
        for(AlumnoDB alumnoDB : dbAlumno){
            String id_alumno = alumnoDB.id_alumno;
            String nombreAlumno = alumnoDB.nombre;
            String domicilio = alumnoDB.domicilio;
            String hora_manana = alumnoDB.hora_manana;
            String horaRegreso = alumnoDB.hora_regreso;
            String ascenso = alumnoDB.ascenso;
            String descenso = alumnoDB.descenso;
            String domicilio_s = alumnoDB.domicilio_s;
            String grupo = alumnoDB.grupo;
            String grado = alumnoDB.grado;
            String nivel = alumnoDB.nivel;
            String foto = "";
            String ascenso_t = alumnoDB.ascenso_t;
            String descenso_t = alumnoDB.descenso_t;
            String salida = alumnoDB.salida;

            items.add(new Asistencia(id_alumno,nombreAlumno,domicilio,hora_manana,
                    horaRegreso,ascenso,descenso,domicilio_s,grupo,grado,nivel,foto,
                    false,false, ascenso_t,descenso_t,salida));

            //Contar cuantos amarillos (suben)
            if(ascenso.equalsIgnoreCase("1"))
                totalAscensos++;

            if(ascenso.equalsIgnoreCase("2") && descenso.equalsIgnoreCase("2"))
                totalInasistencias++;
            //Contar cuantos rosados (no suben)

        }

        totalAlumnos = items.size();

        lblTotales.setText(String.valueOf(totalAscensos)+"/"+String.valueOf(totalAlumnos));
        lblTotalInasist.setText(String.valueOf(totalInasistencias));

        if(totalAscensos+totalInasistencias<totalAlumnos){
            ///todosSubidos = false;
            btnCerrarRegistro.setEnabled(false);
            btnCerrarRegistro.setBackgroundColor(Color.parseColor("#303030"));
            btnCerrarRegistro.setText("Todavía no puede cerrarse");
            btnCerrarRegistro.setTextColor(getResources().getColor(R.color.colorAccent));
        }else{
            ///todosSubidos = true;
            btnCerrarRegistro.setEnabled(true);
            btnCerrarRegistro.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            btnCerrarRegistro.setText("Cerrar Registro");
            btnCerrarRegistro.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        //Esto sirve para ordenar un arreglo de objetos
        Collections.sort(items, new Comparator<Asistencia>() {
            @Override
            public int compare(Asistencia a1, Asistencia a2) {
                return Integer.valueOf((a1.getAscenso()).compareTo(a2.getAscenso()));
            }
        });
        adapter = new AsistenciaAdapter(getActivity(),items,turno,idRuta);
        lstAlumnos.setAdapter(adapter);

    }


        public void trabajarTurnoMan(final Asistencia asistencia){

            //De blanco a amarillo
            if (asistencia.getAscenso().equalsIgnoreCase("0") &&
                    asistencia.getDescenso().equalsIgnoreCase("0")) {
                if(!hayConexion()){
                    registraAscensoDB(idRuta,asistencia.getIdAlumno());
                }else
                    registraAscenso(asistencia.getIdAlumno(), idRuta,asistencia.getNombreAlumno());
            }


            //De amarillo a rosado
            if (asistencia.getAscenso().equalsIgnoreCase("1") &&
                    asistencia.getDescenso().equalsIgnoreCase("0")){
                if(!hayConexion())
                    registraInasistenciaDB(idRuta,asistencia.getIdAlumno());
                else
                    registraInasistencia(asistencia.getIdAlumno(),idRuta);
            }

            //De rosado a blanco
            if (asistencia.getAscenso().equalsIgnoreCase("2") &&
                    asistencia.getDescenso().equalsIgnoreCase("2")) {
                if(!hayConexion())
                    reiniciaRegistroDB(idRuta,asistencia.getIdAlumno());
                else
                    reiniciaAsistencia(asistencia.getIdAlumno(),idRuta);
            }

        }
        public void trabajarTurnoTar(final Asistencia asistencia){

            //De blanco a amarillo
            if (asistencia.getAscenso_t().equalsIgnoreCase("0") &&
                    asistencia.getDescenso_t().equalsIgnoreCase("0")) {
                if(!hayConexion()){
                    registraAscensoTardeDB(idRuta,asistencia.getIdAlumno());
                }else
                    registraAscensoTarde(asistencia.getIdAlumno(), idRuta,asistencia.getNombreAlumno());
            }


            //De amarillo a rosado
            if (asistencia.getAscenso_t().equalsIgnoreCase("1") &&
                    asistencia.getDescenso_t().equalsIgnoreCase("0")){
                if(!hayConexion()) {
                    registraInasistenciaTardeDB(idRuta,asistencia.getIdAlumno());
                }else
                    registraInasistenciaTarde(asistencia.getIdAlumno(),idRuta);
            }

            //De rosado a blanco
            if (asistencia.getAscenso_t().equalsIgnoreCase("2") &&
                    asistencia.getDescenso_t().equalsIgnoreCase("2")) {
                if(!hayConexion()) {
                    reiniciaRegistroTardeDB(idRuta, asistencia.getIdAlumno());
                }else{
                    reiniciaAsistenciaTarde(asistencia.getIdAlumno(),idRuta);
                }
            }

        }
    public void getAsistencia(String ruta_id, String metodo){
        //getTotal(idRuta,METODO_ALUMNOS_TOTL_MAT);
        totalAscensos = 0;
        totalInasistencias = 0;

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+metodo+"?ruta_id="+ruta_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {



                        if(response.length()<=0){

                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            //Toast.makeText(getApplicationContext(),foto,Toast.LENGTH_LONG).show();
                            //editor.commit();
                        }

                        try {
                            for(int i=0; i<response.length(); i++){
                                new Delete().from(AlumnoDB.class).execute();
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);

                                String id_alumno = jsonObject.getString("id_alumno");
                                String nombreAlumno = jsonObject.getString("nombre");
                                String domicilio = jsonObject.getString("domicilio");
                                String hora_manana = jsonObject.getString("hora_manana");
                                String horaRegreso = jsonObject.getString("hora_regreso");
                                String ascenso = jsonObject.getString("ascenso");
                                String descenso = jsonObject.getString("descenso");
                                String domicilio_s = jsonObject.getString("domicilio_s");
                                String grupo = jsonObject.getString("grupo");
                                String grado = jsonObject.getString("grado");
                                String nivel = jsonObject.getString("nivel");
                                String foto = jsonObject.getString("foto");
                                String ascenso_t = jsonObject.getString("ascenso_t");
                                String descenso_t = jsonObject.getString("descenso_t");
                                String salida = jsonObject.getString("salida");
                                foto = foto.replace("C:\\IDCARDDESIGN\\CREDENCIALES\\alumnos\\","http://chmd.chmd.edu.mx:65083/CREDENCIALES/alumnos/");
                                foto = foto.replace(" ","%20");
                                Log.d("FOTO",foto);
                                items.add(new Asistencia(id_alumno,nombreAlumno,domicilio,hora_manana,
                                        horaRegreso,ascenso,descenso,domicilio_s,grupo,grado,nivel,foto,
                                        false,false, ascenso_t,descenso_t,salida));

                                //Contar cuantos amarillos (suben)
                                if(turno.equalsIgnoreCase("1")){
                                    if(ascenso.equalsIgnoreCase("1"))
                                        totalAscensos++;

                                    if(ascenso.equalsIgnoreCase("2") && descenso_t.equalsIgnoreCase("2"))
                                        totalInasistencias++;
                                }
                                if(turno.equalsIgnoreCase("2")){
                                    if(ascenso_t.equalsIgnoreCase("1"))
                                        totalAscensos++;

                                    if(ascenso_t.equalsIgnoreCase("2") && descenso_t.equalsIgnoreCase("2"))
                                        totalInasistencias++;
                                }

                                //Contar cuantos rosados (no suben)

                            }

                            totalAlumnos = items.size();

                            lblTotales.setText(String.valueOf(totalAscensos)+"/"+String.valueOf(totalAlumnos));
                            lblTotalInasist.setText(String.valueOf(totalInasistencias));

                            if(totalAscensos+totalInasistencias<totalAlumnos){
                                ///todosSubidos = false;
                                btnCerrarRegistro.setEnabled(false);
                                btnCerrarRegistro.setBackgroundColor(Color.parseColor("#303030"));
                                btnCerrarRegistro.setText("Todavía no puede cerrarse");
                                btnCerrarRegistro.setTextColor(getResources().getColor(R.color.colorAccent));
                            }else{
                                ///todosSubidos = true;
                                btnCerrarRegistro.setEnabled(true);
                                btnCerrarRegistro.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                                btnCerrarRegistro.setText("Cerrar Registro");
                                btnCerrarRegistro.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            }

                            //Esto sirve para ordenar un arreglo de objetos
                            Collections.sort(items, new Comparator<Asistencia>() {
                                @Override
                                public int compare(Asistencia a1, Asistencia a2) {
                                    return Integer.valueOf((a1.getAscenso()).compareTo(a2.getAscenso()));
                                }
                            });

                            adapter = new AsistenciaAdapter(getActivity(),items,turno,idRuta);
                            lstAlumnos.setAdapter(adapter);
                            if(sharedPreferences.getInt("posicion",0)>0){
                                lstAlumnos.setSelection(posicion);
                            }
                            for(Asistencia asistencia : items){
                                AlumnoDB alumnoDB = new AlumnoDB();
                                alumnoDB.id_ruta_h = idRuta;
                                alumnoDB.id_alumno = asistencia.getIdAlumno();
                                alumnoDB.nombre = asistencia.getNombreAlumno();
                                alumnoDB.domicilio = asistencia.getDomicilio();
                                alumnoDB.hora_manana = asistencia.getHora_manana();
                                alumnoDB.hora_regreso = asistencia.getHoraRegreso();
                                alumnoDB.ascenso = asistencia.getAscenso();
                                alumnoDB.domicilio_s = asistencia.getDomicilio_s();
                                alumnoDB.grupo = asistencia.getGrupo();
                                alumnoDB.grado = asistencia.getGrado();
                                alumnoDB.nivel = asistencia.getNivel();
                                alumnoDB.ascenso_t = asistencia.getAscenso_t();
                                alumnoDB.descenso_t = asistencia.getDescenso_t();
                                alumnoDB.procesado=0;
                                alumnoDB.save();



                            }







                        }catch (JSONException e)
                        {
                            e.printStackTrace();
                            //Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

                        }




                        //TODO: Cambiarlo cuando pase a prueba en MX
                        // if (existe.equalsIgnoreCase("1")) {
                        //llenado de datos
                        //eliminar circulares y guardar las primeras 10 del registro
                        //Borra toda la tabla
                        /*new Delete().from(DBCircular.class).execute();

                        for(int i=0; i<10; i++){
                            DBCircular dbCircular = new DBCircular();
                            dbCircular.idCircular = circulares.get(i).getIdCircular();
                            dbCircular.estado = circulares.get(i).getEstado();
                            dbCircular.nombre = circulares.get(i).getNombre();
                            dbCircular.textoCircular = circulares.get(i).getTextoCircular();
                            dbCircular.save();
                        }*/



                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                VolleyLog.d("ERROR", "Error: " + error.getMessage());
                /*
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                        */

            }
        });

        // Adding request to request queue
        AppTransporte.getInstance().addToRequestQueue(req);
    }

    public void registraAscenso(String alumno_id, String ruta_id,final String alumno){
        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_ALUMNO_ASISTE+"?id_alumno="+alumno_id+"&id_ruta="+ruta_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if(response.length()<=0){

                        }

                        try {
                            for(int i=0; i<response.length(); i++){
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                //inasistencias  = jsonObject.getString("recuento");

                            }

                            items.clear();

                            if(turno.equalsIgnoreCase(TURNO_MAN)){
                                if(!hayConexion())
                                    getAsistenciaDB(idRuta);
                                else
                                if(estatus<2){
                                    getAsistencia(idRuta,METODO_ALUMNOS_MAT);
                                }else{
                                    Toast.makeText(getActivity().getApplicationContext(),"Esta ruta ya fue cerrada",Toast.LENGTH_LONG).show();
                                }

                            }
                            if(turno.equalsIgnoreCase(TURNO_TAR)){
                                if(!hayConexion())
                                    getAsistenciaDB(idRuta);
                                else
                                if(estatus<2){
                                    getAsistencia(idRuta,METODO_ALUMNOS_TAR);
                                }else{
                                    Toast.makeText(getActivity().getApplicationContext(),"Esta ruta ya fue cerrada",Toast.LENGTH_LONG).show();
                                }

                            }

                            notifyAdapter();

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

    public void registraAscensoTarde(String alumno_id, String ruta_id,final String alumno){
        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_ALUMNO_SUBE_TARDE+"?id_alumno="+alumno_id+"&id_ruta="+ruta_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if(response.length()<=0){

                        }

                        try {
                            for(int i=0; i<response.length(); i++){
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                //inasistencias  = jsonObject.getString("recuento");

                            }

                            items.clear();

                            if(turno.equalsIgnoreCase(TURNO_MAN)){
                                if(!hayConexion())
                                    getAsistenciaDB(idRuta);
                                else
                                if(estatus<2){
                                    getAsistencia(idRuta,METODO_ALUMNOS_MAT);
                                }else{
                                    Toast.makeText(getActivity().getApplicationContext(),"Esta ruta ya fue cerrada",Toast.LENGTH_LONG).show();
                                }

                            }
                            if(turno.equalsIgnoreCase(TURNO_TAR)){
                                if(!hayConexion())
                                    getAsistenciaDB(idRuta);
                                else
                                if(estatus<2){
                                    getAsistencia(idRuta,METODO_ALUMNOS_TAR);
                                }else{
                                    Toast.makeText(getActivity().getApplicationContext(),"Esta ruta ya fue cerrada",Toast.LENGTH_LONG).show();
                                }

                            }

                            notifyAdapter();

                        }catch (JSONException e)
                        {
                            e.printStackTrace();
                            Toast.makeText(getActivity().getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

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
    public void registraInasistencia(String alumno_id, String ruta_id){
        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_ALUMNO_NO_ASISTE+"?id_alumno="+alumno_id+"&id_ruta="+ruta_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if(response.length()<=0){

                        }

                        try {
                            for(int i=0; i<response.length(); i++){
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                //inasistencias  = jsonObject.getString("recuento");
                            }

                            items.clear();


                            if(turno.equalsIgnoreCase(TURNO_MAN)){
                                if(!hayConexion())
                                    getAsistenciaDB(idRuta);
                                else
                                    getAsistencia(idRuta,METODO_ALUMNOS_MAT);

                            }
                            if(turno.equalsIgnoreCase(TURNO_TAR)){
                                if(!hayConexion())
                                    getAsistenciaDB(idRuta);
                                else
                                    getAsistencia(idRuta,METODO_ALUMNOS_TAR);

                            }

                            notifyAdapter();

                        }catch (JSONException e)
                        {
                            e.printStackTrace();
                            Toast.makeText(getActivity().getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

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
    public void registraInasistenciaTarde(String alumno_id, String ruta_id){


        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_ALUMNO_NO_ASISTE_TARDE+"?id_alumno="+alumno_id+"&id_ruta="+ruta_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if(response.length()<=0){

                        }

                        try {
                            for(int i=0; i<response.length(); i++){
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                //inasistencias  = jsonObject.getString("recuento");
                            }

                            items.clear();


                            if(turno.equalsIgnoreCase(TURNO_MAN)){
                                if(!hayConexion())
                                    getAsistenciaDB(idRuta);
                                else
                                    getAsistencia(idRuta,METODO_ALUMNOS_MAT);

                            }
                            if(turno.equalsIgnoreCase(TURNO_TAR)){
                                if(!hayConexion())
                                    getAsistenciaDB(idRuta);
                                else
                                    getAsistencia(idRuta,METODO_ALUMNOS_TAR);

                            }

                            notifyAdapter();

                        }catch (JSONException e)
                        {
                            e.printStackTrace();
                            Toast.makeText(getActivity().getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

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
    public void reiniciaAsistencia(String alumno_id, String ruta_id){
        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_ALUMNO_REINICIA+"?id_alumno="+alumno_id+"&id_ruta="+ruta_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if(response.length()<=0){

                        }

                        try {
                            for(int i=0; i<response.length(); i++){
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                //inasistencias  = jsonObject.getString("recuento");
                            }

                            items.clear();


                            if(turno.equalsIgnoreCase(TURNO_MAN)){
                                if(!hayConexion())
                                    getAsistenciaDB(idRuta);
                                else
                                    getAsistencia(idRuta,METODO_ALUMNOS_MAT);

                            }
                            if(turno.equalsIgnoreCase(TURNO_TAR)){
                                if(!hayConexion())
                                    getAsistenciaDB(idRuta);
                                else
                                    getAsistencia(idRuta,METODO_ALUMNOS_TAR);

                            }

                            notifyAdapter();

                        }catch (JSONException e)
                        {
                            e.printStackTrace();
                            Toast.makeText(getActivity().getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

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

                        try {
                            for(int i=0; i<response.length(); i++){
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                //inasistencias  = jsonObject.getString("recuento");
                            }

                            items.clear();


                            if(turno.equalsIgnoreCase(TURNO_MAN)){
                                if(!hayConexion())
                                    getAsistenciaDB(idRuta);
                                else
                                    getAsistencia(idRuta,METODO_ALUMNOS_MAT);

                            }
                            if(turno.equalsIgnoreCase(TURNO_TAR)){
                                if(!hayConexion())
                                    getAsistenciaDB(idRuta);
                                else
                                    getAsistencia(idRuta,METODO_ALUMNOS_TAR);

                            }

                            notifyAdapter();

                        }catch (JSONException e)
                        {
                            e.printStackTrace();
                            Toast.makeText(getActivity().getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

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
    public void cerrarRuta(String metodo, String ruta_id,final int est){


        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+metodo+"?id_ruta="+ruta_id+"&estatus="+(est+1),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if(response.length()<=0){

                        }

                        try {
                            for(int i=0; i<response.length(); i++){
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                //inasistencias  = jsonObject.getString("recuento");
                            }

                            //si viene con ascenso, la va a poner en descenso
                            //si viene con descenso, la va a cerrar
                            //Toast.makeText(getApplicationContext(),"estatus anterior:"+estatus,Toast.LENGTH_LONG).show();
                            estatus = est+1;
                            //Toast.makeText(getApplicationContext(),"nuevo estatus: "+estatus,Toast.LENGTH_LONG).show();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("estatus",est+1);
                            editor.apply();

                            if(est+1==2){
                                //enviarNotificacion("fIlaNjGQBVk:APA91bFfr0nTafsAjh8PXAJ8oOD1W7AcqvAGDOn3Y1hAN-WCO2ukAwIYStXuTS39ELBvR1DxRw-iG_h6Pvy_pcXphb4VJ8wW6KjXKJUuVdSTJvsMgkhcoGM17m0Q8Aa0Y369Qpj-lU45","Su hijo ha llegado al colegio","CHMD");
                                Intent intent = new Intent(getContext(), SeleccionRutaActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            }

                            //notifyAdapter();

                        }catch (JSONException e)
                        {
                            e.printStackTrace();
                            Toast.makeText(getActivity().getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

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
    public boolean hayConexion() {
        ConnectivityManager cm =
                (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();

    }

}
