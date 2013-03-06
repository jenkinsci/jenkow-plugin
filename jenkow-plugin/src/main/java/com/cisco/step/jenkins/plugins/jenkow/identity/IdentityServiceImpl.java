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

import jenkins.model.Jenkins;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.identity.Account;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@link IdentityService} backed by Jenkins.
 *
 * This is still a work in progress. Requires UserQuery and GroupQuery implementation.
 *
 * @author Kohsuke Kawaguchi
 */
public class IdentityServiceImpl implements IdentityService {
    private final Jenkins jenkins = Jenkins.getInstance();

    @Override
    public User newUser(String userId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveUser(User user) {
        // no-op
    }

    @Override
    public UserQuery createUserQuery() {
        if (jenkins.isUseSecurity())
            return new JenkowUserQueryImpl(jenkins.getSecurityRealm());
        else
            return new AnonymousUserQueryImpl();
    }

    @Override
    public void deleteUser(String userId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Group newGroup(String groupId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GroupQuery createGroupQuery() {
        return new JenkowGroupQueryImpl();
    }

    @Override
    public void saveGroup(Group group) {
        // no-op
    }

    @Override
    public void deleteGroup(String groupId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createMembership(String userId, String groupId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteMembership(String userId, String groupId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean checkPassword(String userId, String password) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAuthenticatedUserId(String authenticatedUserId) {
    }

    @Override
    public void setUserPicture(String userId, Picture picture) {
        // no-op
    }

    @Override
    public Picture getUserPicture(String userId) {
        return null;
    }

    @Override
    public void setUserInfo(String userId, String key, String value) {
        // no-op
        // TODO: possibly support this by storing stuff as UserProperty on Jenkins
    }

    @Override
    public String getUserInfo(String userId, String key) {
        return null;
    }

    @Override
    public List<String> getUserInfoKeys(String userId) {
        return Collections.emptyList();
    }

    @Override
    public void deleteUserInfo(String userId, String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setUserAccount(String userId, String userPassword, String accountName, String accountUsername, String accountPassword, Map<String, String> accountDetails) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getUserAccountNames(String userId) {
        return Collections.singletonList(userId);
    }

    @Override
    public Account getUserAccount(final String userId, final String userPassword, final String accountName) {
        return new Account() {
            @Override
            public String getName() {
                return userId;
            }

            @Override
            public String getUsername() {
                return userId;
            }

            @Override
            public String getPassword() {
                return userPassword;
            }

            @Override
            public Map<String, String> getDetails() {
                return Collections.emptyMap();
            }
        };
    }

    @Override
    public void deleteUserAccount(String userId, String accountName) {
        throw new UnsupportedOperationException();
    }

}
