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

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

import org.activiti.engine.RuntimeService;
import org.apache.commons.lang.StringUtils;

import hudson.Extension;
import hudson.model.TaskListener;
import hudson.model.TopLevelItem;
import hudson.model.Job;
import hudson.model.Project;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import hudson.tasks.Builder;

@Extension
public class JenkowRunListener extends RunListener<Run>{
	private final static Logger LOG = Logger.getLogger(JenkowRunListener.class.getName());
	
	private static class JenkowExecContext{
		String execId;
		Project parentProj;
		int buildNum;
		PrintStream log;
		
		private JenkowExecContext(Run r) {
			// TODO 8 is it possible that we have both JenkowActions?
		    JenkowAction ja = r.getAction(JenkowAction.class);
			if (ja == null) ja = JenkowAction.findDeferredAction(r);
			
			if (ja != null){
				execId = ja.getTaskExecId();
				if (execId != null){
					RuntimeService rtSvc = JenkowEngine.getEngine().getRuntimeService();
					Object bpObj = rtSvc.getVariable(execId,"jenkow_build_parent");
					Object bnObj = rtSvc.getVariable(execId,"jenkow_build_number");
					if (bpObj instanceof String && bnObj instanceof Integer){
						String parentJobName = (String)bpObj;
						int buildNum = ((Integer)bnObj).intValue();
						TopLevelItem it = Jenkins.getInstance().getItem(parentJobName);
						if (it instanceof Project){
							parentProj = (Project)it;
							Run build = parentProj.getLastBuild();
							if (build != null){
								if (build.getNumber() == buildNum){
									buildNum = buildNum;
									log = BuildLoggerMap.get(parentJobName,buildNum);
								}
							}
						}
					}
				}
			}
        }
		
		private void onStarted(Run r, TaskListener listener){
			// TODO 8: make console entries pointing to other builds a hyperlink
			log(Consts.UI_PREFIX+": "+r.getFullDisplayName()+" started");
		}
		
		private void onFinalized(Run r){
			LOG.info("signaling to execId="+execId);
			if (execId != null){
				RuntimeService rtSvc = JenkowEngine.getEngine().getRuntimeService();
				String br = ""+r.getResult();
				log(Consts.UI_PREFIX+": "+r.getFullDisplayName()+" ended ("+br+")");
				// TODO 7: can we "hand-over" more variables from job to workflow context?
				
				Map<String,JobMD> jobs = JobMD.getJobs(execId);
				String jobName = r.getParent().getName();
				JobMD job = jobs.get(jobName);
				if (job == null){
					jobs.put(jobName,new JobMD().setLastBuildResult(br));
					JobMD.setJobs(execId,jobs);
				}else{
					if (!StringUtils.equals(br,job.getLastBuildResult())){
						job.setLastBuildResult(br);
						JobMD.setJobs(execId,jobs);
					}
				}
				rtSvc.signal(execId);
			}
		}
		
		private void log(String msg){
			if (log != null) log.println(msg);
		}
	}
	
	@Override
    public void onStarted(Run r, TaskListener listener) {
		LOG.finer("JenkowRunListener.onStarted: "+r.getParent().getDisplayName()+" "+r.getDisplayName());
	    new JenkowExecContext(r).onStarted(r,listener);
    }

	@Override
    public void onFinalized(Run r) {
	    LOG.finer("JenkowRunListener.onFinalized: "+r.getParent().getDisplayName()+" "+r.getDisplayName());
		new JenkowExecContext(r).onFinalized(r);
    }
}
