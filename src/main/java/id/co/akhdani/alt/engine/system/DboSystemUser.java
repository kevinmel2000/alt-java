package id.co.akhdani.alt.engine.system;

import labs.akhdani.alt.AltDbo;
import labs.akhdani.alt.AltHttpRequest;

public class DboSystemUser extends AltDbo {

    public DboSystemUser(){
        this(null);
    }

    public DboSystemUser(AltHttpRequest request){
        super(request);

        this.pkey           = "userid";
        this.autoinc        = true;
        this.table_name     = "sys_user";
        this.sequence_name  = "seq_sys_user";
        this.table_fields.put("userid", "");
        this.table_fields.put("username", "");
        this.table_fields.put("password", "");
        this.table_fields.put("name", "");
        this.table_fields.put("address", "");
        this.table_fields.put("email", "");
        this.table_fields.put("phone", "");
        this.table_fields.put("usergroupid", "");
        this.table_fields.put("isenabled", "");
        this.table_fields.put("islogin", "");
    }
}