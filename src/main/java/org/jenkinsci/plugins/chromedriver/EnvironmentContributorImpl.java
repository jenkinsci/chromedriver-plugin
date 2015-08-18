package org.jenkinsci.plugins.chromedriver;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Modifies PATH to include chromedriver
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class EnvironmentContributorImpl extends EnvironmentContributor {
    private static final Logger LOGGER = Logger.getLogger(EnvironmentContributorImpl.class.getName());

    @Override
    public void buildEnvironmentFor(Run r, EnvVars envs, TaskListener listener) throws IOException, InterruptedException {
        Computer c = Computer.currentComputer();
        if (c == null) {
        	// not an executor, so no need to populate environment
        	return;
        }
        if (c.getNode() == null) {
        	LOGGER.warning("Build node for computer is gone!");
        	return;
        }
        if (c.getNode().getRootPath() == null) {
        	LOGGER.warning("Build node is online, failed to resolve root path!");
        	return;
        }
        FilePath path = c.getNode().getRootPath().child(ComputerListenerImpl.INSTALL_DIR);
        envs.put("PATH+CHROMEDRIVER",path.getRemote());
    }
}
