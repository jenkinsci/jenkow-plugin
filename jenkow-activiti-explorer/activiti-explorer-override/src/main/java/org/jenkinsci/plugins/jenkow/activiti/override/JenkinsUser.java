package org.jenkinsci.plugins.jenkow.activiti.override;

import org.activiti.explorer.identity.LoggedInUser;
import org.jenkinsci.plugins.activiti_explorer.dto.UserDTO;

/**
 * @author Kohsuke Kawaguchi
 */
public class JenkinsUser implements LoggedInUser {
    private final UserDTO dto;

    public JenkinsUser(UserDTO dto) {
        if (dto ==null)
            throw new IllegalArgumentException("No information given");
        this.dto = dto;
    }

    public String getId() {
        return dto.id;
    }

    public String getFirstName() {
        return dto.firstName;
    }

    public String getLastName() {
        return dto.lastName;
    }

    public String getFullName() {
        return dto.fullName;
    }

    public String getPassword() {
        return null;
    }

    public boolean isAdmin() {
        return dto.isAdmin;
    }

    public boolean isUser() {
        return dto.isUser;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LoggedInUser)
            return ((LoggedInUser)obj).getId().equals(getId());
        return false;
    }
}
