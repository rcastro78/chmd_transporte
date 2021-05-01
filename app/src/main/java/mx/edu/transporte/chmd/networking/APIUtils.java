package mx.edu.transporte.chmd.networking;

public class APIUtils {
    public static final String BASE_URL = "https://www.chmd.edu.mx/WebAdminCirculares/wsTransporte/";

    public static ITransporteCHMD getTransporteService() {
        return RetrofitClient.getClient(BASE_URL).create(ITransporteCHMD.class);
    }
}
