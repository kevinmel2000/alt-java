package id.co.akhdani.alt.route.system;

import id.co.akhdani.alt.engine.system.DboSystemSession;
import id.co.akhdani.alt.engine.system.DboSystemUser;
import id.co.akhdani.alt.engine.system.DboSystemUsergroup;
import labs.akhdani.Alt;
import labs.akhdani.alt.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Auth {
    private static final String TAG = Auth.class.getName();

    public String login(AltHttpRequest req, AltHttpResponse res) throws AltException, NoSuchAlgorithmException {
        // parameter needed
        String username = req.data.get("username");
        String password = req.data.get("password");

        // validate
        AltValidation.instance()
                .rule(AltValidation.required(username), "Username harus diisi!")
                .rule(AltValidation.required(password), "Password harus diisi!")
                .check();

        // get user from database
        DboSystemUser dboSystemUser = new DboSystemUser(req);
        HashMap<String, String> user = new HashMap<>();
        user.put("where", "username = " + dboSystemUser.quote(username));
        AltDbRows users = dboSystemUser.get(user);

        if(users.size() != 1)
            throw new AltException("User tidak ditemukan!");

        AltDbRow userdata = users.get(0);

        // checking if password correct
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());
        byte[] tmp = md.digest();
        StringBuffer sb = new StringBuffer();
        for(byte b : tmp){
            sb.append(String.format("%02x", b & 0xff));
        }

        if(!userdata.getString("password").equals(sb.toString()))
            throw new AltException("Password tidak cocok!");

        // get usergroup
        DboSystemUsergroup dboSystemUsergroup = new DboSystemUsergroup();
        HashMap<String, String> usergroup = new HashMap<>();
        usergroup.put("where", "usergroupid = " + dboSystemUsergroup.quote(userdata.getString("usergroupid")));

        AltDbRows usergroups = dboSystemUsergroup.get(usergroup);

        if(usergroups.size() != 1)
            throw new AltException("Usergroup tidak ditemukan!");

        userdata.put("usergroupname", usergroups.get(0).getString("name"));
        userdata.put("userlevel", usergroups.get(0).getString("level"));

        // generate token
        String token = Alt.generate_token(userdata.map());

        // save to session
        Alt.set_token(token, req);
        try {
            DboSystemSession dboSystemSession = new DboSystemSession(req);
            HashMap<String, String> session = new HashMap<>();
            session.put("userid", userdata.getString("userid"));
            session.put("token", token);
            session.put("ipaddress", req.ip());
            session.put("useragent", req.useragent());
            session.put("lastactivity", String.valueOf((new Date()).getTime()));
            dboSystemSession.insert(session);
        }catch (Exception e){
            // do nothing
        }

        return token;
    }

    public int logout(AltHttpRequest req, AltHttpResponse res) throws AltException {
        if(!Alt.islogin(req) && Alt.get_token(req).equals(""))
            throw new AltException("Anda belum login atau sesi anda telah habis");

        DboSystemSession dboSystemSession = new DboSystemSession(req);
        HashMap<String, String> session = new HashMap<>();
        session.put("where", "token = " + dboSystemSession.quote(Alt.get_token(req)));

        return dboSystemSession.delete(session);
    }

    public String token(AltHttpRequest req, AltHttpResponse res) throws AltException {
        String token = Alt.get_token(req);

        // validate
        AltValidation.instance()
                .rule(AltValidation.required(token), "Token harus ada!")
                .check();

        // verify token
        Alt.verify_token(token);

        Map<String, String> userdata = Alt.get_userdata(req);
        if(userdata.get("userid").equals(""))
            throw new AltException("Anda belum login atau sesi anda telah habis");

        DboSystemSession dboSystemSession = new DboSystemSession(req);
        HashMap<String, String> session = new HashMap<>();

        try{
            session.put("where", "token = " + dboSystemSession.quote(token));
            AltDbRows rows = dboSystemSession.get(session);

            if(rows.size() != 1) {
                this.logout(req, res);

                // generate new token
                userdata.remove("iss");
                userdata.remove("jti");
                userdata.remove("exp");
                userdata.remove("iat");
                token = Alt.generate_token(userdata);
                Alt.set_token(token, req);

                // save to session
                session = new HashMap<>();
                session.put("sessionid", rows.get(0).getString("sessionid"));
                session.put("token", token);
                session.put("userid", userdata.get("userid"));
                session.put("lastactivity", String.valueOf((new Date()).getTime()));
                session.put("ipaddress", req.ip());
                session.put("useragent", req.useragent());
                dboSystemSession.update(session);
            }
        }catch(Exception e){

        }

        return token;
    }

    public Map<String, String> secure(AltHttpRequest req, AltHttpResponse res) throws AltException {
        req.secure = false;

        // validate
        AltValidation.instance()
                .rule(AltValidation.required(req.data.get("appid")), "ID aplikasi harus ada!")
                .check();

        if(!req.data.get("appid").equalsIgnoreCase(AltConfig.get("app.id")))
            throw new AltException("Not allowed here!");

        return new HashMap<String, String>(){{
            put("key", AltConfig.get("app.secure.key"));
            put("iv", AltConfig.get("app.secure.iv"));
        }};
    }
}