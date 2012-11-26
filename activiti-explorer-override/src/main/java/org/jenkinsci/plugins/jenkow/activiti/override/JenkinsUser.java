package org.jenkinsci.plugins.jenkow.activiti.override;

import org.activiti.explorer.identity.LoggedInUser;

import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class JenkinsUser implements LoggedInUser {
    private final Map bag;

    public JenkinsUser(Map bag) {
        if (bag==null)
            throw new IllegalArgumentException("No information given");
        this.bag = bag;
    }

    public String getId() {
        return (String)bag.get("id");
    }

    public String getFirstName() {
        return (String)bag.get("firstName");
    }

    public String getLastName() {
        return (String)bag.get("lastName");
    }

    public String getFullName() {
        return (String)bag.get("fullName");
    }

    public String getPassword() {
        return (String)bag.get("password");
    }

    public boolean isAdmin() {
        return bag.containsKey("admin");
    }

    public boolean isUser() {
        return bag.containsKey("user");
    }
}
