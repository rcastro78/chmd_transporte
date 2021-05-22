package mx.edu.transporte.chmd.modelos;

public class Usuario {
    private String id,id_usuario,correo;

    public Usuario(String id, String id_usuario, String correo) {
        this.id = id;
        this.id_usuario = id_usuario;
        this.correo = correo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(String id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}
