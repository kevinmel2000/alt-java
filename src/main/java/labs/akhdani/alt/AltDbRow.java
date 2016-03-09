package labs.akhdani.alt;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AltDbRow extends HashMap<String, Object>{
    public Map<String, String> map(){
        Map<String, String> raw = new HashMap<>();

        for(Entry<String, Object> entry : this.entrySet()){
            raw.put(entry.getKey(), this.getString(entry.getKey()));
        }

        return raw;
    }

    public String getString(String key){
        String res = String.valueOf(super.get(key));
        return res.equals("null") ? "" : res;
    }

    public Integer getInteger(String key){
        return Integer.valueOf(this.getString(key).equalsIgnoreCase("") ? "0" : this.getString(key));
    }

    public Long getLong(String key){
        return Long.valueOf(this.getString(key).equalsIgnoreCase("") ? "0" : this.getString(key));
    }

    public Double getDouble(String key){
        return Double.valueOf(this.getString(key).equalsIgnoreCase("") ? "0" : this.getString(key));
    }

    public Float getFloat(String key){
        return Float.valueOf(this.getString(key).equalsIgnoreCase("") ? "0" : this.getString(key));
    }

    public Date getDate(String key){
        return this.getDate(key, "YYYYMMDD");
    }
    public Date getDate(String key, String format){
        if(format.equals("X")){
            return new Date(this.getLong(key));
        }else{
            // YYYYMMDD
            String ymd = this.getString(key);
            if(ymd.length() < 6) return null;

            String Y = ymd.substring(0, 4);
            String M = ymd.substring(4, 2);
            String D = ymd.substring(6, 2);

            String h = "0";
            String m = "0";
            String s = "0";
            if(ymd.length() == 12){
                ymd.substring(8, 2);
                ymd.substring(10, 2);
                ymd.substring(12, 2);
            }

            return new Date(Integer.valueOf(Y), Integer.valueOf(M), Integer.valueOf(D), Integer.valueOf(h), Integer.valueOf(m), Integer.valueOf(s));
        }
    }
}
