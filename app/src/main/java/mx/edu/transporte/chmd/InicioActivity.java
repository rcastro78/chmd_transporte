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
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.telephony.SmsManager;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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
import mx.edu.transporte.chmd.servicios.LocalizacionService;
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
    TextView lblTotalInasist,lblTotales,lblAscDesc,lblInasist,lblAlumno,lblDatos;
    public static ListView lstAlumnos;
    String rsp,hexadecimal;
    FloatingActionButton btnComentario,fabAyuda;
    boolean todosSubidos;
    Button btnCerrarRegistro;
    int totalAscensos = 0;
    int totalInasistencias = 0;
    int totalAlumnos = 0;
    int totalBajan = 0;
    int totalDescensos = 0;
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
    static String METODO_ESTADO_RUTA="getEstatusRuta.php";
    static String METODO_ALUMNO_ASISTE="asistenciaAlumno.php";
    static String METODO_ALUMNO_SUBE_TARDE="asistenciaAlumnoTarde.php";
    static String METODO_ALUMNO_DESC="descensoAlumno.php";
    static String METODO_RUTA_DESC="descensoTodosMan.php";
    static String METODO_RUTA_ASC="ascensoTodosTar.php";
    static String METODO_ALUMNO_DESC_TARDE="descensoAlumnoTarde.php";
    static String METODO_ALUMNO_REINICIA="reiniciaAsistenciaAlumno.php";
    static String METODO_ALUMNO_REINICIA_TARDE="reiniciaAsistenciaAlumnoTarde.php";
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
        android.nfc.NfcManager manager = (android.nfc.NfcManager) getSystemService(Context.NFC_SERVICE);
        mNfcAdapter = manager.getDefaultAdapter();
        searchView = findViewById(R.id.searchView);
        lblRuta = findViewById(R.id.lblRuta);
        lblAscDesc = findViewById(R.id.lblAscDesc);
        lblTotalInasist =findViewById(R.id.lblTotalInasist);
        lblInasist = findViewById(R.id.lblInasist);
        lblTotales = findViewById(R.id.lblTotales);
        lblAlumno = findViewById(R.id.lblAlumno);
        lblDatos = findViewById(R.id.lblDatos);
        lstAlumnos = findViewById(android.R.id.list);
        btnComentario = findViewById(R.id.fabComentario);
        fabAyuda = findViewById(R.id.fabAyuda);
        btnCerrarRegistro = findViewById(R.id.btnCerrarRegistro);
        lblRuta.setTypeface(tf);
        lblAscDesc.setTypeface(tf);
        lblTotalInasist.setTypeface(tf);
        lblInasist.setTypeface(tf);
        lblTotales.setTypeface(tf);
        btnCerrarRegistro.setTypeface(tf);
        lblDatos.setTypeface(tf);
        lblAlumno.setTypeface(tf);
        estatus = sharedPreferences.getInt("estatus",-1);

        fabAyuda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                new AlertDialog.Builder(InicioActivity.this)
                        .setTitle("CHMD - Transporte")
                        .setMessage(msjRuta+lblRuta.getText().toString()+"?")

                        .setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String latitude = sharedPreferences.getString("latitude","0");
                                String longitude = sharedPreferences.getString("longitude","0");
                                String nombreRuta = sharedPreferences.getString("nombreRuta","");
                                enviarMensajeSMS("+50371276577","Tenemos un problema en la ruta " + nombreRuta + ", estamos en:  http://maps.google.com/?q="+latitude+","+longitude);

                            }
                        })

                        .setNegativeButton("Cancelar", null)
                        .setIcon(R.mipmap.ic_launcher)
                        .show();



                //Toast.makeText(getApplicationContext(),"Aún no implementado: enviar ubicación por alguna emergencia via SMS",Toast.LENGTH_LONG).show()

//


            }
        });

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
            final String nombreRuta = sharedPreferences.getString("nomRuta","");
            lblRuta.setText(nombreRuta);

            if(hayConexion()){
                if(turno.equals("1")){
                //Toast.makeText(getApplicationContext(),"Aqui se llama",Toast.LENGTH_LONG).show();
                    if(estatus==0) {
                        items.clear();
                        getAsistencia(idRuta, METODO_ALUMNOS_MAT);
                    }
                    if(estatus==1)
                      getAlumnosAbordo(idRuta,METODO_ALUMNOS_MAT);

            }
            if(turno.equals("2")){
                if(estatus==0) {
                    items.clear();
                    getAsistencia(idRuta, METODO_ALUMNOS_TAR);
                }
                if(estatus==1)
                    getAlumnosAbordo(idRuta,METODO_ALUMNOS_TAR);

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
        /*if(hayConexion())
            getRutaTransporte(aux_id);
        else{
            Toast.makeText(getApplicationContext(),"Sin conexión",Toast.LENGTH_LONG).show();
            getRutaTransporteDB(aux_id);
        }*/




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
                                    if(estatus==0){
                                        //Toast.makeText(getApplicationContext(),"Los niños se han subido y están listos para ir en el viaje hacia su casa",Toast.LENGTH_LONG).show();
                                        if (totalAscensos + totalInasistencias == totalAlumnos && totalAlumnos>0) {
                                            Ruta ruta = new Ruta(idRuta, "1");
                                            cerrarRuta(ruta);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putInt("estatus", 1);
                                            editor.putInt("retornar",1);
                                            editor.apply();
                                            //getAlumnosAbordo(idRuta, METODO_ALUMNOS_MAT);
                                            Intent intent = new Intent(InicioActivity.this, SeleccionRutaActivity.class);
                                            startActivity(intent);
                                            finish();

                                        }else{
                                            Toast.makeText(getApplicationContext(), "No se puede cerrar todavía", Toast.LENGTH_LONG).show();
                                        }

                                    }

                                    if(estatus==1){
                                        if (totalAscensos == totalBajan) {
                                            Ruta ruta = new Ruta(idRuta, "2");
                                            cerrarRuta(ruta);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putInt("estatus", 2);
                                            editor.putInt("retornar",0);
                                            editor.apply();

                                            //Finalizar el service
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                stopService(new Intent(InicioActivity.this, LocalizacionService.class));
                                            }else{
                                                stopService(new Intent(InicioActivity.this, LocalizacionService.class));
                                            }



                                            Intent intent = new Intent(InicioActivity.this, SeleccionRutaActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "No se puede cerrar todavía", Toast.LENGTH_LONG).show();
                                        }
                                    }




                                }


                                if(turno.equalsIgnoreCase(TURNO_TAR))
                                {
                                    if(estatus==0){
                                        //Toast.makeText(getApplicationContext(),"Los niños se han subido y están listos para ir en el viaje hacia su casa",Toast.LENGTH_LONG).show();
                                        if (totalAscensos + totalInasistencias == totalAlumnos && totalAlumnos>0) {
                                            Ruta ruta = new Ruta(idRuta, "1");
                                            cerrarRuta(ruta);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putInt("estatus", 1);
                                            editor.putInt("retornar",1);
                                            editor.apply();
                                            //getAlumnosAbordo(idRuta, METODO_ALUMNOS_TAR);
                                            Intent intent = new Intent(InicioActivity.this, SeleccionRutaActivity.class);
                                           startActivity(intent);

                                            finish();

                                        }else{
                                            Toast.makeText(getApplicationContext(), "No se puede cerrar todavía", Toast.LENGTH_LONG).show();
                                        }

                                    }

                                    if(estatus==1){
                                        if (totalAscensos == totalBajan) {
                                            Ruta ruta = new Ruta(idRuta, "2");
                                            cerrarRuta(ruta);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putInt("estatus", 2);
                                            editor.putInt("retornar",0);
                                            editor.apply();
                                            Intent intent = new Intent(InicioActivity.this, SeleccionRutaActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "No se puede cerrar todavía", Toast.LENGTH_LONG).show();
                                        }
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

//De verde a amarillo
        if (asistencia.getAscenso().equalsIgnoreCase("1") &&
                asistencia.getDescenso().equalsIgnoreCase("1") && estatus==1) {


            new android.app.AlertDialog.Builder(InicioActivity.this)
                    .setTitle("Transporte")
                    .setMessage("¿Desea deshacer el registro de descenso de " + asistencia.getNombreAlumno() + "?")

                    .setPositiveButton("Autorizo", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Ejecutar el POST de asistencia y recargar la vista
                            //De verde a amarillo
                            if(!hayConexion()){
                                registraAscensoDB(idRuta,asistencia.getIdAlumno());
                                getAsistenciaDB(idRuta);
                            }else{
                                items.clear();
                                registraAscenso(asistencia.getIdAlumno(),idRuta,asistencia.getNombreAlumno());
                            }

                        }
                    })

                    .setNeutralButton("No Autorizo", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })


                    .setIcon(R.drawable.logo2)
                    .show();





        }


        //De amarillo a verde, son los niños que ya van en el bus
        if (asistencia.getAscenso().equalsIgnoreCase("1") &&
                asistencia.getDescenso().equalsIgnoreCase("0") && estatus==1) {

            if(!hayConexion()){
                registraDescensoTardeDB(idRuta,asistencia.getIdAlumno());
                getAsistenciaDB(idRuta);
            }else{
                items.clear();
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
                //items.clear();
                registraAscenso(asistencia.getIdAlumno(), idRuta,asistencia.getNombreAlumno());

            }
         }


        //de amarillo a blanco
        if (asistencia.getAscenso().equalsIgnoreCase("1") &&
                asistencia.getDescenso().equalsIgnoreCase("0") && estatus==0) {

            new android.app.AlertDialog.Builder(InicioActivity.this)
                    .setTitle("Transporte")
                    .setMessage("¿Desea reiniciar el registro de asistencia de " + asistencia.getNombreAlumno() + "?")

                    .setPositiveButton("Autorizo", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Ejecutar el POST de asistencia y recargar la vista
                            //De amarillo a blanco
                            if(!hayConexion()) {
                                reiniciaRegistroDB(idRuta,asistencia.getIdAlumno());
                                getAsistenciaDB(idRuta);
                            }else{
                                items.clear();
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


                    .setIcon(R.drawable.logo2)
                    .show();

        }


        //De rosado a amarillo (no pregunta)
        if (asistencia.getAscenso().equalsIgnoreCase("2") &&
                asistencia.getDescenso().equalsIgnoreCase("2") && estatus==0) {
            if(!hayConexion()) {
                registraAscensoDB(idRuta, asistencia.getIdAlumno());
                getAsistenciaDB(idRuta);
            }else {
                items.clear();
                registraAscenso(asistencia.getIdAlumno(), idRuta, asistencia.getNombreAlumno());
            }
        }

    }
    public void trabajarTurnoTar(final Asistencia asistencia){

        estatus = sharedPreferences.getInt("estatus",0);

        //De verde volver al amarillo
        if (asistencia.getAscenso_t().equalsIgnoreCase("1") &&
                asistencia.getDescenso_t().equalsIgnoreCase("1")  && estatus==1) {


            new android.app.AlertDialog.Builder(InicioActivity.this)
                    .setTitle("Transporte")
                    .setMessage("¿Desea reiniciar el registro de asistencia de " + asistencia.getNombreAlumno() + "?")

                    .setPositiveButton("Autorizo", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Ejecutar el POST de asistencia y recargar la vista
                            //De amarillo a blanco
                            if(!hayConexion()){
                                //No pide diálogo
                                registraAscensoTardeDB(idRuta,asistencia.getIdAlumno());
                                getAsistenciaDBTarde(idRuta);
                            }else{
                                registraAscensoTarde(asistencia.getIdAlumno(), idRuta,asistencia.getNombreAlumno());
                            }
                        }
                    })

                    .setNeutralButton("No Autorizo", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })


                    .setIcon(R.drawable.logo2)
                    .show();




        }



        //De amarillo a verde, son los niños que ya van en el bus

        if (asistencia.getAscenso_t().equalsIgnoreCase("1") &&
                asistencia.getDescenso_t().equalsIgnoreCase("0")  && estatus==1) {
            if(!hayConexion()){
                //No pide diálogo
                registraDescensoTardeDB(idRuta,asistencia.getIdAlumno());
                getAsistenciaDBTarde(idRuta);
            }else{
               registraDescensoTarde(asistencia.getIdAlumno(), idRuta,asistencia.getNombreAlumno());
            }
        }


        //De blanco a amarillo
        if (asistencia.getAscenso_t().equalsIgnoreCase("0") &&
                asistencia.getDescenso_t().equalsIgnoreCase("0")) {
            if(!hayConexion()){
                //No pide diálogo
                registraAscensoTardeDB(idRuta,asistencia.getIdAlumno());
                getAsistenciaDBTarde(idRuta);
            }else{
                registraAscensoTarde(asistencia.getIdAlumno(), idRuta,asistencia.getNombreAlumno());

            }
        }


        //de amarillo a blanco
        if (asistencia.getAscenso_t().equalsIgnoreCase("1") &&
                asistencia.getDescenso_t().equalsIgnoreCase("0") && estatus==0) {

            new android.app.AlertDialog.Builder(InicioActivity.this)
                    .setTitle("Transporte")
                    .setMessage("¿Desea reiniciar el registro de asistencia de " + asistencia.getNombreAlumno() + "?")

                    .setPositiveButton("Autorizo", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Ejecutar el POST de asistencia y recargar la vista
                            //De amarillo a blanco
                            if(!hayConexion()) {
                                reiniciaRegistroTardeDB(idRuta,asistencia.getIdAlumno());
                                getAsistenciaDBTarde(idRuta);
                            }else{
                                items.clear();
                                reiniciaAsistenciaTarde(asistencia.getIdAlumno(), idRuta);
                            }
                        }
                    })

                    .setNeutralButton("No Autorizo", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })


                    .setIcon(R.drawable.logo2)
                    .show();

        }




        //De rosado a amarillo (no pregunta)
        if (asistencia.getAscenso_t().equalsIgnoreCase("2") &&
                asistencia.getDescenso_t().equalsIgnoreCase("2")  && estatus==0) {
            if(!hayConexion()) {
                registraAscensoTardeDB(idRuta, asistencia.getIdAlumno());
                getAsistenciaDBTarde(idRuta);
            }else {
                items.clear();
                registraAscensoTarde(asistencia.getIdAlumno(), idRuta, asistencia.getNombreAlumno());
            }
        }

    }
    public void getAlumnosAbordo(String ruta_id, String metodo){
        //getTotal(idRuta,METODO_ALUMNOS_TOTL_MAT);
        totalBajan = 0;
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
                                if(ascenso.equalsIgnoreCase("1") || ascenso_t.equalsIgnoreCase("1"))
                                    items.add(new Asistencia(tarjeta,id_alumno,nombreAlumno,domicilio,hora_manana,
                                        horaRegreso,ascenso,descenso,domicilio_s,grupo,grado,nivel,foto,
                                        false,false, ascenso_t,descenso_t,salida,ordenIn,ordenOut,false,false));

                                if(turno.equalsIgnoreCase("1"))
                                if(ascenso.equalsIgnoreCase("1") && descenso.equalsIgnoreCase("1"))
                                    totalBajan++;
                                if (turno.equalsIgnoreCase("2"))
                                if(ascenso_t.equalsIgnoreCase("1") && descenso_t.equalsIgnoreCase("1"))
                                    totalBajan++;

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


                            lblAscDesc.setText("Descensos/Total");
                            lblTotales.setText(totalBajan+"/"+items.size());
                            lblTotalInasist.setText("N/A");
                            totalAlumnos=items.size();
                            totalAscensos=items.size();
                            totalInasistencias=0;

                            if(estatus==1) {
                                //Cuando van a bajar
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


        new Delete().from(AlumnoDB.class).where("id_ruta_h=?",idRuta).execute();

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+metodo+"?ruta_id="+ruta_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {


                        if(response.length()<=0){

                            SharedPreferences.Editor editor = sharedPreferences.edit();

                        }
                        int parada=1;
                        try {
                            items.clear();
                            totalAscensos=0;
                            totalInasistencias = 0;
                            totalBajan = 0;
                            for(int i=0; i<response.length(); i++){
                                //new Delete().from(AlumnoDB.class).execute();
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);



                                String tarjeta = "";
                                if(jsonObject.getString("tarjeta")!=null){
                                    tarjeta = jsonObject.getString("tarjeta");
                                }

                                //Esto es para pruebas.
                                /*if(jsonObject.getString("tarjeta").equalsIgnoreCase("87F94A62")){
                                    tarjeta="DE8B7889";
                                }*/

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


                                if(estatus==0) {
                                    if (parada == 0) {
                                        parada = 1;
                                    }
                                    items.add(new Asistencia(tarjeta, id_alumno, nombreAlumno, domicilio, hora_manana,
                                            horaRegreso, ascenso, descenso, domicilio_s, grupo, grado, nivel, foto,
                                            false, false, ascenso_t, descenso_t, salida, String.valueOf(parada), String.valueOf(parada), true,true));
                                }
                                if(estatus==1) {
                                    //No se debe ver el botón de inasitencia
                                    if (parada == 0) {
                                        parada = 1;
                                    }
                                    if ((!ascenso.equalsIgnoreCase("2") && !descenso.equalsIgnoreCase("2"))
                                    || (!ascenso_t.equalsIgnoreCase("2") && !descenso_t.equalsIgnoreCase("2")))
                                        items.add(new Asistencia(tarjeta, id_alumno, nombreAlumno, domicilio, hora_manana,
                                                horaRegreso, ascenso, descenso, domicilio_s, grupo, grado, nivel, foto,
                                                false, false, ascenso_t, descenso_t, salida, String.valueOf(parada), String.valueOf(parada), false,false));
                                }





                                //Contar cuantos amarillos (suben)
                                if(turno.equalsIgnoreCase("1")){


                                    if(i<response.length()-1) {
                                        JSONObject jsonObject1 = (JSONObject) response
                                                .get(i+1);

                                        if (!jsonObject.getString("hora_manana")
                                                .equalsIgnoreCase(jsonObject1.getString("hora_manana"))) {
                                            parada++;
                                        }

                                    }


                                    if(ascenso.equalsIgnoreCase("1"))
                                        totalAscensos++;

                                    if(ascenso.equalsIgnoreCase("2") && descenso.equalsIgnoreCase("2"))
                                        totalInasistencias++;

                                    if(ascenso.equalsIgnoreCase("1") && descenso.equalsIgnoreCase("1"))
                                        totalBajan++;



                                }
                                if(turno.equalsIgnoreCase("2")){

                                    if(i<response.length()-1) {
                                        JSONObject jsonObject1 = (JSONObject) response
                                                .get(i+1);

                                        if (!jsonObject.getString("hora_regreso")
                                                .equalsIgnoreCase(jsonObject1.getString("hora_regreso"))) {
                                            parada++;
                                        }

                                    }


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
                            if(estatus==0) {
                                lblTotales.setText(totalAscensos + "/" + totalAlumnos);
                                lblTotalInasist.setText(String.valueOf(totalInasistencias));
                            }
                            if(estatus==1) {
                                lblTotales.setText(totalBajan + "/" + totalAscensos);
                                lblTotalInasist.setText("N/A");
                            }


                            if(estatus==1) {
                                //Cuando van a bajar
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
                                    return (a1.getAscenso()).compareTo(a2.getAscenso());
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


    public void registraDescensoTardeDB(String ruta_id, String idAlumno){
        //Ascenso 1
        //Descenso 1
        new Update(AlumnoDB.class)
                .set("ascenso_t='1', descenso_t='1', procesado=1")
                .where("id_ruta_h="+ruta_id+" AND id_alumno="+idAlumno)
                .execute();

        notifyAdapter();

    }

    public void registraDescensoDB(String ruta_id, String idAlumno){
        //Ascenso 1
        //Descenso 1
        new Update(AlumnoDB.class)
                .set("ascenso='1', descenso='1', procesado=1")
                .where("id_ruta_h="+ruta_id+"and descenso<2 and ascenso<2 AND id_alumno="+idAlumno)
                .execute();

        notifyAdapter();

    }

    public void registraDescensoDB(String ruta_id){
        //Descenso de toda la ruta
        new Update(AlumnoDB.class)
                .set("ascenso='1', descenso='1', procesado=1")
                .where("id_ruta_h="+ruta_id+" and descenso<2 and ascenso<2 ")
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

    public void registraAscensoTardeDB(String ruta_id){
        //Ascensot 1
        //Descensot 0
        new Update(AlumnoDB.class)
                .set("ascenso_t='1', descenso_t='0', salida='0',procesado=1")
                .where("id_ruta_h="+ruta_id)
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


    public void registraDescensoRutaMan(String ruta_id){
        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_RUTA_DESC+"?id_ruta="+ruta_id,
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

    public void registraAscensoRutaTar(String ruta_id){
        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_RUTA_ASC+"?id_ruta="+ruta_id,
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
        Toast.makeText(getApplicationContext(),""+estatus,Toast.LENGTH_LONG).show();

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_ALUMNO_DESC_TARDE+"?id_alumno="+alumno_id+"&id_ruta="+ruta_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        try {

                            if(turno.equalsIgnoreCase(TURNO_MAN)){
                                if(!hayConexion())
                                    getAsistenciaDB(idRuta);
                                else {
                                    if(estatus==0) {
                                        items.clear();
                                        getAsistencia(idRuta, METODO_ALUMNOS_MAT);
                                    }
                                    if(estatus==1) {
                                        items.clear();
                                        getAlumnosAbordo(idRuta, METODO_ALUMNOS_MAT);
                                    }
                                }
                            }
                            if(turno.equalsIgnoreCase(TURNO_TAR)){
                                if(!hayConexion())
                                    getAsistenciaDB(idRuta);
                                else {
                                    if(estatus==0){
                                        items.clear();
                                        getAsistencia(idRuta, METODO_ALUMNOS_TAR);
                                    }
                                    if(estatus==1) {
                                        items.clear();
                                        getAlumnosAbordo(idRuta, METODO_ALUMNOS_TAR);
                                    }
                                }

                            }

                            notifyAdapter();

                        }catch (Exception e)
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

                             Toast.makeText(getApplicationContext(),"La ruta ha sido cerrada",Toast.LENGTH_LONG).show();


                        }
                    }

                    @Override
                    public void onFailure(Call<Ruta> call, Throwable t) {

                    }
                });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_inicio, menu);

        // return true so that the menu pop up is opened
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        if (item.getItemId() == R.id.action_back) {
            super.onBackPressed();
        }

        if(item.getItemId()==R.id.action_bajar){


            if(turno.equalsIgnoreCase(TURNO_MAN)){
                if(estatus==1) {


                    new android.app.AlertDialog.Builder(InicioActivity.this)
                            .setTitle("Transporte")
                            .setMessage("¿Desea efectuar el descenso de todos los alumnos de la ruta de la mañana?")

                            .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if(hayConexion())
                                        registraDescensoRutaMan(idRuta);
                                    else
                                        registraDescensoDB(idRuta);

                                }
                            })

                            .setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })


                            .setIcon(R.drawable.logo2)
                            .show();



                    //Bajar todos los que van en el camión y llegaron al colegio



                }else{
                    Toast.makeText(getApplicationContext(),"Esto solo funciona para el turno de la mañana para bajar a todos los alumnos",Toast.LENGTH_LONG).show();
                }
            }else{
                //Turno de la tarde (subirlos al bus en la tarde)
                if(estatus==0) {


                    new android.app.AlertDialog.Builder(InicioActivity.this)
                            .setTitle("Transporte")
                            .setMessage("¿Desea efectuar el ascenso de todos los alumnos de la ruta de la tarde?")

                            .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if(hayConexion())
                                        registraAscensoRutaTar(idRuta);
                                    else
                                        registraAscensoTardeDB(idRuta);

                                }
                            })

                            .setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })


                            .setIcon(R.drawable.logo2)
                            .show();



                    //Bajar todos los que van en el camión y llegaron al colegio



                }else{
                    Toast.makeText(getApplicationContext(),"Esto solo funciona para el turno de la mañana para bajar a todos los alumnos",Toast.LENGTH_LONG).show();
                }
            }






            /*if(estatus==1 && turno.equalsIgnoreCase(TURNO_MAN)){
                for(Asistencia asistencia:items) {
                    if (asistencia.getAscenso().equalsIgnoreCase("2")) {
                        Log.d("ALUMNOS", asistencia.getAscenso() + " -> " + asistencia.getNombreAlumno());
                    }else{

                    }
                }
            }else{
                Toast.makeText(getApplicationContext(),"Esto solo funciona para el turno de la mañana para bajar a todos los alumnos",Toast.LENGTH_LONG).show();
            }*/
        }


        if(item.getItemId() == R.id.action_logout){

            new android.app.AlertDialog.Builder(InicioActivity.this)
                    .setTitle("Transporte")
                    .setMessage("¿Desea cerrar sesión?")

                    .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("adm_id","0");
                            editor.putString("id_usuario","0");
                            editor.putString("correo","");
                            editor.putInt("cuentaValida",0);
                            editor.apply();
                            //Finalizar el service
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                stopService(new Intent(InicioActivity.this, LocalizacionService.class));
                            }else{
                                stopService(new Intent(InicioActivity.this, LocalizacionService.class));
                            }


                            Intent intent = new Intent(InicioActivity.this,LoginActivity.class);
                            startActivity(intent);
                            finish();


                        }
                    })

                    .setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })


                    .setIcon(R.drawable.logo2)
                    .show();


        }

        return true;
    }


    @Override
    public void onBackPressed() {

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
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            // creating intent receiver for NFC events:
            IntentFilter filter = new IntentFilter();
            filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
            filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
            filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
            // enabling foreground dispatch for getting intent from NFC event:
            NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, new IntentFilter[]{filter}, this.techList);
        }else{
            Toast.makeText(getApplicationContext(),"Este dispositivo no tiene habilitado el NFC. Solo puede trabajar de forma manual",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d("onPause", "1");

        // disabling foreground dispatch:
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {
            NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            nfcAdapter.disableForegroundDispatch(this);
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("onNewIntent", "1");
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {
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
        }else{
            Toast.makeText(getApplicationContext(),
                    "NFC no está habilitado en el dispositivo, solo puede trabajar de forma manual",
                    Toast.LENGTH_LONG).show();
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

       for(Asistencia asistencia:items){

           //Para pruebas de lectura
            /*if (asistencia.getTarjeta().equalsIgnoreCase("25B70421")) {
                hexadecimal = "25B70421";
            }*/
            if (asistencia.getTarjeta().equals(hexadecimal)) {
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
            } else {
                if (counter == 0)
                    //DE8B7889
                    Toast.makeText(getApplicationContext(), "Esta tarjeta no existe en esta lista: " + hexadecimal, Toast.LENGTH_LONG).show();
            }
            counter++;
            notifyAdapter();
            lstAlumnos.invalidate();
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


    public void enviarMensajeSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Se envió el mensaje",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

}