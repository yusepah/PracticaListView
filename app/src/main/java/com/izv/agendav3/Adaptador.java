package com.izv.agendav3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by 2dam on 06/10/2014.
 */
public class Adaptador extends ArrayAdapter<Contacto> /*implements SectionIndexer*/{

    private Context contexto;
    private ArrayList<Contacto> contactos;
    private int recurso;
    private LayoutInflater i;
    /*HashMap<String, Integer> azIndexer;
    String[]sections;*/

    public Adaptador(Context context, int resource, ArrayList<Contacto> objects){
        super(context, resource, objects);
        this.contexto = context;
        this.recurso = resource;
        this.contactos = objects;
        this.i = (LayoutInflater)contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        /*
        Log.v("Contactos->>>", String.valueOf(contactos.size()));

        azIndexer = new HashMap<String, Integer>();

        int size = contactos.size();

        for(int i = size-1; i >=0; i--){
            String nombre = contactos.get(i).getNombre().toUpperCase();
            Log.v("Letras de los nombres", nombre.substring(0,1));
            azIndexer.put(nombre.substring(0,1), i);
        }

        Log.v("Indexer->>>", String.valueOf(azIndexer.size()));

        Set<String> keys = azIndexer.keySet();
        ArrayList<String> keyList = new ArrayList<String>(keys);
        Collections.sort(keyList);

        for(int i=0; i<keyList.size(); i++)
            Log.v("KeyList", keyList.get(i));

        sections = new String[keyList.size()];
        keyList.toArray(sections);

        for(int i=0; i<keyList.size(); i++){
            sections[i] = keyList.get(i).toUpperCase();
        }*/
    }

    /*@Override
    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int section) {
        Log.v("Letra", sections[section]);
        return azIndexer.get(sections[section]);
    }

    @Override
    public int getSectionForPosition(int i) {
        Log.v("getSectionForPosition", "called");
        return 1;
    }*/

    public static class ViewHolder{
        public TextView tv1, tv2, tv3;
        public int posicion;
        public ImageView iv;
        public ImageView x;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if(convertView == null){
            convertView = i.inflate(recurso, null);
            vh = new ViewHolder();
            vh.tv1 = (TextView)convertView.findViewById(R.id.tvAgenda);
            vh.tv2 = (TextView)convertView.findViewById(R.id.tvMail);
            vh.tv3 = (TextView)convertView.findViewById(R.id.tvTlf);
            vh.iv = (ImageView)convertView.findViewById(R.id.ivFoto);
            vh.x = (ImageView)convertView.findViewById(R.id.ivX);
            convertView.setTag(vh);
        }else{
            vh = (ViewHolder)convertView.getTag();
        }
        Collections.sort(contactos);
        vh.posicion = position;
        vh.tv1.setText(contactos.get(position).getNombre());
        vh.tv2.setText(contactos.get(position).getMail());
        vh.tv3.setText(contactos.get(position).getTelefono());
        vh.tv3.setTag(position);
        Bitmap img = BitmapFactory.decodeFile(contactos.get(position).getImagen());
        Bitmap imagen = Bitmap.createScaledBitmap(img, 200, 225, false);
        vh.iv.setImageBitmap(imagen);
        vh.x.setTag(position);
        vh.iv.setTag(position);
        return convertView;
    }
}
