package id.co.akhdani.alt.engine.system;

import labs.akhdani.alt.AltDbo;
import labs.akhdani.alt.AltHttpRequest;

public class DboSystemUsergroup extends AltDbo {

    public DboSystemUsergroup(){
        this(null);
    }

    public DboSystemUsergroup(AltHttpRequest request){
        super(request);

        this.pkey           = "usergroupid";
        this.autoinc        = true;
        this.table_name     = "sys_usergroup";
        this.sequence_name  = "seq_sys_usergroup";
        this.table_fields.put("usergroupid", "");
        this.table_fields.put("name", "");
        this.table_fields.put("level", "");
        this.table_fields.put("isallowregistration", "");
        this.table_fields.put("isdisplayed", "");
    }
}