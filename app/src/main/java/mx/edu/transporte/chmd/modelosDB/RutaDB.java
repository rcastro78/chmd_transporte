package mx.edu.transporte.chmd.modelosDB;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
//id_ruta_h,nombre_ruta,camion,turno,tipo_ruta
@Table(name = "dbRuta", id="id")
public class RutaDB extends Model {
    @Column(name="idRuta",unique = true)
    public String idRuta;
    @Column(name="nombreRuta")
    public String nombreRuta;
    @Column(name="camion")
    public String camion;
    @Column(name="turno")
    public String turno;
    @Column(name="tipo_ruta")
    public String tipo_ruta;
    @Column(name="estatus_ruta")
    public int estatus_ruta;
}
