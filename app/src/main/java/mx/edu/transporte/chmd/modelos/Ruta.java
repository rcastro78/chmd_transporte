package mx.edu.transporte.chmd.modelos;

public class Ruta {
    private String idRutaH;
    private String nombreRuta;
    private String camion;
    private String turno;
    private String tipoRuta;

    public Ruta(String idRutaH, String nombreRuta, String camion, String turno, String tipoRuta) {
        this.idRutaH = idRutaH;
        this.nombreRuta = nombreRuta;
        this.camion = camion;
        this.turno = turno;
        this.tipoRuta = tipoRuta;
    }

    public String getIdRutaH() {
        return idRutaH;
    }

    public void setIdRutaH(String idRutaH) {
        this.idRutaH = idRutaH;
    }

    public String getNombreRuta() {
        return nombreRuta;
    }

    public void setNombreRuta(String nombreRuta) {
        this.nombreRuta = nombreRuta;
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
        return tipoRuta;
    }

    public void setTipoRuta(String tipoRuta) {
        this.tipoRuta = tipoRuta;
    }
}
