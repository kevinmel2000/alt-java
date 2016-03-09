package labs.akhdani.alt;

import java.util.ArrayList;

public class AltValidation {
    private static final String TAG = AltValidation.class.getName();
    public ArrayList<Boolean> rules = new ArrayList<>();
    public ArrayList<String> messages = new ArrayList<>();

    public AltValidation(){

    }

    public AltValidation(String db_instance){

    }

    public static AltValidation instance(){
        return new AltValidation();
    }

    public AltValidation rule(boolean rule, String message){
        this.rules.add(rule);
        this.messages.add(message);
        return this;
    }

    public Object[] validate(){
        boolean res = true;
        ArrayList<String> messages = new ArrayList<>();

        for(int i=0; i<this.rules.size(); i++) if(!this.rules.get(i)){
            res = false;
            messages.add(this.messages.get(i));
        }

        Object[] result = new Object[2];
        result[0] = res;
        result[1] = messages;
        return result;
    }

    public void check() throws AltException {
        Object[] result = this.validate();
        if(!(Boolean) result[0]){
            ArrayList<String> messages = (ArrayList<String>) result[1];
            String msg = "";
            for(int i=0; i<messages.size(); i++){
                msg += messages.get(i) + ". ";
            }
            throw new AltException(msg);
        }
    }

    public static boolean required(Object value){
        return value != null && !String.valueOf(value).equals("");
    }

    public static boolean equals(String value1, String value2){
        return value1.equals(value2);
    }
}
