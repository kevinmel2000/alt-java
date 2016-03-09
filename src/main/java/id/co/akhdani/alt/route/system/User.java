package id.co.akhdani.alt.route.system;

import id.co.akhdani.alt.engine.system.DboSystemUser;
import labs.akhdani.Alt;
import labs.akhdani.alt.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class User {
    private static final String TAG = User.class.getName();

    public int count(AltHttpRequest req, AltHttpResponse res){
        DboSystemUser dbo = new DboSystemUser(req);
        return dbo.count(req.data);
    }

    public AltDbRows list(AltHttpRequest req, AltHttpResponse res){
        DboSystemUser dbo = new DboSystemUser(req);
        return dbo.get(req.data);
    }

    public Map<String, Object> table(AltHttpRequest req, AltHttpResponse res){
        DboSystemUser dbo = new DboSystemUser(req);

        return new HashMap<String, Object>(){{
            put("total", dbo.count(req.data));
            put("list", dbo.get(req.data));
        }};
    }

    public AltDbRow retrieve(AltHttpRequest req, AltHttpResponse res) throws AltException {
        DboSystemUser dbo = new DboSystemUser(req);
        AltDbRow user = dbo.retrieve(req.data);
        user.remove("password");

        return user;
    }

    public int insert(AltHttpRequest req, AltHttpResponse res) throws AltException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            if(!req.data.get("password").equals("")) {
                md.update(req.data.get("password").getBytes());
                byte[] tmp = md.digest();
                StringBuffer sb = new StringBuffer();
                for (byte b : tmp) {
                    sb.append(String.format("%02x", b & 0xff));
                }

                req.data.put("password", sb.toString());
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new AltException("Tidak dapat melakukan hash password");
        }

        AltValidation.instance()
                .rule(AltValidation.required(req.data.get("username")), "Isi username terlebih dahulu!")
                .rule(AltValidation.required(req.data.get("password")), "Isi password terlebih dahulu!")
                .rule(AltValidation.required(req.data.get("name")), "Isi nama terlebih dahulu!")
                .rule(AltValidation.required(req.data.get("usergroupid")), "Pilih usergroup terlebih dahulu!")
                .check();


        DboSystemUser dbo = new DboSystemUser(req);

        // check
        int exist = 0;
        if(!req.data.get("username").equalsIgnoreCase("")) {
            if (req.data.containsKey("username")) exist = dbo.count(new HashMap<String, String>() {{
                put("username", "= " + dbo.quote(req.data.get("username")));
            }});

            if (exist > 0)
                throw new AltException("Sudah ada user dengan username tersebut!");
        }

        // insert
        return dbo.insert(req.data);
    }

    public int update(AltHttpRequest req, AltHttpResponse res) throws AltException {
        req.data.remove("password");

        AltValidation.instance()
                .rule(AltValidation.required(req.data.get("userid")), "Isi user terlebih dahulu!")
                .check();

        // update
        DboSystemUser dbo = new DboSystemUser(req);
        return dbo.update(req.data);
    }

    public int chpasswd(AltHttpRequest req, AltHttpResponse res) throws AltException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            if(req.data.get("oldpassword") != null && !req.data.get("oldpassword").equals("")) {
                md.update(req.data.get("oldpassword").getBytes());
                byte[] tmp = md.digest();
                StringBuffer sb = new StringBuffer();
                for (byte b : tmp) {
                    sb.append(String.format("%02x", b & 0xff));
                }

                req.data.put("oldpassword", sb.toString());
            }

            if(req.data.get("newpassword") != null && !req.data.get("newpassword").equals("")) {
                md.update(req.data.get("newpassword").getBytes());
                byte[] tmp = md.digest();
                StringBuffer sb = new StringBuffer();
                for (byte b : tmp) {
                    sb.append(String.format("%02x", b & 0xff));
                }

                req.data.put("newpassword", sb.toString());
            }

            if(req.data.get("confnewpass") != null && !req.data.get("confnewpass").equals("")) {
                md.update(req.data.get("confnewpass").getBytes());
                byte[] tmp = md.digest();
                StringBuffer sb = new StringBuffer();
                for (byte b : tmp) {
                    sb.append(String.format("%02x", b & 0xff));
                }

                req.data.put("confnewpass", sb.toString());
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new AltException("Tidak dapat melakukan hash password");
        }

        AltValidation.instance()
                .rule(AltValidation.required(req.data.get("userid")), "Pilih user terlebih dahulu!")
                .rule(Alt.check(3, req) || (!Alt.check(3, req) && AltValidation.required(req.data.get("oldpassword"))), "Isi password lama terlebih dahulu!")
                .rule(AltValidation.required(req.data.get("newpassword")), "Isi password baru terlebih dahulu!")
                .rule(AltValidation.required(req.data.get("confnewpass")), "Isi password baru terlebih dahulu!")
                .rule(AltValidation.equals(req.data.get("newpassword"), req.data.get("confnewpass")), "Isi password baru terlebih dahulu!")
                .check();

        // update
        DboSystemUser dbo = new DboSystemUser(req);
        AltDbRows users = dbo.get(new HashMap<String, String>(){{
            put("userid", req.data.get("userid"));
        }});
        if(users == null || users.size() != 1)
            throw new AltException("Tidak dapat menemukan user");

        if(!Alt.check(3, req) && !users.get(0).getString("password").equals(req.data.get("oldpassword")))
            throw new AltException("Password tidak sesuai");

        return dbo.update(new HashMap<String, String>(){{
            put("userid", req.data.get("userid"));
            put("password", req.data.get("newpassword"));
        }});
    }

    public int delete(AltHttpRequest req, AltHttpResponse res) throws AltException {
        AltValidation.instance()
                .rule(AltValidation.required(req.data.get("userid")), "User belum dipilih!")
                .check();

        // insert
        DboSystemUser dbo = new DboSystemUser(req);
        return dbo.delete(req.data);
    }
}