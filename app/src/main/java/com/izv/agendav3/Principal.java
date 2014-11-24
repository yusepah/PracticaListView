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
import android.util.Xml;
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
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Principal extends Activity {

    //Declaracion de variables de clase
    private ArrayList<Contacto> contactos;
    private ListView lv;
    private Adaptador ad;
    private final int SELECT_IMAGE = 1;
    private final int TAKE_PICTURE = 2;
    private ImageView ivNewUser;
    private Bitmap foto;
    private String fotoPath;
    private boolean seleccionada;
    private Bitmap defecto;
    private String defectoPath;

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
                    fotoCamara(data);
                    break;
                case SELECT_IMAGE:
                    fotoGaleria(data);
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
        try {
            xml();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
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
        }else if(id == R.id.accion_borrar){
            confirmacion(-1, 0);
            return true;
        }else if(id == R.id.accion_cerrar){
            System.exit(0);
        }else if(id == R.id.accion_about){
            acercaDe();
        }
        return super.onOptionsItemSelected(item);
    }

    //Para guardar los datos para el cambio de orientacion
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList guardar = contactos;
        outState.putParcelableArrayList("guardar", guardar);
    }

    //Para recuperar los datos en el cambio de orientacion
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList <Contacto>aux;
        aux = savedInstanceState.getParcelableArrayList("guardar");
        contactos = aux;
        ad.notifyDataSetChanged();
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
                dialogoLlamadaMail(view);
            }
        });
    }

    public void escuchadorAceptar(final AlertDialog alert, final EditText et1, final EditText et2,
                                  final EditText et3, final int posicion, final int id){
        /*Listener del boton Aceptar sobreescrito para poder validar el cambo Nombre y Telefono
        *                           que son obligatorios*/
        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button b = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Aceptar el contacto EDITADO
                        if(id == 0){
                            if(filtrar(et1, et3)) {
                                aceptarEditado(posicion, et1, et2, et3);
                                ad.notifyDataSetChanged();
                                alert.dismiss();
                            }
                        }
                        //Aceptar el contacto AÑADIDO
                        if (id == 1) {
                            if(filtrar(et1, et3)) {
                                aceptarNuevo(et1, et2, et3);
                                ad.notifyDataSetChanged();
                                alert.dismiss();
                            }
                        }
                    }
                });
            }
        });
    }

    //Listener para la imagen de los elementos del listview
    public void escuchadorImagen(){
        //Escuchador para seleccionar una imagen de la galeria o de la cámara
        ivNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogoFoto(view);
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
                borrado(id, posicion);
            }
        });
        alertDialog.setNegativeButton(android.R.string.no, null);
        alertDialog.show();
    }

    //Borra un contacto o todos segun el id
    public void borrado(int id, int posicion){
        if(id == 1) {
            contactos.remove(posicion);
            crearXml();
            ad.notifyDataSetChanged();
            tostada(getString(R.string.borrado));
        }
        if(id == 0){
            borrarTodos();
            ad.notifyDataSetChanged();
            tostada(getString(R.string.todosBorrados));
        }
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
        escuchadorImagen();
        et1 = (EditText)vista.findViewById(R.id.etNombre);
        et2 = (EditText)vista.findViewById(R.id.etMail);
        et3 = (EditText)vista.findViewById(R.id.etTelefono);
        //Editar
        if(id == 0) {
            editar(alert, et1, et2, et3, posicion);
        }
        //Añadir
        if(id == 1){
            añadir(alert);
        }
        escuchadorAceptar(alert, et1, et2, et3, posicion, id);
        alert.show();
    }

    /***************************************************************/
    /*                        AUXILIARES                           */
    /***************************************************************/

    //Metodo para aceptar un nuevo contacto
    public void aceptarNuevo(EditText et1, EditText et2, EditText et3){
        if (seleccionada) {
            contactos.add(new Contacto(et1.getText().toString(),
                    et2.getText().toString(), et3.getText().toString(),
                    fotoPath));
            crearXml();
        } else {
            contactos.add(new Contacto(et1.getText().toString(),
                    et2.getText().toString(), et3.getText().toString(),
                    defectoPath));
            crearXml();
        }
        ad.notifyDataSetChanged();
        seleccionada = false;
        tostada(getString(R.string.anadido));
    }

    //Metodo para aceptar el contacto editado
    public void aceptarEditado(int posicion, EditText et1, EditText et2, EditText et3){
        if (seleccionada) {
            Uri uri = getImageUri(getApplicationContext(), foto);
            String path = getPath(getApplicationContext(), uri);
            contactos.get(posicion).setImagen(path);
        } else {
            contactos.get(posicion).setImagen(contactos.get(posicion).getImagen());
        }
        contactos.get(posicion).setNombre(et1.getText().toString());
        contactos.get(posicion).setTelefono(et3.getText().toString());
        contactos.get(posicion).setMail(et2.getText().toString());
        crearXml();
        seleccionada = false;
        tostada(getString(R.string.editado));
    }

    //Para mostrar el dialogo acerca de...
    public void acercaDe(){
        Intent dialogo = new Intent(this, About.class);
        startActivity(dialogo);
    }

    //Para mostrar la opcion de añadir en el AlertDialog
    public void añadir(AlertDialog alert){
        alert.setTitle(R.string.action_nuevo);
        alert.setIcon(android.R.drawable.ic_menu_add);
        ivNewUser.setImageBitmap(defecto);
    }

    //Metodo que borra TODOS los contactos
    public void borrarTodos(){
        contactos.clear();
        crearXml();
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

    //Para guardar un contacto en el xml
    public void contacto(XmlSerializer docxml, int i) throws IOException {
        docxml.startTag(null, "Contacto");

        docxml.startTag(null, "Nombre");
        docxml.text(contactos.get(i).getNombre());
        docxml.endTag(null, "Nombre");

        docxml.startTag(null, "Email");
        docxml.text(contactos.get(i).getMail());
        docxml.endTag(null, "Email");

        docxml.startTag(null, "Telefono");
        docxml.text(contactos.get(i).getTelefono());
        docxml.endTag(null, "Telefono");

        docxml.startTag(null, "Foto");
        docxml.text(contactos.get(i).getImagen());
        docxml.endTag(null, "Foto");

        docxml.endTag(null, "Contacto");
    }

    //Metodo para crear el xml
    public void crearXml(){
        try {
            File salida = new File(getFilesDir(), "contactos.xml");
            FileOutputStream output = new FileOutputStream(salida);
            XmlSerializer docxml = Xml.newSerializer();
            docxml.setOutput(output, "UTF-8");
            docxml.startDocument(null, Boolean.valueOf(true));
            docxml.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            docxml.startTag(null, "Contactos");
            for(int i=0; i<contactos.size(); i++) {
                contacto(docxml, i);
            }
            docxml.endTag(null, "Contactos");
            docxml.endDocument();
            docxml.flush();
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Para mostrar la opcion de editar en el alertdialog
    public void editar(AlertDialog alert, EditText et1, EditText et2, EditText et3, int posicion){
        alert.setTitle(R.string.editar);
        alert.setIcon(android.R.drawable.ic_menu_edit);
        Bitmap bitmap = BitmapFactory.decodeFile(contactos.get(posicion).getImagen());
        Bitmap img = Bitmap.createScaledBitmap(bitmap, 220, 250, false);
        ivNewUser.setImageBitmap(img);
        et1.setText(contactos.get(posicion).getNombre());
        et2.setText(contactos.get(posicion).getMail());
        et3.setText(contactos.get(posicion).getTelefono());
        seleccionada = false;
    }

    //Para asegurarnos que el nombre y el telefono se rellenan
    public boolean filtrar(EditText et1, EditText et3){
        boolean nombre, telefono;
        if(campoVacio(et1.getText().toString())) {
            nombre = false;
            tostada(getString(R.string.nombreOblig));
        }else {
            nombre = true;
        }
        if(campoVacio(et3.getText().toString())) {
            telefono = false;
            tostada(getString(R.string.tlfOblig));
        }else {
            telefono = true;
        }
        if(telefono && nombre){
            return true;
        }else {
            return false;
        }
    }

    //Intent para obtener una foto de la camara
    public void fotoCamara(Intent data){
        Bundle extras = data.getExtras();
        foto = (Bitmap) extras.get(getString(R.string.datos));
        Uri uri = getImageUri(getApplicationContext(), foto);
        fotoPath = getPath(getApplicationContext(), uri);
        ivNewUser.setImageBitmap(foto);
        seleccionada = true;
    }

    //Metodo para cargar una foto de la galeria
    public void fotoGaleria(Intent data){
        Uri selectedImageUri = data.getData();
        fotoPath = getPath(getApplicationContext(), selectedImageUri);
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(fotoPath);
            foto = Bitmap.createScaledBitmap(bitmap, 220, 250, false);
            ivNewUser.setImageBitmap(foto);
            seleccionada = true;
        }catch(Exception c){
            tostada(getString(R.string.error));
        }
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
        contactos = new ArrayList<Contacto>();
        ad = new Adaptador(this, R.layout.lista_detalle, contactos);
        lv = (ListView)findViewById(R.id.lvLista);
        lv.setFastScrollEnabled(true);
        lv.setAdapter(ad);
        registerForContextMenu(lv);
        seleccionada = false;
        Bitmap aux = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                R.drawable.ic_agenda);
        Uri uri = getImageUri(getApplicationContext(), aux);
        defectoPath = getPath(getApplicationContext(), uri);
        defecto = Bitmap.createScaledBitmap(aux, 200, 250, false);
        escuchadorLista();
    }

    //Metodo para leer el XML
    public void leerXml() throws IOException, XmlPullParserException {
        String nombre = null, email = null, tlf = null, foto = null;
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new FileInputStream(new File(getFilesDir(), "contactos.xml")), "utf-8");
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if(eventType == XmlPullParser.START_TAG) {
                if(xpp.getName().compareTo("Nombre") == 0){
                    nombre = xpp.nextText();
                }else if(xpp.getName().compareTo("Email") == 0){
                    email = xpp.nextText();
                }else if(xpp.getName().compareTo("Telefono") == 0){
                    tlf = xpp.nextText();
                }else if(xpp.getName().compareTo("Foto") == 0){
                    foto = xpp.nextText();
                }
            } else if(eventType == XmlPullParser.END_TAG) {
                if(xpp.getName().compareTo("Contacto") == 0){
                    contactos.add(new Contacto(nombre, email, tlf, foto));
                }
            }
            eventType = xpp.next();
        }
        ad.notifyDataSetChanged();
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

    //Metodo que devuelve un Path dado un Uri(no se como hacerlo de otra manera)
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

    //Obtener uri de un bitmap
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void tostada(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    //Metodo para crear o leer el archivo xml
    public void xml() throws IOException, XmlPullParserException {
        File archivoXml = new File(getFilesDir(), "contactos.xml");
        if(archivoXml.exists()){
            leerXml();
        }else{
            crearXml();
            leerXml();
        }
    }
}