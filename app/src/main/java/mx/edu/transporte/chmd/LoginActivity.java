package mx.edu.transporte.chmd;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import mx.edu.transporte.chmd.adapter.RutaAdapter;
import mx.edu.transporte.chmd.modelos.Ruta;
import mx.edu.transporte.chmd.modelos.Usuario;
import mx.edu.transporte.chmd.modelos.Valida;
import mx.edu.transporte.chmd.modelosDB.AlumnoDB;
import mx.edu.transporte.chmd.modelosDB.RutaDB;
import mx.edu.transporte.chmd.modelosDB.UsuarioDB;
import mx.edu.transporte.chmd.networking.APIUtils;
import mx.edu.transporte.chmd.networking.ITransporteCHMD;
import mx.edu.transporte.chmd.servicios.NetworkChangeReceiver;
import retrofit2.Call;
import retrofit2.Callback;

public class LoginActivity extends AppCompatActivity {
    static String BASE_URL;
    static String RUTA;
    static String METODO_LOGIN="validarSesion.php";
    static String METODO_RUTA="getRutaTransporte.php";
    int valida;
    String email,clave;
    TextView txtUsuario,txtClave;
    Button btnLogin;
    ArrayList<Ruta> items = new ArrayList<>();
    SharedPreferences sharedPreferences;
    NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
    private static final int REQUEST_FINE_ACCESS_PERMISSION = 201;
    ITransporteCHMD iTransporteCHMD;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        iTransporteCHMD = APIUtils.getTransporteService();
        sharedPreferences = this.getSharedPreferences(this.getString(R.string.SHARED_PREF), 0);
        txtClave = findViewById(R.id.txtClave);
        txtUsuario = findViewById(R.id.txtUsuario);
        btnLogin = findViewById(R.id.btnLogin);

        txtUsuario.setText(sharedPreferences.getString("correo",""));

        email = txtUsuario.getText().toString();
        clave = txtClave.getText().toString();
        BASE_URL = this.getString(R.string.BASE_URL);
        RUTA = this.getString(R.string.PATH);

        //compararToken(sharedPreferences.getString("correo",""));

        //Permiso
        try {
            if (ActivityCompat.checkSelfPermission( LoginActivity.this,android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission( LoginActivity.this, Manifest.permission.SEND_SMS ) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission( LoginActivity.this, Manifest.permission.FOREGROUND_SERVICE ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(LoginActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS,
                                Manifest.permission.FOREGROUND_SERVICE},
                        1);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("GEO",e.getMessage());
        }



        btnLogin.setOnClickListener(v -> {

            email = txtUsuario.getText().toString();
            clave = txtClave.getText().toString();

            if(hayConexion()){

                if(email.length()>0 && clave.length()>0){
                    //validarSesion2(email);
                    //iniciarSesion(email,clave);
                    /*Date c = Calendar.getInstance().getTime();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-ddd", Locale.getDefault());
                    String hoy = df.format(c);
                    String token="";
                    String base = email+hoy;
                    token = Base64.getEncoder().encodeToString(base.getBytes());*/

                    iniciarSesion(email,clave);


                }else{
                    Toast.makeText(getApplicationContext(),"Ambos campos son obligatorios",Toast.LENGTH_LONG).show();
                }
            }else{
                int valido = loginOffline(email,clave);
                if(valido>0){
                    Intent intent = new Intent(LoginActivity.this,SeleccionRutaActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });



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

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
        System.exit(0);
    }

    public boolean hayConexion() {
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();

    }


    public int loginOffline(String correo, String clave){
        int v = 0;


            List<UsuarioDB> list = new Select().from(UsuarioDB.class).where("usuario=? AND clave=?",correo,clave).execute();
            try {
                v = list.size();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("adm_id",list.get(0).adm_id);
                editor.putString("id_usuario",list.get(0).idAuxiliar);
                editor.putString("correo",correo);
                editor.putInt("cuentaValida",1);


            }catch (Exception ex){
                v = -1;
                Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            }

        
        return v;

    }

/*
    public void validarSesion(String usuario_id,String clave){
        iTransporteCHMD.validarSesion(usuario_id).enqueue(new Callback<List<Valida>>() {
            @Override
            public void onResponse(Call<List<Valida>> call, retrofit2.Response<List<Valida>> response) {

                if(response.isSuccessful()){
                    for(Valida v : response.body()){
                        if(Integer.parseInt(v.getSesionValida())==0){

                            Date c = Calendar.getInstance().getTime();
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-ddd", Locale.getDefault());
                            String validez = df.format(c);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("validoHasta",validez);
                            editor.apply();
                            iniciarSesion(usuario_id,clave);
                        }else{
                            Toast.makeText(getApplicationContext(), "Tu sesión es válida hasta las 11:59 PM", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this,SeleccionRutaActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }


            }

            @Override
            public void onFailure(Call<List<Valida>> call, Throwable t) {
                Log.d("VALIDAR_SESION",t.getMessage());
            }
        });
    }
*/
    @SuppressLint("NewApi")


    public void validarSesion2(String usuario_id,String token){
        iTransporteCHMD.validarSesion(usuario_id,token).enqueue(new Callback<List<Valida>>() {
            @Override
            public void onResponse(Call<List<Valida>> call, retrofit2.Response<List<Valida>> response) {

                if(response.isSuccessful()){
                    for(Valida v : response.body()){
                        if(Integer.parseInt(v.getSesionValida())==1){
                            Toast.makeText(getApplicationContext(), "Tu sesión es válida hasta las 11:59 PM", Toast.LENGTH_SHORT).show();
                            iniciarSesion(usuario_id,clave);
                        }else{
                            //La sesión ya no es válida
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("adm_id","");
                            editor.putString("id_usuario","");
                            editor.putString("correo","");
                            editor.putString("clave","");
                            editor.putInt("cuentaValida",0);
                            editor.apply();
                        }
                    }
                }


            }

            @Override
            public void onFailure(Call<List<Valida>> call, Throwable t) {
                Log.d("VALIDAR_SESION",t.getMessage());
            }
        });
    }



    public void crearSesion(String usuario_id, String token){
        iTransporteCHMD.crearSesion(usuario_id,token).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                String valido = response.body();
                Log.d("CREAR_SESION",valido);
                //Si el valido es 0, iniciar sesion y crearlo en la tabla

                //si el valido es 1, dejar pasar
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("CREAR_SESION",t.getMessage());
            }
        });
    }
    @SuppressLint("NewApi")
    public void iniciarSesion(String email, final String clave){

        new Delete().from(UsuarioDB.class).execute();

//BASE_URL+RUTA+METODO_LOGIN+"?usuario="+email+"&clave="+clave

        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+RUTA+METODO_LOGIN+"?usuario="+email+"&clave="+clave,
                response -> {

                    if(response.length()<=0){
                        Toast.makeText(getApplicationContext(),"No puedes iniciar sesión",Toast.LENGTH_LONG).show();
                    }

                    try {
                        for(int i=0; i<response.length(); i++){
                            JSONObject jsonObject = (JSONObject) response
                                    .get(i);
                            String adm_id = jsonObject.getString("id");
                            //Este es el que se usa para consultar la ruta
                            String id_usuario = jsonObject.getString("id_usuario");
                            String correo = jsonObject.getString("correo");

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("adm_id",adm_id);
                            editor.putString("id_usuario",id_usuario);
                            editor.putString("correo",email);
                            editor.putString("clave",clave);
                            editor.putInt("cuentaValida",1);

                            UsuarioDB usuarioDB = new UsuarioDB();
                            usuarioDB.idAuxiliar=id_usuario;
                            usuarioDB.adm_id = adm_id;
                            usuarioDB.usuario = correo;
                            usuarioDB.clave = clave;
                            usuarioDB.save();


                            editor.apply();
                            if(id_usuario.length()>0){

                                Date c = Calendar.getInstance().getTime();
                                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-ddd", Locale.getDefault());
                                String hoy = df.format(c);

                                //Generar token
                                String token="";
                                String base = email+id_usuario+hoy;
                                token = Base64.getEncoder().encodeToString(base.getBytes());
                                Log.d("CHMD_TOKEN",token);


                                editor.putString("token",token);
                                editor.apply();

                                crearSesion(email,token);
                                getRutaTransporte(id_usuario);

                                Intent intent = new Intent(LoginActivity.this,SeleccionRutaActivity.class);
                                startActivity(intent);
                                finish();

                            }else{

                            }


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


    public void getRutaTransporte(String aux_id){


        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+RUTA+METODO_RUTA+"?aux_id="+aux_id,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = (JSONObject) response.get(i);

                            String id_ruta_h = jsonObject.getString("id_ruta_h");
                            String nombre_ruta = jsonObject.getString("nombre_ruta");
                            String camion = jsonObject.getString("camion");
                            String turno = jsonObject.getString("turno");
                            String tipo_ruta = jsonObject.getString("tipo_ruta");
                            items.add(new Ruta(id_ruta_h, nombre_ruta, camion, turno, tipo_ruta));
                        }
                    }catch (Exception ex){
                        Log.e(LoginActivity.class.getName(),ex.getMessage());
                    }


                    try {
                        //Borrar tabla de rutas
                        new Delete().from(RutaDB.class).execute();
                        new Delete().from(AlumnoDB.class).where("procesado=0").execute();
                        //Llenar tabla de rutas
                       for(int j=0; j<items.size(); j++){
                            RutaDB rutaDB = new RutaDB();
                            rutaDB.idRuta = items.get(j).getIdRutaH();
                            rutaDB.nombreRuta = items.get(j).getNombreRuta();
                            rutaDB.camion = items.get(j).getCamion();
                            rutaDB.turno = items.get(j).getTurno();
                            rutaDB.tipo_ruta = items.get(j).getTipoRuta();
                            rutaDB.estatus_ruta = 0;
                            Log.d("RUTAS->DB",""+rutaDB.save());

                        }

                    }catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.d("RUTAS->DB",""+e.getMessage());

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
