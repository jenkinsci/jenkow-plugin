package org.jenkinsci.plugins.jenkow.activiti.override;

import org.activiti.explorer.ui.login.LoginHandler;
import org.activiti.explorer.identity.LoggedInUser;
import org.jenkinsci.plugins.activiti_explorer.dto.UserDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class JenkinsLoginHandler implements LoginHandler {
    /**
     * Authenticate the user with the given username and given password.
     *
     * @return the logged in user. Return null of authentication failed.
     */
    public LoggedInUser authenticate(String userName, String password) {
        throw new UnsupportedOperationException();
    }

    /**
     * Authenticate the current user. Use this to eg. shared autentication,
     * which can be done without the user actually having to provide
     * credentials.
     *
     * @return The logged in user. Return null, if no user can be logged in
     * automatically. When null is returned, user will be requested to provide
     * credentials.
     */
    public LoggedInUser authenticate(HttpServletRequest request, HttpServletResponse response) {
        return new JenkinsUser((UserDTO)request.getSession().getAttribute("jenkins.user"));
    }

    /**
     * Called when the user is logged out, should clear all context related
     * to authorization and authentication for the current logged in user.
     */
    public void logout(LoggedInUser userTologout) {

    }

    /**
     * Called when request started. Allows eg. validating of authentication or
     * renewing.
     */
    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {}

    /**
     * Called when request started. Allows eg. validating of authentication or
     * renewing.
     */
    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {}

}
