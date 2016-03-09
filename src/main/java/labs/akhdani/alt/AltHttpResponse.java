package labs.akhdani.alt;

import labs.akhdani.Alt;

public class AltHttpResponse {
    private spark.Response response;
    private Alt alt;

    public AltHttpResponse(spark.Response response, Alt alt){
        this.response = response;
        this.alt = alt;
    }

    public void header(String header, String value){
        this.response.header(header, value);
    }

    public void status(int statusCode){
        this.response.status(statusCode);
    }
}
