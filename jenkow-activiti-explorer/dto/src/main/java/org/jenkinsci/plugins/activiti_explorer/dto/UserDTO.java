package org.jenkinsci.plugins.activiti_explorer.dto;

/**
 * @author Kohsuke Kawaguchi
 */
public class UserDTO {
    public String id, firstName, lastName, fullName, email;

    public boolean isAdmin, isUser;
}
