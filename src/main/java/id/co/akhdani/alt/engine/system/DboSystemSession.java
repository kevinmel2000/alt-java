package id.co.akhdani.alt.engine.system;

import labs.akhdani.alt.AltDbo;
import labs.akhdani.alt.AltHttpRequest;

public class DboSystemSession extends AltDbo {

    public DboSystemSession(){
        this(null);
    }

    public DboSystemSession(AltHttpRequest request){
        super(request);

        this.pkey           = "sessionid";
        this.autoinc        = true;
        this.table_name     = "sys_session";
        this.sequence_name  = "seq_sys_session";
        this.table_fields.put("sessionid", "");
        this.table_fields.put("userid", "");
        this.table_fields.put("token", "");
        this.table_fields.put("ipaddress", "");
        this.table_fields.put("useragent", "");
        this.table_fields.put("lastactivity", "");
    }
}