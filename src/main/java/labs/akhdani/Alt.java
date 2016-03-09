package labs.akhdani;

import com.fasterxml.jackson.databind.ObjectMapper;
import labs.akhdani.alt.*;
import spark.Route;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static spark.Spark.*;
import static spark.Spark.externalStaticFileLocation;

public class Alt {
    private static final String TAG = Alt.class.getName();

    public static final int ENV_DEVELOPMENT                 = 0;
    public static final int ENV_TESTING                     = 1;
    public static final int ENV_PRODUCTION                  = 2;

    public static final int STATUS_OK                       = 200;
    public static final int STATUS_UNAUTHORIZED             = 401;
    public static final int STATUS_FORBIDDEN                = 403;
    public static final int STATUS_NOTFOUND                 = 404;
    public static final int STATUS_ERROR                    = 500;

    public int environment                                  = Alt.ENV_PRODUCTION;
    public String defaultRoute                              = "home";
    public int port                                         = 9090;
    public String staticFolder                              = "static";
    public String externalFolder                            = "/var/www/";
    public int maxPostSize                                  = 25 * 1024 * 1024;

    public boolean surpressResponseCode                     = true;

    public long timestart                                   = 0;
    public long timestop                                    = 0;
    public long memorystart                                 = 0;
    public long memorystop                                  = 0;

    public static Map<String, Alt> instances                = new HashMap<>();

    private Class<?> clazz;
    private Alt instance;
    private Runtime runtime = Runtime.getRuntime();
    private ObjectMapper mapper = new ObjectMapper();

    public static Alt instance() {
        return instances.get("default");
    }

    public static Alt instance(Class<?> clazz){
        return instance("default", clazz);
    }

    public static Alt instance(Class<?> clazz, Map<String, Object> config){
        return instance("default", clazz, config);
    }

    public static Alt instance(String name, Class<?> clazz){
        Map<String, Object> config = new HashMap<>();

        if(AltConfig.get("app.port") != null)
            config.put("port", Integer.valueOf(AltConfig.get("app.port")));

        if(AltConfig.get("app.environment") != null)
            config.put("environment", AltConfig.get("app.environment").equalsIgnoreCase("development") ? ENV_DEVELOPMENT : (AltConfig.get("app.environment").equalsIgnoreCase("testing") ? ENV_TESTING : ENV_PRODUCTION));

        if(AltConfig.get("app.staticFolder") != null)
            config.put("staticFolder", AltConfig.get("app.staticFolder"));

        if(AltConfig.get("app.externalFolder") != null)
            config.put("externalFolder", AltConfig.get("app.externalFolder"));

        if(AltConfig.get("app.defaultRoute") != null)
            config.put("defaultRoute", AltConfig.get("app.defaultRoute"));

        if(AltConfig.get("app.maxPostSize") != null)
            config.put("defaultRoute", AltConfig.get("app.defaultRoute"));

        if(AltConfig.get("app.surpressResponseCode") != null)
            config.put("surpressResponseCode", AltConfig.get("app.surpressResponseCode").equalsIgnoreCase("true"));

        return instance(name, clazz, config);
    }

    public static Alt instance(String name, Class<?> clazz, Map<String, Object> config){
        if(instances.get(name) == null){
            Alt alt = new Alt(clazz);

            if(config.get("port") != null)
                alt.setPort((Integer) config.get("port"));

            if(config.get("staticFolder") != null)
                alt.setStaticFolder((String) config.get("staticFolder"));

            if(config.get("externalFolder") != null)
                alt.setExternalFolder((String) config.get("externalFolder"));

            if(config.get("environment") != null)
                alt.setEnvironment((Integer) config.get("environment"));

            if(config.get("defaultRoute") != null)
                alt.setDefaultRoute((String) config.get("defaultRoute"));

            if(config.get("maxPostSize") != null)
                alt.setMaxPostSize((Integer) config.get("maxPostSize"));

            if(config.get("surpressResponseCode") != null)
                alt.setSurpressResponseCode((Boolean) config.get("surpressResponseCode"));

            instances.put(name, alt);
        }

        return instances.get(name);
    }

    public Alt(Class<?> clazz){
        this.clazz = clazz;
        this.instance = this;
    }

    private void setEnvironment(int environment){
        this.environment = environment;
    }

    private void setDefaultRoute(String defaultRoute){
        this.defaultRoute = defaultRoute;
    }

    private void setPort(int port){
        this.port = port;
        port(port);
    }

    private void setStaticFolder(String staticFolder){
        this.staticFolder = staticFolder;
        staticFileLocation(staticFolder);
    }

    private void setExternalFolder(String externalFolder){
        this.externalFolder = externalFolder;
        externalStaticFileLocation(externalFolder);
    }

    private void setMaxPostSize(int maxPostSize){
        this.maxPostSize = maxPostSize;
    }

    private void setSurpressResponseCode(boolean surpressResponseCode){
        this.surpressResponseCode = surpressResponseCode;
    }

    public void route(String url){
        url = url.equalsIgnoreCase("") ? "/" + url : url;

        Route route = (req, res)->{
            AltHttpRequest altHttpRequest = new AltHttpRequest(req, instance);
            AltHttpResponse altHttpResponse = new AltHttpResponse(res, instance);

            // handle options request
            if(req.requestMethod().equalsIgnoreCase("options"))
                return instance.response(altHttpRequest, altHttpResponse, Alt.STATUS_OK, "", null);

            instance.timestart = System.nanoTime();
            instance.memorystart = runtime.totalMemory() - runtime.freeMemory();
            String[] splat = altHttpRequest.splat();

            // handle without "/"
            if(splat.length == 0) {
                splat = new String[1];
                splat[0] = this.defaultRoute;
            }

            // uri
            String uri = splat[0].charAt(splat[0].length()-1) == '/' ? splat[0].substring(0, splat[0].length()-1) : splat[0];

            // skip favicon
            if(uri.equalsIgnoreCase("favicon.ico"))
                return "";

            // split uri by "/"
            String[] uris = uri.split("/");

            // handle static file served in
            if(this.staticFolder.equalsIgnoreCase("") || this.externalFolder.equalsIgnoreCase("") || uris[0].equalsIgnoreCase(this.staticFolder) || uris[0].equalsIgnoreCase(this.externalFolder)){
                try{
                    InputStream inputStream;
                    try {
                        File file = new File(this.externalFolder + uri);
                        inputStream = new FileInputStream(uri);
                    }catch(Exception e) {
                        try{
                            File file = new File(this.staticFolder + uri);
                            inputStream = new FileInputStream(uri);
                        }catch(Exception e2){
                            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                            inputStream = classloader.getResourceAsStream(uri);
                        }
                    }

                    res.type(Files.probeContentType(Paths.get(uri)));
                    res.status(200);

                    byte[] buf = new byte[1024];
                    OutputStream os = res.raw().getOutputStream();
                    OutputStreamWriter outWriter = new OutputStreamWriter(os);
                    int count = 0;
                    while ((count = inputStream.read(buf)) >= 0) {
                        os.write(buf, 0, count);
                    }
                    inputStream.close();
                    outWriter.close();

                    return "";
                }catch (Exception e){
                    e.printStackTrace();
                    return instance.response(altHttpRequest, altHttpResponse, Alt.STATUS_NOTFOUND, "File tidak ditemukan", null);
                }
            }

            // set log in AltLog
            if(AltLog.dbo_class != null){
                try{
                    Class<?> clazz = Class.forName(AltLog.dbo_class);
                    Constructor ctor = clazz.getConstructor(clazz);
                    AltLog.dbo = (AltDbo) ctor.newInstance(altHttpRequest);
                }catch (Exception e){
                    // do nothing
                }
            }

            // serve as webservice
            int indexOf = uris.length > 1 ? uris.length-2 : uris.length-1;
            String className = uris[indexOf].substring(0, 1).toUpperCase() + uris[indexOf].substring(1);
            String methodName = uris.length > 1 ? uris[uris.length-1] : "index";
            String packageLocation = ".route.";
            for(int i=0; i<indexOf; i++){
                packageLocation += uris[i] + ".";
            }

            // try to find class to handle route
            try{
                // create session
                altHttpRequest.session(true);

                Class<?> clazz = Class.forName(this.clazz.getPackage().getName() + packageLocation + className);
                Object obj = clazz.newInstance();
                Object result = clazz.getMethod(methodName, AltHttpRequest.class, AltHttpResponse.class).invoke(obj, altHttpRequest, altHttpResponse);

                return instance.response(altHttpRequest, altHttpResponse, Alt.STATUS_OK, "", result);
            }catch(Exception e){
                if(e.getClass().equals(ClassNotFoundException.class) || e.getClass().equals(NoSuchMethodException.class)){
                    return instance.response(altHttpRequest, altHttpResponse, Alt.STATUS_NOTFOUND, "URL Not Found", null);
                }else if(e.getClass().equals(InvocationTargetException.class)){
                    if(e.getCause() instanceof AltException){
                        return instance.response(altHttpRequest, altHttpResponse, ((AltException) e.getCause()).getCode(), e.getCause().getMessage(), null);
                    }else{
                        if(this.instance.environment == Alt.ENV_DEVELOPMENT)
                            e.getCause().printStackTrace();

                        return instance.response(altHttpRequest, altHttpResponse, Alt.STATUS_ERROR, instance.environment == Alt.ENV_PRODUCTION ? "Internal Server Error" : e.getCause().getMessage(), null);
                    }
                }else{
                    return instance.response(altHttpRequest, altHttpResponse, Alt.STATUS_ERROR, instance.environment == Alt.ENV_PRODUCTION ? "Internal Server Error" : e.getMessage(), null);
                }
            }
        };

        this.route(url, route);
    }
    public void route(String url, Route route){
        // set all http verb needed
        options(url, route);
        get(url, route);
        post(url, route);
        put(url, route);
        delete(url, route);
    }

    public String response(AltHttpRequest altHttpRequest, AltHttpResponse altHttpResponse, int status, String message, Object res){
        // set time stop
        instance.timestop = System.nanoTime();
        instance.memorystop = runtime.totalMemory() - runtime.freeMemory();

        // set output
        HashMap<String, Object> output = new HashMap<>();
        output.put("s", status);
        output.put("m", message);
        output.put("d", res);

        if(this.environment != Alt.ENV_PRODUCTION) {
            output.put("t", (double) (instance.timestop - instance.timestart) / 1000000000);
            output.put("u", (double) instance.memorystop / (1024 * 1024));
        }

        // enable cors
        altHttpResponse.header("Access-Control-Allow-Origin", "*");
        altHttpResponse.header("Access-Control-Allow-Headers", "Origin, Authorization, X-Requested-With, Content-Type, Accept");
        altHttpResponse.header("Access-Control-Request-Method", "*");

        // surpress response code
        output.put("s", this.surpressResponseCode ? 200 : (Integer) output.get("s"));

        // set http status
        altHttpResponse.status((Integer) output.get("s"));

        try{
            Object data;
            if((Integer) output.get("s") == Alt.STATUS_OK && instance.environment == Alt.ENV_PRODUCTION) {
                data = output.get("d");
            }else if((Integer) output.get("s") != Alt.STATUS_OK && instance.environment == Alt.ENV_PRODUCTION){
                data = output.getOrDefault("m", "Error pada server!");
            }else{
                data = output;
            }

            AltSecure secure = new AltSecure();
            String result = "";
            if(data.getClass().equals(String.class) || data.getClass().equals(Integer.class) || data.getClass().equals(Float.class) || data.getClass().equals(Double.class)){
                result = String.valueOf(data);
            }else {
                result = mapper.writeValueAsString(data);
            }

            return altHttpRequest.secure && Alt.instance().environment == Alt.ENV_PRODUCTION && AltConfig.get("app.secure.key") != null && !AltConfig.get("app.secure.key").equalsIgnoreCase("") && AltConfig.get("app.secure.iv") != null && !AltConfig.get("app.secure.iv").equalsIgnoreCase("") ? secure.encrypt(result) : result;
        }catch (Exception e){
            return "";
        }
    }

    public void start(){
        route("");
        route("/");
        route("/*");
    }

    public void log(int level, String tag){
        AltLog.level   = level;
        AltLog.tag     = tag;
    }

    public static String generate_token(Map<String, String> userdata){
        try {
            return AltJwt.encode(userdata);
        } catch (AltException e) {
            return "";
        }
    }

    public static void clear_token(AltHttpRequest altHttpRequest){
        altHttpRequest.session().removeAttribute("token");
    }

    public static void set_token(String token, AltHttpRequest altHttpRequest){
        altHttpRequest.session().attribute("token", token);
    }

    public static String get_token(AltHttpRequest altHttpRequest){
        String token = altHttpRequest.data.get("token");
        if(token == null){
            String tmp = altHttpRequest.headers("Authorization");
            if(tmp != null && !tmp.equals("")){
                String[] tmp2 = tmp.split(" ");
                if(tmp2.length == 2){
                    token = tmp2[1];
                }
            }
        }
        if(token == null){
            token = altHttpRequest.session().attribute("token");
        }

        return token;
    }

    public static Map<String, String> verify_token(String token) throws AltException {
        return AltJwt.decode(token);
    }

    public static Map<String, String> get_userdata(AltHttpRequest altHttpRequest){
        String token = Alt.get_token(altHttpRequest);

        return Alt.get_userdata(token);
    }
    public static Map<String, String> get_userdata(String token){
        try {
            return AltJwt.decode(token);
        } catch (AltException e) {
            return new HashMap<String, String>(){{
                put("userid", "");
                put("username", "");
            }};
        }
    }

    public static boolean islogin(AltHttpRequest altHttpRequest){
        Map<String, String> userdata = Alt.get_userdata(altHttpRequest);
        return userdata.get("userid") != null && !userdata.get("userid").equals("");
    }

    public static boolean check(int permission, AltHttpRequest altHttpRequest){
        Map<String, String> userdata = Alt.get_userdata(altHttpRequest);
        int level = Integer.valueOf(userdata.get("userlevel"));
        return (level & permission) > 0;
    }

    public static void set_permission(int permission, AltHttpRequest altHttpRequest) throws AltException {
        Map<String, String> userdata = Alt.get_userdata(altHttpRequest);
        String level = userdata.get("userlevel");

        if(level == null || (permission == 0 && !Alt.islogin(altHttpRequest))){
            throw new AltException("Anda belum login atau session anda sudah habis!", Alt.STATUS_UNAUTHORIZED);
        }else if(!Alt.check(permission, altHttpRequest)){
            throw new AltException("Anda tidak berhak untuk mengakses!", Alt.STATUS_FORBIDDEN);
        }
    }
}
