package com.izv.agendav3;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;


public class Principal extends Activity {

    private ArrayList<Contacto> contactos;
    private ListView lv;
    private Adaptador ad;
    private final int SELECT_IMAGE = 1;
    private final int TAKE_PICTURE = 2;
    private ImageView ivNewUser;
    private Bitmap foto;
    private boolean seleccionada;
    private Bitmap defecto;
    int posicion;

    /***************************************************************/
    /*                      METODOS ON                             */
    /***************************************************************/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TAKE_PICTURE:
                    Bundle extras = data.getExtras();
                    Bitmap selectedImage = (Bitmap) extras.get(getString(R.string.datos));
                    foto = selectedImage;
                    ivNewUser.setImageBitmap(foto);
                    seleccionada = true;
                    break;
                case SELECT_IMAGE:
                    Uri selectedImageUri = data.getData();
                    String path = getPath(getApplicationContext(), selectedImageUri);
                    try {
                        Bitmap bitmap = BitmapFactory.decodeFile(path);
                        foto = Bitmap.createScaledBitmap(bitmap, 220, 250, false);
                        ivNewUser.setImageBitmap(foto);
                        seleccionada = true;
                    }catch(Exception c){
                        tostada(getString(R.string.error));
                    }
                    break;
            }
        }else{
            seleccionada = false;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        Adaptador.ViewHolder vh = (Adaptador.ViewHolder)info.targetView.getTag();
        if(id == R.id.accion_editar){
            ventanaContactos(0, vh.posicion);
        }else if(id == R.id.accion_borrar){
            confirmacion(vh.posicion);
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        initComponents();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.desplegable, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_nuevo) {
            ventanaContactos(1, 0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /***************************************************************/
    /*                      METODOS CLICK                          */
    /***************************************************************/

    public void borrar(View view){
        int posicion = (Integer)view.getTag();
        confirmacion(posicion);
    }

    public void edit(View v){
        int posicion = (Integer)v.getTag();
        ventanaContactos(0, posicion);
    }

    public void escuchadorLista(){
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, int i, long l) {
                Object o = view.getTag();
                Adaptador.ViewHolder vh;
                vh = (Adaptador.ViewHolder)o;
                dialogoLlamadaMail(view);
            }
        });
    }

    /***************************************************************/
    /*                 METODOS MENU/ALERTDIALOG                    */
    /***************************************************************/

    public void confirmacion(final int posicion){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(R.string.deseaEliminar);
        alertDialog.setTitle(R.string.borrar);
        alertDialog.setIcon(android.R.drawable.ic_delete);
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                contactos.remove(posicion);
                Collections.sort(contactos);
                ad.notifyDataSetChanged();
                tostada(getString(R.string.borrado));
            }
        });
        alertDialog.setNegativeButton(android.R.string.no, null);
        alertDialog.show();
    }

    public void dialogoFoto(View v){
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        myAlertDialog.setTitle(R.string.carga);
        myAlertDialog.setMessage(R.string.donde);
        myAlertDialog.setIcon(android.R.drawable.ic_input_get);
        myAlertDialog.setPositiveButton(R.string.galeria, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                galeria();
            }
        });

        myAlertDialog.setNegativeButton(R.string.camara, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                camara();
            }
        });
        myAlertDialog.show();
    }

    public void dialogoLlamadaMail(final View view){
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        myAlertDialog.setTitle(R.string.opcion);
        myAlertDialog.setMessage(R.string.hacer);
        myAlertDialog.setIcon(android.R.drawable.ic_menu_call);
        myAlertDialog.setPositiveButton(R.string.llamada, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                llamada(view);
            }
        });

        myAlertDialog.setNegativeButton(R.string.mail, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mandarEmail(view);
            }
        });

        myAlertDialog.setNeutralButton(R.string.textMessage, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sms();
            }
        });
        myAlertDialog.show();
    }

    //Para Editar accion = 0
    //Para Agregar accion = 1
    public void ventanaContactos(final int accion, final int posicion){
        final AlertDialog alert = new AlertDialog.Builder(this)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        LayoutInflater inflater = LayoutInflater.from(this);
        final View vista = inflater.inflate(R.layout.dialogo_contactos, null);
        alert.setView(vista);

        final EditText et1, et2, et3;
        ivNewUser = (ImageView)vista.findViewById(R.id.ivFoto);
        et1 = (EditText)vista.findViewById(R.id.etNombre);
        et2 = (EditText)vista.findViewById(R.id.etMail);
        et3 = (EditText)vista.findViewById(R.id.etTelefono);

        if(accion == 0) {
            alert.setTitle(R.string.editar);
            alert.setIcon(android.R.drawable.ic_menu_edit);
            ivNewUser.setImageBitmap(contactos.get(posicion).getImagen());
            et1.setText(contactos.get(posicion).getNombre());
            et2.setText(contactos.get(posicion).getMail());
            et3.setText(contactos.get(posicion).getTelefono());
            seleccionada = false;
        }else if(accion == 1){
            alert.setTitle(R.string.action_nuevo);
            alert.setIcon(android.R.drawable.ic_menu_add);
            ivNewUser.setImageBitmap(defecto);
        }

        ivNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogoFoto(view);
            }
        });
//pepe
        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button b = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean nombre = false;
                        boolean telefono = false;
                    //asdfasdfasdf
                        if(campoVacio(et1.getText().toString())) {
                            nombre = false;
                            tostada(getString(R.string.nombreOblig));
                        }else
                            nombre = true;

                        if(campoVacio(et3.getText().toString())) {
                            telefono = false;
                            tostada(getString(R.string.tlfOblig));
                        }else
                            telefono = true;

                        if(accion == 0){
                            if (nombre && telefono) {
                                if (seleccionada) {
                                    contactos.get(posicion).setImagen(foto);
                                } else {
                                    contactos.get(posicion).setImagen(contactos.get(posicion).getImagen());
                                }
                                contactos.get(posicion).setNombre(et1.getText().toString());
                                contactos.get(posicion).setTelefono(et3.getText().toString());
                                contactos.get(posicion).setMail(et2.getText().toString());
                                seleccionada = false;
                                Collections.sort(contactos);
                                ad.notifyDataSetChanged();
                                tostada(getString(R.string.editado));
                                alert.dismiss();
                            }

                        } else if (accion == 1) {
                            if(nombre && telefono) {
                                if (seleccionada) {
                                    contactos.add(new Contacto(et1.getText().toString(), et2.getText().toString(), et3.getText().toString(), foto));
                                } else {
                                    contactos.add(new Contacto(et1.getText().toString(), et2.getText().toString(), et3.getText().toString(), defecto));
                                }
                                seleccionada = false;
                                Collections.sort(contactos);
                                ad.notifyDataSetChanged();
                                tostada(getString(R.string.anadido));
                                alert.dismiss();
                            }
                        }
                    }
                });
            }
        });
        alert.show();
    }

    /***************************************************************/
    /*                        AUXILIARES                           */
    /***************************************************************/

    public void camara(){
        Intent fotoPick = new Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(fotoPick,
                TAKE_PICTURE);
    }

    public boolean campoVacio(String campo){
        String aux = campo.trim();
        if(aux.isEmpty()){
            return true;
        }else {
            return false;
        }
    }

    public void galeria(){
        Intent fotoPick = new Intent(Intent.ACTION_PICK);
        fotoPick.setType(getString(R.string.imagenes));
        startActivityForResult(fotoPick, SELECT_IMAGE);
    }

    private void initComponents(){
        TextView text = (TextView)findViewById(R.id.tvAgenda);
        String udata=getString(R.string.agenda);
        SpannableString content = new SpannableString(udata);
        content.setSpan(new UnderlineSpan(), 0, udata.length(), 0);
        text.setText(content);
        Bitmap aux = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_launcher);
        defecto = Bitmap.createScaledBitmap(aux, 200, 250, false);
        contactos = new ArrayList<Contacto>();
        /*
        contactos.add(new Contacto("alber", "sinmail", "666666666", defecto));
        contactos.add(new Contacto("blber", "sinmail", "666666666", defecto));
        contactos.add(new Contacto("clber", "sinmail", "666666666", defecto));
        contactos.add(new Contacto("dlber", "sinmail", "666666666", defecto));
        contactos.add(new Contacto("elber", "sinmail", "666666666", defecto));
        contactos.add(new Contacto("flber", "sinmail", "666666666", defecto));
        contactos.add(new Contacto("glber", "sinmail", "666666666", defecto));
        contactos.add(new Contacto("hlber", "sinmail", "666666666", defecto));
        contactos.add(new Contacto("ilber", "sinmail", "666666666", defecto));
        contactos.add(new Contacto("jlber", "sinmail", "666666666", defecto));
        contactos.add(new Contacto("klber", "sinmail", "666666666", defecto));
        contactos.add(new Contacto("llber", "sinmail", "666666666", defecto));
        contactos.add(new Contacto("mlber", "sinmail", "666666666", defecto));
        contactos.add(new Contacto("nlber", "sinmail", "666666666", defecto));
        contactos.add(new Contacto("olber", "sinmail", "666666666", defecto));
        */

        ad = new Adaptador(this, R.layout.lista_detalle, contactos);
        lv = (ListView)findViewById(R.id.lvLista);
        lv.setFastScrollEnabled(true);
        lv.setAdapter(ad);
        registerForContextMenu(lv);
        seleccionada = false;
        escuchadorLista();
    }

    public void llamada(View view){
        Object o = view.getTag();
        Adaptador.ViewHolder vh;
        vh = (Adaptador.ViewHolder)o;
        int posicion = vh.posicion;
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:"+contactos.get(posicion).getTelefono()));
        startActivity(intent);
    }

    public void mandarEmail(View view){
        String[] TO = {contactos.get(posicion).getMail()};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse(getString(R.string.mailto)));
        emailIntent.setType(getString(R.string.text));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        startActivity(emailIntent);
    }

    private String getPath(Context context, Uri uri){
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(uri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void sms(){
        Uri uri = Uri.parse("smsto:"+contactos.get(posicion).getTelefono());
        Intent it = new Intent(Intent.ACTION_SENDTO, uri);
        startActivity(it);
    }
    private void tostada(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

}