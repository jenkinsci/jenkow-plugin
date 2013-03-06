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
