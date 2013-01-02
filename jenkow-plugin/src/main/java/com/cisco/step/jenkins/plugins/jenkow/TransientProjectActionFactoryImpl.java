package com.cisco.step.jenkins.plugins.jenkow;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Project;
import hudson.model.TransientProjectActionFactory;
import hudson.tasks.Builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Adds {@link JenkowWorkflowPicture}s as appropriate to the UI.
 *
 * <p>
 * {@link Builder#getProjectActions(AbstractProject)} is bit painful because
 * we want to handle index-based image generation, yet builders don't know their indices.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class TransientProjectActionFactoryImpl extends TransientProjectActionFactory {
    @Override
    public Collection<? extends Action> createFor(AbstractProject target) {
        List<JenkowBuilder> builders = null;

        if (target instanceof Project) {
            for (Builder b : ((Project<?,?>) target).getBuilders()) {
                if (b instanceof JenkowBuilder) {
                    if (builders==null)    builders = new ArrayList<JenkowBuilder>();
                    builders.add((JenkowBuilder) b);
                }
            }
        }
        return builders==null ? Collections.<Action>emptyList() :
                Collections.singletonList(new JenkowWorkflowPicture(builders));
    }
}
