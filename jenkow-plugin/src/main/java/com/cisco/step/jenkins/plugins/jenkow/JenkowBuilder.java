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
import org.activiti.engine.impl.repository.DeploymentBuilderImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

public class JenkowBuilder extends Builder{
    private String workflowName;
    
    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public JenkowBuilder(String workflowName) {
        this.workflowName = workflowName;
        JenkowPlugin jpl = JenkowPlugin.getInstance();
        if (jpl != null){
            jpl.eclipseResources.ensureWorkflowDefinition(getDescriptor().getEffectiveWorkflowRepoRoot(),workflowName);        
        }
    }

    public String getWorkflowName() {
        return workflowName;
    }
    
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, FileNotFoundException {
        PrintStream log = listener.getLogger();
        BuildLoggerMap.put(build,log);
        
        try {
            // just so we have a chance to click the progress bar
            log.println("sleep 5");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        
        ProcessEngine eng = JenkowEngine.getEngine();
        RuntimeService rtSvc = eng.getRuntimeService();
        RepositoryService repoSvc = eng.getRepositoryService();
        
        String procId = null;
        try {
            File wff = getDescriptor().getWorkflowFile(workflowName);
            if (!wff.exists()) log.println("error: "+wff+" does not exist");
            String wfn = wff+"20.xml"; // TODO 9: workaround for http://forums.activiti.org/en/viewtopic.php?f=8&t=3745&start=10
            DeploymentBuilder db = repoSvc.createDeployment().addInputStream(wfn,new FileInputStream(wff));

            // skip XML Schema validation as documents produced by Activiti designer doesn't seem to conform to them
            ((DeploymentBuilderImpl)db).getDeployment().setValidatingSchema(false);

            // TODO 7: We should avoid redeploying here, if workflow is already deployed?
            Deployment d = db.deploy();
            ProcessDefinition pDef = repoSvc.createProcessDefinitionQuery().deploymentId(d.getId()).singleResult();

            Map<String,Object> varMap = new HashMap<String,Object>();
            // TODO 9: move jenkow variables into JenkowProcessData
            varMap.put("jenkow_build_parent",build.getParent().getName());
            varMap.put("jenkow_build_number",Integer.valueOf(build.getNumber()));
            varMap.put("console",new ConsoleLogger(build.getParent().getName(),Integer.valueOf(build.getNumber())));
            JobMD.setJobs(varMap,JobMD.newJobs());
            // varMap.put("log",log); // can't put a non-serializable variable here
            
            log.println(Consts.UI_PREFIX+": \""+workflowName+"\" started");
            procId = rtSvc.startProcessInstanceById(pDef.getId(),varMap).getId();
            
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
        } finally {
        	Thread.currentThread().setContextClassLoader(previous);
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
        // TODO 8: make workflowRepoRoot optional in config UI, similar to "External Database"
        private String workflowRepoRoot;
        private JenkowEngineConfig engineConfig;
        
        public DescriptorImpl(){
            super(JenkowBuilder.class);
            load();
            ensureWorkflowProject();
        }

        public FormValidation doCheckWorkflowRepoRoot(@QueryParameter String value) throws IOException, ServletException {
            return checkFile(value,new File(value));
        }

        public FormValidation doCheckWorkflowName(@QueryParameter String value) throws IOException, ServletException {
            return checkFile(value,getWorkflowFile(value));
        }
        
        private static FormValidation checkFile(String value, File f){
            if (!StringUtils.isEmpty(value)){
                if (!f.exists()) return FormValidation.warning("workflow "+value+" does not exist");
                if (!f.canRead()) return FormValidation.warning("workflow "+value+" is not readable");
            }
            return FormValidation.ok();
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
            workflowRepoRoot = formData.getString("workflowRepoRoot");
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
            
            ensureWorkflowProject();
            return super.configure(req,formData);
        }

        public String getWorkflowRepoRoot() {
            ensureWorkflowProject();
            return workflowRepoRoot;
        }
        
        private File getEffectiveWorkflowRepoRoot() {
            if (!StringUtils.isEmpty(workflowRepoRoot)) return new File(workflowRepoRoot);
            
            File wfd = new File(Jenkins.getInstance().getRootDir(),"workflows");
            JenkowPlugin.getInstance().eclipseResources.prepareProject(wfd);
            return wfd.getAbsoluteFile();
        }
        
        void ensureWorkflowProject(){
            // TODO 7: improve this wiring up
            JenkowPlugin jpl = JenkowPlugin.getInstance();
            if (jpl != null){
                jpl.eclipseResources.prepareProject(getEffectiveWorkflowRepoRoot());
            }
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

		File getWorkflowFile(String wfName){
            return new File(getEffectiveWorkflowRepoRoot(),EclipseResources.mkWfPath(wfName)).getAbsoluteFile();
        }
		
		public JenkowEngineConfig getEngineConfig(){
			return engineConfig;
		}
    }
}
