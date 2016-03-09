package labs.akhdani.alt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.oreilly.servlet.MultipartRequest;
import labs.akhdani.Alt;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import spark.Session;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

public class AltHttpRequest {
    private static final String TAG = AltHttpRequest.class.getName();

    private spark.Request request;
    private Alt alt;
    public String[] uri;
    public Map<String, String> data = new HashMap<>();
    public Map<String, File> file = new HashMap<>();
    public boolean secure = true;

    public AltHttpRequest(spark.Request request){
        this(request, Alt.instance());
    }

    public AltHttpRequest(spark.Request request, Alt alt){
        this.request = request;
        this.alt = alt;
        this.data = this.data();
        this.secure = Alt.instance().environment == Alt.ENV_PRODUCTION;
    }

    public String[] splat(){
        return this.request.splat();
    }

    public void uris(String[] uri){
        this.uri = uri;
    }

    public Map<String, String> data(){
        HashMap<String, String> data = new HashMap<>();

        // set from query string
        Map<String, String> qs = this.request.params();
        for(Map.Entry<String, String> entry : qs.entrySet()){
            String val = entry.getValue();
            data.put(entry.getKey(), entry.getValue());
        }

        // parse from request body
        String contentType = this.request.headers("Content-Type");
        if(contentType == null) contentType = "";

        String body = "";
        if(contentType.equals("") || contentType.contains("application/json") || contentType.contains("application/x-www-form-urlencoded")){
            body = this.request.body();
            if(Alt.instance().environment == Alt.ENV_PRODUCTION && AltConfig.get("app.secure.key") != null && !AltConfig.get("app.secure.key").equalsIgnoreCase("") && AltConfig.get("app.secure.iv") != null && !AltConfig.get("app.secure.iv").equalsIgnoreCase("") && this.secure && body.length() > 0){
                try {
                    AltSecure secure = new AltSecure();
                    String decrypt = secure.decrypt(body);
                    if(!decrypt.equalsIgnoreCase(""))
                        body = decrypt;
                }catch (Exception e){

                }
            }

            Set<String> qp = this.request.queryParams();
            for(String s : qp){
                data.put(s, this.request.queryParams(s));
                if(body.equalsIgnoreCase("") && Alt.instance().environment == Alt.ENV_PRODUCTION && AltConfig.get("app.secure.key") != null && !AltConfig.get("app.secure.key").equalsIgnoreCase("") && AltConfig.get("app.secure.iv") != null && !AltConfig.get("app.secure.iv").equalsIgnoreCase("") && this.secure && body.length() > 0){
                    try {
                        AltSecure secure = new AltSecure();
                        body = secure.decrypt(s);
                    } catch (Exception e) {

                    }
                }
            }
        }

        // parse json
        if(contentType.contains("application/json")){
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode node = mapper.readTree(body);
                Map<String, Object> res = mapper.convertValue(node, Map.class);
                for(Map.Entry<String, Object> entry : res.entrySet()){
                    data.put(entry.getKey(), (String) entry.getValue());
                }
            } catch (JsonProcessingException e) {
                // nothing to do
            } catch (IOException e) {
                // nothing to do
            }
        }

        // parse form urlencoded
        if(contentType.contains("application/x-www-form-urlencoded")){
            String[] pairs = body.split("\\&");
            for (int i = 0; i < pairs.length; i++) {
                String[] fields = pairs[i].split("=");
                if(fields.length > 0) {
                    try {
                        String key = URLDecoder.decode(fields[0], "UTF-8");
                        String value = URLDecoder.decode(fields[1], "UTF-8");
                        data.put(key, value);
                    } catch (UnsupportedEncodingException e) {
                        // nothing to do
                    } catch (ArrayIndexOutOfBoundsException e){
                        // nothing to do
                    }
                }
            }
        }

        // parse multipart using apache common uploads
        if(contentType.contains("multipart/form-data") || ServletFileUpload.isMultipartContent(request.raw())){
            try{
                final File uploadDir = new File(alt.staticFolder);
                if (!uploadDir.exists() && !uploadDir.mkdirs()) {
                    throw new RuntimeException("Failed to create directory " + uploadDir.getAbsolutePath());
                }

                // Create a factory for disk-based file items
                DiskFileItemFactory factory = new DiskFileItemFactory();

                // Set factory constraints
                factory.setSizeThreshold(alt.maxPostSize);
                factory.setRepository(new File(alt.staticFolder));

                // Create a new file upload handler
                ServletFileUpload upload = new ServletFileUpload(factory);

                // Parse the request
                List<FileItem> items = upload.parseRequest(request.raw());

                // Process the uploaded items
                Iterator<FileItem> iter = items.iterator();
                while (iter.hasNext()) {
                    FileItem item = iter.next();
                    String key = item.getFieldName();

                    if (item.isFormField()) {
                        String value = item.getString();
                        data.put(key, value);
                    } else {
                        String fileName = item.getName();

                        File uploadedFile = new File(uploadDir + "/" + fileName);
                        item.write(uploadedFile);

                        this.file.put(key, uploadedFile);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        // parse multipart using o'reilly
        /*if(contentType.contains("multipart/form-data")) {
            try {
                final File upload = new File(alt.staticFolder);
                if (!upload.exists() && !upload.mkdirs()) {
                    throw new RuntimeException("Failed to create directory " + upload.getAbsolutePath());
                }
                // this dumps all files contained in the multipart request to target directory.
                MultipartRequest req = new MultipartRequest(request.raw(), upload.getAbsolutePath(), Alt.instance().maxPostSize);
                Enumeration<String> params = req.getParameterNames();
                while(params.hasMoreElements()){
                    String key = params.nextElement();
                    String value = req.getParameter(key);
                    data.put(key, value);
                }

                // this dumps all files contained in the multipart request to target directory.
                Enumeration<String> params2 = req.getFileNames();
                while(params.hasMoreElements()){
                    String key = params.nextElement();
                    File value = req.getFile(key);
                    this.file.put(key, value);
                }
            } catch (IOException e) {
                // nothing to do
                e.printStackTrace();
            }
        }*/

        return data;
    }

    public void session(boolean iscreate){
        this.request.session(iscreate);
    }
    public Session session(){
        return this.request.session();
    }
    public String ip(){
        return this.request.ip();
    }
    public String useragent(){
        return this.request.userAgent();
    }
    public String params(String name){
        return this.request.params(name);
    }
    public Set<String> headers(){
        return this.request.headers();
    }
    public String headers(String name){
        return this.request.headers(name);
    }
}
