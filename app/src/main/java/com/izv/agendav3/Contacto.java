package com.izv.agendav3;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Created by 2dam on 06/10/2014.
 */
public class Contacto implements Comparable<Contacto> {

    private String nombre, mail, telefono;
    private Bitmap imagen;

    public Bitmap getImagen() {
        return imagen;
    }

    public void setImagen(Bitmap imagen) {
        this.imagen = imagen;
    }

    public Contacto(String nombre, String mail, String telefono, Bitmap foto) {
        this.nombre = nombre;
        this.mail = mail;
        this.telefono = telefono;
        this.imagen = foto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    @Override
    public String toString() {
        return super.toString();
    }


    @Override
    public int compareTo(Contacto contacto) {
        return this.getNombre().toLowerCase().compareTo(contacto.getNombre().toLowerCase());
    }
}
