package org.jenkinsci.plugins.activiti_explorer;

import com.cisco.step.jenkins.plugins.jenkow.JenkowBuilder.DescriptorImpl;
import com.cisco.step.jenkins.plugins.jenkow.JenkowEngine;
import com.cloudbees.vietnam4j.ProxiedWebApplication;
import hudson.Extension;
import hudson.Util;
import hudson.model.UnprotectedRootAction;
import hudson.model.User;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.activiti.engine.ProcessEngine;
import org.apache.commons.io.IOUtils;
import org.dom4j.DocumentException;
import org.jenkinsci.plugins.activiti_explorer.dto.UserDTO;
import org.jenkinsci.plugins.jenkow.activiti.override.JenkinsProcessEngineFactory;
import org.jenkinsci.plugins.jenkow.activiti.override.ServletContextDataSource;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
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

    public String getIconFileName() {
        return "/plugin/activiti-explorer/images/24x24/activiti.png";
    }

    public String getDisplayName() {
        return "Activiti Explorer";
    }

    public String getUrlName() {
        return "activiti-explorer";
    }

    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException {
        ProxiedWebApplication webApp = getProxyWebApplication(req);

        HttpSession session = req.getSession();
        HttpSession ps = webApp.getProxiedSession(session);
        UserDTO oldUser = (UserDTO)ps.getAttribute("jenkins.user");
        UserDTO newUser = createUserInfo();
        if (!mapToId(oldUser).equals(mapToId(newUser))) {
            // force a new session. AE isn't designed to anticipate the user change without invalidating a session,
            // but Jenkins does that. So when we see that the user has changed in Jenkins, force a new session
            // (but only in AE.)
            webApp.clearProxiedSession(session);
            ps = webApp.getProxiedSession(session);
        }

        ps.setAttribute("jenkins.user", newUser);


        webApp.handleRequest(req, rsp);
    }

    private String mapToId(UserDTO o) {
        return o==null ? "\u0000" : o.id;
    }

    /**
     * Creates {@link UserDTO} that represents the currently logged-in user.
     */
    private UserDTO createUserInfo() {
        Authentication a = Jenkins.getAuthentication();
        User u = User.current();

        UserDTO user = new UserDTO();
        user.id = a.getName();
        user.firstName = u != null ? u.getFullName() : a.getName();
        user.lastName = "";
        user.fullName = u != null ? u.getFullName() : a.getName();
        user.isAdmin = Jenkins.getInstance().getACL().hasPermission(a,Jenkins.ADMINISTER);
        user.isUser = true;

        return user;
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
                        IOUtils.copy(jar, out);
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
        new XmlPatcher(new File(war,"WEB-INF/activiti-standalone-context.xml")) {
            public void patch() {
                // patch data source in
                overrideBeanTo("dataSource", ServletContextDataSource.class.getName());

                // single sign-on
                // can't load the class yet, so string
                overrideBeanTo("activitiLoginHandler", "org.jenkinsci.plugins.jenkow.activiti.override.JenkinsLoginHandler");

                // inject our own fully configured ProcessEngine
                overrideBeanTo("processEngine", JenkinsProcessEngineFactory.class.getName());

                // no more demo data generation
                removeBean("demoDataGenerator");
            }
        };
        new XmlPatcher(new File(war,"WEB-INF/activiti-ui-context.xml")) {
            public void patch() {
                // tweak the navigation bar
                overrideBeanTo("componentFactories", "org.jenkinsci.plugins.jenkow.activiti.override.JenkinsComponentFactories");

                swapClass("explorerApp","org.jenkinsci.plugins.jenkow.activiti.override.ExplorerApp2");
            }
        };
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

                webApp.setParentLoaderHasPriority(true);

                webApp.addClassPath(ourLoader.getResource("activiti-explorer-override.jar"));
                // inject DataSource
                webApp.getProxiedServletContext().setAttribute(ServletContextDataSource.class.getName(),
                        descriptor.getDatabase().getDataSource());
                webApp.getProxiedServletContext().setAttribute(ProcessEngine.class.getName(),
                        JenkowEngine.getEngine());

                // pass through to the servlet container so that Activiti won't get confused
                // by what Jenkins loads (e.g., log4j version inconsistency)
                // but we do expose DTO classes to share them between two apps.
                webApp.setParentClassLoader(new URLClassLoader(new URL[0],HttpServletRequest.class.getClassLoader()) {
                    @Override
                    protected Class<?> findClass(String name) throws ClassNotFoundException {
                        if (name.startsWith("org.jenkinsci.plugins.activiti_explorer.dto.")
                         || name.startsWith("org.activiti.engine.")) {
                            return getClass().getClassLoader().loadClass(name);
                        }
                        throw new ClassNotFoundException(name);
                    }
                });

                webApp.start();
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
        return webApp;
    }
}
