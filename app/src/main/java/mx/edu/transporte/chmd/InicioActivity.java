package mx.edu.transporte.chmd;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.ToneGenerator;
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
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import mx.edu.transporte.chmd.adapter.AsistenciaAdapter;
import mx.edu.transporte.chmd.adapter.NavigationHomeAdapter;
import mx.edu.transporte.chmd.adapter.RutaAdapter;
import mx.edu.transporte.chmd.modelos.Asistencia;
import mx.edu.transporte.chmd.modelos.Ruta;
import mx.edu.transporte.chmd.modelosDB.AlumnoDB;
import mx.edu.transporte.chmd.modelosDB.RutaDB;
import mx.edu.transporte.chmd.networking.APIUtils;
import mx.edu.transporte.chmd.networking.ITransporteCHMD;
import mx.edu.transporte.chmd.receiver.NetworkChangeReceiver;
import retrofit2.Call;
import retrofit2.Callback;

public class InicioActivity extends AppCompatActivity {
    private ToneGenerator generadorTono;
    Toolbar toolbar;
    String comentario;
    String msjRuta="";
        RecyclerView mRecyclerView;
    RecyclerView.Adapter rAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    DrawerLayout Drawer;
    ActionBarDrawerToggle mDrawerToggle;
    TextView lblTotalInasist,lblTotales,lblAscDesc,lblInasist;
    public static ListView lstAlumnos;
    String rsp,hexadecimal;
    Button btnComentario;
    boolean todosSubidos;
    Button btnCerrarRegistro;
    int totalAscensos = 0;
    int totalInasistencias = 0;
    int totalAlumnos = 0;
    int totalBajan = 0;
    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;
    Typeface tf;
    TextView lblRuta;
    ArrayList<Asistencia> items = new ArrayList<>();
    ArrayList<Ruta> rutas = new ArrayList<>();
    static String BASE_URL;
    static String PATH;
    String id_usuario;
    ITransporteCHMD iTransporteCHMD;
    static String METODO_ALUMNOS_MAT="getAlumnosRutaMat.php";
    static String METODO_ALUMNOS_TAR="getAlumnosRutaTar.php";
    static String METODO_COMENTARIO="getComentario.php";
    //Metodos POST
    static String METODO_COMENTAR="registraComentario.php";
    static String METODO_RUTA="getRutaTransporte.php";
    static String METODO_RUTA_TARDE="getRutaTransporteTarde.php";
    static String METODO_ESTADO_RUTA="getEstatusRuta.php";
    static String METODO_ESTADO_RUTA_TARDE="getEstatusRutaTarde.php";
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
    public AsistenciaAdapter adapter;
    SharedPreferences sharedPreferences;
    String idRuta,turno,tarjeta;
    int estatus=-1;
    int posicion=-1;
    String aux_id="";
    //Estados para las rutas de la mañana o de la tarde
    static int ASCENSO=0;
    static int DESCENSO=1;

    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private int mCount = 0;
    NfcAdapter mNfcAdapter;
    String hexadecimalInv;

    private static String TURNO_MAN="1",TURNO_TAR="2";

    private final String[][] techList = new String[][] {
            new String[] {
                    NfcA.class.getName(),
                    NfcB.class.getName(),
                    NfcF.class.getName(),
                    NfcV.class.getName(),
                    IsoDep.class.getName(),
                    MifareClassic.class.getName(),
                    MifareUltralight.class.getName(), Ndef.class.getName()
            }
    };

    public void notifyAdapter()  {
        runOnUiThread(new Runnable()  {
            public void run() {

                lstAlumnos.setAdapter(null);
                if(adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }
    String menu[];
    int iconos[];
    int viaMenu;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        toolbar = findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        iTransporteCHMD = APIUtils.getTransporteService();
        generadorTono = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        sharedPreferences = getSharedPreferences(this.getString(R.string.SHARED_PREF), 0);
        BASE_URL = this.getString(R.string.BASE_URL);
        PATH = this.getString(R.string.PATH);
        //llenarlos dinámicamente
        aux_id = sharedPreferences.getString("id_usuario","");

        tf = Typeface.createFromAsset(getAssets(),"fonts/GothamRoundedMedium_21022.ttf");

        searchView = findViewById(R.id.searchView);
        lblRuta = findViewById(R.id.lblRuta);
        lblAscDesc = findViewById(R.id.lblAscDesc);
        lblTotalInasist =findViewById(R.id.lblTotalInasist);
        lblInasist = findViewById(R.id.lblInasist);
        lblTotales = findViewById(R.id.lblTotales);
        lstAlumnos = findViewById(android.R.id.list);
        btnComentario = findViewById(R.id.btnComentario);
        btnCerrarRegistro = findViewById(R.id.btnCerrarRegistro);
        lblRuta.setTypeface(tf);
        lblAscDesc.setTypeface(tf);
        lblTotalInasist.setTypeface(tf);
        lblInasist.setTypeface(tf);
        lblTotales.setTypeface(tf);
        btnCerrarRegistro.setTypeface(tf);
        estatus = sharedPreferences.getInt("estatus",-1);
        btnComentario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getComentario(sharedPreferences.getString("idRuta",""));
            }
        });

        viaMenu = sharedPreferences.getInt("viaMenu",0);

            items.clear();
            idRuta = sharedPreferences.getString("idRuta","");
            turno = sharedPreferences.getString("turno","");

            final String nombreRuta = sharedPreferences.getString("nombreRuta","");
            lblRuta.setText(nombreRuta);

            if(hayConexion()){
                if(turno.equals("1")){
                //Toast.makeText(getApplicationContext(),"Aqui se llama",Toast.LENGTH_LONG).show();
                getAsistencia(idRuta,METODO_ALUMNOS_MAT);

            }
            if(turno.equals("2")){
                getAsistencia(idRuta,METODO_ALUMNOS_TAR);

            }
            }else{
                if(turno.equals("1")){
                getAsistenciaDB(idRuta);

                //getEstatusRuta(id_usuario,idRuta,nombreRuta,turno);
            }
            if(turno.equals("2")){
                getAsistenciaDBTarde(idRuta);

                //getEstatusRutaTarde(id_usuario,idRuta,nombreRuta,turno);
            }
            }



        id_usuario = sharedPreferences.getString("id_usuario","");
        if(hayConexion())
            getRutaTransporte(aux_id);
        else{
            Toast.makeText(getApplicationContext(),"Sin conexión",Toast.LENGTH_LONG).show();
            getRutaTransporteDB(aux_id);
        }




        if(adapter!=null)
            adapter.refrescar(items);

        if(turno.equalsIgnoreCase(TURNO_MAN)) {
            if(estatus==0)
                msjRuta = "¿Autoriza el cierre de los ascensos en la ruta ";
            if(estatus==1)
                msjRuta = "¿Autoriza el cierre de los descensos en la ruta ";
        }

        if(turno.equalsIgnoreCase(TURNO_TAR)) {
            if(estatus==0)
                msjRuta = "¿Autoriza el cierre de los ascensos en la ruta ";
            if(estatus==1)
                msjRuta = "¿Autoriza el cierre de los descensos en la ruta ";
        }

        btnCerrarRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(InicioActivity.this)
                        .setTitle("CHMD - Transporte")
                        .setMessage(msjRuta+lblRuta.getText().toString()+"?")

                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if(turno.equalsIgnoreCase(TURNO_MAN)) {
                                    //El cierre de la ruta solo se hará cuando los que han subido y los que no asisten
                                    //sea igual al total de alumnos.
                                    //enviarNotificacion("fIlaNjGQBVk:APA91bFfr0nTafsAjh8PXAJ8oOD1W7AcqvAGDOn3Y1hAN-WCO2ukAwIYStXuTS39ELBvR1DxRw-iG_h6Pvy_pcXphb4VJ8wW6KjXKJUuVdSTJvsMgkhcoGM17m0Q8Aa0Y369Qpj-lU45","Aviso de transporte","CHMD");
                                    if (totalAscensos + totalInasistencias == totalAlumnos && totalAlumnos>0) {
                                        estatus = sharedPreferences.getInt("estatus",0);
                                        if(estatus==ASCENSO){
                                            //Los niños se han subido y van para el colegio
                                            Ruta ruta = new Ruta(idRuta,"1");
                                            //cerrarRutaAnterior(METODO_CERRAR_RUTA,idRuta,ASCENSO);
                                            cerrarRuta(ruta);
                                            //Recargar ruta sin los niños que no asistieron, que son
                                            //los que se van a bajar en el CHMD
                                            getAlumnosAbordo(idRuta,METODO_ALUMNOS_MAT);
                                        }


                                        if(estatus==DESCENSO){
                                            //Los niños llegan al colegio y ya se van a bajar (todos deben estar en verde)
                                            Ruta ruta = new Ruta(idRuta,"2");
                                            cerrarRuta(ruta);
                                            Intent intent = new Intent(InicioActivity.this,SeleccionRutaActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                            //cerrarRutaAnterior(METODO_CERRAR_RUTA,idRuta,DESCENSO);
                                    } else {
                                        Toast.makeText(getApplicationContext(), "No se puede cerrar todavía", Toast.LENGTH_LONG).show();
                                    }
                                }

                                if(turno.equalsIgnoreCase(TURNO_TAR)) {
                                  if (totalAscensos + totalInasistencias == totalAlumnos && totalAlumnos>0) {
                                        estatus = sharedPreferences.getInt("estatus",0);
                                        if(estatus==ASCENSO){
                                            //cerrarRutaAnterior(METODO_CERRAR_RUTA_TARDE,idRuta,ASCENSO);
                                            Ruta ruta = new Ruta(idRuta,String.valueOf(ASCENSO+1));
                                            cerrarRutaTarde(ruta);
                                        }

                                        if(estatus==DESCENSO){
                                            //cerrarRutaAnterior(METODO_CERRAR_RUTA_TARDE,idRuta,DESCENSO);
                                            Ruta ruta = new Ruta(idRuta,String.valueOf(DESCENSO+1));
                                            cerrarRutaTarde(ruta);
                                            Intent intent = new Intent(InicioActivity.this,SeleccionRutaActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }

                                    } else {
                                        Toast.makeText(getApplicationContext(), "No se puede cerrar todavía", Toast.LENGTH_LONG).show();
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
                posicion = position;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("posicion", posicion);
                editor.apply();
                Log.d("ESTATUS",""+estatus);



                if (estatus == 0) {
                    //La ruta no está cerrada -> METODOS DE ASCENSOS
                    if (turno.equalsIgnoreCase(TURNO_MAN)) {
                        trabajarTurnoMan(asistencia);
                    }
                    if (turno.equalsIgnoreCase(TURNO_TAR)) {
                        trabajarTurnoTar(asistencia);
                    }
                }


                //Los niños bajan en el colegio o en su casa
                if (estatus == 1) {
                    //La ruta no está cerrada -> METODOS DE DESCENSOS
                    if (turno.equalsIgnoreCase(TURNO_MAN)) {

                       trabajarTurnoMan(asistencia);
                    }
                    if (turno.equalsIgnoreCase(TURNO_TAR)) {
                        trabajarTurnoTar(asistencia);
                    }
                }


            }
        });

                      // Setting the layout Manage
        /*Drawer = findViewById(R.id.DrawerLayout);        // Drawer object Assigned to the view
        mDrawerToggle = new ActionBarDrawerToggle(this,Drawer,toolbar,R.string.openDrawer,R.string.closeDrawer){

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

            }



        }; // Drawer Toggle Object Made
        Drawer.setDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle
        mDrawerToggle.syncState();*/

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

    }


    public void trabajarTurnoMan(final Asistencia asistencia){


        //De amarillo a verde, son los niños que ya van en el bus
        if (asistencia.getAscenso().equalsIgnoreCase("1") &&
                asistencia.getDescenso().equalsIgnoreCase("0") && estatus==1) {
                if(!hayConexion()){
                    registraAscensoDB(idRuta,asistencia.getIdAlumno());
                    getAsistenciaDB(idRuta);
                }else{
                    registraDescenso(asistencia.getIdAlumno(),idRuta,asistencia.getNombreAlumno());
                }
        }


        //De blanco a amarillo
        if (asistencia.getAscenso().equalsIgnoreCase("0") &&
                asistencia.getDescenso().equalsIgnoreCase("0")) {
            if(!hayConexion()){
                //No pide diálogo
                registraAscensoDB(idRuta,asistencia.getIdAlumno());
                getAsistenciaDB(idRuta);
            }else{
                registraAscenso(asistencia.getIdAlumno(), idRuta,asistencia.getNombreAlumno());
            }
         }


        //de amarillo a blanco
        if (asistencia.getAscenso().equalsIgnoreCase("1") &&
                asistencia.getDescenso().equalsIgnoreCase("0") && estatus==0) {

            new android.app.AlertDialog.Builder(InicioActivity.this)
                    .setTitle("CHMD - Transporte")
                    .setMessage("¿Desea reiniciar el registro de asistencia de " + asistencia.getNombreAlumno() + "?")

                    .setPositiveButton("Autorizo", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Ejecutar el POST de asistencia y recargar la vista
                            //De amarillo a blanco
                            if(!hayConexion()) {
                                reiniciaRegistroDB(idRuta,asistencia.getIdAlumno());
                                getAsistenciaDB(idRuta);
                            }else{
                                reiniciaAsistencia(asistencia.getIdAlumno(), idRuta);
                            }
                        }
                    })

                    .setNeutralButton("No Autorizo", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })


                    .setIcon(R.mipmap.ic_launcher)
                    .show();

        }


        //De amarillo a rosado
        /*if (asistencia.getAscenso().equalsIgnoreCase("1") &&
                asistencia.getDescenso().equalsIgnoreCase("0")){
            if(!hayConexion())
                registraInasistenciaDB(idRuta,asistencia.getIdAlumno());
            else
                registraInasistencia(asistencia.getIdAlumno(),idRuta);
        }*/

        //De rosado a amarillo (no pregunta)
        if (asistencia.getAscenso().equalsIgnoreCase("2") &&
                asistencia.getDescenso().equalsIgnoreCase("2")) {
            if(!hayConexion()) {
                registraAscensoDB(idRuta, asistencia.getIdAlumno());
                getAsistenciaDB(idRuta);
            }else
                registraAscenso(asistencia.getIdAlumno(), idRuta,asistencia.getNombreAlumno());
        }

    }
    public void trabajarTurnoTar(final Asistencia asistencia){

        //De blanco a amarillo
        if (asistencia.getAscenso_t().equalsIgnoreCase("0") &&
                asistencia.getDescenso_t().equalsIgnoreCase("0")) {
            if(!hayConexion()){
                registraAscensoTardeDB(idRuta,asistencia.getIdAlumno());
                getAsistenciaDBTarde(idRuta);
            }else
                registraAscensoTarde(asistencia.getIdAlumno(), idRuta,asistencia.getNombreAlumno());
        }

        if (asistencia.getAscenso_t().equalsIgnoreCase("1") &&
                asistencia.getDescenso_t().equalsIgnoreCase("0") && estatus==0) {
            if(!hayConexion()){
                registraDescensoTardeDB(idRuta,asistencia.getIdAlumno());
                getAsistenciaDBTarde(idRuta);
            }else{
                registraDescensoTarde(asistencia.getIdAlumno(),idRuta,asistencia.getNombreAlumno());
            }
        }

        //de amarillo a verde
        if (asistencia.getAscenso_t().equalsIgnoreCase("1") &&
                asistencia.getDescenso_t().equalsIgnoreCase("0") && estatus==1) {
            if(!hayConexion()){
                registraDescensoTardeDB(idRuta,asistencia.getIdAlumno());
                getAsistenciaDBTarde(idRuta);
            }else{
                registraDescensoTarde(asistencia.getIdAlumno(),idRuta,asistencia.getNombreAlumno());
            }
        }


        //de amarillo a blanco
        if (asistencia.getAscenso_t().equalsIgnoreCase("1") &&
                asistencia.getDescenso_t().equalsIgnoreCase("0") && estatus==0) {

            new android.app.AlertDialog.Builder(InicioActivity.this)
                    .setTitle("CHMD - Transporte")
                    .setMessage("¿Desea reiniciar el registro de asistencia de " + asistencia.getNombreAlumno() + "?")

                    .setPositiveButton("Autorizo", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Ejecutar el POST de asistencia y recargar la vista
                            //De amarillo a blanco
                            if(hayConexion()) {
                                reiniciaAsistenciaTarde(asistencia.getIdAlumno(), idRuta);
                            }else{
                                reiniciaRegistroTardeDB(idRuta,asistencia.getIdAlumno());
                                getAsistenciaDBTarde(idRuta);
                            }
                        }
                    })

                    .setNeutralButton("No Autorizo", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })


                    .setIcon(R.mipmap.ic_launcher)
                    .show();

        }

        if (asistencia.getAscenso_t().equalsIgnoreCase("2") &&
                asistencia.getDescenso_t().equalsIgnoreCase("2")) {
            if(!hayConexion()) {
                registraAscensoTardeDB(idRuta, asistencia.getIdAlumno());
                getAsistenciaDBTarde(idRuta);
            }else
                registraAscensoTarde(asistencia.getIdAlumno(), idRuta,asistencia.getNombreAlumno());
        }

    }



    public void getAlumnosAbordo(String ruta_id, String metodo){
        //getTotal(idRuta,METODO_ALUMNOS_TOTL_MAT);
        totalAscensos = 0;
        totalInasistencias = 0;
        items.clear();
        new Delete().from(AlumnoDB.class).where("id_ruta_h=?",idRuta).execute();

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+metodo+"?ruta_id="+ruta_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {


                        if(response.length()<=0){

                            SharedPreferences.Editor editor = sharedPreferences.edit();

                        }

                        try {
                            for(int i=0; i<response.length(); i++){
                                //new Delete().from(AlumnoDB.class).execute();
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
                                //Mostrar solo los que han subido al bus
                                if(!ascenso.equalsIgnoreCase("2") && !descenso.equalsIgnoreCase("2"))
                                    items.add(new Asistencia(tarjeta,id_alumno,nombreAlumno,domicilio,hora_manana,
                                        horaRegreso,ascenso,descenso,domicilio_s,grupo,grado,nivel,foto,
                                        false,false, ascenso_t,descenso_t,salida,ordenIn,ordenOut,false));



                                if(estatus==2){
                                    btnCerrarRegistro.setText("Bajar a los alumnos");
                                }

                            }

                            //Esto sirve para ordenar un arreglo de objetos
                            Collections.sort(items, new Comparator<Asistencia>() {
                                @Override
                                public int compare(Asistencia a1, Asistencia a2) {
                                    return Integer.valueOf((a1.getAscenso()).compareTo(a2.getAscenso()));
                                }
                            });

                            adapter = new AsistenciaAdapter(InicioActivity.this,items,turno,idRuta);
                            adapter.notifyDataSetChanged();
                            lstAlumnos.setAdapter(adapter);
                            if(sharedPreferences.getInt("posicion",0)>0){
                                lstAlumnos.setSelection(posicion);
                            }



                        }catch (JSONException e)
                        {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

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


    public void getAsistencia(String ruta_id, String metodo){
        //getTotal(idRuta,METODO_ALUMNOS_TOTL_MAT);
        totalAscensos = 0;
        totalInasistencias = 0;
        totalBajan = 0;
        items.clear();
        new Delete().from(AlumnoDB.class).where("id_ruta_h=?",idRuta).execute();

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+metodo+"?ruta_id="+ruta_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {


                        if(response.length()<=0){

                            SharedPreferences.Editor editor = sharedPreferences.edit();

                        }

                        try {
                            for(int i=0; i<response.length(); i++){
                                //new Delete().from(AlumnoDB.class).execute();
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
                                Log.d("FOTO_AL",foto);




                                if(estatus==0)
                                    items.add(new Asistencia(tarjeta,id_alumno,nombreAlumno,domicilio,hora_manana,
                                        horaRegreso,ascenso,descenso,domicilio_s,grupo,grado,nivel,foto,
                                        false,false, ascenso_t,descenso_t,salida,ordenIn,ordenOut,true));

                                if(estatus==1)
                                    //No se debe ver el botón de inasitencia
                                    if((!ascenso.equalsIgnoreCase("2") && !descenso.equalsIgnoreCase("2")))
                                    items.add(new Asistencia(tarjeta,id_alumno,nombreAlumno,domicilio,hora_manana,
                                            horaRegreso,ascenso,descenso,domicilio_s,grupo,grado,nivel,foto,
                                            false,false, ascenso_t,descenso_t,salida,ordenIn,ordenOut,false));

                                /*if(estatus==1 && (!ascenso.equalsIgnoreCase("2") && !descenso.equalsIgnoreCase("2")))
                                    items.add(new Asistencia(tarjeta,id_alumno,nombreAlumno,domicilio,hora_manana,
                                            horaRegreso,ascenso,descenso,domicilio_s,grupo,grado,nivel,foto,
                                            false,false, ascenso_t,descenso_t,salida,ordenIn,ordenOut,true));*/


                                //Contar cuantos amarillos (suben)
                                if(turno.equalsIgnoreCase("1")){
                                    if(ascenso.equalsIgnoreCase("1"))
                                        totalAscensos++;

                                    if(ascenso.equalsIgnoreCase("2") && descenso.equalsIgnoreCase("2"))
                                        totalInasistencias++;

                                    if(ascenso.equalsIgnoreCase("1") && descenso.equalsIgnoreCase("1"))
                                        totalBajan++;



                                }
                                if(turno.equalsIgnoreCase("2")){
                                    if(ascenso_t.equalsIgnoreCase("1"))
                                        totalAscensos++;

                                    if(ascenso_t.equalsIgnoreCase("2") && descenso_t.equalsIgnoreCase("2"))
                                        totalInasistencias++;

                                    if(ascenso_t.equalsIgnoreCase("1") && descenso_t.equalsIgnoreCase("1"))
                                        totalBajan++;

                                }

                                //Contar cuantos rosados (no suben)

                            }

                            totalAlumnos = items.size();

                            lblTotales.setText(String.valueOf(totalAscensos)+"/"+String.valueOf(totalAlumnos));
                            lblTotalInasist.setText(String.valueOf(totalInasistencias));

                            if(estatus==1) {

                                if (totalAscensos > totalBajan) {
                                    btnCerrarRegistro.setEnabled(false);
                                    btnCerrarRegistro.setBackgroundColor(Color.parseColor("#303030"));
                                    btnCerrarRegistro.setText("Todavía no puede cerrarse");
                                    btnCerrarRegistro.setTextColor(getResources().getColor(R.color.colorAccent));
                                }else{
                                    btnCerrarRegistro.setEnabled(true);
                                    btnCerrarRegistro.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                                    btnCerrarRegistro.setText("Cerrar Registro");
                                    btnCerrarRegistro.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                                }
                            }

                                if(estatus==0) {
                                    if (totalAscensos + totalInasistencias < totalAlumnos) {
                                        ///todosSubidos = false;
                                        btnCerrarRegistro.setEnabled(false);
                                        btnCerrarRegistro.setBackgroundColor(Color.parseColor("#303030"));
                                        btnCerrarRegistro.setText("Todavía no puede cerrarse");
                                        btnCerrarRegistro.setTextColor(getResources().getColor(R.color.colorAccent));
                                    } else {
                                        ///todosSubidos = true;
                                        btnCerrarRegistro.setEnabled(true);
                                        btnCerrarRegistro.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                                        btnCerrarRegistro.setText("Cerrar Registro");
                                        btnCerrarRegistro.setTextColor(getResources().getColor(R.color.colorPrimaryDark));


                                    }
                                }


                            //Esto sirve para ordenar un arreglo de objetos
                            Collections.sort(items, new Comparator<Asistencia>() {
                                @Override
                                public int compare(Asistencia a1, Asistencia a2) {
                                    return Integer.valueOf((a1.getAscenso()).compareTo(a2.getAscenso()));
                                }
                            });

                            adapter = new AsistenciaAdapter(InicioActivity.this,items,turno,idRuta);
                            adapter.notifyDataSetChanged();
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
                                alumnoDB.descenso = asistencia.getDescenso();
                                alumnoDB.ascenso_t = asistencia.getAscenso_t();
                                alumnoDB.descenso_t = asistencia.getDescenso_t();
                                alumnoDB.procesado=0;
                                Log.d("ALUMNOS",""+alumnoDB.save());



                            }


                        }catch (JSONException e)
                        {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

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
    public void registraInasistenciaTardeDB(String ruta_id, String idAlumno){
        //Ascenso 1
        //Descenso 0
        new Update(AlumnoDB.class)
                .set("ascenso_t=2, descenso_t=2")
                .where("id_ruta_h="+ruta_id+" AND id_alumno="+idAlumno)
                .execute();

        notifyAdapter();

    }
    public void reiniciaRegistroTardeDB(String ruta_id, String idAlumno){
        //Ascenso 0
        //Descenso 0
        new Update(AlumnoDB.class)
                .set("ascenso_t=0,descenso_t=0, procesado=0")
                .where("id_ruta_h="+ruta_id+" AND id_alumno="+idAlumno)
                .execute();

        notifyAdapter();

    }
    public void registraAscensoTardeDB(String ruta_id, String idAlumno){
        //Ascensot 1
        //Descensot 0
        new Update(AlumnoDB.class)
                .set("ascenso_t='1', descenso_t='0', salida='0',procesado=1")
                .where("id_ruta_h="+ruta_id+" AND id_alumno="+idAlumno)
                .execute();

        notifyAdapter();

    }

    public void registraAscensoDB(String ruta_id, String idAlumno){
        //Ascenso 1
        //Descenso 0
        new Update(AlumnoDB.class)
                .set("ascenso='1', descenso='0', procesado=1")
                .where("id_ruta_h="+ruta_id+" AND id_alumno="+idAlumno)
                .execute();

        notifyAdapter();

    }


    public void registraDescensoDB(String ruta_id, String idAlumno){
        //Ascenso 1
        //Descenso 1
        new Update(AlumnoDB.class)
                .set("ascenso='1', descenso='1', procesado=1")
                .where("id_ruta_h="+ruta_id+" AND id_alumno="+idAlumno)
                .execute();

        notifyAdapter();

    }

    public void registraDescensoTardeDB(String ruta_id, String idAlumno){
        //Ascenso 1
        //Descenso 1
        new Update(AlumnoDB.class)
                .set("ascenso_t='1', descenso_t='1', procesado=1")
                .where("id_ruta_h="+ruta_id+" AND id_alumno="+idAlumno)
                .execute();

        notifyAdapter();

    }




    public void registraInasistenciaDB(String ruta_id, String idAlumno){
        //Ascenso 2
        //Descenso 2
        new Update(AlumnoDB.class)
                .set("ascenso='2', descenso='2', procesado=1")
                .where("id_ruta_h="+ruta_id+" AND id_alumno="+idAlumno)
                .execute();

        notifyAdapter();

    }
    public void reiniciaRegistroDB(String ruta_id, String idAlumno){
        //Ascenso 0
        //Descenso 0
        new Update(AlumnoDB.class)
                .set("ascenso=0, descenso=0, procesado=0")
                .where("id_ruta_h="+ruta_id+" AND id_alumno="+idAlumno)
                .execute();

        notifyAdapter();

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
                                    Toast.makeText(getApplicationContext(),"Esta ruta ya fue cerrada",Toast.LENGTH_LONG).show();
                                }

                            }
                            if(turno.equalsIgnoreCase(TURNO_TAR)){
                                if(!hayConexion())
                                    getAsistenciaDB(idRuta);
                                else
                                if(estatus<2){
                                    getAsistencia(idRuta,METODO_ALUMNOS_TAR);
                                }else{
                                    Toast.makeText(getApplicationContext(),"Esta ruta ya fue cerrada",Toast.LENGTH_LONG).show();
                                }

                            }

                            notifyAdapter();

                        }catch (JSONException e)
                        {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

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
                                    Toast.makeText(getApplicationContext(),"Esta ruta ya fue cerrada",Toast.LENGTH_LONG).show();
                                }

                            }
                            if(turno.equalsIgnoreCase(TURNO_TAR)){
                                if(!hayConexion())
                                    getAsistenciaDB(idRuta);
                                else
                                if(estatus<2){
                                    getAsistencia(idRuta,METODO_ALUMNOS_TAR);
                                }else{
                                    Toast.makeText(getApplicationContext(),"Esta ruta ya fue cerrada",Toast.LENGTH_LONG).show();
                                }

                            }

                            notifyAdapter();

                        }catch (JSONException e)
                        {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

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
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

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
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

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
    /*public void registraDescensoTarde(String alumno_id, String ruta_id,final String alumno){
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
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

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

    }*/
    public void getAsistenciaDB(String ruta_id){
        items.clear();
        ArrayList<AlumnoDB> dbAlumno = new ArrayList<>();
        //.where("id_ruta_h=?",ruta_id)
        totalAscensos=0;
        List<AlumnoDB> list = new Select().from(AlumnoDB.class).where("id_ruta_h=?",ruta_id).execute();
        dbAlumno.addAll(list);
        for(AlumnoDB alumnoDB : dbAlumno){
            Log.d("ALUMNOS DB",alumnoDB.nombre);
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

            if(ascenso.equalsIgnoreCase("1") && descenso.equalsIgnoreCase("1"))
                totalBajan++;

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
        adapter = new AsistenciaAdapter(InicioActivity.this,items,turno,idRuta);
        adapter.notifyDataSetChanged();
        lstAlumnos.setAdapter(adapter);

    }
    public void getAsistenciaDBTarde(String ruta_id){
        items.clear();
        totalAscensos=0;
        ArrayList<AlumnoDB> dbAlumno = new ArrayList<>();
        //.where("id_ruta_h=?",ruta_id)
        List<AlumnoDB> list = new Select().from(AlumnoDB.class).where("id_ruta_h=?",ruta_id).execute();
        dbAlumno.addAll(list);
        for(AlumnoDB alumnoDB : dbAlumno){
            Log.d("ALUMNOS DB",alumnoDB.nombre);
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
            if(ascenso_t.equalsIgnoreCase("1"))
                totalAscensos++;

            if(ascenso_t.equalsIgnoreCase("2") && descenso_t.equalsIgnoreCase("2"))
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
        adapter = new AsistenciaAdapter(InicioActivity.this,items,turno,idRuta);
        adapter.notifyDataSetChanged();
        lstAlumnos.setAdapter(adapter);

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
                                    Toast.makeText(getApplicationContext(),"Esta ruta ya fue cerrada",Toast.LENGTH_LONG).show();
                                }

                            }
                            if(turno.equalsIgnoreCase(TURNO_TAR)){
                                if(!hayConexion())
                                    getAsistenciaDB(idRuta);
                                else
                                if(estatus<2){
                                    getAsistencia(idRuta,METODO_ALUMNOS_TAR);
                                }else{
                                    Toast.makeText(getApplicationContext(),"Esta ruta ya fue cerrada",Toast.LENGTH_LONG).show();
                                }

                            }

                            notifyAdapter();

                        }catch (JSONException e)
                        {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

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
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

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
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

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

                            //items.clear();

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
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

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
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

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

    public void cerrarRuta(final Ruta r){
        iTransporteCHMD.cerrarRuta(r.getIdRutaH(),r.getEstatus())
                .enqueue(new Callback<Ruta>() {
                    @Override
                    public void onResponse(Call<Ruta> call, retrofit2.Response<Ruta> response) {
                        if(response.isSuccessful()){
                             SharedPreferences.Editor editor = sharedPreferences.edit();
                             editor.putInt("estatus",Integer.parseInt(r.getEstatus()));
                             editor.apply();
                        }
                    }

                    @Override
                    public void onFailure(Call<Ruta> call, Throwable t) {

                    }
                });
    }
    public void cerrarRutaTarde(Ruta r){
        iTransporteCHMD.cerrarRutaTarde(r.getIdRutaH(),r.getEstatus())
                .enqueue(new Callback<Ruta>() {
                    @Override
                    public void onResponse(Call<Ruta> call, retrofit2.Response<Ruta> response) {
                        if(response.isSuccessful()){

                        }
                    }

                    @Override
                    public void onFailure(Call<Ruta> call, Throwable t) {

                    }
                });
    }

    public void cerrarRutaAnterior(String metodo, String ruta_id,final int est){


        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+metodo+"?id_ruta="+ruta_id+"&estatus="+(est+1),
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
                            new Update(RutaDB.class).set("estatus_ruta=?",estatus).where("idRuta=?",idRuta).execute();
                            //Toast.makeText(getApplicationContext(),"nuevo estatus: "+estatus,Toast.LENGTH_LONG).show();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("estatus",est+1);
                            editor.apply();

                            if(est+1==2){
                                new Update(RutaDB.class).set("estatus_ruta=?",estatus).where("idRuta=?",idRuta).execute();
                                rutas.clear();
                                getRutaTransporte(id_usuario);
                            }

                            //notifyAdapter();

                        }catch (JSONException e)
                        {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

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

    public void getComentario(final String ruta_id){

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_COMENTARIO+"?id_ruta="+ruta_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if(response.length()<=0){

                        }

                        try {
                            JSONObject jsonObject = (JSONObject) response
                                    .get(0);
                            comentario = jsonObject.getString("comentario");

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("comentario",comentario);
                            editor.apply();
                            ViewDialogComentario viewDialogComentario = new ViewDialogComentario();
                            viewDialogComentario.showDialog(InicioActivity.this,comentario);

                        }catch (JSONException e)
                        {
                            e.printStackTrace();
                            Log.d("ESTATUS",e.getMessage());

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


    public void getEstatusRuta(final String aux_id,final String ruta_id,final String nomRuta, final String turno){
        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_ESTADO_RUTA+"?aux_id="+aux_id+"&ruta_id="+ruta_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if(response.length()<=0){
                            Log.d("ESTATUS",response.toString());

                        }

                        try {
                                JSONObject jsonObject = (JSONObject) response
                                        .get(0);
                                estatus =  Integer.parseInt(jsonObject.getString("estatus"));
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt("estatus",estatus);

                                editor.apply();
                                new Update(RutaDB.class).set("estatus_ruta=?",estatus).where("idRuta=?",idRuta).execute();
                                if(estatus==2){
                                    btnCerrarRegistro.setText("BAJAR A LOS ALUMNOS");
                                }

                        }catch (JSONException e)
                        {
                            e.printStackTrace();
                            Log.d("ESTATUS",e.getMessage());

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
    public void getEstatusRutaTarde(String aux_id,final String ruta_id,final String nomRuta, final String turno){
        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_ESTADO_RUTA_TARDE+"?aux_id="+aux_id+"&ruta_id="+ruta_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if(response.length()<=0){

                        }

                        try {
                            JSONObject jsonObject = (JSONObject) response
                                    .get(0);
                            estatus =  Integer.parseInt(jsonObject.getString("estatus"));
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("estatus",estatus);
                            editor.apply();
 /*Intent intent = new Intent(SeleccionRutaActivity.this, HomeActivity.class);
                                intent.putExtra("idRuta", ruta_id);
                                intent.putExtra("estatus",estatus);
                                intent.putExtra("nomRuta", nomRuta);
                                intent.putExtra("turno", turno);

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt("estatus",estatus);
                                editor.apply();

                                startActivity(intent);*/


                        }catch (JSONException e)
                        {
                            e.printStackTrace();
                            Log.d("ESTATUS",e.getMessage());

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


    public void getRutaTransporte(final String aux_id){
        rutas.clear();
        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_RUTA+"?aux_id="+aux_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if(response.length()<=0){
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            editor.commit();
                        }
                        if(estatus<2) {
                            try {
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject jsonObject = (JSONObject) response
                                            .get(i);

                                    String id_ruta_h = jsonObject.getString("id_ruta_h");
                                    String nombre_ruta = jsonObject.getString("nombre_ruta");
                                    String camion = jsonObject.getString("camion");
                                    String turno = jsonObject.getString("turno");
                                    String tipo_ruta = jsonObject.getString("tipo_ruta");

                                    String trn = "", truta = "", cmn;
                                    if (turno.equalsIgnoreCase("1")) {
                                        trn = "M";
                                    }
                                    if (turno.equalsIgnoreCase("2")) {
                                        trn = "T";
                                    }
                                    if (tipo_ruta.equalsIgnoreCase("1")) {
                                        truta = "G";
                                    }
                                    if (tipo_ruta.equalsIgnoreCase("2")) {
                                        truta = "K";
                                    }
                                    if (tipo_ruta.equalsIgnoreCase("3")) {
                                        truta = "T";
                                    }
                                    if (tipo_ruta.equalsIgnoreCase("4")) {
                                        truta = "R";
                                    }
                                    if (Integer.parseInt(camion) < 10) {
                                        cmn = "0" + camion;
                                    } else {
                                        cmn = camion;
                                    }

                                    String codigo = trn + truta + cmn;


                                        rutas.add(new Ruta(id_ruta_h, codigo + " " + nombre_ruta, camion, turno, tipo_ruta));

                                }

                            /*new Delete().from(RutaDB.class).execute();


                            for(int j=0; j<rutas.size(); j++){

                                    RutaDB rutaDB = new RutaDB();
                                    rutaDB.idRuta = rutas.get(j).getIdRutaH();
                                    rutaDB.nombreRuta = rutas.get(j).getNombreRuta();
                                    rutaDB.camion = rutas.get(j).getCamion();
                                    rutaDB.turno = rutas.get(j).getTurno();
                                    rutaDB.tipo_ruta = rutas.get(j).getTipoRuta();

                            }*/

                                rutas.add(new Ruta("-1", "Salir", "0", "0", "0"));
                                Log.d("TAMANIO", "" + rutas.size());
                                rAdapter = new NavigationHomeAdapter(rutas, iconos, "Auxiliar", "", 0, InicioActivity.this);
                                //mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView); // Assigning the RecyclerView Object to the xml View
                                //mRecyclerView.setHasFixedSize(true);

                                //mRecyclerView.setAdapter(rAdapter);                              // Setting the adapter to RecyclerView
                               // mLayoutManager = new LinearLayoutManager(InicioActivity.this);                 // Creating a layout Manager
                                //mRecyclerView.setLayoutManager(mLayoutManager);
                                //for(int j=0; j<rutas.size(); j++) {
                                if (viaMenu == 0) {
                                    idRuta = rutas.get(0).getIdRutaH();
                                    turno = rutas.get(0).getTurno();
                                    String nombreRuta = rutas.get(0).getNombreRuta();
                                    getEstatusRuta(aux_id, idRuta, nombreRuta, turno);
                                    //Como armar el codigo
                                    String codigo = "";
                                    String trn = "";
                                    String truta = "";
                                    String camion = "";

                                    lblRuta.setText(codigo + " " + nombreRuta);
                                    if (turno.equals("1")) {
                                        if (hayConexion()) {
                                            getAsistencia(idRuta, METODO_ALUMNOS_MAT);


                                        } else {
                                            getAsistenciaDB(idRuta);
                                            //estatus = sharedPreferences.getInt("estatus",0);
                                            List<RutaDB> lst = new Select().from(RutaDB.class).where("idRuta=?", idRuta).execute();
                                            estatus = lst.get(0).estatus_ruta;
                                        }

                                    }
                                    if (turno.equals("2")) {
                                        if (hayConexion()) {
                                            getAsistencia(idRuta, METODO_ALUMNOS_TAR);
                                            getEstatusRuta(aux_id, idRuta, nombreRuta, turno);
                                        } else {
                                            getAsistenciaDB(idRuta);
                                            estatus = sharedPreferences.getInt("estatus", 0);
                                        }

                                    }
                                }
                                //fin de viaMenu
                                //}


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
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

        AppTransporte.getInstance().addToRequestQueue(req);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_inicio, menu);

        // return true so that the menu pop up is opened
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_back) {
            super.onBackPressed();
        }

        return true;
    }


    @Override
    public void onBackPressed() {

    }

    public void getRutaTransporteDB(String aux_id) {
        rutas.clear();
        List<RutaDB> rutaDB = new Select().from(RutaDB.class).execute();
        ArrayList<RutaDB> dbRuta = new ArrayList<>(rutaDB);
        for (RutaDB r : dbRuta) {
            String id_ruta_h = r.idRuta;
            String nombre_ruta = r.nombreRuta;
            String camion = r.camion;
            String turno = r.turno;
            String tipo_ruta = r.tipo_ruta;
            String trn="",truta = "",cmn;
            if (turno.equalsIgnoreCase("1")) {
                trn = "M";
            }
            if (turno.equalsIgnoreCase("2")) {
                trn = "T";
            }
            if (tipo_ruta.equalsIgnoreCase("1")) {
                truta = "G";
            }
            if (tipo_ruta.equalsIgnoreCase("2")) {
                truta = "K";
            }
            if (tipo_ruta.equalsIgnoreCase("3")) {
                truta = "T";
            }
            if (tipo_ruta.equalsIgnoreCase("4")) {
                truta = "R";
            }
            if (Integer.parseInt(camion)<10) {
                cmn = "0"+camion;
            }else{
                cmn = camion;
            }

            String codigo = trn+truta+cmn;



            rutas.add(new Ruta(id_ruta_h, codigo+" "+nombre_ruta, camion, turno, tipo_ruta));
        }


        rutas.add(new Ruta("-1", "Salir", "0", "0", "0"));
        Log.d("TAMANIO", "" + rutas.size());
        /*rAdapter = new NavigationHomeAdapter(rutas, iconos, "Auxiliar", "", 0, InicioActivity.this);
        mRecyclerView = findViewById(R.id.recyclerView); // Assigning the RecyclerView Object to the xml View
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setAdapter(rAdapter);                              // Setting the adapter to RecyclerView
        mLayoutManager = new LinearLayoutManager(InicioActivity.this);                 // Creating a layout Manager
        mRecyclerView.setLayoutManager(mLayoutManager);*/

        if (viaMenu == 0) {
            idRuta = rutas.get(0).getIdRutaH();
            turno = rutas.get(0).getTurno();
            String nombreRuta = rutas.get(0).getNombreRuta();
            lblRuta.setText(nombreRuta);
            if (turno.equals("1")) {

                    Toast.makeText(getApplicationContext(),"Sin conexión",Toast.LENGTH_LONG).show();
                    getAsistenciaDB(idRuta);
                    estatus = sharedPreferences.getInt("estatus",0);
                    //getEstatusRuta(id_usuario, idRuta, nombreRuta, turno);
               }
            if (turno.equals("2")) {
                getAsistenciaDB(idRuta);
            }
        }

    }







    public boolean hayConexion() {
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();

    }

    //funciones NFC
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume", "1");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        // creating intent receiver for NFC events:
        IntentFilter filter = new IntentFilter();
        filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
        filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
        // enabling foreground dispatch for getting intent from NFC event:
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, new IntentFilter[]{filter}, this.techList);
    }

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



        //mTextView.setText('\n' + sb.toString());

        int counter=0;
        for(Asistencia asistencia : items){

            if(asistencia.getTarjeta().equals(hexadecimal)){
                generadorTono.startTone(ToneGenerator.TONE_CDMA_PIP, 500);
                generadorTono.startTone(ToneGenerator.TONE_CDMA_ANSWER, 500);
                //if (estatus == ASCENSO) {
                    //La ruta no está cerrada -> METODOS DE ASCENSOS
                    if (turno.equalsIgnoreCase(TURNO_MAN)) {
                        trabajarTurnoMan(asistencia);
                    }
                    if (turno.equalsIgnoreCase(TURNO_TAR)) {
                        trabajarTurnoTar(asistencia);
                    }
                //}
            }else{
                if(counter==0)
                    Toast.makeText(getApplicationContext(),"Esta tarjeta no existe en esta lista",Toast.LENGTH_LONG).show();
            }
            counter++;
        }



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


    //Dialogos
    public class ViewDialogComentario {

        public void showDialog(final Activity activity,final String comentario){
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);

            dialog.setContentView(R.layout.dialogo_comentario);
            final TextView txtComentario = dialog.findViewById(R.id.txtComentario);
            //TextView lblEncab = dialog.findViewById(R.id.lblEncab);
            Typeface t = Typeface.createFromAsset(activity.getAssets(),"fonts/GothamRoundedBook_21018.ttf");
            txtComentario.setTypeface(t);
            //lblEncab.setTypeface(t);
            txtComentario.setText(comentario);
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


}