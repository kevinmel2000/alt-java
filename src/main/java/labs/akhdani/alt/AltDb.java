package labs.akhdani.alt;

import labs.akhdani.Alt;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class AltDb {
    private static final String TAG = AltDb.class.getName();
    public static Map<String, AltDb> instances = new HashMap<>();

    private Connection connect;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;
    private Map<String, String> config = new HashMap<>();

    public AltDb(){
        this("default");
    }

    public AltDb(String db_instance){
        Map<String, String> config = new HashMap<>();
        config.put("persistent", AltConfig.get("db." + db_instance + ".persistent") != null ? AltConfig.get("db." + db_instance + ".persistent") : "true");
        config.put("class", AltConfig.get("db." + db_instance + ".class"));
        config.put("dsn", AltConfig.get("db." + db_instance + ".dsn"));
        config.put("username", AltConfig.get("db." + db_instance + ".username"));
        config.put("password", AltConfig.get("db." + db_instance + ".password"));

        this.config = config;
    }

    public AltDb(Map<String, String> config){
        this.config = config;
    }

    public static AltDb instance(){
        return AltDb.instance("default");
    }

    public static AltDb instance(String db_instance){
        if(AltDb.instances.get(db_instance) == null){
            AltDb.instances.put(db_instance, new AltDb(db_instance));
        }
        return AltDb.instances.get(db_instance);
    }

    private void connect(){
        try {
            //load the driver class
            Class.forName(this.config.get("class"));

            //create the connection object
            connect = DriverManager.getConnection(this.config.get("dsn"), this.config.get("username"), this.config.get("password"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AltDbRows query(String sql){
        Statement statement = null;
        ResultSet rs = null;
        AltDbRows res = null;

        try {
            if(this.connect == null || getConfig().getOrDefault("persistent", "false").equalsIgnoreCase("false"))
                this.connect();

            // create the statement object
            statement = this.connect.createStatement();

            // execute query
            rs =  statement.executeQuery(this.transform(sql));
            ResultSetMetaData meta = rs.getMetaData();

            res = new AltDbRows();
            while(rs.next()){
                AltDbRow altDbRow = new AltDbRow();
                for(int i=1; i<=meta.getColumnCount(); i++){
                    altDbRow.put(meta.getColumnName(i).toLowerCase(), rs.getString(i));
                }

                res.add(altDbRow);
            }

            try{
                rs.close();
            }catch (Exception e){

            }
        }catch(Exception e){

        }finally {
            try{
                if(statement != null)
                    statement.close();
            }catch (Exception e){

            }finally {
                if(getConfig().getOrDefault("persistent", "false").equalsIgnoreCase("false"))
                    this.close();
            }

            return res == null ? new AltDbRows() : res;
        }
    }

    public int queryUpdate(String sql) throws AltException {
        int res = 0;

        try {
            if(this.connect == null || getConfig().getOrDefault("persistent", "false").equalsIgnoreCase("false"))
                this.connect();

            // create the statement object
            Statement statement = this.connect.createStatement();

            // execute query
            res = statement.executeUpdate(sql);

            statement.close();
        }catch(Exception e){

        }finally {
            if(getConfig().getOrDefault("persistent", "false").equalsIgnoreCase("false"))
                this.close();

            return res;
        }
    }

    private void close() {
        try {
            if (connect != null) {
                connect.close();
                connect = null;
            }
        } catch (Exception e) {

        }
    }

    public Map<String, String> getConfig(){
        return this.config;
    }

    private String transform(String query){
        String sql = "";

        switch(this.config.get("class")){
            case "oracle.jdbc.driver.OracleDriver":
                query = query.replaceAll("LIMIT", "limit");
                query = query.replaceAll("OFFSET", "offset");

                String[] tmp = query.split(" limit ");
                if(tmp.length <= 1)
                    return query;

                int limit = 0;
                int offset = 0;

                String q = tmp[0].trim();
                String[] tmp2 = tmp[1].split(" offset ");
                if(tmp2.length == 1){
                    limit = Integer.valueOf(tmp2[1].trim());
                }else{
                    limit = Integer.valueOf(tmp2[0].trim());
                    offset = Integer.valueOf(tmp2[1].trim());
                }

                sql = "select * from ( select a.*, ROWNUM rnum from ( " + q + " ) a where ROWNUM <= " + (limit+offset) + " ) where rnum  > " + offset;
                break;

            default:
                sql = query;
                break;
        }

        return sql;
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
        return s == null ? "" : "'" + s + "'";
    }
}
