package com.cisco.step.jenkins.plugins.jenkow.identity;

import hudson.security.SecurityRealm;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.interceptor.CommandContext;

import java.util.Collections;
import java.util.List;

/**
 * TODO:
 *
 * Perhaps define just one group and put everyone in it, or define some extension
 * in {@link SecurityRealm}?
 *
 * @author Kohsuke Kawaguchi
 */
public class JenkowGroupQueryImpl extends GroupQueryImpl {
    @Override
    public List<Group> executeList(CommandContext _, Page page) {
        return Collections.emptyList();
    }

    @Override
    public long executeCount(CommandContext _) {
        return executeList(_,null).size();
    }

}
