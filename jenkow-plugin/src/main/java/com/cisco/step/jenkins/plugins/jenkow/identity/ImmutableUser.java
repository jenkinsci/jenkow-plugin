/*
 * The MIT License
 * 
 * Copyright (c) 2012, Cisco Systems, Inc., Max Spring
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.cisco.step.jenkins.plugins.jenkow.identity;

import hudson.Util;
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
        return Util.fixNull(firstName);
    }

    public void setFirstName(String firstName) {
        throw new UnsupportedOperationException();
    }

    public void setLastName(String lastName) {
        throw new UnsupportedOperationException();
    }

    public String getLastName() {
        return Util.fixNull(lastName);
    }

    public void setEmail(String email) {
        throw new UnsupportedOperationException();
    }

    public String getEmail() {
        return Util.fixNull(email);
    }

    public String getPassword() {
        return "";
    }

    public void setPassword(String string) {
        throw new UnsupportedOperationException();
    }
}
