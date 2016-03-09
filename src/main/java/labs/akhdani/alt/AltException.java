package labs.akhdani.alt;

import labs.akhdani.Alt;

public class AltException extends Exception {
    private static final String TAG = AltException.class.getName();
    private int code;
    public Throwable error;

    public AltException(){
        super();
    }

    public AltException(Throwable e){
        super(e.getMessage());
        this.code = Alt.STATUS_ERROR;
        this.error = e;
    }

    public AltException(Throwable e, String message){
        super(message);
        this.code = Alt.STATUS_ERROR;
        this.error = e;
    }

    public AltException(String message){
        super(message);
        this.code = Alt.STATUS_ERROR;
    }

    public AltException(String message, int code){
        super(message);
        this.code = code;
    }

    public int getCode(){
        return this.code;
    }
}