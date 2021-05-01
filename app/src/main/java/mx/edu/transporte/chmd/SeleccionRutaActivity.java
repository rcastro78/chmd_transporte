package mx.edu.transporte.chmd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import mx.edu.transporte.chmd.servicios.NetworkChangeReceiver;
import retrofit2.Call;
import retrofit2.Callback;

public class SeleccionRutaActivity extends AppCompatActivity {
    TextView lblRuta;
    ListView lstRuta;
    static String BASE_URL;
    static String PATH;
    static String METODO_RUTA="getRutaTransporte.php";
    static String METODO_ESTADO_RUTA="getEstatusRuta.php";
    SharedPreferences sharedPreferences;
    String id_usuario;
    int estatus;
    ITransporteCHMD iTransporteCHMD;
    private ArrayList<Ruta> items = new ArrayList<>();
    NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccion_ruta);
        BASE_URL = this.getString(R.string.BASE_URL);
        PATH = this.getString(R.string.PATH);
        sharedPreferences = this.getSharedPreferences(this.getString(R.string.SHARED_PREF), 0);
        estatus = sharedPreferences.getInt("estatus",0);
        iTransporteCHMD = APIUtils.getTransporteService();
        id_usuario = sharedPreferences.getString("id_usuario","");
        lblRuta = findViewById(R.id.lblRuta);
        lstRuta = findViewById(R.id.lstRuta);

        if(hayConexion())
            getRutaTransporte2(id_usuario);
        else
            obtenerRutas();

        lstRuta.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Ruta r = (Ruta)lstRuta.getItemAtPosition(position);
                String idRuta = r.getIdRutaH();
                String nomRuta = r.getNombreRuta();
                String turno = r.getTurno();
                String tipo_ruta = r.getTipoRuta();
                String camion = r.getCamion();
                String trn = "", truta = "", cmn;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("nombreRuta",nomRuta);
                editor.apply();
                getEstatusRuta(id_usuario,idRuta,nomRuta,turno);


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
                                editor.putString("idRuta",ruta_id);
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


                        items.add(new Ruta(id_ruta_h,codigo+" "+nombre_ruta,camion,turno,tipo_ruta));
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



                }
            }

            @Override
            public void onFailure(Call<List<Ruta>> call, Throwable t) {

            }
        });
    }



    public void getRutaTransporte(String aux_id){


        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_RUTA+"?aux_id="+aux_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {



                        if(response.length()<=0){
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            //Toast.makeText(getApplicationContext(),foto,Toast.LENGTH_LONG).show();
                            editor.commit();
                        }

                        try {
                            for(int i=0; i<response.length(); i++){
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);
                                String id_ruta_h = jsonObject.getString("id_ruta_h");
                                String nombre_ruta = jsonObject.getString("nombre_ruta");
                                String camion = jsonObject.getString("camion");
                                String turno = jsonObject.getString("turno");
                                String tipo_ruta = jsonObject.getString("tipo_ruta");
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

                                items.add(new Ruta(id_ruta_h,codigo+" "+nombre_ruta,camion,turno,tipo_ruta));
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
                /*
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                        */

            }
        });

        // Adding request to request queue
        AppTransporte.getInstance().addToRequestQueue(req);
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
