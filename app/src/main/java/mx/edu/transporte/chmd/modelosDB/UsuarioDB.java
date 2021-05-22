package mx.edu.transporte.chmd.modelosDB;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "appUsuario", id="id")
public class UsuarioDB extends Model {
    @Column(name="idAuxiliar",unique = true)
    public String idAuxiliar;
    @Column(name="usuario")
    public String usuario;
    @Column(name="adm_id")
    public String adm_id;
    @Column(name="clave")
    public String clave;
}
