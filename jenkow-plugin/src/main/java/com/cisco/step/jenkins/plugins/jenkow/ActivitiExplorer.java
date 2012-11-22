package com.cisco.step.jenkins.plugins.jenkow;

import com.cloudbees.vietnam4j.ProxiedWebApplication;
import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import hudson.util.MaskingClassLoader;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

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
            webApp = new ProxiedWebApplication(new File("/home/kohsuke/Downloads/activiti-explorer.war"),
                    req.getContextPath()+'/'+getUrlName());
            webApp.setParentClassLoader(HttpServletRequest.class.getClassLoader());

            try {
                webApp.start();
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
        return webApp;
    }
}
