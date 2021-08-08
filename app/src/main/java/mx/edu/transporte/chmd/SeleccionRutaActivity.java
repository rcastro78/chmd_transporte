package mx.edu.transporte.chmd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import mx.edu.transporte.chmd.adapter.RutaAdapter;
import mx.edu.transporte.chmd.modelos.Ruta;
import mx.edu.transporte.chmd.modelosDB.RutaDB;
import mx.edu.transporte.chmd.networking.APIUtils;
import mx.edu.transporte.chmd.networking.ITransporteCHMD;
import mx.edu.transporte.chmd.servicios.LocalizacionService;
import mx.edu.transporte.chmd.servicios.NetworkChangeReceiver;
import retrofit2.Call;
import retrofit2.Callback;

public class SeleccionRutaActivity extends AppCompatActivity {
    TextView lblRuta;
    ListView lstRuta;
    int retornar=0;
    FloatingActionButton fabLogout,fabConfig;
    static String BASE_URL;
    static String PATH;
    static String METODO_RUTA="getRutaTransporte.php";
    static String METODO_ESTADO_RUTA="getEstatusRuta.php";
    SharedPreferences sharedPreferences;
    String id_usuario;
    int estatus,cuentaValida;
    ITransporteCHMD iTransporteCHMD;
    private ArrayList<Ruta> items = new ArrayList<>();
    NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccion_ruta);
        BASE_URL = this.getString(R.string.BASE_URL);
        PATH = this.getString(R.string.PATH);
        sharedPreferences = this.getSharedPreferences(this.getString(R.string.SHARED_PREF), 0);
        estatus = sharedPreferences.getInt("estatus",0);
        cuentaValida = sharedPreferences.getInt("cuentaValida",0);
        retornar = sharedPreferences.getInt("retornar",0);
        iTransporteCHMD = APIUtils.getTransporteService();
        id_usuario = sharedPreferences.getString("id_usuario","");
        lblRuta = findViewById(R.id.lblRuta);
        lstRuta = findViewById(R.id.lstRuta);
        fabLogout = findViewById(R.id.fabLogout);

        if(retornar==1){
            Intent intent = new Intent(SeleccionRutaActivity.this,InicioActivity.class);
            startActivity(intent);
        }




        fabLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new android.app.AlertDialog.Builder(SeleccionRutaActivity.this)
                        .setTitle("Transporte")
                        .setMessage("¿Desea cerrar sesión?")

                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("adm_id","0");
                                editor.putString("id_usuario","0");
                                editor.putString("correo","");
                                editor.putInt("cuentaValida",0);
                                editor.putInt("retornar",0);
                                editor.apply();
                                Intent intent = new Intent(SeleccionRutaActivity.this,LoginActivity.class);
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
        });

        if(cuentaValida==0)
        {
            System.exit(0);
            finish();
        }
        if(hayConexion())
            getRutaTransporte2(id_usuario);
        else
            obtenerRutas();

        lstRuta.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {




                Ruta r = (Ruta)lstRuta.getItemAtPosition(position);
                String idRuta = r.getIdRutaH();

                if(!idRuta.equalsIgnoreCase("0")) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(new Intent(SeleccionRutaActivity.this, LocalizacionService.class));
                    }else{
                        startService(new Intent(SeleccionRutaActivity.this, LocalizacionService.class));
                    }

                    String nomRuta = r.getNombreRuta();
                    String turno = r.getTurno();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("nombreRuta", nomRuta);
                    editor.putString("idRuta",idRuta);
                    editor.putInt("retornar",0);
                    editor.apply();
                    if(hayConexion())
                        getEstatusRuta(id_usuario, idRuta, nomRuta, turno);
                    else{
                        Ruta r1 = obtenerRutas(idRuta);
                        String nomRuta1 = r1.getNombreRuta();
                        String turno1 = r1.getTurno();
                        SharedPreferences.Editor editor1 = sharedPreferences.edit();
                        editor1.putString("nombreRuta", nomRuta1);
                        editor1.putInt("retornar",0);
                        editor.apply();

                        if(estatus<2) {
                            Intent intent = new Intent(SeleccionRutaActivity.this, InicioActivity.class);
                            intent.putExtra("idRuta", idRuta);
                            intent.putExtra("estatus", estatus);
                            intent.putExtra("nomRuta", nomRuta1);
                            intent.putExtra("turno", turno1);

                            SharedPreferences.Editor editor2 = sharedPreferences.edit();
                            editor.putInt("estatus", estatus);

                            editor.putInt("estatus", estatus);
                            editor.putString("idRuta", idRuta);
                            editor.putString("nomRuta", nomRuta1);
                            editor.putString("turno", turno1);
                            editor.apply();

                            startActivity(intent);
                        }


                    }
                }
            }
        });
    }


    public void getEstatusRuta(String aux_id,final String ruta_id,final String nomRuta, final String turno){
        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_ESTADO_RUTA+"?aux_id="+aux_id+"&ruta_id="+ruta_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if(response.length()<=0){

                        }

                        try {
                            for(int i=0; i<response.length(); i++){
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                estatus =  Integer.parseInt(jsonObject.getString("estatus"));
                         }

                            if(estatus<2) {
                                Intent intent = new Intent(SeleccionRutaActivity.this, InicioActivity.class);
                                intent.putExtra("idRuta", ruta_id);
                                intent.putExtra("estatus",estatus);
                                intent.putExtra("nomRuta", nomRuta);
                                intent.putExtra("turno", turno);

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt("estatus",estatus);
                                editor.putString("idRuta",ruta_id);
                                editor.putInt("estatus",estatus);
                                editor.putString("nomRuta",nomRuta);
                                editor.putString("turno",turno);
                                editor.apply();

                                startActivity(intent);

                            }else{
                                Toast.makeText(getApplicationContext(),"Esta ruta ya está cerrada",Toast.LENGTH_LONG).show();
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


        AppTransporte.getInstance().addToRequestQueue(req);
    }


    int i=0;
    public void getRutaTransporte2(String aux_id){

        Call<List<Ruta>> rutaTransporte = iTransporteCHMD.getRutaTransporte(aux_id);
        rutaTransporte.enqueue(new Callback<List<Ruta>>() {
            @Override
            public void onResponse(Call<List<Ruta>> call, retrofit2.Response<List<Ruta>> response) {
                if(response.isSuccessful()){
                    for(Ruta r : response.body()){

                        String id_ruta_h = r.getIdRutaH();
                        String nombre_ruta = r.getNombreRuta();
                        String camion = r.getCamion();
                        String turno = r.getTurno();
                        String tipo_ruta = r.getTipoRuta();
                        String estatus = r.getEstatus();
                        String trn="",truta="",cmn="";
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

                        if(Integer.parseInt(estatus)<2)
                            items.add(new Ruta(id_ruta_h,codigo+" "+nombre_ruta,camion,turno,tipo_ruta));
                        else
                        if(i==0)
                                items.add(new Ruta("0","No hay rutas disponibles","0","0","-1"));
                        i++;
                    }

                    RutaAdapter adapter = new RutaAdapter(SeleccionRutaActivity.this,items);
                    lstRuta.setAdapter(adapter);

                    //Borrar tabla de rutas
                    new Delete().from(RutaDB.class).execute();
                    //Llenar tabla de rutas
                    for(int j=0; j<items.size(); j++){
                        RutaDB rutaDB = new RutaDB();
                        rutaDB.idRuta = items.get(j).getIdRutaH();
                        rutaDB.nombreRuta = items.get(j).getNombreRuta();
                        rutaDB.camion = items.get(j).getCamion();
                        rutaDB.turno = items.get(j).getTurno();
                        rutaDB.tipo_ruta = items.get(j).getTipoRuta();
                        rutaDB.save();
                    }



                }else{
                    items.add(new Ruta("0","No hay rutas disponibles","0","0","-1"));
                    RutaAdapter adapter = new RutaAdapter(SeleccionRutaActivity.this,items);
                    lstRuta.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Ruta>> call, Throwable t) {

            }
        });
    }


    public boolean hayConexion() {
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();

    }


    public void obtenerRutas(){
        items.clear();
        ArrayList<RutaDB> dbRuta = new ArrayList<>();
        List<RutaDB> list = new Select().from(RutaDB.class).execute();
        dbRuta.addAll(list);
        for(int i=0; i<dbRuta.size(); i++){

            String id_ruta_h = dbRuta.get(i).idRuta;
            String nombre_ruta = dbRuta.get(i).nombreRuta;
            String camion = dbRuta.get(i).camion;
            String turno = dbRuta.get(i).turno;
            String tipo_ruta = dbRuta.get(i).tipo_ruta;
            items.add(new Ruta(id_ruta_h,nombre_ruta,camion,turno,tipo_ruta));
        }

        RutaAdapter adapter = new RutaAdapter(SeleccionRutaActivity.this,items);
        lstRuta.setAdapter(adapter);

    }

    public Ruta obtenerRutas(String id_ruta_h){
        Ruta r = null;
        ArrayList<RutaDB> dbRuta = new ArrayList<>();
        List<RutaDB> list = new Select().from(RutaDB.class).where("idRuta=?",id_ruta_h).execute();
        dbRuta.addAll(list);

        for(int i=0; i<dbRuta.size(); i++){


            String nombre_ruta = dbRuta.get(i).nombreRuta;
            String camion = dbRuta.get(i).camion;
            String turno = dbRuta.get(i).turno;
            String tipo_ruta = dbRuta.get(i).tipo_ruta;
            r = new Ruta(id_ruta_h,nombre_ruta,camion,turno,tipo_ruta);
        }

       return r;

    }



    protected void onStart(){
        super.onStart();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);
    }
    @Override
    protected void onStop(){
        super.onStop();
        unregisterReceiver(networkChangeReceiver);
    }

}
