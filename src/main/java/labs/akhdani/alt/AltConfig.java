package labs.akhdani.alt;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AltConfig {
    private static final String TAG = AltConfig.class.getName();
    private static Properties prop;

    public static void load(){
        if(prop == null) {
            InputStream input = null;
            prop = new Properties();

            try {
                ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                input = classloader.getResourceAsStream("alt.properties");

                // load a properties file
                prop.load(input);
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static String get(String key){
        if(prop == null) AltConfig.load();

        return AltConfig.prop.getProperty(key) != null ? AltConfig.prop.getProperty(key) : "";
    }
}
