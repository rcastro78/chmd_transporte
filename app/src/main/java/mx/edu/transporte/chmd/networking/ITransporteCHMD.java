package mx.edu.transporte.chmd.networking;

import java.util.List;

import mx.edu.transporte.chmd.modelos.Asistencia;
import mx.edu.transporte.chmd.modelos.Ruta;
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

    //Cerrar ruta
    @FormUrlEncoded
    @POST("cerrarRuta2.php")
    Call<Ruta> cerrarRuta(@Field("id_ruta") String id_ruta,@Field("estatus") String estatus);


    //Cerrar ruta
    @FormUrlEncoded
    @POST("cerrarRutaTarde2.php")
    Call<Ruta> cerrarRutaTarde(@Field("id_ruta") String id_ruta,@Field("estatus") String estatus);

}
