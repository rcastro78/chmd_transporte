package mx.edu.transporte.chmd.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import mx.edu.transporte.chmd.InicioActivity;
import mx.edu.transporte.chmd.LoginActivity;
import mx.edu.transporte.chmd.R;
import mx.edu.transporte.chmd.modelos.Ruta;

public class NavigationHomeAdapter extends RecyclerView.Adapter<NavigationHomeAdapter.ViewHolder> {
    public static ArrayList<Ruta> rutas;
    private static final int TYPE_HEADER = 0;  // Declaring Variable to Understand which View is being worked on
    // IF the view under inflation and population is header or Item
    private static final int TYPE_ITEM = 1;
    static SharedPreferences sharedpreferences;
    private static final String PREFS_NAME = "mx.edu.transporte.chmd.spref";

 // String Array to store the passed titles Value from MainActivity.java
    private int mIcons[];       // Int Array to store the passed icons resource value from MainActivity.java

    private String name;        //String Resource for header View Name
    private int profile;        //int Resource for header view profile picture
    private String email;       //String Resource for header view email
    static Context context;
    static int ureg;

    static int velocidad=0;
    static double lat,lng;
    private static int EVENTO_LOGOUT=241;
    static int grados;

    private static String TAG="NavigationHomeAdapter";


// Creating a ViewHolder which extends the RecyclerView View Holder
// ViewHolder are used to to store the inflated views in order to recycle them

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        int Holderid;

        TextView textView;
        ImageView imageView;
        ImageView profile;
        TextView Name;
        TextView email;
        Context contxt;
//"Monitoreo de vehículos","Control Remoto","Carga Combustible","Mi Posición","Rastreo","Mis contactos de emergencias","Contáctenos","Cerrar Sesión"

        public static int RUTA_MAN = 1;
        public static int RUTA_TAR = 2;
        public static int SALIR = 3;
        private ArrayList<Ruta> rutas;

        public ViewHolder(View itemView, int ViewType, Context c, ArrayList<Ruta> rutas) {                 // Creating ViewHolder Constructor with View and viewType As a parameter
            super(itemView);
            contxt = c;
            this.rutas = rutas;

            itemView.setClickable(true);
            itemView.setOnClickListener(this);
            // Here we set the appropriate view in accordance with the the view type as passed when the holder object is created

            if (ViewType == TYPE_ITEM) {
                textView = (TextView) itemView.findViewById(R.id.rowText); // Creating TextView object with the id of textView from item_row.xml
                imageView = (ImageView) itemView.findViewById(R.id.rowIcon);// Creating ImageView object with the id of ImageView from item_row.xml
                Holderid = 1;                                               // setting holder id as 1 as the object being populated are of type item row
            } else {


                Name = (TextView) itemView.findViewById(R.id.name);         // Creating Text View object from header.xml for name
                Holderid = 0;                                                // Setting holder id = 0 as the object being populated are of type header view
            }


        }


        @Override
        public void onClick(View v) {

            int pos = getPosition();

            String idRuta = rutas.get(pos-1).getIdRutaH();
            String nombreRuta = rutas.get(pos-1).getNombreRuta();
            String turno = rutas.get(pos-1).getTurno();
            if(!idRuta.equals("-1")){
                SharedPreferences sharedpreferences;
                sharedpreferences = contxt.getSharedPreferences(PREFS_NAME, 0);
                Intent intent = new Intent(contxt, InicioActivity.class);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt("viaMenu", 1);
                editor.putString("idRuta",idRuta);
                editor.putString("turno",turno);
                editor.putString("nombreRuta",nombreRuta);
                editor.commit();
                contxt.startActivity(intent);
                Log.d("TURNO",""+turno);
            }else{
                SharedPreferences sharedpreferences;
                sharedpreferences = contxt.getSharedPreferences(PREFS_NAME, 0);
                Intent intent = new Intent(contxt, LoginActivity.class);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt("viaMenu", 0);
                editor.putInt("cuentaValida", 0);
                editor.putString("idRuta","");
                editor.putString("turno","");
                editor.putString("nombreRuta","");
                editor.commit();
                contxt.startActivity(intent);
                System.exit(0);
            }

		/*
		if (getPosition()==1)
		{
			SharedPreferences sharedpreferences;
			String PREFS_NAME = "ar.com.empleosbuenosaires.pref";
			sharedpreferences = contxt.getSharedPreferences(PREFS_NAME, 0);
			Intent intent = new Intent(contxt,EmpleosActivity.class);
			SharedPreferences.Editor editor = sharedpreferences.edit();
			editor.putInt("flag", 1);
			editor.commit();
			contxt.startActivity(intent);
		}
*/
            if (getPosition() == RUTA_MAN) {


            }

            if (getPosition() == RUTA_TAR) {

                //Intent monitoreo = new Intent(contxt, MonitoreoActivity.class);
                //contxt.startActivity(monitoreo);

            }


            if (getPosition() == SALIR) {


            }

        }

    }



// public NavigationHomeAdapter(String Titles[], int Icons[], String Name, String Email, int Profile, Context passedContext){
    public NavigationHomeAdapter(ArrayList<Ruta> rutas, int Icons[], String Name, String Email, int Profile, Context passedContext){ // MyAdapter Constructor with titles and icons parameter
        // titles, icons, name, email, profile pic are passed from the main activity as we
        this.rutas = rutas;                //have seen earlier
        mIcons = Icons;
        name = Name;
        email = Email;
        profile = Profile;                     //here we assign those passed values to the values we declared here
        //in adapter
        this.context = passedContext;
        sharedpreferences = passedContext.getSharedPreferences(PREFS_NAME, 0);
        ureg = sharedpreferences.getInt("ureg",0);
        Log.d(TAG,String.valueOf(ureg));

        /*
        Tracker tracker = new Tracker(context);
        lat = tracker.getLatitude();
        lng = tracker.getLongitude();
*/

    }



    //Below first we ovverride the method onCreateViewHolder which is called when the ViewHolder is
    //Created, In this method we inflate the item_row.xml layout if the viewType is Type_ITEM or else we inflate header.xml
    // if the viewType is TYPE_HEADER
    // and pass it to the view holder


    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row,parent,false); //Inflating the layout

            ViewHolder vhItem = new ViewHolder(v,viewType,context,rutas); //Creating ViewHolder and passing the object of type view

            return vhItem; // Returning the created object

            //inflate your layout and pass it to view holder

        } else if (viewType == TYPE_HEADER) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_xml,parent,false); //Inflating the layout

            ViewHolder vhHeader = new ViewHolder(v,viewType,context,NavigationHomeAdapter.rutas); //Creating ViewHolder and passing the object of type view

            return vhHeader; //returning the object created


        }
        return null;

    }

    //Next we override a method which is called when the item in a row is needed to be displayed, here the int position
    // Tells us item at which position is being constructed to be displayed and the holder id of the holder object tell us
    // which view type is being created 1 for item row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(holder.Holderid ==1) {                              // as the list view is going to be called after the header view so we decrement the
            // position by 1 and pass it to the holder while setting the text and image
            holder.textView.setText(rutas.get(position-1).getNombreRuta()); // Setting the Text with the array of our Titles
            //holder.imageView.setImageResource(mIcons[position -1]);// Settimg the image with array of our icons
        }
        else{

            // Similarly we set the resources for header view
            holder.Name.setText(name);

        }
    }

    // This method returns the number of items present in the list
    @Override
    public int getItemCount() {
        return rutas.size()+1; // the number of items in the list will be +1 the titles including the header view.
    }


    // Witht the following method we check what type of view is being passed
    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }


/*
    public static class MonitoreoAsyncTask extends AsyncTask<Integer,Void,Integer>
    {
        //Parametros

        private int evento;

        public MonitoreoAsyncTask(int evento) {
            this.evento = evento;
        }

        String NAMESPACE = "http://tempuri.org/";
        String URL="http://01.logix-uet.com:8080/webservice_logix/ws_controlremoto.asmx";
        String METHOD_NAME = "rastreo_dispositivo";
        String SOAP_ACTION = "http://tempuri.org/rastreo_dispositivo";


        public int llamar()
        {
            int r=0;
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            int nivel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int escala = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            float pctBateria = nivel *100f/ (float)escala;
            Log.d(TAG,String.valueOf(pctBateria));
            Log.d(TAG,String.valueOf(lat));
            Log.d(TAG,String.valueOf(lng));

            int idUsuario = sharedpreferences.getInt("idusuario",0);
            TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = telephonyManager.getDeviceId();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            String fecha = df.format(Calendar.getInstance().getTime());


            try {
                SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

                request.addProperty("idusuario", idUsuario);
                request.addProperty("imei", imei);
                request.addProperty("fecha", fecha);
                request.addProperty("latitud", lat);
                request.addProperty("longitud", lng);
                request.addProperty("velocidad", velocidad);
                request.addProperty("rumbo", grados);
                request.addProperty("evento", evento);
                request.addProperty("bateria", (int) pctBateria);

                SoapSerializationEnvelope envelope =
                        new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.dotNet = true;
                envelope.setOutputSoapObject(request);

                envelope.implicitTypes = true;
                envelope.encodingStyle = SoapSerializationEnvelope.XSD;
                MarshalDouble md = new MarshalDouble();
                md.register(envelope);

                HttpTransportSE transporte = new HttpTransportSE(URL);
                transporte.call(SOAP_ACTION,envelope);
                Object resultado_xml = envelope.getResponse();
                //SoapObject datos = (SoapObject) resultado_xml.getProperty(0);
                //String res= datos.getProperty("control_remotoResult").toString();
                Log.d("RESPONSE",resultado_xml.toString());


            }catch (Exception ex){
                Log.d("RESPONSE",ex.getMessage());

            }

            return r;
        }



        @Override
        protected Integer doInBackground(Integer... params) {
            return llamar();
        }
    }
*/

}
