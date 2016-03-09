package labs.akhdani.alt;

import labs.akhdani.Alt;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AltDbo {
    private static final String TAG = AltDbo.class.getName();
    private AltHttpRequest request;

    public AltDb db;
    public String db_instance;
    public String pkey;

    // table
    public boolean autoinc = true;
    public String sequence_name;
    public String table_name;
    public Map<String, String> table_fields = new HashMap<String, String>();

    // view if exist
    public String view_name;
    public Map<String, String> view_fields = new HashMap<String, String>();

    public AltDbo(){
        this("default");
    }

    public AltDbo(String db_instance){
        this(db_instance, null);
    }

    public AltDbo(AltHttpRequest request){
        this("default", request);
    }

    public AltDbo(String db_instance, AltHttpRequest request){
        this.db_instance = db_instance;
        this.db = AltDb.instance(this.db_instance);
        this.request = request;
    }

    public String get_tablename(){
        return this.get_tablename(false);
    }
    public String get_tablename(boolean returnview){
        return returnview && this.view_name != null ? this.view_name : this.table_name;
    }

    public Map<String, String> get_fields(){
        return this.get_fields(false);
    }
    public Map<String, String> get_fields(boolean returnview){
        return returnview && this.view_name != null ? this.view_fields : this.table_fields;
    }

    public String get_select(){
        return this.get_select(new HashMap<>());
    }
    public String get_select(Map<String, String> data){
        return data.get("select") != null && data.get("select") != "" ? data.get("select") : "*";
    }

    public String get_from(){
        return this.get_from("");
    }
    public String get_from(String as){
        return this.get_tablename() + (!as.equals("") ? " as " + as : "");
    }

    public String get_where(){
        return this.get_where(new HashMap<>());
    }
    public String get_where(Map<String, String> data){
        StringBuilder sb = new StringBuilder();

        if(data.get("where") != null && data.get("where") != ""){
            sb.append(data.get("where"));
        }

        Map<String, String> fields = this.get_fields();
        for(Map.Entry<String, String> cursor : data.entrySet()){
            if(cursor.getValue() != "" && fields.get(cursor.getKey()) != null){
                if(sb.length() > 0) sb.append(" and ");
                String[] tmp = cursor.getValue().split(" ");
                if(tmp.length > 1 && (tmp[0].equalsIgnoreCase("like") || tmp[0].equalsIgnoreCase("=") || tmp[0].equalsIgnoreCase("<") || tmp[0].equalsIgnoreCase("<=") || tmp[0].equalsIgnoreCase(">") || tmp[0].equalsIgnoreCase(">=") || tmp[0].equalsIgnoreCase("in") || tmp[0].equalsIgnoreCase("not"))){
                    sb.append(cursor.getKey() + " " + cursor.getValue());
                }else{
                    sb.append(cursor.getKey() + " like " + this.quote("%" + cursor.getValue() + "%"));
                }

            }
        }

        if (fields.get("isdeleted") != null) {
            if(sb.length() > 0) sb.append(" and ");
            sb.append("isdeleted = " + (data.get("isdeleted") == null ? "0" : this.quote(data.get("isdeleted"))));
        }

        return sb.length() > 0 ? " where " + sb.toString() : "";
    }

    public String get_group(){
        return this.get_group(new HashMap<>());
    }
    public String get_group(Map<String, String> data){
        return data.get("group") != null && data.get("group") != "" ? " group by " + data.get("group") : "";
    }

    public String get_join(){
        return this.get_join(new HashMap<>());
    }
    public String get_join(Map<String, String> data){
        return data.get("join") != null && data.get("join") != "" ? data.get("join") : "";
    }

    public String get_order(){
        return this.get_order(new HashMap<>());
    }
    public String get_order(Map<String, String> data){
        return data.get("order") != null && data.get("order") != "" ? " order by " + data.get("order") : "";
    }

    public String get_limit(){
        return this.get_limit(new HashMap<>());
    }
    public String get_limit(Map<String, String> data){
        return data.get("limit") != null && data.get("limit") != "" ? " limit " + data.get("limit") + " offset " + data.getOrDefault("offset", "0")  : "";
    }

    public String quote(int s){
        return this.quote(Integer.toString(s));
    }
    public String quote(double s){
        return this.quote(Double.toString(s));
    }
    public String quote(long s){
        return this.quote(Long.toString(s));
    }
    public String quote(float s){
        return this.quote(Float.toString(s));
    }
    public String quote(String s){
        return s == null ? "" : (this.sequence_name != null && s.contains(this.sequence_name) ? s : "'" + s + "'");
    }

    public AltDbRows query(String sql){
        return this.db.query(sql);
    }

    public int queryUpdate(String sql) throws AltException {
        return this.db.queryUpdate(sql);
    }

    public int count(){
        return this.count(new HashMap<>());
    }
    public int count(Map<String, String> data){
        // sql query
        String sql      = "select count(*) as numofrow from " + this.get_from() + this.get_where(data);
        AltDbRows res   = this.db.query(sql);
        int count       = 0;

        if(res == null || res.size() == 0)
            return count;

        try{
            count = res.get(0).getInteger("numofrow");

            return count;
        }catch(Exception e){
            return count;
        }
    }

    public AltDbRows get(){
        return this.get(new HashMap<>());
    }
    public AltDbRows get(Map<String, String> data){
        AltDbRows res        = new AltDbRows();

        String sql      = "select " + this.get_select(data) + " from " + this.get_tablename() + this.get_where(data) + this.get_group(data) + this.get_order(data) + this.get_join(data) + this.get_limit(data);

        try{
            res = this.db.query(sql);
            return res == null ? new AltDbRows() : res;
        }catch(Exception e){
            return res;
        }
    }

    public AltDbRow retrieve() throws AltException {
        return this.retrieve(new HashMap<>());
    }
    public AltDbRow retrieve(Map<String, String> data) throws AltException {
        if(data.get(this.pkey) != null)
            data.put(this.pkey, "= " + data.get(this.pkey));

        AltDbRows res = this.get(data);

        if(res == null || res.size() < 0)
            throw new AltException("Data tidak ditemukan!");

        return res.get(0);
    }

    public int insert() throws AltException {
        return this.insert(new HashMap<>());
    }
    public int insert(Map<String, String> data) throws AltException {
        // sql query
        String sql      = "insert into " + this.table_name + " (";

        // imploding field names
        if (this.pkey != null && this.autoinc){
            if(this.sequence_name != null && AltConfig.get("db." + this.db_instance + ".class").equalsIgnoreCase("oracle.jdbc.driver.OracleDriver")){
                data.put(this.pkey, this.sequence_name + ".NEXTVAL");
            }else{
                data.remove(this.pkey);
            }
        }

        // set field values
        Map<String, String> table_fields = this.get_fields(false);

        // add entry time and entry user if exist
        if (table_fields.get("entrytime") != null && data.get("entrytime") == null)
            data.put("entrytime", String.valueOf((new Date()).getTime()));

        if(this.request != null) {
            Map<String, String> userdata = Alt.get_userdata(this.request);
            if (table_fields.get("entryuser") != null && data.get("entryuser") == null)
                data.put("entryuser", userdata.get("username"));
        }

        // set fields and values to insert
        ArrayList<String> fnames = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        for (Map.Entry<String, String> entry : data.entrySet()) if(table_fields.get(entry.getKey()) != null) {
            fnames.add(entry.getKey());
            values.add(entry.getValue() == null ? null : this.quote(entry.getValue()));
        }

        for(int i=0; i<fnames.size(); i++){
            sql += (i > 0 ? ", " : "") + fnames.get(i);
        }
        sql += ") values (";

        for(int i=0; i<values.size(); i++){
            sql += (i > 0 ? ", " : "") + values.get(i);
        }
        sql += ")";

        // execute
        int res = this.db.queryUpdate(sql);

        if(this.sequence_name != null){
            String sql2 = "select " + this.sequence_name + ".currVal as last_inserted from " + this.table_name;
            AltDbRows res2 = this.db.query(sql2);
            if(res2 != null && res2.size() > 0){
                return res2.get(0).getInteger("last_inserted");
            }else{
                return res;
            }
        }else{
            return res;
        }
    }

    public int update() throws AltException {
        return this.update(new HashMap<String, String>());
    }
    public int update(Map<String, String> data) throws AltException {
        // sql query
        String sql      = "update " + this.table_name + " set ";

        String pkey     = data.get(this.pkey);
        data.remove(this.pkey);

        // set field values
        Map<String, String> table_fields = this.get_fields(false);

        // add modified time and modified user if exist
        if (table_fields.get("modifiedtime") != null && data.get("modifiedtime") == null)
            data.put("modifiedtime", String.valueOf((new Date()).getTime()));
        if(this.request != null) {
            Map<String, String> userdata = Alt.get_userdata(this.request);
            if (table_fields.get("modifieduser") != null && data.get("modifieduser") == null)
                data.put("modifieduser", userdata.get("username"));
        }

        // set fields and values to update
        ArrayList<String> fields = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, String> entry : data.entrySet()) if(table_fields.get(entry.getKey()) != null) {
            String value = entry.getValue() != null ? entry.getValue() : "";
            fields.add(entry.getKey() + " = " + this.quote(value));
            sql += (i != 0 ? "," : "") + entry.getKey() + " = " + this.quote(value);
            i++;
        }

        // forge sql
        if(fields.size() <= 0)
            return 0;

        sql += data.get("where") != null ? " where " + data.get("where") : ((pkey != null) ? " where " + this.pkey + " = " + this.quote(pkey) : "");

        return this.db.queryUpdate(sql);
    }

    public int delete() throws AltException {
        return this.delete(new HashMap<String, String>());
    }
    public int delete(Map<String, String> data) throws AltException {
        if(data.get(this.pkey) != null){
            data.put("where", this.pkey + " = " + this.quote(data.get(this.pkey)));
            data.remove(this.pkey);
        }else if(this.get_where(data).equals("")){
            return -1;
        }

        // add modified time and modified user if exist
        Map<String, String> fields = this.get_fields(false);
        if(fields.get("isdeleted") != null){
            if (fields.get("deletedtime") != null && data.get("deletedtime") == null)
                data.put("deletedtime", String.valueOf((new Date()).getTime()));
            if (fields.get("isdeleted") != null)
                data.put("isdeleted", "1");

            if(this.request != null) {
                Map<String, String> userdata = Alt.get_userdata(this.request);
                if (fields.get("deleteduser") != null && data.get("deleteduser") == null)
                    data.put("deleteduser", userdata.get("username"));
            }

            return this.update(data);
        }

        // delete
        String sql = "delete from " + this.table_name + this.get_where(data);
        return this.db.queryUpdate(sql);
    }
}