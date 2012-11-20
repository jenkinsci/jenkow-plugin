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
import hudson.model.TopLevelItem;
import hudson.model.Project;
import hudson.model.Run;
import hudson.tasks.Builder;

import java.util.List;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

/**
 * Allows to associate a Jenkins build back to the Jenkins BPMN task,
 * if the build was started by a BPMN task.
 */
public class JenkowAction implements Action{
    private final static Logger LOG = Logger.getLogger(JenkowAction.class.getName());
    
	private String taskId;
	private String taskExecId;
	private String calleeJobName;
	
	public JenkowAction(String taskId, String taskExecId, String calleeJobName) {
		this.taskId = taskId;
		this.taskExecId = taskExecId;
		this.calleeJobName = calleeJobName;
	}
	
	public String getTaskId() {
    	return taskId;
    }

	public String getTaskExecId() {
		return taskExecId;
	}
	
	public String getCalleeJobName() {
        return calleeJobName;
    }

    @Override
	public String getIconFileName() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return "JenkowAction";
	}

	@Override
	public String getUrlName() {
		return null;
	}

	@Override
    public String toString() {
	    return "task="+taskId+",exec="+taskExecId+",callee="+calleeJobName;
    }
	
	static void setDeferredAction(String callerJobName, JenkowAction ja){
	    LOG.finer("JenkowAction.setDeferredAction: caller="+callerJobName+" ja="+ja);
        for (TopLevelItem it : Jenkins.getInstance().getItems()){
            if (it instanceof Project){
                Project proj = (Project)it;
                LOG.finer("  proj="+proj.getDisplayName());
                if (proj.getDisplayName().equals(callerJobName)){
                    for (Builder b : (List<Builder>)proj.getBuilders()){
                        LOG.finer("    builder="+b.getDescriptor().getDisplayName());
                        if (b instanceof JenkowBuilder){
                            JenkowBuilder jb = (JenkowBuilder)b;
                            jb.addDeferredAction(ja);
                        }
                    }
                }
            }
        }
	}

    static JenkowAction findDeferredAction(Run r){
        for (TopLevelItem it : Jenkins.getInstance().getItems()){
            if (it instanceof Project){
                Project proj = (Project)it;
                LOG.finer("proj="+proj.getDisplayName());
                for (Builder b : (List<Builder>)proj.getBuilders()){
                    LOG.finer("  builder="+b.getDescriptor().getDisplayName());
                    if (b instanceof JenkowBuilder){
                        return ((JenkowBuilder)b).getDeferredAction(r);
                    }
                }
            }
        }
        return null;
    }
}
