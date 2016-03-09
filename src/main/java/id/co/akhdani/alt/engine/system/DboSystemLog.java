package id.co.akhdani.alt.engine.system;

import labs.akhdani.alt.AltDbo;
import labs.akhdani.alt.AltHttpRequest;

public class DboSystemLog extends AltDbo {

    public DboSystemLog(){
        this(null);
    }

    public DboSystemLog(AltHttpRequest request){
        super(request);

        this.pkey           = "logid";
        this.autoinc        = true;
        this.table_name     = "sys_log";
        this.sequence_name  = "seq_sys_log";
        this.table_fields.put("logid", "");
        this.table_fields.put("type", "");
        this.table_fields.put("tag", "");
        this.table_fields.put("data", "");
        this.table_fields.put("entrytime", "");
    }
}