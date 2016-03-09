package id.co.akhdani.alt.route.system;

import id.co.akhdani.alt.engine.system.DboSystemUsergroup;
import labs.akhdani.alt.*;

import java.util.HashMap;
import java.util.Map;

public class Usergroup {
    private static final String TAG = Usergroup.class.getName();

    public int count(AltHttpRequest req, AltHttpResponse res){
        DboSystemUsergroup dbo = new DboSystemUsergroup(req);
        return dbo.count(req.data);
    }

    public AltDbRows list(AltHttpRequest req, AltHttpResponse res){
        DboSystemUsergroup dbo = new DboSystemUsergroup(req);
        return dbo.get(req.data);
    }

    public Map<String, Object> table(AltHttpRequest req, AltHttpResponse res){
        DboSystemUsergroup dbo = new DboSystemUsergroup(req);

        return new HashMap<String, Object>(){{
            put("total", dbo.count(req.data));
            put("list", dbo.get(req.data));
        }};
    }

    public AltDbRow retrieve(AltHttpRequest req, AltHttpResponse res) throws AltException {
        DboSystemUsergroup dbo = new DboSystemUsergroup(req);
        AltDbRows tmp = dbo.get(req.data);

        return dbo.retrieve(req.data);
    }

    public int insert(AltHttpRequest req, AltHttpResponse res) throws AltException {
        AltValidation.instance()
                .check();

        // insert
        DboSystemUsergroup dbo = new DboSystemUsergroup(req);
        return dbo.insert(req.data);
    }

    public int update(AltHttpRequest req, AltHttpResponse res) throws AltException {
        AltValidation.instance()
                .check();

        // insert
        DboSystemUsergroup dbo = new DboSystemUsergroup(req);
        return dbo.update(req.data);
    }

    public int delete(AltHttpRequest req, AltHttpResponse res) throws AltException {
        AltValidation.instance()
                .rule(AltValidation.required(req.data.get("userid")), "User belum dipilih!")
                .check();

        // insert
        DboSystemUsergroup dbo = new DboSystemUsergroup(req);
        return dbo.delete(req.data);
    }
}