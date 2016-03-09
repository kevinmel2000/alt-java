package labs.akhdani.alt;

import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class AltJwt {

    private static final String TAG = AltJwt.class.getName();

    public static String encode(Map<String, String> data) throws AltException {
        return AltJwt.encode(data, AltConfig.get("session.secret"));
    }

    public static String encode(Map<String, String> data, String key) throws AltException {
        return AltJwt.encode(data, key, AlgorithmIdentifiers.HMAC_SHA256);
    }

    public static String encode(Map<String, String> data, String key, String algorithm) throws AltException {
        String token = "";

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(key.getBytes("UTF-8"));
            byte[] bytekey = md.digest();

            Key hmacKey = new HmacKey(bytekey);

            // Create the Claims, which will be the content of the JWT
            JwtClaims claims = new JwtClaims();
            claims.setIssuer(AltConfig.get("app.id"));
            claims.setGeneratedJwtId();
            claims.setExpirationTimeMinutesInTheFuture(Integer.valueOf(AltConfig.get("session.lifetime")) / 60);
            claims.setIssuedAtToNow();

            // add data to claims
            for(Map.Entry<String, String> entry : data.entrySet()){
                claims.setClaim(entry.getKey(), entry.getValue());
            }

            // set signature
            JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setKey(hmacKey);
            jws.setDoKeyValidation(false);
            jws.setAlgorithmHeaderValue(algorithm);

            token = jws.getCompactSerialization();
        } catch (JoseException e) {
            e.printStackTrace();
            throw new AltException("Token tidak dapat dibuat");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new AltException("Token tidak dapat dibuat");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new AltException("Token tidak dapat dibuat");
        }

        return token;
    }

    public static Map<String, String> decode(String token) throws AltException {
        return AltJwt.decode(token, AltConfig.get("session.secret"));
    }
    public static Map<String, String> decode(String token, String key) throws AltException {
        Map<String, String> data = new HashMap<>();

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(key.getBytes("UTF-8"));
            byte[] bytekey = md.digest();

            Key hmacKey = new HmacKey(bytekey);

            JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                    .setRequireExpirationTime()
                    .setExpectedIssuer(AltConfig.get("app.id"))
                    .setVerificationKey(hmacKey)
                    .setRelaxVerificationKeyValidation()
                    .build();

            JwtClaims jwtClaims = jwtConsumer.processToClaims(token);

            Map<String, Object> tmp = jwtClaims.getClaimsMap();
            for(Map.Entry<String, Object> entry : tmp.entrySet()){
                data.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        } catch (UnsupportedEncodingException e) {
            throw new AltException("Token tidak valid");
        } catch (InvalidJwtException e) {
            throw new AltException("Token tidak valid");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return data;
    }

}
