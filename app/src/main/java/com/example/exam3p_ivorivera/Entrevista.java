package com.example.exam3p_ivorivera;

public class Entrevista {
    private String IdOrden;
    private String Descripcion;
    private String Periodista;
    private String Fecha;
    private String foto;
    private String audio;

    public Entrevista() {
    }

    public String getIdOrden() {
        return IdOrden;
    }

    public void setIdOrden(String idOrden) {
        IdOrden = idOrden;
    }

    public String getDescripcion() {
        return Descripcion;
    }

    public void setDescripcion(String descripcion) {
        Descripcion = descripcion;
    }

    public String getPeriodista() {
        return Periodista;
    }

    public void setPeriodista(String periodista) {
        Periodista = periodista;
    }

    public String getFecha() {
        return Fecha;
    }

    public void setFecha(String fecha) {
        Fecha = fecha;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    @Override
    public String toString() {
        return  Periodista ;
    }
}
