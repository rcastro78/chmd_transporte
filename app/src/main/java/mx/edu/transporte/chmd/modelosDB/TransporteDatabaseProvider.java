package mx.edu.transporte.chmd.modelosDB;

import com.activeandroid.Configuration;
import com.activeandroid.content.ContentProvider;

public class TransporteDatabaseProvider extends ContentProvider {
    protected Configuration getConfiguration() {
        Configuration.Builder builder = new Configuration.Builder(getContext());
        builder.addModelClass(AlumnoDB.class);
        builder.addModelClass(RutaDB.class);
        builder.addModelClass(UsuarioDB.class);
        return builder.create();
    }
}
