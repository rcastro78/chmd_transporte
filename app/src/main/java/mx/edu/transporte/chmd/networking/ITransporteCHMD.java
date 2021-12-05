package mx.edu.transporte.chmd.networking;

import java.util.List;

import kotlin.jvm.JvmMultifileClass;
import mx.edu.transporte.chmd.modelos.Asistencia;
import mx.edu.transporte.chmd.modelos.Ruta;
import mx.edu.transporte.chmd.modelos.Usuario;
import mx.edu.transporte.chmd.modelos.Valida;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ITransporteCHMD {
    //@FormUrlEncoded
    @GET("getRutaTransporte.php")
    Call<List<Ruta>> getRutaTransporte(@Query("aux_id") String aux_id);

    @GET("validarSesion.php")
    Call<List<Usuario>> iniciarSesion(@Query("usuario") String email,@Query("clave") String clave);

    //Cerrar ruta
    @FormUrlEncoded
    @POST("cerrarRuta2.php")
    Call<Ruta> cerrarRuta(@Field("id_ruta") String id_ruta,@Field("estatus") String estatus);


    //Cerrar ruta
    @FormUrlEncoded
    @POST("cerrarRutaTarde2.php")
    Call<Ruta> cerrarRutaTarde(@Field("id_ruta") String id_ruta,@Field("estatus") String estatus);

    //Postear recorrido
    //0->no es emergencia
    //1->es emergencia
    @FormUrlEncoded
    @POST("enviaRuta.php")
    Call<String> enviarRuta(@Field("id_ruta") String idRuta, @Field("aux_id") String aux_id,
                            @Field("latitud") String latitud, @Field("longitud") String longitud,
                            @Field("es_emergencia") String emergencia);


    //Validaciones de sesión

    @FormUrlEncoded
    @POST("validarSesionApp.php")
    Call<List<Valida>> validarSesion(@Field("usuario_id") String usuario_id,@Field("token") String token);

    @FormUrlEncoded
    @POST("crearSesion.php")
    Call<String> crearSesion(@Field("usuario_id") String usuario_id,@Field("token") String token);


}
