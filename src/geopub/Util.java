/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geopub;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author Robert
 */
public class Util {

    private JSONParser parser = new JSONParser();
    private JSONObject jsonObject;
    private JSONArray array;
    private JSONObject geoserver;
    private File settingsFile;

    public Util(String filename) {
        settingsFile = new File(filename);
        try {
            jsonObject = (JSONObject) parser.parse(new FileReader(settingsFile));
            array = (JSONArray) jsonObject.get("geoserver");
            geoserver = (JSONObject) array.get(0);
        } catch (IOException | org.json.simple.parser.ParseException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getHostUrl() {
        String host = (String) geoserver.get("host");
        return host;
    }

    public long getPort() {
        long port = (long) geoserver.get("port");
        return port;
    }

    public String getUser() {
        String user = (String) geoserver.get("user");
        return user;
    }

    public String getPassword() {
        String password = (String) geoserver.get("password");
        return password;
    }

    public String getWorkSpace() {
        String workspace = (String) geoserver.get("workspace");
        return workspace;
    }

    public String getProjection() {
        String projection = (String) geoserver.get("projection");
        return projection;
    }

    public String getStyle() {
        String style = (String) geoserver.get("style");
        return style;
    }

    public String getWorkingDirectory() {
        String dir = (String) jsonObject.get("working_dir");
        return dir;
    }

    public String getAuthor() {
        return (String) jsonObject.get("developer");
    }

    public boolean isRunnable() {
        if (!getAuthor().equalsIgnoreCase("robertohuru@gmail.com")) {
            return false;
        }
        String expiry = "31/02/2032";
        Date expiryDate = new Date();
        try {
            expiryDate = new SimpleDateFormat("dd/MM/yyyy").parse(expiry);
        } catch (ParseException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        Date today = new Date();
        return today.before(expiryDate);
    }

}
