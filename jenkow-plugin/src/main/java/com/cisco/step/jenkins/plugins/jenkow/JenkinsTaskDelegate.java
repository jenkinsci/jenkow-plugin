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

import hudson.model.TopLevelItem;
import hudson.model.Project;
import hudson.util.DescribableList;

import java.util.logging.Logger;

import jenkins.model.Jenkins;

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.behavior.ReceiveTaskActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

public class JenkinsTaskDelegate extends ReceiveTaskActivityBehavior{
    private static final Logger LOG = Logger.getLogger(JenkinsTaskDelegate.class.getName());
    private Expression jobName;
    private Expression isManualJobLaunchMode;

	@Override
	public final void execute(ActivityExecution exec) throws Exception {
		LOG.finer("JenkinsTaskDelegate.execute()");
		
		String aid = exec.getActivity().getId();
		
		System.out.println("execution.id                       = "+exec.getId());
		System.out.println("execution.activity.id              = "+aid);
		System.out.println("execution.activity.procDef.id      = "+exec.getActivity().getProcessDefinition().getId());
		System.out.println("execution.activity.processDef.name = "+exec.getActivity().getProcessDefinition().getName());
		
		System.out.println("execution variables:");
		for (String vn : exec.getVariableNames()){
			System.out.println("  "+vn+"="+exec.getVariable(vn));
		}
		
		Integer loopCounter = (Integer)exec.getVariable("loopCounter");
		
		String jn = (jobName == null)? null : jobName.getValue(exec).toString();
		LOG.finer("jobName="+jn);
		
        boolean isManual = (isManualJobLaunchMode != null && Boolean.parseBoolean(isManualJobLaunchMode.getValue(exec).toString()));
        LOG.finer("isManual="+isManual);
        
        JenkowAction ja = new JenkowAction(aid,exec.getId(),jn);
        LOG.finer("ja="+ja);
        
        if (isManual){
            // TODO 9: move jenkow variables into JenkowProcessData
            Object jbp = exec.getVariable("jenkow_build_parent");
            JenkowAction.setDeferredAction((jbp == null)? null : jbp.toString(),ja);
            // TODO 9: log to build console "awaiting completion of job xyz"
            return;
        }
        
		if (jn != null){
			Jenkins jenkins = Jenkins.getInstance();
			TopLevelItem it = jenkins.getItem(jn);
			if (it == null){
                LOG.info("unable to launch job "+jn+", it=null");
			}else if (!(it instanceof Project)){
			    LOG.info("unable to launch job "+jn+", because it's not a Project, but just "+it.getClass());
			}else{
				// TODO 8: would like to have AbstractProject here, but it doesn't have BuildWrappers.
			    Project p = (Project)it;
				
				DescribableList wrappers = p.getBuildWrappersList();
				JenkowBuildWrapper wrapper = new JenkowBuildWrapper();
				if (!wrappers.contains(wrapper.getDescriptor())) wrappers.add(wrapper);
				
				p.scheduleBuild2(jenkins.getQuietPeriod(),new WorkflowCause("triggered by workflow"),ja);
				return;
			}
		}
		
		// TODO 9: need test for Jenkins task with empty / non-existing job name
		leave(exec);
	}
}
