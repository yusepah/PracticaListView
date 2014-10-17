package com.izv.agendav3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;


public class Principal extends Activity {

    //Declaracion de variables de clase
    private ArrayList<Contacto> contactos;
    private ListView lv;
    private Adaptador ad;
    private final int SELECT_IMAGE = 1;
    private final int TAKE_PICTURE = 2;
    private ImageView ivNewUser;
    private Bitmap foto;
    private boolean seleccionada;
    private Bitmap defecto;

    /***************************************************************/
    /*                      METODOS ON                             */
    /***************************************************************/

    /*Método onActivityResult sobreescrito para obtejer las imagenes de
                       la camara y de la galeria*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TAKE_PICTURE:
                    Bundle extras = data.getExtras();
                    foto = (Bitmap) extras.get(getString(R.string.datos));
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

    /*Metodo onContextItemSelected sobreescrito para Editar un contacto o
                            Borrar un contacto*/
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        Adaptador.ViewHolder vh = (Adaptador.ViewHolder)info.targetView.getTag();
        if(id == R.id.accion_editar){
            ventanaContactos(0, vh.posicion);
        }else if(id == R.id.accion_borrar){
            confirmacion(vh.posicion, 1);
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        initComponents();   //Metodo para inicializar mis variables
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

    /*Metodo onOptionsItemSelected sobreescrito para Añadir un contacto o
                            Editar un contacto*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_nuevo) {
            ventanaContactos(1, 0);
            return true;
        }
        if(id == R.id.accion_borrar){
            confirmacion(-1, 0);
        }
        return super.onOptionsItemSelected(item);
    }

    /***************************************************************/
    /*                      METODOS CLICK                          */
    /***************************************************************/

    //Método para borrar un contacto dada una posicion
    public void borrar(View view){
        int posicion = (Integer)view.getTag();
        confirmacion(posicion, 1);
    }

    //Método para editar un contacto dada una posicion
    public void edit(View v){
        int posicion = (Integer)v.getTag();
        ventanaContactos(0, posicion);
    }

    /*Listener de la ListView para hacer Llamada, mandar Email o mandar SMS*/
    public void escuchadorLista(){
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, int i, long l) {
                /*
                Object o = view.getTag();
                Adaptador.ViewHolder vh;
                vh = (Adaptador.ViewHolder)o;
                */
                dialogoLlamadaMail(view);
            }
        });
    }

    /***************************************************************/
    /*                 METODOS MENU/ALERTDIALOG                    */
    /***************************************************************/

    //Confirmación para borrar (id = 1)Un contacto o (id = 0) Todos los contactos
    public void confirmacion(final int posicion, final int id){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(R.string.deseaEliminar);
        if(id == 1) {
            alertDialog.setTitle(R.string.borrar);
            alertDialog.setIcon(android.R.drawable.ic_delete);
        }
        if(id == 0){
            alertDialog.setTitle(R.string.confirmarTodos);
            alertDialog.setIcon(android.R.drawable.stat_sys_warning);
        }
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                if(id == 1) {
                    contactos.remove(posicion);
                    Collections.sort(contactos);
                    ad.notifyDataSetChanged();
                    tostada(getString(R.string.borrado));
                }
                if(id == 0){
                    borrarTodos();
                    tostada(getString(R.string.todosBorrados));
                }
            }
        });
        alertDialog.setNegativeButton(android.R.string.no, null);
        alertDialog.show();
    }

    //Dialogo para seleccionar una foto de la Cámara o de la Galería
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

    //Dialogo para hacer Llamada, mandar Email o mandar SMS
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
                sms(view);
            }
        });
        myAlertDialog.show();
    }

    /*Ventana para (id = 0)Editar un contacto o (id = 1)Añadir un contacto */
    public void ventanaContactos(final int id, final int posicion){
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
        //Editar
        if(id == 0) {
            alert.setTitle(R.string.editar);
            alert.setIcon(android.R.drawable.ic_menu_edit);
            ivNewUser.setImageBitmap(contactos.get(posicion).getImagen());
            et1.setText(contactos.get(posicion).getNombre());
            et2.setText(contactos.get(posicion).getMail());
            et3.setText(contactos.get(posicion).getTelefono());
            seleccionada = false;
        }
        //Añadir
        if(id == 1){
            alert.setTitle(R.string.action_nuevo);
            alert.setIcon(android.R.drawable.ic_menu_add);
            ivNewUser.setImageBitmap(defecto);
        }
        //Escuchador para editar un contacto al hacer click en la imagen del ListView
        ivNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogoFoto(view);
            }
        });
        /*Listener del boton Aceptar sobreescrito para poder validar el cambo Nombre y Telefono
        *                           que son obligatorios*/
        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button b = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean nombre, telefono;
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
                        //Aceptar el contacto EDITADO
                        if(id == 0){
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
                        }
                        //Aceptar el contacto AÑADIDO
                        if (id == 1) {
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

    //Metodo que borra TODOS los contactos
    public void borrarTodos(){
        contactos.clear();
        ad.notifyDataSetChanged();
    }

    //Intent para obtener imagen de la camara
    public void camara(){
        Intent fotoPick = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(fotoPick,TAKE_PICTURE);
    }

    //Devuelve True si el campo esta vacio, false en caso contrario
    public boolean campoVacio(String campo){
        String aux = campo.trim();
        if(aux.isEmpty()) {
            return true;
        }
        return false;
    }

    //Una lista precargada en la aplicacion
    public void cargarListaPrueba(){
        contactos.add(new Contacto("Aaron", "example@expample.com", "666666666", defecto));
        contactos.add(new Contacto("Angel", "example@expample.com", "666666666", defecto));
        contactos.add(new Contacto("Sergio", "example@expample.com", "666666666", defecto));
        contactos.add(new Contacto("Rafa", "example@expample.com", "666666666", defecto));
        contactos.add(new Contacto("Jonathan", "example@expample.com", "666666666", defecto));
        contactos.add(new Contacto("Josue", "example@expample.com", "666666666", defecto));
        contactos.add(new Contacto("Marian", "example@expample.com", "666666666", defecto));
        contactos.add(new Contacto("Ivan", "example@expample.com", "666666666", defecto));
        contactos.add(new Contacto("Alberto", "example@expample.com", "666666666", defecto));
        contactos.add(new Contacto("Mati", "example@expample.com", "666666666", defecto));
        contactos.add(new Contacto("Sandra", "example@expample.com", "666666666", defecto));
        contactos.add(new Contacto("Ainhoa", "example@expample.com", "666666666", defecto));
        contactos.add(new Contacto("Krys", "example@expample.com", "666666666", defecto));
        contactos.add(new Contacto("Guille", "example@expample.com", "666666666", defecto));
        contactos.add(new Contacto("Patri", "example@expample.com", "666666666", defecto));
    }

    //Intent para obtener una imagen de la galeria
    public void galeria(){
        Intent fotoPick = new Intent(Intent.ACTION_PICK);
        fotoPick.setType(getString(R.string.imagenes));
        startActivityForResult(fotoPick, SELECT_IMAGE);
    }

    //Inicializacion de todas las variables necesarias
    private void initComponents(){
        TextView text = (TextView)findViewById(R.id.tvAgenda);
        String udata=getString(R.string.agenda);
        SpannableString content = new SpannableString(udata);
        content.setSpan(new UnderlineSpan(), 0, udata.length(), 0);
        text.setText(content);
        Bitmap aux = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_launcher);
        defecto = Bitmap.createScaledBitmap(aux, 200, 250, false);
        contactos = new ArrayList<Contacto>();
        cargarListaPrueba();
        ad = new Adaptador(this, R.layout.lista_detalle, contactos);
        lv = (ListView)findViewById(R.id.lvLista);
        lv.setFastScrollEnabled(true);
        lv.setAdapter(ad);
        registerForContextMenu(lv);
        seleccionada = false;
        escuchadorLista();
    }

    //Intent para realizar una llamada
    public void llamada(View view){
        Object o = view.getTag();
        Adaptador.ViewHolder vh;
        vh = (Adaptador.ViewHolder)o;
        int posicion = vh.posicion;
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:"+contactos.get(posicion).getTelefono()));
        startActivity(intent);
    }

    //Intent para enviar un Email
    public void mandarEmail(View view){
        Adaptador.ViewHolder vh = (Adaptador.ViewHolder)view.getTag();
        String[] TO = {contactos.get(vh.posicion).getMail()};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse(getString(R.string.mailto)));
        emailIntent.setType(getString(R.string.text));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        startActivity(emailIntent);
    }

    //Metodo que devuelve un Path dado un Uri
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

    //Intent para mandar sms
    public void sms(View view){
        Adaptador.ViewHolder vh = (Adaptador.ViewHolder)view.getTag();
        Uri uri = Uri.parse("smsto:"+contactos.get(vh.posicion).getTelefono());
        Intent it = new Intent(Intent.ACTION_SENDTO, uri);
        startActivity(it);
    }
    private void tostada(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}