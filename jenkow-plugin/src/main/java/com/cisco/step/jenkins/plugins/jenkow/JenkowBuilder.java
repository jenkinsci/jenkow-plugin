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
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

public class JenkowBuilder extends Builder{
    private String workflowName;
    private Map<String,JenkowAction> deferredActions;
    
    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public JenkowBuilder(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getWorkflowName() {
        return workflowName;
    }
    
    public void addDeferredAction(JenkowAction ja){
        if (deferredActions == null) deferredActions = new HashMap<String,JenkowAction>();
        deferredActions.put(ja.getCalleeJobName(),ja);
    }
    
    public JenkowAction getDeferredAction(Run r){
        return (deferredActions == null)? null : deferredActions.get(r.getParent().getDisplayName());
    }
    
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, FileNotFoundException {
        PrintStream log = listener.getLogger();
        BuildLoggerMap.put(build,log);
        
        // TODO 5: remove later
        try {
            // just so we have a chance to click the progress bar
            log.println("sleep 5");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        ProcessEngine eng = JenkowEngine.getEngine();
        RuntimeService rtSvc = eng.getRuntimeService();
        String procId = null;
        try {
            procId = WfUtil.launchWf(log,workflowName,build.getParent().getName(),build.getNumber());
            
            if (procId != null){
                // TODO 5: is there a better way than polling to detect the termination of a process?
                HistoryService hstSvc = eng.getHistoryService();
                while (true){
                    // TODO 8: can we get a hold of any exception the engine is throwing and show it at least in the log?
                    // TODO 8: builder should log each time the process makes a state change
                    HistoricProcessInstance hProcInst = hstSvc.createHistoricProcessInstanceQuery().processInstanceId(procId).singleResult();
                    if (hProcInst.getEndTime() != null){
                        log.println(Consts.UI_PREFIX+": \""+workflowName+"\" ended");
                        break;
                    }
                    Thread.sleep(1000);
                }
            }
        } finally {
        	BuildLoggerMap.remove(build);
            if (procId != null && rtSvc.createExecutionQuery().processInstanceId(procId).singleResult() != null){
            	// should kick in when the current build is aborted
            	log.println(Consts.UI_PREFIX+": aborting \""+workflowName+"\" (#"+procId+")");
            	rtSvc.deleteProcessInstance(procId,build.getFullDisplayName()+" finished");
            }
        }
        return true;
    }
    
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    public static DescriptorImpl descriptor() {
        return Jenkins.getInstance().getDescriptorByType(JenkowBuilder.DescriptorImpl.class);
    }
    
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private JenkowEngineConfig engineConfig;
        
        public DescriptorImpl(){
            super(JenkowBuilder.class);
            load();
        }

        public FormValidation doCheckWorkflowName(@QueryParameter String value) throws IOException, ServletException {
            return WfUtil.checkWorkflowName(value);
        }
        
        public void doCreateWorkflow(@QueryParameter String wfn){
            JenkowPlugin.getInstance().repo.ensureWorkflowDefinition(wfn);
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return Consts.UI_PREFIX;
        }
        
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
//            System.out.println("formData="+formData);
        	
        	JenkowEngineConfig oldEngineConfig = engineConfig;
            if (!formData.containsKey("useExternalDB")){
            	engineConfig = null;
            }else{
            	// TODO 9: add online help to global.jelly after attributes have been settled
            	JSONObject extDB = formData.getJSONObject("useExternalDB");
            	engineConfig = new JenkowEngineConfig();
            	engineConfig.setDsUrl(extDB.getString("jdbcUrl"));
            	engineConfig.setDsUsername(extDB.getString("jdbcUsername"));
            	// TODO 7: need to find a way to store DB password encrypted
            	engineConfig.setDsPassword(extDB.getString("jdbcPassword"));
            	// TODO 7: should be a drop-down of available drivers
            	engineConfig.setDsDriverClass(extDB.getString("jdbcDriver"));
            	// TODO 6: can we have a validate DB function here?
            }
            save();
            
            // TODO 8: close engine also if there's a change in engineConfig
            if (engineConfig != oldEngineConfig) JenkowEngine.closeEngine();
            
            return super.configure(req,formData);
        }

        public boolean getUseExternalDB() {
        	return (engineConfig != null);
        }
        
        public String getJdbcUrl(){
        	return (engineConfig == null)? null : engineConfig.getDsUrl();
        }

        public String getJdbcUsername(){
        	return (engineConfig == null)? null : engineConfig.getDsUsername();
        }

        public String getJdbcPassword(){
        	return (engineConfig == null)? null : engineConfig.getDsPassword();
        }

        public String getJdbcDriver(){
        	return (engineConfig == null)? null : engineConfig.getDsDriverClass();
        }

		public JenkowEngineConfig getEngineConfig(){
			return engineConfig;
		}
    }
}
