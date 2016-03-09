package labs.akhdani.alt;

import org.jose4j.base64url.internal.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.AlgorithmParameterSpec;

public class AltSecure {
    private static final String TAG = AltSecure.class.getName();
    private String key;
    private String iv;

    public AltSecure(){
        this(AltConfig.get("app.id"));
    }

    public AltSecure(String key){
        this(key, key);
    }

    public AltSecure(String key, String iv){
        this.key = key;
        this.iv = iv;
    }

    public String encrypt(String text){
        String result = "";

        try{
            SecretKey key = new SecretKeySpec(Base64.decodeBase64(AltConfig.get("app.secure.key")), "AES");
            AlgorithmParameterSpec iv = new IvParameterSpec(Base64.decodeBase64(AltConfig.get("app.secure.iv")));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            result = Base64.encodeBase64String(cipher.doFinal(text.getBytes("UTF-8")));
        }catch(Exception e){
            e.printStackTrace();
        }

        return result;
    }

    public String decrypt(String text){
        String result = "";

        if(text.equalsIgnoreCase(""))
            return result;

        try{
            SecretKey key = new SecretKeySpec(Base64.decodeBase64(AltConfig.get("app.secure.key")), "AES");
            AlgorithmParameterSpec iv = new IvParameterSpec(Base64.decodeBase64(AltConfig.get("app.secure.iv")));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            result = new String(cipher.doFinal(Base64.decodeBase64(text)));
        }catch(Exception e){

        }

        return result;
    }
}
