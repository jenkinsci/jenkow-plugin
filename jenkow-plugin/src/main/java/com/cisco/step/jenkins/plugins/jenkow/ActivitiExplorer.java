package com.cisco.step.jenkins.plugins.jenkow;

import com.cisco.step.jenkins.plugins.jenkow.JenkowBuilder.DescriptorImpl;
import com.cloudbees.vietnam4j.ProxiedWebApplication;
import hudson.Extension;
import hudson.Util;
import hudson.model.UnprotectedRootAction;
import jenkins.model.Jenkins;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXWriter;
import org.dom4j.io.XMLWriter;
import org.jenkinsci.plugins.jenkow.activiti.override.ServletContextDataSource;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Activiti Explorer web application embedded inside Jenkins.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class ActivitiExplorer implements UnprotectedRootAction {
    private ProxiedWebApplication webApp;

    @Inject
    DescriptorImpl descriptor;

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

    /**
     * Extracts a war file into the specified directory.
     */
    private void extract(URL war, File dir) throws IOException {
        if (dir.exists())
            Util.deleteRecursive(dir);

        JarInputStream jar = new JarInputStream(war.openStream());
        try {
            JarEntry e;
            while ((e=jar.getNextJarEntry())!=null) {
                File dst = new File(dir,e.getName());
                if (e.isDirectory())
                    dst.mkdirs();
                else {
                    dst.getParentFile().mkdirs();
                    FileOutputStream out = new FileOutputStream(dst);
                    try {
                        IOUtils.copy(jar,out);
                    } finally {
                        out.close();
                    }

                    if (e.getTime()>=0)
                        dst.setLastModified(e.getTime());
                }
            }
        } finally {
            jar.close();
        }
    }

    /**
     * Patch activiti-explorer war file so that we can inject our stuff into it.
     */
    private void patch(File war) throws DocumentException, IOException {
        File xml = new File(war,"WEB-INF/applicationContext.xml");
        Document dom = new SAXReader().read(xml);
        Element ds = (Element)dom.selectSingleNode("/*/*[@id='dataSource']");
        if (ds==null)
            throw new IllegalStateException("Can't find the dataSource bean in "+xml);
        ds.elements().clear();
        ds.addAttribute("class", ServletContextDataSource.class.getName());
        FileOutputStream out = new FileOutputStream(xml);
        try {
            new XMLWriter(out, OutputFormat.createPrettyPrint()).write(dom);
        } finally {
            out.close();
        }
    }

    private synchronized ProxiedWebApplication getProxyWebApplication(StaplerRequest req) throws ServletException {
        if (webApp==null) {
            try {
                final ClassLoader ourLoader = getClass().getClassLoader();

                File war = new File(Jenkins.getInstance().getRootDir(), "cache/activiti-explorer");
                extract(ourLoader.getResource("activiti-explorer.war"),war);
                patch(war);
                webApp = new ProxiedWebApplication(
                        war,
                        req.getContextPath()+'/'+getUrlName());

                webApp.addClassPath(ourLoader.getResource("activiti-explorer-override.jar"));
                // inject DataSource
                webApp.getProxiedServletContext().setAttribute(ServletContextDataSource.class.getName(),
                        descriptor.getDatabase().getDataSource());

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
