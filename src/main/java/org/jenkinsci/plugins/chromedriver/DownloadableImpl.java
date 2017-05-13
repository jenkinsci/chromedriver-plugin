package org.jenkinsci.plugins.chromedriver;

import hudson.Extension;
import hudson.FilePath;
import hudson.ProxyConfiguration;
import hudson.model.DownloadService.Downloadable;
import hudson.model.TaskListener;
import hudson.util.AtomicFileWriter;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Downloads chromedriver installation data.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class DownloadableImpl extends Downloadable {
    public DownloadableImpl() {
        super("org.jenkins-ci.plugins.chromedriver.ChromeDriver");
    }

    /**
     * If the package isn't downloaded yet, download it and return its local cache.
     */
    public File resolve(String osName, String sunArchDataModel, TaskListener listener) throws IOException {
        URL url = select(osName, sunArchDataModel);
        File f = getLocalCacheFile(url);
        if (f.exists()) return f;
        
        // download to a temporary file and rename it in to handle concurrency and failure correctly,
        listener.getLogger().println("Downloading "+url);
        File tmp = new File(f.getPath()+".tmp");
        tmp.getParentFile().mkdirs();
        try {
            FileOutputStream out = new FileOutputStream(tmp);
            try {
                IOUtils.copy(ProxyConfiguration.open(url).getInputStream(), out);
            } finally {
                IOUtils.closeQuietly(out);
            }

            tmp.renameTo(f);
            return f;
        } finally {
            tmp.delete();
        }
    }
    
    private File getLocalCacheFile(URL src) {
        String s = src.toExternalForm();
        String fileName = s.substring(s.lastIndexOf('/')+1);
        return new File(Jenkins.getInstance().getRootDir(),"cache/chromedriver/"+fileName);
    }
    
    /**
     * Selects the right binary to download and returns its URL.
     *
     * @param osName
     *      Value of the osName system property.
     * @param sunArchDataModel
     *      Value of "sun.arch.data.model" system property.
     */
    public URL select(String osName, String sunArchDataModel) throws IOException {
        JSONObject d = getData();
        if (d==null)    throw new IOException("No installation data is downloaded from chromedriver yet");
        JSONArray list = d.optJSONArray("list");
        if (list==null) throw new IOException("Malformed chromedriver installation data");
        
        String type = getType(osName,sunArchDataModel);
        if (type==null)
            throw new IOException("Couldn't pick the right chromedriver binary for "+osName+","+sunArchDataModel);
        
        for (JSONObject o : (List<JSONObject>)(List)list) {
            if (type.equals(o.optString("id")))
                return new URL(o.optString("url"));
        }

        throw new IOException("No matching binary found for type="+type);
    }
    
    private String getType(String osName, String sunArchDataModel) {
        if (osName.contains("Mac") || osName.contains("Darwin")) {
            if (sunArchDataModel.equals("32"))
                return "mac32";
            if (sunArchDataModel.equals("64"))
                return "mac64";
            return "mac";
        }
        
        if (osName.contains("Windows"))
            return "win";

        if (osName.contains("Linux")) {
            if (sunArchDataModel.equals("32"))
                return "linux32";
            if (sunArchDataModel.equals("64"))
                return "linux64";
        }

        return null;
    }
}
