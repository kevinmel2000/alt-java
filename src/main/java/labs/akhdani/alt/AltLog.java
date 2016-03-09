package labs.akhdani.alt;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AltLog {
    public static final int LEVEL_LOG   = 5;
    public static final int LEVEL_DEBUG = 4;
    public static final int LEVEL_INFO  = 3;
    public static final int LEVEL_WARN  = 2;
    public static final int LEVEL_ERROR = 1;

    public static int level             = AltLog.LEVEL_LOG;
    public static String dbo_class;
    public static AltDbo dbo;
    public static String tag;
    private static ObjectMapper mapper  = new ObjectMapper();

    public static void write(int level, String tag, Object data){
        String type     = "[";
        String color    = "\u001B[";
        String time     = "<" + (new Date()).toString() + ">";
        switch(level){
            case AltLog.LEVEL_LOG:
                type += "LOG";
                color += "32";
                break;
            case AltLog.LEVEL_DEBUG:
                type += "DEBUG";
                color += "36";
                break;
            case AltLog.LEVEL_INFO:
                type += "INFO";
                color += "34";
                break;
            case AltLog.LEVEL_WARN:
                type += "WARN";
                color += "33";
                break;
            case AltLog.LEVEL_ERROR:
                type += "ERROR";
                color += "31";
                break;
            default:
                type += "";
                color += "0";
                break;
        }
        type += "]";
        color += "m";

        try{
            if(AltLog.level >= level && (AltLog.tag == null || tag.contains(AltLog.tag))) {
                System.out.println(color + type + " " + time + " " + tag + " : " + mapper.writeValueAsString(data != null ? data : "") + "\u001B[0m");

                if(AltLog.dbo != null){
                    Map<String, String> log = new HashMap<>();
                    log.put("type", type);
                    log.put("tag", tag);
                    log.put("data", mapper.writeValueAsString(data != null ? data : ""));

                    AltLog.dbo.insert(log);
                }
            }
        }catch (Exception e){
            System.out.println(color + type + " " + time + " " + tag + " : " + (data != null ? data.toString() : "") + "\u001B[0m");

            if(AltLog.dbo != null){
                Map<String, String> log = new HashMap<>();
                log.put("type", type);
                log.put("tag", tag);
                log.put("data", data != null ? data.toString() : "null");

                try {
                    AltLog.dbo.insert(log);
                }catch (Exception ex){
                    // do nothing
                }
            }
        }
    }

    public static void log(String tag, Object data){
        AltLog.write(AltLog.LEVEL_LOG, tag, data);
    }

    public static void debug(String tag, Object data){
        AltLog.write(AltLog.LEVEL_DEBUG, tag, data);
    }

    public static void info(String tag, Object data){
        AltLog.write(AltLog.LEVEL_INFO, tag, data);
    }

    public static void warn(String tag, Object data){
        AltLog.write(AltLog.LEVEL_WARN, tag, data);
    }

    public static void error(String tag, Object data){
        AltLog.write(AltLog.LEVEL_ERROR, tag, data);
    }
}
