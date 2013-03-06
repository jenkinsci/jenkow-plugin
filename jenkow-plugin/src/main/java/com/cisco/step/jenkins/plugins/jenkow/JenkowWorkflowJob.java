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
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import hudson.model.Queue.FlyweightTask;
import hudson.util.FormValidation;

import java.io.IOException;

import javax.servlet.ServletException;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.jenkinsci.plugins.asyncjob.AsyncJob;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class JenkowWorkflowJob extends AsyncJob<JenkowWorkflowJob,JenkowWorkflowRun> implements FlyweightTask, TopLevelItem {
    private String workflowName;
     
    public JenkowWorkflowJob(ItemGroup parent, String name) {
        super(parent, name);
    }
    
    public String getWorkflowName() {
        return workflowName;
    }

    public WorkflowDiagram doDiagram() {
        return new WorkflowDiagram(workflowName);
    }

    public TopLevelItemDescriptor getDescriptor() {
        return (TopLevelItemDescriptor)Jenkins.getInstance().getDescriptor(getClass());
    }
    
    @Override
    protected void submit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, FormException {
        super.submit(req,rsp);
        JSONObject json = req.getSubmittedForm();
        workflowName = json.getString("workflowName");
    }

//    @Extension
//    public static class DescriptorImpl extends TopLevelItemDescriptor {
//        
//        @Override
//        public String getDisplayName() {
//            return "BPMN workflow project";
//        }
//
//        public FormValidation doCheckWorkflowName(@QueryParameter String value) throws IOException, ServletException {
//            return WfUtil.checkWorkflowName(value);
//        }
//        
//        @Override
//        public TopLevelItem newInstance(ItemGroup parent, String name) {
//            return new JenkowWorkflowJob(parent,name);
//        }
//    }
}
