package org.jenkinsci.plugins.jenkow.activiti.override;

import org.activiti.explorer.ExplorerApp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Kohsuke Kawaguchi
 */
public class ExplorerApp2 extends ExplorerApp {
    @Override
    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
        // AE caches the logged in user into a session, which means it won't notice when the user logs out in Jenkins
        // or logs in. Our JenkinsLoginHandler is efficient, no need to cache this.
        setUser(null);

        super.onRequestStart(request, response);
    }
}
