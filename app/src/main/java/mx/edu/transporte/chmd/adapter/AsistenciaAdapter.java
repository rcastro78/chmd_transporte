package mx.edu.transporte.chmd.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import mx.edu.transporte.chmd.AppTransporte;
import mx.edu.transporte.chmd.HomeActivity;
import mx.edu.transporte.chmd.InicioActivity;
import mx.edu.transporte.chmd.R;
import mx.edu.transporte.chmd.modelos.Asistencia;

public class AsistenciaAdapter extends BaseAdapter implements View.OnClickListener {
    protected Activity activity;
    protected ArrayList<Asistencia> items,originales;

    Asistencia asistencia;
    ViewHolder holder=new ViewHolder();
    String TAG="AsistenciaAdapter";
    Typeface tf,tfBold;
    SharedPreferences sharedPreferences;
    String idRuta,id_usuario;
    String turno;
    String nombreAlumno;
    static String BASE_URL;
    static String PATH;
    static String METODO_ALUMNO_NO_ASISTE="noAsistenciaAlumno.php";
    static String METODO_ALUMNO_NO_ASISTE_TARDE="noAsistenciaAlumnoTarde.php";
    static String METODO_ALUMNOS_MAT="getAlumnosRutaMat.php";
    static String METODO_ALUMNOS_TAR="getAlumnosRutaTar.php";
public AsistenciaAdapter(Activity activity, ArrayList<Asistencia> items, String turno, String idRuta) {
        this.activity = activity;
        this.items = items;
        this.turno = turno;
        this.idRuta = idRuta;
        BASE_URL = activity.getString(R.string.BASE_URL);
        PATH = activity.getString(R.string.PATH);
        tf = Typeface.createFromAsset(activity.getAssets(),"fonts/GothamRoundedMedium_21022.ttf");
    sharedPreferences = activity.getSharedPreferences(activity.getString(R.string.SHARED_PREF), 0);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }




    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        asistencia = items.get(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_listado, null);
            holder = new ViewHolder();
            holder.lblNombreEstudiante = convertView.findViewById(R.id.lblNombreEstudiante);
            holder.lblHora = convertView.findViewById(R.id.lblHora);
            holder.imgFotoEstudiante = convertView.findViewById(R.id.imgFotoEstudiante);
            holder.lblDireccion = convertView.findViewById(R.id.lblDireccion);
            holder.lblParada = convertView.findViewById(R.id.lblParada);
            //holder.btnAsistencia = convertView.findViewById(R.id.btnAsistencia);
            holder.btnInasistencia = convertView.findViewById(R.id.btnInasistencia);
            holder.llPic = convertView.findViewById(R.id.llPic);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.lblNombreEstudiante.setTypeface(tf);
        holder.lblHora.setTypeface(tf);
        holder.lblDireccion.setTypeface(tf);
        holder.lblParada.setTypeface(tf);
        holder.btnInasistencia.setTypeface(tf);
        holder.lblNombreEstudiante.setText(asistencia.getNombreAlumno());
        nombreAlumno = holder.lblNombreEstudiante.getText().toString();
        if (turno.equalsIgnoreCase("1")) {
            holder.lblHora.setText("Hora: "+asistencia.getHora_manana());
            holder.lblDireccion.setText(asistencia.getDomicilio());
            holder.lblParada.setText("Parada: "+asistencia.getOrdenIn());
        } else {
            holder.lblHora.setText("Hora: "+asistencia.getHoraRegreso());
            holder.lblDireccion.setText(asistencia.getDomicilio_s());
            holder.lblParada.setText("Parada: "+asistencia.getOrdenOut());
        }

       if(turno.equalsIgnoreCase("1")) {
           if (!asistencia.isInasist())
               holder.btnInasistencia.setVisibility(View.GONE);
           else
               holder.btnInasistencia.setVisibility(View.VISIBLE);
       }

        if(turno.equalsIgnoreCase("2")) {
            //if (!asistencia.isInasistTarde())
                holder.btnInasistencia.setVisibility(View.GONE);
            //else
            //    holder.btnInasistencia.setVisibility(View.VISIBLE);
        }

        holder.btnInasistencia.setOnClickListener(this);


        Glide.with(activity)
                .load(asistencia.getFoto()) // image url
                .placeholder(R.drawable.usuario) // any placeholder to load at start
                .error(R.drawable.usuario)  // any image in case of error
                .circleCrop()
                .into(holder.imgFotoEstudiante);

        if (turno.equalsIgnoreCase("1")) {


            if(asistencia.getAscenso().equalsIgnoreCase("1")){
                holder.llPic.setBackgroundColor(Color.parseColor("#ffff85"));
                holder.imgFotoEstudiante.clearColorFilter();
            }
        //Inasistencias
            if(asistencia.getAscenso().equalsIgnoreCase("2") &&
                asistencia.getDescenso().equalsIgnoreCase("2")) {
                holder.llPic.setBackgroundColor(Color.parseColor("#ffe5e8"));
                holder.imgFotoEstudiante.setColorFilter(Color.parseColor("#C0C0C0C0"), PorterDuff.Mode.SCREEN);

            }
            if(asistencia.getAscenso().equalsIgnoreCase("0")){
                holder.llPic.setBackgroundColor(Color.WHITE);
                holder.imgFotoEstudiante.clearColorFilter();
            }

            //Los niños han bajado en el colegio, estos son los que tienen ascenso 1 y descenso 1
            if(asistencia.getAscenso().equalsIgnoreCase("1") && asistencia.getDescenso().equalsIgnoreCase("1")){
                holder.llPic.setBackgroundColor(Color.parseColor("#27ae12"));
                holder.imgFotoEstudiante.clearColorFilter();
            }


        }

        if (turno.equalsIgnoreCase("2")) {
            if(asistencia.getAscenso_t().equalsIgnoreCase("1")){
                holder.llPic.setBackgroundColor(Color.parseColor("#ffff85"));
                holder.imgFotoEstudiante.clearColorFilter();
            }
            //Inasistencias
            //Rosados
            if(asistencia.getAscenso_t().equalsIgnoreCase("2") &&
                    asistencia.getDescenso_t().equalsIgnoreCase("2")) {
                holder.llPic.setBackgroundColor(Color.parseColor("#ffe5e8"));
                holder.imgFotoEstudiante.setColorFilter(Color.parseColor("#C0C0C0C0"), PorterDuff.Mode.SCREEN);
            }
            if(asistencia.getAscenso_t().equalsIgnoreCase("0")){
                holder.llPic.setBackgroundColor(Color.WHITE);
                holder.imgFotoEstudiante.clearColorFilter();
            }

            //Los niños llegan a su destino
            if(asistencia.getAscenso_t().equalsIgnoreCase("1") && asistencia.getDescenso_t().equalsIgnoreCase("1")){
                holder.llPic.setBackgroundColor(Color.parseColor("#27ae12"));
                holder.imgFotoEstudiante.clearColorFilter();
            }

        }
        //holder.btnAsistencia.setOnClickListener(this);
        //holder.btnInasistencia.setOnClickListener(this);


        //holder.btnAsistencia.setTag(position);
        //Esto se debe hacer para que un elemento recuerde su posición en la lista
        holder.btnInasistencia.setTag(position);
        holder.llPic.setTag(position);
        holder.imgFotoEstudiante.setTag(position);
        return convertView;
    }



    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btnInasistencia) {
            int position = (Integer) v.getTag();
            final Asistencia asistencia = items.get(position);
            if (turno.equalsIgnoreCase("1")) {
                if (asistencia.getAscenso().equalsIgnoreCase("0") &&
                        asistencia.getDescenso().equalsIgnoreCase("0")){
                    //Mostrar diálogo

                    new android.app.AlertDialog.Builder(activity)
                            .setTitle("CHMD - Transporte")
                            .setMessage("¿Autorizo el registro de inasistencia de " + asistencia.getNombreAlumno() + "?")

                            .setPositiveButton("Autorizo", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //Ejecutar el POST de asistencia y recargar la vista

                                    id_usuario = sharedPreferences.getString("id_usuario","");
                                    //Con esta forma se llaman métodos de la activity desde el fragment.
                                    if (activity instanceof InicioActivity) {
                                        if( ((InicioActivity)activity).hayConexion()){
                                            registraInasistencia(asistencia.getIdAlumno(),idRuta);
                                            ((InicioActivity)activity).getAsistencia(idRuta,METODO_ALUMNOS_MAT);
                                        }else{

                                            ((InicioActivity)activity).registraInasistenciaDB(idRuta,asistencia.getIdAlumno());
                                            ((InicioActivity)activity).getAsistenciaDB(idRuta);
                                        }

                                    }

                                    //InicioActivity.lstAlumnos.setAdapter(null);
                                    //InicioActivity.adapter.notifyDataSetChanged();
                                    //InicioActivity.lstAlumnos.setAdapter(InicioActivity.adapter);
                                    //InicioActivity.lstAlumnos.invalidateViews();
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
            }else{
                //Tarde
                if ((asistencia.getAscenso_t().equalsIgnoreCase("0") || asistencia.getAscenso_t().equalsIgnoreCase("1")) &&
                        asistencia.getDescenso_t().equalsIgnoreCase("0")){
                    //Mostrar diálogo

                    new android.app.AlertDialog.Builder(activity)
                            .setTitle("CHMD - Transporte")
                            .setMessage("¿Autorizo el registro de inasistencia de " + asistencia.getNombreAlumno() + "?")

                            .setPositiveButton("Autorizo", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //Ejecutar el POST de asistencia y recargar la vista

                                    id_usuario = sharedPreferences.getString("id_usuario","");
                                    //Con esta forma se llaman métodos de la activity desde el fragment.


                                    if (activity instanceof InicioActivity) {
                                        if( ((InicioActivity)activity).hayConexion()){
                                            registraInasistenciaTarde(asistencia.getIdAlumno(),idRuta);
                                            ((InicioActivity)activity).getAsistencia(idRuta,METODO_ALUMNOS_TAR);
                                        }else{

                                            ((InicioActivity)activity).registraInasistenciaTardeDB(idRuta,asistencia.getIdAlumno());
                                            ((InicioActivity)activity).getAsistenciaDBTarde(idRuta);
                                        }

                                    }


                                    //InicioActivity.lstAlumnos.setAdapter(null);
                                    //InicioActivity.adapter.notifyDataSetChanged();
                                    //InicioActivity.lstAlumnos.setAdapter(InicioActivity.adapter);
                                    //InicioActivity.lstAlumnos.invalidateViews();
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
            }

            //Toast.makeText(activity,asistencia.getNombreAlumno()+" será marcado como inasistente",Toast.LENGTH_LONG).show();
        }

/*
            if (!asistencia.getAscenso().equalsIgnoreCase("2") &&
                    !asistencia.getDescenso().equalsIgnoreCase("2")) {
                //Los que estan en blanco, no pregunto (OK)
                registraAscenso(asistencia.getIdAlumno(), idRuta);
            }

            //Reiniciar asistencia. Preguntar
            if(asistencia.getAscenso().equalsIgnoreCase("1")
                    && asistencia.getDescenso().equalsIgnoreCase("0")){
                //pregunto si reinicio el ascenso, lo pongo blanco

                new AlertDialog.Builder(activity)
                        .setTitle("CHMD - Transporte")
                        .setMessage("¿Desea reiniciar el registro de asistencia del alumno "+asistencia.getNombreAlumno()+"?")

                        .setPositiveButton("Autorizo", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Ejecutar el POST de asistencia y recargar la vista
                                reiniciaAsistencia(asistencia.getIdAlumno(),idRuta);

                            }
                        })

                        .setNegativeButton("No Autorizo", null)
                        .setIcon(R.mipmap.ic_launcher)
                        .show();




            }



        }
        if(v.getId()==R.id.btnInasistencia){
            int position = (Integer) v.getTag();
            Asistencia asistencia = items.get(position);
        }*/

        //notifyDataSetChanged();

    }





    static class ViewHolder {
        ImageView imgFotoEstudiante;
        TextView lblNombreEstudiante,lblHora,lblDireccion,lblParada;
        LinearLayout llPic;
        TextView btnInasistencia;
        //Button btnAsistencia, btnInasistencia;

    }

    //refrescar
    public void refrescar(ArrayList<Asistencia> items){
    this.items=items;
    notifyDataSetChanged();
    }


    //Filtrar resultados
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final ArrayList<Asistencia> results = new ArrayList<>();
                if (originales == null)
                    originales = items;
                if (constraint != null) {
                    if (originales != null && originales.size() > 0) {
                        for (final Asistencia a : originales) {
                            if (a.getNombreAlumno().toLowerCase()
                                    .contains(constraint.toString()))
                                results.add(a);
                        }
                    }
                    oReturn.values = results;
                }
                return oReturn;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                items = (ArrayList<Asistencia>)results.values;
                notifyDataSetChanged();
            }
        };

    }


    //Funciones de asistencia e inasistencia


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


    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


   /* public void registraAscenso(String alumno_id, final String ruta_id){
        JsonArrayRequest req = new JsonArrayRequest(BASE_URL+PATH+METODO_ALUMNO_ASISTE+"?id_alumno="+alumno_id+"&id_ruta="+ruta_id,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if(response.length()<=0){
                            getAsistencia(ruta_id,METODO_ALUMNOS_MAT);

                        }

                        try {
                            for(int i=0; i<response.length(); i++){
                                JSONObject jsonObject = (JSONObject) response
                                        .get(i);

                            }


                        }catch (JSONException e)
                        {
                            e.printStackTrace();
                            //Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

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

                            //if(turno.equalsIgnoreCase(TURNO_MAN)){
                                //getAsistencia(idRuta,METODO_ALUMNOS_MAT);
                                //getTotal(idRuta,METODO_ALUMNOS_TOTL_MAT);
                                //getTotalAscensos(idRuta,METODO_ALUMNOS_ASC_MAT);
                                //getTotalInasist(idRuta,METODO_ALUMNOS_INASIST_MAT);
                            //}
                            //if(turno.equalsIgnoreCase(TURNO_TAR)){
                                //getAsistencia(idRuta,METODO_ALUMNOS_TAR);
                                //getTotal(idRuta,METODO_ALUMNOS_TOTL_TAR);
                                //getTotalAscensos(idRuta,METODO_ALUMNOS_ASC_TAR);
                                //getTotalInasist(idRuta,METODO_ALUMNOS_INASIST_TAR);
                            //}

                            //notifyAdapter();

                        }catch (JSONException e)
                        {
                            e.printStackTrace();
                            //Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

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
   */
}

