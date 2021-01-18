package mx.edu.transporte.chmd.modelosDB;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "dbAlumno", id="id")
public class AlumnoDB extends Model {
    @Column(name="id_alumno",unique = true)
    public String id_alumno;
    @Column(name="nombre")
    public String nombre;
    @Column(name="id_ruta_h")
    public String id_ruta_h;
    @Column(name="domicilio")
    public String domicilio;
    @Column(name="hora_manana")
    public String hora_manana;
    @Column(name="orden_in")
    public String orden_in;
    @Column(name="id_ruta_h_s")
    public String id_ruta_h_s;
    @Column(name="domicilio_s")
    public String domicilio_s;
    @Column(name="hora_regreso")
    public String hora_regreso;
    @Column(name="orden_out")
    public String orden_out;
    @Column(name="fecha")
    public String fecha;
    @Column(name="estatus")
    public String estatus;
    @Column(name="ascenso")
    public String ascenso;
    @Column(name="descenso")
    public String descenso;
    @Column(name="asistencia")
    public String asistencia;
    @Column(name="ascenso_t")
    public String ascenso_t;
    @Column(name="descenso_t")
    public String descenso_t;
    @Column(name="salida")
    public String salida;
    @Column(name="tipo_asistencia")
    public String tipo_asistencia;
    @Column(name="grupo")
    public String grupo;
    @Column(name="grado")
    public String grado;
    @Column(name="nivel")
    public String nivel;
    @Column(name="procesado")
    public int procesado;


}
