package com.izv.agendav3;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by 2dam on 06/10/2014.
 */
public class Contacto implements Comparable<Contacto>, Parcelable, Serializable {

    private String nombre, mail, telefono;
    private String imagen;

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public Contacto(){

    }

    public Contacto(String nombre, String mail, String telefono, String foto) {
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
        return nombre + " " + mail + " " + telefono;
    }

    @Override
    public int compareTo(Contacto contacto) {
        return this.getNombre().toLowerCase().compareTo(contacto.getNombre().toLowerCase());
    }

    @Override
    public boolean equals (Object obj) {
        if (obj instanceof Contacto) {
            Contacto tmpPersona = (Contacto) obj;
            if (this.nombre.equals(tmpPersona.nombre) && this.mail.equals(tmpPersona.mail) &&
                    this.telefono == tmpPersona.telefono){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(nombre);
        parcel.writeString(mail);
        parcel.writeString(telefono);
        parcel.writeString(imagen);
    }

    private Contacto(Parcel in) {
        this.nombre = in.readString();
        this.mail = in.readString();
        this.telefono = in.readString();
        this.imagen = in.readString();
    }

    public static final Parcelable.Creator<Contacto> CREATOR = new Parcelable.Creator<Contacto>() {
        public Contacto createFromParcel(Parcel in) {
            return new Contacto(in);
        }

        public Contacto[] newArray(int size) {
            return new Contacto[size];
        }
    };
}
