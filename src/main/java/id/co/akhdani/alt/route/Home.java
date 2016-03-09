package id.co.akhdani.alt.route;

import labs.akhdani.alt.AltHttpRequest;
import labs.akhdani.alt.AltHttpResponse;

public class Home {
    public static final String TAG = Home.class.getName();

    public String index(AltHttpRequest req, AltHttpResponse res){
        return "Alt-Java is RUNNING";
    }
}