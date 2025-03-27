package com.example.contacttracker;
public class Contacto {
    private int id;
    private String nombres;
    private String telefono;
    private String ubicacion;
    private String firma;

    public Contacto(int id, String nombres, String telefono, String ubicacion, String firma) {
        this.id = id;
        this.nombres = nombres;
        this.telefono = telefono;
        this.ubicacion = ubicacion;
        this.firma = firma;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombres;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public String getFirma() {
        return firma;
    }

    @Override
    public String toString() {
        return id + " - " + nombres;  // Mostrará el ID junto con el nombre en el ListView
    }
}

