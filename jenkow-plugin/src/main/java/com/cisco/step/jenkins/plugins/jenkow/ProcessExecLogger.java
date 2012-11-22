package com.cisco.step.jenkins.plugins.jenkow;

import hudson.model.Result;
import hudson.model.TopLevelItem;

import java.util.logging.Logger;

import jenkins.model.Jenkins;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

public class ProcessExecLogger implements ExecutionListener{
    private static final Logger LOG = Logger.getLogger(TaskExecLogger.class.getName());
    private String name;
    
    ProcessExecLogger(String name) {
        this.name = name;
    }

    @Override
    public void notify(DelegateExecution exec) throws Exception {
        LOG.info("workflow "+name+"#"+exec.getProcessInstanceId()+" finished");
        
        Object jobName = exec.getVariable("jenkow_build_parent");
        LOG.finer("jenkow_build_parent = "+jobName);
        
        Object buildNumber = exec.getVariable("jenkow_build_number");
        LOG.finer("jenkow_build_number = "+buildNumber);
        
        LOG.info("marking job "+jobName+"#"+buildNumber+" as complete");
        if (jobName instanceof String && buildNumber instanceof Integer){
            TopLevelItem it = Jenkins.getInstance().getItem((String)jobName);
            if (it instanceof JenkowWorkflowJob){
                JenkowWorkflowJob wfj = (JenkowWorkflowJob)it;
                JenkowWorkflowRun build = wfj.getBuildByNumber(((Integer)buildNumber).intValue());
                if (build != null){
                    // TODO 8: handle results other than success
                    build.markCompleted(Result.SUCCESS);
                }
            }
        }
    }
}
