/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geopub;

import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Robert
 */
public class GeoPub {

    GeoServerRESTPublisher geoServerRESTPublisher;
    GeoServerRESTReader geoServerRESTReader;

    public GeoPub(String url, String user, String password) {
        geoServerRESTPublisher = new GeoServerRESTPublisher(url, user, password);
        try {
            geoServerRESTReader = new GeoServerRESTReader(url, user, password);
        } catch (MalformedURLException ex) {
            Logger.getLogger(GeoPub.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean createWorkSpace(String workspace) {
        boolean result = false;
        if (!geoServerRESTReader.existsWorkspace(workspace)) {
            result = geoServerRESTPublisher.createWorkspace(workspace);
        } else {
            System.out.println("WorkSpace Exist");
        }
        return result;
    }

    public void publishVector(String workspace, String storename, String layer, File inputMap, String georef, String style) {
        try {
            if (geoServerRESTReader.existsDatastore(workspace, storename)) {
                geoServerRESTPublisher.removeDatastore(workspace, storename, true);
            }
            geoServerRESTPublisher.publishShp(workspace, storename, layer, inputMap, georef, style);
        } catch (Exception ex) {
            Logger.getLogger(GeoPub.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void publishRaster(String workspace, String storename, String layer, File inputMap, String georef, String style) {
        try {
            if (geoServerRESTReader.existsCoverage(workspace, storename, layer)) {
                geoServerRESTPublisher.removeCoverageStore(workspace, storename);
            }
            geoServerRESTPublisher.publishGeoTIFF(workspace, storename, layer, inputMap, georef, GSResourceEncoder.ProjectionPolicy.REPROJECT_TO_DECLARED, style, null);

        } catch (Exception ex) {
            Logger.getLogger(GeoPub.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean publishStyle(File styleFile, String name) {
        return geoServerRESTPublisher.publishStyle(styleFile, name);

    }

    public boolean workspaceExist(String workspace) {
        boolean result = false;
        if (geoServerRESTReader.existsWorkspace(workspace)) {
            result = true;
        }
        return result;
    }

    public boolean removeWorkSpace(String workspace) {
        boolean result = false;
        if (geoServerRESTReader.existsWorkspace(workspace)) {
            result = geoServerRESTPublisher.removeWorkspace(workspace, true);
        } else {
            System.out.println("WorkSpace Does not Exist");
        }
        return result;
    }

    public static void maris(String[] args) {
        String workspace = args[0];
        String dataFolder = args[1];
        File folder = new File(dataFolder);
        String style = args[2];
        String projection = args[3];
        String configFile = args[4];
        Util util = new Util(configFile);
        if (util.isRunnable()) {
            GeoPub geo = new GeoPub(util.getHostUrl(), util.getUser(), util.getPassword());
            boolean isStyle = (args.length == 7);
            if (isStyle) {
                if (folder.isDirectory()) {
                    File files[] = folder.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        String ext = FilenameUtils.getExtension(files[i].getAbsolutePath());
                        if (ext.equalsIgnoreCase("sld")) {
                            String styleName = FilenameUtils.getBaseName(files[i].getAbsolutePath());
                            geo.publishStyle(files[i], styleName);
                        }
                    }
                } else if (folder.isFile()) {
                    File file = folder;
                    String ext = FilenameUtils.getExtension(file.getAbsolutePath());
                    if (ext.equalsIgnoreCase("sld")) {
                        String styleName = FilenameUtils.getBaseName(file.getAbsolutePath());
                        geo.publishStyle(file, styleName);
                    }
                }
            } else {
                if (args[5].equalsIgnoreCase("Yes")) {
                    geo.removeWorkSpace(workspace);
                    geo.createWorkSpace(workspace);
                } else if (args[5].equalsIgnoreCase("No")) {
                    if (geo.workspaceExist(workspace) == false) {
                        geo.createWorkSpace(workspace);
                    }
                }
                if (folder.isDirectory()) {
                    File files[] = folder.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        String ext = FilenameUtils.getExtension(files[i].getAbsolutePath());
                        if (ext.equalsIgnoreCase("tif")) {
                            String layer = FilenameUtils.getBaseName(files[i].getAbsolutePath());
                            if (layer.contains("_tamsat")) {
                                int value = Integer.parseInt(layer.replaceAll("[^0-9]", ""));
                                layer = "rainfall_map_" + value;
                            }
                            geo.publishRaster(args[0], layer, layer, files[i], projection, style);

                        }
                    }
                } else if (folder.isFile()) {
                    File file = folder;
                    String ext = FilenameUtils.getExtension(file.getAbsolutePath());
                    if (ext.equalsIgnoreCase("tif")) {
                        String layer = FilenameUtils.getBaseName(file.getAbsolutePath());
                        if (layer.contains("_tamsat")) {
                            int value = Integer.parseInt(layer.replaceAll("[^0-9]", ""));
                            layer = "rainfall_map_" + value;
                        }
                        geo.publishRaster(args[0], layer, layer, file, projection, style);
                    } else if (ext.equalsIgnoreCase("zip")) {
                        String layer = FilenameUtils.getBaseName(file.getAbsolutePath());
                        geo.publishVector(args[0], layer, layer, file, projection, style);
                    }
                }

            }
        }else{
            System.err.println("Incompatibility with your Java version? Please contact the developer at robertohuru@gmail.com");
        }

    }

    public static void run(String[] args) {
        String configFile = args[0];
        Util util = new Util(configFile);
        File dataFolder = new File(util.getWorkingDirectory());
        String workSpace = util.getWorkSpace();
        String style = util.getStyle();
        String projection = util.getProjection();

        GeoPub geo = new GeoPub(util.getHostUrl(), util.getUser(), util.getPassword());
        if (geo.workspaceExist(workSpace) == false) {
            geo.createWorkSpace(workSpace);
        }
        if (dataFolder.isDirectory()) {
            File files[] = dataFolder.listFiles();
            for (int i = 0; i < files.length; i++) {
                String ext = FilenameUtils.getExtension(files[i].getAbsolutePath());
                if (ext.equalsIgnoreCase("tif")) {
                    String layer = FilenameUtils.getBaseName(files[i].getAbsolutePath());
                    geo.publishRaster(workSpace, layer, layer, files[i], projection, style);
                }
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //run(args);
        maris(args);
    }

}
