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
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.OutputStream;

public class JenkowWorkflowPicture implements Action, HttpResponse {
    JenkowBuilder builder;

    public JenkowWorkflowPicture(JenkowBuilder builder) {
        this.builder = builder;
    }

    // invoked with /job/${jobname}/`getUrlName`/graph
    public JenkowWorkflowPicture doGraph(StaplerResponse rsp) {
        return this;
    }

    private void generateTo(OutputStream os) throws IOException {
        builder.generateWfDiagramTo(os);
    }

    @Override
    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
        rsp.setContentType("image/png");
        // TODO: send out proper HTTP cache
        generateTo(rsp.getOutputStream());
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
        // TODO kk?: how to deal with multiple JenkowBuilders in one job here?
        // have one generic "jenkow" action which returns always the first wf
        // and have "jenkow/${wfName}" to get the builder with a given wfName?
        return "jenkow";
    }
}
