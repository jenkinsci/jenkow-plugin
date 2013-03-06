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
