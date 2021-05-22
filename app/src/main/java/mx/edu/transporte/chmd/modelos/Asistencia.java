package mx.edu.transporte.chmd.modelos;

public class Asistencia {
    /*
    * {"id_alumno":"1502","id_ruta_h":"1030","domicilio":"PASEO DE LOS TAMARINDOS 130-1302, BOSQUES DE LAS LOMAS",
    * "hora_manana":"06:30","orden_in":"1","id_ruta_h_s":"128","domicilio_s":"PASEO DE LOS TAMARINDOS 130-1302, BOSQUES DE LAS LOMAS",
    * "hora_regreso":"16:00","orden_out":"33","fecha":"2020-06-15","estatus":"0","ascenso":"0","descenso":"0",
    * "asistencia":"1","ascenso_t":"0","descenso_t":"0","salida":"0","tipo_asistencia":null,"grupo":"1P-C",
    * "grado":"1o. de Primaria","nivel":"PRIMARIA","foto":"C:\\IDCARDDESIGN\\CREDENCIALES\\alumnos\\SIDAUY ROMANO, NATHALIE.jpg",
    * "nombre":"SIDAUY ROMANO NATHALIE"}
    *
    * {"id_alumno":"985","id_ruta_h":"1030","domicilio":"AHUEHUETES NTE. 1333- T. VENDOME 304 304, BOSQUES DE LAS LOMAS",
    * "hora_manana":"07:17","orden_in":"23","id_ruta_h_s":"128","domicilio_s":"AHUEHUETES NTE. 1333- T. VENDOME 304 304, BOSQUES DE LAS LOMAS",
    * "hora_regreso":"15:15","orden_out":"1","fecha":"2020-06-15","estatus":"0","ascenso":"0","descenso":"0",
    * "asistencia":"1","ascenso_t":"0","descenso_t":"0","salida":"0","tipo_asistencia":null,"grupo":"III-D",
    * "grado":"3er Semestre Bachillerato","nivel":"BACHILLERATO","foto":"C:\\IDCARDDESIGN\\CREDENCIALES\\alumnos\\SITT SASSON, TERRY.jpg",
    * "nombre":"SITT SASSON TERRY"}
    *
    *
    * */
    private String idAlumno;
    private String tarjeta;
    private String nombreAlumno,domicilio;
    private String hora_manana,horaRegreso;
    private String ascenso,descenso;
    private String ascenso_t,descenso_t;
    private String salida;
    private String domicilio_s,grupo,grado,nivel,foto;
    private String ordenIn,ordenOut;
    boolean selected;
    boolean selectedNA;
    boolean inasist, inasistTarde;
    public Asistencia(String idAlumno, String nombreAlumno, String domicilio, String hora_manana,
                      String horaRegreso, String ascenso, String descenso, String domicilio_s,
                      String grupo, String grado, String nivel, String foto, boolean selected, boolean selectedNA,
                      String ascenso_t, String descenso_t, String salida) {
        this.idAlumno = idAlumno;
        this.nombreAlumno = nombreAlumno;
        this.domicilio = domicilio;
        this.hora_manana = hora_manana;
        this.horaRegreso = horaRegreso;
        this.ascenso = ascenso;
        this.descenso = descenso;
        this.domicilio_s = domicilio_s;
        this.grupo = grupo;
        this.grado = grado;
        this.nivel = nivel;
        this.foto = foto;
        this.selected = selected;
        this.selectedNA = selectedNA;
        this.ascenso_t = ascenso_t;
        this.descenso_t = descenso_t;
        this.salida = salida;
    }

    public Asistencia(String tarjeta,String idAlumno, String nombreAlumno, String domicilio, String hora_manana,
                      String horaRegreso, String ascenso, String descenso, String domicilio_s,
                      String grupo, String grado, String nivel, String foto, boolean selected, boolean selectedNA,
                      String ascenso_t, String descenso_t, String salida, String ordenIn, String ordenOut, boolean inasist,
                      boolean inasistTarde) {
        this.idAlumno = idAlumno;
        this.tarjeta = tarjeta;
        this.nombreAlumno = nombreAlumno;
        this.domicilio = domicilio;
        this.hora_manana = hora_manana;
        this.horaRegreso = horaRegreso;
        this.ascenso = ascenso;
        this.descenso = descenso;
        this.domicilio_s = domicilio_s;
        this.grupo = grupo;
        this.grado = grado;
        this.nivel = nivel;
        this.foto = foto;
        this.selected = selected;
        this.selectedNA = selectedNA;
        this.ascenso_t = ascenso_t;
        this.descenso_t = descenso_t;
        this.salida = salida;
        this.ordenIn = ordenIn;
        this.ordenOut = ordenOut;
        this.inasistTarde = inasistTarde;
        this.inasist = inasist;
    }

    public String getTarjeta() {
        return tarjeta;
    }

    public void setTarjeta(String tarjeta) {
        this.tarjeta = tarjeta;
    }

    public String getIdAlumno() {
        return idAlumno;
    }

    public void setIdAlumno(String idAlumno) {
        this.idAlumno = idAlumno;
    }

    public String getNombreAlumno() {
        return nombreAlumno;
    }

    public void setNombreAlumno(String nombreAlumno) {
        this.nombreAlumno = nombreAlumno;
    }

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    public String getHora_manana() {
        return hora_manana;
    }

    public void setHora_manana(String hora_manana) {
        this.hora_manana = hora_manana;
    }

    public String getHoraRegreso() {
        return horaRegreso;
    }

    public void setHoraRegreso(String horaRegreso) {
        this.horaRegreso = horaRegreso;
    }

    public String getAscenso() {
        return ascenso;
    }

    public void setAscenso(String ascenso) {
        this.ascenso = ascenso;
    }

    public String getDescenso() {
        return descenso;
    }

    public void setDescenso(String descenso) {
        this.descenso = descenso;
    }

    public String getDomicilio_s() {
        return domicilio_s;
    }

    public void setDomicilio_s(String domicilio_s) {
        this.domicilio_s = domicilio_s;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public String getGrado() {
        return grado;
    }

    public void setGrado(String grado) {
        this.grado = grado;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelectedNA() {
        return selectedNA;
    }

    public void setSelectedNA(boolean selectedNA) {
        this.selectedNA = selectedNA;
    }

    public String getAscenso_t() {
        return ascenso_t;
    }

    public void setAscenso_t(String ascenso_t) {
        this.ascenso_t = ascenso_t;
    }

    public String getDescenso_t() {
        return descenso_t;
    }

    public void setDescenso_t(String descenso_t) {
        this.descenso_t = descenso_t;
    }

    public String getSalida() {
        return salida;
    }

    public void setSalida(String salida) {
        this.salida = salida;
    }

    public String getOrdenIn() {
        return ordenIn;
    }

    public void setOrdenIn(String ordenIn) {
        this.ordenIn = ordenIn;
    }

    public String getOrdenOut() {
        return ordenOut;
    }

    public void setOrdenOut(String ordenOut) {
        this.ordenOut = ordenOut;
    }

    public boolean isInasist() {
        return inasist;
    }

    public void setInasist(boolean inasist) {
        this.inasist = inasist;
    }

    public boolean isInasistTarde() {
        return inasistTarde;
    }

    public void setInasistTarde(boolean inasistTarde) {
        this.inasistTarde = inasistTarde;
    }
}
