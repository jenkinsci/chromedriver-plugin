package org.jenkinsci.plugins.chromedriver;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;
import hudson.slaves.ComputerListener;
import hudson.util.RemotingDiagnostics;
import jenkins.model.Jenkins;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Auto-installs chromedriver upon connection.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class ComputerListenerImpl extends ComputerListener {
    @Inject
    private DownloadableImpl downloadable;

    @Override
    public void onOnline(Computer c, TaskListener listener) throws IOException, InterruptedException {
        if (c.getNode()== Jenkins.getInstance())    // work around the bug where master doesn't call preOnline method
            process(c, c.getChannel(), c.getNode().getRootPath(), listener);
    }

    @Override
    public void preOnline(Computer c, Channel channel, FilePath root, TaskListener listener) throws IOException, InterruptedException {
        process(c, channel, root,listener);
    }

    public void process(Computer c, VirtualChannel channel, FilePath root, TaskListener listener) throws IOException, InterruptedException {
        try {
            FilePath remoteDir = root.child(INSTALL_DIR);
            if (!remoteDir.child("chromedriver").exists()) {
                listener.getLogger().println("Installing chromedriver to "+remoteDir);
                Map<Object,Object> props = RemotingDiagnostics.getSystemProperties(channel);
                        File zip = downloadable.resolve((String) props.get("os.name"), (String) props.get("sun.arch.data.model"), listener);
                remoteDir.mkdirs();
                new FilePath(zip).unzip(remoteDir);
            }
        } catch (IOException e) {
            e.printStackTrace(listener.error("Failed to install chromedriver"));
            // but continueing
        }
    }

    public static final String INSTALL_DIR = "tools/chromedriver";
}
