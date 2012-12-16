package com.cisco.step.jenkins.plugins.jenkow.identity;

import hudson.tasks.Mailer.UserProperty;
import org.activiti.engine.identity.User;

/**
 * @author Kohsuke Kawaguchi
 */
class ImmutableUser implements User {
    private final String id,firstName,lastName,email;

    ImmutableUser(String id, String firstName, String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public ImmutableUser(hudson.model.User u) {
        this.id = u.getId();
        String f = u.getFullName();

        // RANT: first name & last name considered harmful. See http://www.w3.org/International/questions/qa-personal-names
        int idx = f.lastIndexOf(' ');
        if (idx>0) {
            // arbitrarily split a name into two portions. this is totally error prone,
            // but at least it works when the name consists of two tokens
            firstName = f.substring(0,idx);
            lastName = f.substring(idx+1);
        } else {
            // there's really nothing sane we can do. bail out.
            firstName = u.getFullName();
            lastName = "";
        }

        UserProperty m = u.getProperty(UserProperty.class);
        email = m != null ? m.getAddress() : "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        throw new UnsupportedOperationException();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        throw new UnsupportedOperationException();
    }

    public void setLastName(String lastName) {
        throw new UnsupportedOperationException();
    }

    public String getLastName() {
        return lastName;
    }

    public void setEmail(String email) {
        throw new UnsupportedOperationException();
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return "";
    }

    public void setPassword(String string) {
        throw new UnsupportedOperationException();
    }
}
