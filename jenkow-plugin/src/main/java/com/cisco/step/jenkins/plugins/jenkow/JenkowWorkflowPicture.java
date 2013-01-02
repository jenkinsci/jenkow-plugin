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

import hudson.model.Action;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Exposes workflow images at the project level.
 *
 * @see TransientProjectActionFactoryImpl
 */
public class JenkowWorkflowPicture implements Action {
    private final List<JenkowBuilder> builders;

    public JenkowWorkflowPicture(List<JenkowBuilder> builders) {
        this.builders = builders;
    }

    // invoked with /job/${jobname}/`getUrlName`/graph
    public WorkflowDiagram doGraph(StaplerRequest req) {
        String rest = req.getRestOfPath();
        if (rest.length()==0)
            return builders.get(0).getWorkflowDiagram();

        String id = rest.substring(1).split("/")[0];  // rest is something like "/FOO"

        try {
            // is this a number?
            int i = Integer.parseInt(id);
            if (0<=i && i<builders.size())
                return builders.get(i).getWorkflowDiagram();
        } catch (NumberFormatException e) {
            // if not is this a workflow name?
            for (JenkowBuilder b : builders) {
                if (b.getWorkflowName().equals(id))
                    return b.getWorkflowDiagram();
            }
        }

        throw HttpResponses.error(404,"No such workflow: "+id);
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return "jenkow";
    }
}
