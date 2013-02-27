package com.cisco.step.jenkins.plugins.jenkow;

import hudson.Extension;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
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

    @Extension
    public static class DescriptorImpl extends TopLevelItemDescriptor {
        @Override
        public String getDisplayName() {
            return "BPMN workflow project";
        }

        public FormValidation doCheckWorkflowName(@QueryParameter String value) throws IOException, ServletException {
            return WfUtil.checkWorkflowName(value);
        }
        
        @Override
        public TopLevelItem newInstance(ItemGroup parent, String name) {
            return new JenkowWorkflowJob(parent,name);
        }
    }
}
