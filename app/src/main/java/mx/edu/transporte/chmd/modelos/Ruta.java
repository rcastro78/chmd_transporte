package mx.edu.transporte.chmd.modelos;

public class Ruta {
    private String id_ruta_h;
    private String nombre_ruta;
    private String camion;
    private String turno;
    private String tipo_ruta;
    private String estatus;

    public Ruta(String id_ruta_h, String estatus) {
        this.id_ruta_h = id_ruta_h;
        this.estatus = estatus;
    }

    public Ruta(String id_ruta_h, String nombre_ruta, String camion, String turno, String tipo_ruta) {
        this.id_ruta_h = id_ruta_h;
        this.nombre_ruta = nombre_ruta;
        this.camion = camion;
        this.turno = turno;
        this.tipo_ruta = tipo_ruta;
    }

    public Ruta(String id_ruta_h, String nombre_ruta, String camion, String turno, String tipo_ruta,String estatus) {
        this.id_ruta_h = id_ruta_h;
        this.nombre_ruta = nombre_ruta;
        this.camion = camion;
        this.turno = turno;
        this.tipo_ruta = tipo_ruta;
        this.estatus = estatus;
    }


    public String getIdRutaH() {
        return id_ruta_h;
    }

    public void setIdRutaH(String id_ruta_h) {
        this.id_ruta_h = id_ruta_h;
    }

    public String getNombreRuta() {
        return nombre_ruta;
    }

    public void setNombreRuta(String nombre_ruta) {
        this.nombre_ruta = nombre_ruta;
    }

    public String getCamion() {
        return camion;
    }

    public void setCamion(String camion) {
        this.camion = camion;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public String getTipoRuta() {
        return tipo_ruta;
    }

    public void setTipoRuta(String tipo_ruta) {
        this.tipo_ruta = tipo_ruta;
    }


    public String getId_ruta_h() {
        return id_ruta_h;
    }

    public void setId_ruta_h(String id_ruta_h) {
        this.id_ruta_h = id_ruta_h;
    }

    public String getNombre_ruta() {
        return nombre_ruta;
    }

    public void setNombre_ruta(String nombre_ruta) {
        this.nombre_ruta = nombre_ruta;
    }

    public String getTipo_ruta() {
        return tipo_ruta;
    }

    public void setTipo_ruta(String tipo_ruta) {
        this.tipo_ruta = tipo_ruta;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }
}
