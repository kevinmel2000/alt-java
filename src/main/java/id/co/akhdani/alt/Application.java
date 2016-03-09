package id.co.akhdani.alt;

import labs.akhdani.Alt;
import labs.akhdani.alt.*;
import spark.servlet.SparkApplication;

public class Application implements SparkApplication {
    public static final String TAG = Application.class.getName();

    @Override
    public void init() {
        // set log to database
        AltLog.dbo_class = "id.co.akhdani.alt.engine.system.DboSystemLog";

        // create alt server application
        Alt server = Alt.instance(Application.class);

        // start server
        server.start();
    }

    public static void main(String[] args) {
        Application app = new Application();
        app.init();
    }
}