package mx.edu.transporte.chmd.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import mx.edu.transporte.chmd.R;
import mx.edu.transporte.chmd.modelos.Ruta;

public class RutaAdapter extends BaseAdapter {
    protected Activity activity;
    protected ArrayList<Ruta> items;
    Ruta ruta;
    ViewHolder holder=new ViewHolder();
    String TAG="AlumnosAdapter";
    Typeface tf,tfBold;

    public RutaAdapter(Activity activity, ArrayList<Ruta> items) {
        this.activity = activity;
        this.items = items;
        tf = Typeface.createFromAsset(activity.getAssets(),"fonts/GothamRoundedMedium_21022.ttf");
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

        ruta = items.get(position);
        ViewHolder holder = null;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_ruta, null);
            holder = new ViewHolder();
            holder.lblHeader = convertView.findViewById(R.id.lblRuta);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        holder.lblHeader.setTypeface(tf);
        holder.lblHeader.setText(ruta.getNombreRuta());


        return convertView;
    }

    static class ViewHolder {
        TextView lblHeader;

    }

}

