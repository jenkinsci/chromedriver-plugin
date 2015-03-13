package org.jenkinsci.plugins.chromedriver;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;

import java.io.IOException;

/**
 * Modifies PATH to include chromedriver
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class EnvironmentContributorImpl extends EnvironmentContributor {
    @Override
    public void buildEnvironmentFor(Run r, EnvVars envs, TaskListener listener) throws IOException, InterruptedException {
        Computer c = Computer.currentComputer();
        if(c != null) {
          FilePath path = c.getNode().getRootPath().child(ComputerListenerImpl.INSTALL_DIR);
          envs.put("PATH+CHROMEDRIVER",path.getRemote());
        }
    }
}
