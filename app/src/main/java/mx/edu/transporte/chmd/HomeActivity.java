package mx.edu.transporte.chmd;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.material.tabs.TabLayout;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mx.edu.transporte.chmd.adapter.AsistenciaAdapter;
import mx.edu.transporte.chmd.adapter.RutaAdapter;
import mx.edu.transporte.chmd.modelos.Asistencia;
import mx.edu.transporte.chmd.modelos.Ruta;
import mx.edu.transporte.chmd.modelosDB.AlumnoDB;
import mx.edu.transporte.chmd.modelosDB.RutaDB;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class HomeActivity extends Fragment {
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
    static String METODO_RUTA_DESC="descensoTodosMan.php";
    static String METODO_ALUMNO_DESC_TARDE="descensoAlumnoTarde.php";
    static String METODO_ALUMNO_NO_ASISTE="noAsistenciaAlumno.php";
    static String METODO_ALUMNO_NO_ASISTE_TARDE="noAsistenciaAlumnoTarde.php";
    static String METODO_ALUMNO_REINICIA="reiniciaAsistenciaAlumno.php";
    static String METODO_ALUMNO_REINICIA_TARDE="reiniciaAsistenciaAlumnoTarde.php";
    static String METODO_CERRAR_RUTA="cerrarRuta.php";
    static String METODO_CERRAR_RUTA_TARDE="cerrarRutaTarde.php";
    AsistenciaAdapter adapter;
    SharedPreferences sharedPreferences;
    String idRuta,turno,tarjeta;
    int estatus=-1;
    int posicion=-1;
    //Estados para las rutas de la mañana o de la tarde
    static int ASCENSO=0;
    static int DESCENSO=1;

    //Lectura NFC (habilitar si se necesita)
    /*private final String[][] techList = new String[][] {
            new String[] {
                    NfcA.class.getName(),
                    NfcB.class.getName(),
                    NfcF.class.getName(),
                    NfcV.class.getName(),
                    IsoDep.class.getName(),
                    MifareClassic.class.getName(),
                    MifareUltralight.class.getName(), Ndef.class.getName()
            }
    };*/



    private static String TURNO_MAN="1",TURNO_TAR="2";



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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        //setContentView(R.layout.activity_home);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        //setSupportActionBar(toolbar);
        Log.d("TARJETA","onCreateView");
        Bundle args = getArguments();
        tarjeta = args.getString("hexadecimal", "0");


        BASE_URL = this.getString(R.string.BASE_URL);
        PATH = this.getString(R.string.PATH);
        tf = Typeface.createFromAsset(getActivity().getAssets(),"fonts/GothamRoundedMedium_21022.ttf");
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
        lblRuta.setText(getActivity().getIntent().getStringExtra("nomRuta"));



        idRuta = getActivity().getIntent().getStringExtra("idRuta");
        //cerrarRuta(idRuta,0);
        turno = getActivity().getIntent().getStringExtra("turno");
        estatus = getActivity().getIntent().getIntExtra("estatus",0);



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
                final Asistencia asistencia = (Asistencia)lstAlumnos.getItemAtPosition(position);
                //guardar la posición
                posicion = position;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("posicion",posicion);
                editor.apply();

               if(estatus==ASCENSO){
                   //La ruta no está cerrada -> METODOS DE ASCENSOS
                   if(turno.equalsIgnoreCase(TURNO_MAN)) {
                       trabajarTurnoMan(asistencia);
                   }
                   if(turno.equalsIgnoreCase(TURNO_TAR)) {
                       trabajarTurnoTar(asistencia);
                   }
               }






/*
* public static int selectStock(int saving, List<Integer> currentValue, List<Integer> futureValue) {
        int maxProfit=0;
    List<Integer> currentProfit = new ArrayList<>();
    List<Integer> futureProfit = new ArrayList<>();
    int sum_cval=0;
    int sum_fval=0;
    for(int i=0; i<currentValue.size(); i++){
        for(int j=0; j<currentValue.size(); j++){
            sum_cval = currentValue.get(i)+currentValue.get(j);
            currentProfit.add(sum_cval);
        }
    }


    for(int i=0; i<futureValue.size(); i++){
        for(int j=0; j<futureValue.size(); j++){
            sum_fval = futureValue.get(i)+futureValue.get(j);
            futureProfit.add(sum_fval);
        }
    }

    Collections.sort(currentProfit);
    Collections.sort(futureProfit);

    int maxCurrentProfit = currentProfit.get(0);
    int maxfutureProfit = futureProfit.get(0);
    maxProfit = maxfutureProfit - maxCurrentProfit;
    return maxProfit;
    }

*
* */


            }






        });


//Buscar niño por nombre
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                adapter.getFilter().filter(newText);
                adapter.notifyDataSetChanged();
                lstAlumnos.setAdapter(adapter);
                return true;
            }
        });


        return v;
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

        Log.d("ASISTENCIA A",asistencia.getAscenso_t());
        Log.d("ASISTENCIA D",asistencia.getDescenso_t());

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

    public void trabajarTurnoManDescenso(final Asistencia asistencia){
        if (asistencia.getAscenso_t().equalsIgnoreCase("2") &&
                asistencia.getDescenso_t().equalsIgnoreCase("2")) {
            //Ponerla en amarillo

            new AlertDialog.Builder(getActivity())
                    .setTitle("CHMD - Transporte")
                    .setMessage("¿Desea registrar o reiniciar el descenso de " + asistencia.getNombreAlumno() + "?")

                    .setPositiveButton("Registrar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Ejecutar el POST de asistencia y recargar la vista
                            //De amarillo a blanco
                            registraDescenso(asistencia.getIdAlumno(), idRuta,asistencia.getNombreAlumno());

                        }
                    })

                    .setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })

                    .setNegativeButton("Reiniciar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //De blanco a rosado
                            reiniciaAsistencia(asistencia.getIdAlumno(), idRuta);

                        }
                    })
                    .setIcon(R.mipmap.ic_launcher)
                    .show();







        }

        if (asistencia.getAscenso_t().equalsIgnoreCase("0") &&
                asistencia.getDescenso_t().equalsIgnoreCase("0")) {
            //Ponerla en amarillo, pero preguntar
            new AlertDialog.Builder(getActivity())
                    .setTitle("CHMD - Transporte")
                    .setMessage("¿Desea registrar el descenso o inasistencia de "+asistencia.getNombreAlumno()+"?")

                    .setPositiveButton("Descenso", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Ejecutar el POST de asistencia y recargar la vista
                            //De amarillo a blanco
                            registraDescenso(asistencia.getIdAlumno(), idRuta,asistencia.getNombreAlumno());

                        }
                    })

                    .setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })

                    .setNegativeButton("No Asiste", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //De blanco a rosado
                            registraInasistencia(asistencia.getIdAlumno(),idRuta);

                        }
                    })
                    .setIcon(R.mipmap.ic_launcher)
                    .show();



        }

        //de amarillo a blanco o rosado
        if (asistencia.getAscenso_t().equalsIgnoreCase("1") &&
                asistencia.getDescenso_t().equalsIgnoreCase("0")){
            new AlertDialog.Builder(getActivity())
                    .setTitle("CHMD - Transporte")
                    .setMessage("¿Desea reiniciar el registro de descenso de "+asistencia.getNombreAlumno()+"?")

                    .setPositiveButton("Autorizo", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Ejecutar el POST de asistencia y recargar la vista
                            //De amarillo a blanco
                            //reiniciaAsistencia(asistencia.getIdAlumno(),idRuta);
                        }
                    })

                    .setNeutralButton("No Autorizo", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })

                    .setNegativeButton("No Asiste", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //De blanco a rosado
                            registraInasistenciaTarde(asistencia.getIdAlumno(),idRuta);
                        }
                    })
                    .setIcon(R.mipmap.ic_launcher)
                    .show();
        }

        //Los que estan en amarillo

    }




        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, new Intent(getActivity(), getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        // creating intent receiver for NFC events:
        /*IntentFilter filter = new IntentFilter();
        filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
        filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
        // enabling foreground dispatch for getting intent from NFC event:
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, new IntentFilter[]{filter}, this.techList);*/
    //}


    //Metodos de base de datos
    public void registraAscensoDB(String ruta_id, String idAlumno){
        //Ascenso 1
        //Descenso 0
        new Update(AlumnoDB.class)
                .set("ascenso=1,procesado=1, descenso=0")
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
                .set("ascenso_t=1,procesado=1, descenso_t=0, salida=0")
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

                        }

                        try {
                            for(int i=0; i<response.length(); i++){
                                new Delete().from(AlumnoDB.class).execute();
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);

                                String tarjeta = "";
                                if(jsonObject.getString("tarjeta")!=null){
                                    tarjeta = jsonObject.getString("tarjeta");
                                }
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
                                String ordenIn = jsonObject.getString("orden_in");
                                String ordenOut = jsonObject.getString("orden_out");
                                foto = foto.replace("C:\\IDCARDDESIGN\\CREDENCIALES\\alumnos\\","http://chmd.chmd.edu.mx:65083/CREDENCIALES/alumnos/");
                                foto = foto.replace(" ","%20");
                                Log.d("FOTO",foto);


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


            }
        });

        // Adding request to request queue
        AppTransporte.getInstance().addToRequestQueue(req);
    }
    public void getTotal(String ruta_id, String metodo){


        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+metodo+"?ruta_id="+ruta_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {



                        if(response.length()<=0){

                            SharedPreferences.Editor editor = sharedPreferences.edit();


                            //editor.commit();
                        }

                        try {
                            for(int i=0; i<response.length(); i++){
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                               recuento = jsonObject.getString("recuento");
                         }

                            //lblTotales.setText(""+recuento);

                        }catch (JSONException e)
                        {
                            e.printStackTrace();

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
    public void getTotalAscensos(String ruta_id, String metodo){

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+metodo+"?ruta_id="+ruta_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {



                        if(response.length()<=0){

                            SharedPreferences.Editor editor = sharedPreferences.edit();


                            //editor.commit();
                        }

                        try {
                            for(int i=0; i<response.length(); i++){
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                ascDesc  = jsonObject.getString("recuento");
                            }

                            lblTotales.setText(ascDesc+"/"+recuento);
                            try {
                                totalAscensos = Integer.parseInt(ascDesc);
                            }catch (Exception ex){

                            }

                            try {
                                totalAlumnos = Integer.parseInt(recuento);
                            }catch (Exception ex){

                            }
                        }catch (JSONException e)
                        {
                            e.printStackTrace();

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
    public void getTotalInasist(String ruta_id, String metodo){

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+metodo+"?ruta_id="+ruta_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {



                        if(response.length()<=0){

                            SharedPreferences.Editor editor = sharedPreferences.edit();

                                                      //editor.commit();
                        }

                        try {
                            for(int i=0; i<response.length(); i++){
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                inasistencias  = jsonObject.getString("recuento");
                            }
                            try {
                                totalInasistencias = Integer.parseInt(inasistencias);
                            }catch (Exception ex){

                            }
                            lblTotalInasist.setText(inasistencias);

                        }catch (JSONException e)
                        {
                            e.printStackTrace();

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
    public void registraComentario(String ruta_id,String metodo, final String comentario){
        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+metodo+"?comentario="+comentario.replace(" ","%20")+"&id_ruta="+ruta_id,
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
    public void registraDescenso(String alumno_id, String ruta_id,final String alumno){
        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_ALUMNO_DESC+"?id_alumno="+alumno_id+"&id_ruta="+ruta_id,
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
    public void registraDescensoTarde(String alumno_id, String ruta_id,final String alumno){
        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_ALUMNO_DESC_TARDE+"?id_alumno="+alumno_id+"&id_ruta="+ruta_id,
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

                        }

                        //Obtener ascensos, sumarlo con las inasistencias, comparar con el
                        //total de alumnos en la ruta
                        int asc = Integer.parseInt(lblTotales.getText().toString().split("/")[0]);
                        int total = Integer.parseInt(lblTotales.getText().toString().split("/")[1]);
                        int inasist = Integer.parseInt(lblTotalInasist.getText().toString().split("/")[1]);

                        if(asc+total==inasist){
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("todosSubidos",true);
                        }else{
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("todosSubidos",false);
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
                                Intent intent = new Intent(getActivity(),SeleccionRutaActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            }

                            //notifyAdapter();

                        }catch (JSONException e)
                        {
                            e.printStackTrace();

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



//Enviar la notificación al responsable

    private void enviarNotificacion(final String to,final String body,final String title) {
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                OkHttpClient client = new OkHttpClient();
                JSONObject json = new JSONObject();
                JSONObject jsonData = new JSONObject();
                try {
                    jsonData.put("body",body);
                    jsonData.put("title",title);
                    jsonData.put("idCircular","23");
                    jsonData.put("click_action",".CircularDetalleActivity");
                    json.put("notification",jsonData);
                    json.put("to",to);

                    RequestBody body = RequestBody.create(JSON,json.toString());
                    okhttp3.Request request = new okhttp3.Request.Builder()
                            .header("Authorization","key=AAAAbG33vkY:APA91bH5Ts7zah-Ho9TxVKcLztA2ZWKpGO-bcn0_2h4yDdLvuanTBLd-hylbLkJ6uX_7qFUSwenkp4OqW133vZc8cVcdqfY8ZgwbgbCfXKg8_VxJFYz-g_BDPDv7JyPaot4v1gI83DpA")
                            .header("Content-Type","application/json")
                            .url("https://fcm.googleapis.com/fcm/send")
                            .post(body)
                            .build();

                    okhttp3.Response response = client.newCall(request).execute();
                    String finalResponse = response.body().string();
                                   } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("HOMEACTIVITY2",e.getMessage());
                 }
                return  null;
            }
        }.execute();
    }


    public class ViewDialogComentario {

        public void showDialog(final Activity activity){
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);

            dialog.setContentView(R.layout.dialogo_comentario);
            final TextView txtComentario = dialog.findViewById(R.id.txtComentario);
            //TextView lblEncab = dialog.findViewById(R.id.lblEncab);
            Typeface t = Typeface.createFromAsset(activity.getAssets(),"fonts/GothamRoundedBook_21018.ttf");
            txtComentario.setTypeface(t);
            //lblEncab.setTypeface(t);

            Button imbCerrar = dialog.findViewById(R.id.imbCerrar);
            imbCerrar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();

                }
            });

            Button btnComentar = dialog.findViewById(R.id.btnComentario);
            btnComentar.setTypeface(t);
            btnComentar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Registrar el comentario
                    registraComentario(idRuta,METODO_COMENTAR,txtComentario.getText().toString());
                    dialog.dismiss();
                }
            });

            dialog.show();

        }
    }


    public boolean hayConexion() {
        ConnectivityManager cm =
                (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();

    }
    //Funciones para leer NFC
/*
    @Override
    protected void onPause() {
        super.onPause();

        Log.d("onPause", "1");

        // disabling foreground dispatch:
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.disableForegroundDispatch(this);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("onNewIntent", "1");

        if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            Log.d("onNewIntent", "2");
            //mTextView.setText( "NFC Tag\n" + bytesToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)));
            //if(getIntent().hasExtra(NfcAdapter.EXTRA_TAG)){

            Parcelable tagN = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tagN != null) {
                Log.d("MAIN", "Parcelable OK");
                NdefMessage[] msgs;
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                byte[] payload = dumpTagData(tagN).getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
                msgs = new NdefMessage[] { msg };

                //Log.d(TAG, msgs[0].toString());


            }
            else {
                Log.d("MAIN", "Parcelable NULL");
            }



            Parcelable[] messages1 = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (messages1 != null) {
                Log.d("MAIN", "Found " + messages1.length + " NDEF messages");
            }
            else {
                Log.d("MAIN", "Not EXTRA_NDEF_MESSAGES");
            }

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Ndef ndef = Ndef.get(tag);
            if(ndef != null) {

                Log.d("onNewIntent:", "NfcAdapter.EXTRA_TAG");

                Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                if (messages != null) {
                    Log.d("MAIN", "Found " + messages.length + " NDEF messages");
                }
            }
            else {
                Log.d("MAIN", "Write to an unformatted tag not implemented");
            }


            //mTextView.setText( "NFC Tag\n" + ByteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_TAG)));
        }
    }
    private String dumpTagData(Parcelable p) {
        StringBuilder sb = new StringBuilder();
        Tag tag = (Tag) p;
        byte[] id = tag.getId();
        sb.append("Tag ID (hex): ").append(getHex(id)).append("\n");
        sb.append("Tag ID (dec): ").append(getDec(id)).append("\n");
        hexadecimal = bytesToHexString(id);
        String prefix = "android.nfc.tech.";
        sb.append("Technologies: ");
        for (String tech : tag.getTechList()) {
            sb.append(tech.substring(prefix.length()));
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {
                sb.append('\n');
                MifareClassic mifareTag = MifareClassic.get(tag);
                String type = "Unknown";
                switch (mifareTag.getType()) {
                    case MifareClassic.TYPE_CLASSIC:
                        type = "Classic";
                        break;
                    case MifareClassic.TYPE_PLUS:
                        type = "Plus";
                        break;
                    case MifareClassic.TYPE_PRO:
                        type = "Pro";
                        break;
                }
                sb.append("Mifare Classic type: ");
                sb.append(type);
                sb.append('\n');

                sb.append("Mifare size: ");
                sb.append(mifareTag.getSize() + " bytes");
                sb.append('\n');

                sb.append("Mifare sectors: ");
                sb.append(mifareTag.getSectorCount());
                sb.append('\n');

                sb.append("Mifare blocks: ");
                sb.append(mifareTag.getBlockCount());
            }

            if (tech.equals(MifareUltralight.class.getName())) {
                sb.append('\n');
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                String type = "Unknown";
                switch (mifareUlTag.getType()) {
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        type = "Ultralight";
                        break;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        type = "Ultralight C";
                        break;
                }
                sb.append("Mifare Ultralight type: ");
                sb.append(type);
            }
        }
        Log.d("Datos: ", sb.toString());

        Log.d("Datos: ",hexadecimal);

        //mTextView.setText('\n' + sb.toString());
        //textView1.setText(hexadecimal);
        return sb.toString();
    }
    public String revertirString(String cad){
        byte [] strAsByteArray = cad.getBytes();
        byte [] result =
                new byte [strAsByteArray.length];
        for (int i = 0; i<strAsByteArray.length; i++)
            result[i] =
                    strAsByteArray[strAsByteArray.length-i-1];
        return new String(result);
    }
    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return revertirString(sb.toString());
    }
    private long getDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    //Fin de funciones NFC

*/



}
