package mx.edu.transporte.chmd;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import mx.edu.transporte.chmd.adapter.RutaAdapter;
import mx.edu.transporte.chmd.fragmentos.HomeFragment;
import mx.edu.transporte.chmd.modelos.Ruta;
import mx.edu.transporte.chmd.modelosDB.RutaDB;
import mx.edu.transporte.chmd.networking.APIUtils;
import mx.edu.transporte.chmd.networking.ITransporteCHMD;
import mx.edu.transporte.chmd.servicios.NetworkChangeReceiver;
import retrofit2.Call;
import retrofit2.Callback;

public class PrincipalActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{
    DrawerLayout drawerLayout;
    ITransporteCHMD iTransporteCHMD;
    //Habilitación del NFC
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private int mCount = 0;
    NfcAdapter mNfcAdapter;
    String hexadecimal,hexadecimalInv;
    boolean isChecked;

    static String BASE_URL;
    static String PATH;
    static String METODO_RUTA="getRutaTransporte.php";
    static String METODO_ESTADO_RUTA="getEstatusRuta.php";
    SharedPreferences sharedPreferences;
    String id_usuario;
    int estatus;
    private ArrayList<Ruta> items = new ArrayList<>();
    NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();


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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        iTransporteCHMD = APIUtils.getTransporteService();
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar((Toolbar) findViewById(R.id.tool_bar));

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        isChecked = sharedPreferences.getBoolean("habilitarNFC",true);

        BASE_URL = this.getString(R.string.BASE_URL);
        PATH = this.getString(R.string.PATH);
        sharedPreferences = this.getSharedPreferences(this.getString(R.string.SHARED_PREF), 0);
        estatus = sharedPreferences.getInt("estatus",0);

        id_usuario = sharedPreferences.getString("id_usuario","");
        getRutaTransporte2(id_usuario);
        //getRutaTransporte(id_usuario);

    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    protected void addFragment(int containerViewId,
                               Fragment fragment,
                               String fragmentTag) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(containerViewId, fragment, fragmentTag)
                .disallowAddToBackStack()
                .commit();
    }

    protected void replaceFragment(int containerViewId,
                                   Fragment fragment,
                                   String fragmentTag,
                                   String backStackStateName) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(containerViewId, fragment, fragmentTag)
                .addToBackStack(backStackStateName)
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int title;
        switch (menuItem.getItemId()) {
            case R.id.ruta_man:

                title = R.string.openDrawer;
                /*SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("turno","1");
                editor.commit();*/
                getRutaTransporte(id_usuario);
                replaceFragment(R.id.home_content,new HomeFragment(),"","A");
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
                break;
            case R.id.ruta_tar:
                title = R.string.closeDrawer;
                /*SharedPreferences.Editor editor1 = sharedPreferences.edit();
                editor1.putString("turno","2");
                editor1.commit();*/
                getRutaTransporteTarde(id_usuario);
                replaceFragment(R.id.home_content,new HomeFragment(),"","B");
                FragmentManager fm2 = getFragmentManager();
                fm2.popBackStack();
                break;
            case R.id.cerrar:
                title = R.string.closeDrawer;
                Intent intent = new Intent(PrincipalActivity.this,LoginActivity.class);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("cuentaValida",0);
                editor.commit();
                FragmentManager fm3 = getFragmentManager();
                fm3.popBackStack();
                startActivity(intent);
                finish();
                break;
            default:
                throw new IllegalArgumentException("menu option not implemented!!");
        }

        /*Fragment fragment = HomeContentFragment.newInstance(getString(title));
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.bottom_nav_enter, R.anim.bottom_nav_exit)
                .replace(R.id.home_content, fragment)
                .commit();*/


        setTitle(getString(title));

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;

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
                        items.add(new Ruta(id_ruta_h,nombre_ruta,camion,turno,tipo_ruta));
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
                                items.add(new Ruta(id_ruta_h,nombre_ruta,camion,turno,tipo_ruta));
                            }
                            //RutaAdapter adapter = new RutaAdapter(SeleccionRutaActivity.this,items);
                            //lstRuta.setAdapter(adapter);
                            //La ruta de la mañana se muestra por defecto
                            String idRuta = items.get(0).getIdRutaH();
                            String nomRuta = items.get(0).getNombreRuta();
                            String turno = items.get(0).getTurno();
                            getEstatusRuta(id_usuario,idRuta,nomRuta,turno);



                            //Borrar tabla de rutas
                            new Delete().from(RutaDB.class).execute();
                            //Llenar tabla de rutas
                            /*for(int j=0; j<items.size(); j++){
                                RutaDB rutaDB = new RutaDB();
                                rutaDB.idRuta = items.get(j).getIdRutaH();
                                rutaDB.nombreRuta = items.get(j).getNombreRuta();
                                rutaDB.camion = items.get(j).getCamion();
                                rutaDB.turno = items.get(j).getTurno();
                                rutaDB.tipo_ruta = items.get(j).getTipoRuta();
                                rutaDB.save();
                            }*/

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
    public void getRutaTransporteTarde(String aux_id){


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
                                items.add(new Ruta(id_ruta_h,nombre_ruta,camion,turno,tipo_ruta));
                            }
                            //RutaAdapter adapter = new RutaAdapter(SeleccionRutaActivity.this,items);
                            //lstRuta.setAdapter(adapter);
                            //La ruta de la mañana se muestra por defecto
                            String idRuta = items.get(1).getIdRutaH();
                            String nomRuta = items.get(1).getNombreRuta();
                            String turno = items.get(1).getTurno();
                            getEstatusRuta(id_usuario,idRuta,nomRuta,turno);



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
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("idRuta",ruta_id);
                                editor.putString("nomRuta",nomRuta);
                                editor.putString("turno",turno);
                                editor.putInt("estatus",estatus);
                                editor.commit();
                                if(turno.equals("1")){
                                    replaceFragment(R.id.home_content,new HomeFragment(),"","1");
                                    FragmentManager fm = getFragmentManager();
                                    fm.popBackStack();
                                }

                                if(turno.equals("2")){
                                    replaceFragment(R.id.home_content,new HomeFragment(),"","2");
                                    FragmentManager fm = getFragmentManager();
                                    fm.popBackStack();
                                }

                                /*Intent intent = new Intent(PrincipalActivity.this, HomeActivity.class);
                                intent.putExtra("idRuta", ruta_id);
                                intent.putExtra("estatus",estatus);
                                intent.putExtra("nomRuta", nomRuta);
                                intent.putExtra("turno", turno);*/

                                /*SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt("estatus",estatus);
                                editor.apply();

                                startActivity(intent);*/
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


    //Funciones para NFC
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
        if(isChecked){
            if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
                Log.d("onNewIntent", "2");

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
        //textView1.setText(hexadecimal);
        Toast.makeText(getApplicationContext(),hexadecimal,Toast.LENGTH_LONG).show();
        //Esto va a manejar la asistencia mediante el carnet

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




    public boolean hayConexion() {
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();

    }

/*
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
*/
 }