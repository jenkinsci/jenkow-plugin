package com.cisco.step.jenkins.plugins.jenkow;

import com.cloudbees.vietnam4j.ProxiedWebApplication;
import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Kohsuke Kawaguchi
 */
@Extension
public class ActivitiExplorer implements UnprotectedRootAction {
    private ProxiedWebApplication webApp;

    @Override
    public String getIconFileName() {
        return "setting.png";
    }

    @Override
    public String getDisplayName() {
        return "Activiti Explorer";
    }

    @Override
    public String getUrlName() {
        return "activiti-explorer";
    }

    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException {
        ProxiedWebApplication webApp = getProxyWebApplication(req);
        webApp.handleRequest(req, rsp);
    }

    private synchronized ProxiedWebApplication getProxyWebApplication(StaplerRequest req) throws ServletException {
        if (webApp==null) {
            try {
                final ClassLoader ourLoader = getClass().getClassLoader();

                webApp = new ProxiedWebApplication(
                        ourLoader.getResource("activiti-explorer.war"),
                        req.getContextPath()+'/'+getUrlName());

                webApp.addClassPath(ourLoader.getResource("activiti-explorer-override.jar"));

                // pass through to the servlet container so that Activiti won't get confused
                // by what Jenkins loads (e.g., log4j version inconsistency)
                // but we do expose some selected resources to control its behavior.
                webApp.setParentClassLoader(HttpServletRequest.class.getClassLoader());

                webApp.start();
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
        return webApp;
    }
}
