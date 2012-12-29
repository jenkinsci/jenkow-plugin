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

import hudson.Util;
import hudson.util.FormValidation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

class WfUtil {
    private static final Logger LOGGER = Logger.getLogger(WfUtil.class.getName());
    
    static FormValidation checkWorkflowName(String value) throws IOException, ServletException {
        return checkFile(value,JenkowPlugin.getInstance().repo.getWorkflowFile(value));
    }
    
    private static FormValidation checkFile(String value, File f){
        if (!StringUtils.isEmpty(value)){
            if (!f.exists()){
                String url = "descriptorByName/"+JenkowBuilder.class.getName()+"/createWorkflow?wfn="+value;
                String msg = "Workflow "+value+" does not exist. "
                           + "<button type=\"button\""
                           + " onclick=\"javascript:"
                           +    "var e=this;"
                           +    "new Ajax.Request"
                           +    "("
                           +      "'"+url+"',"
                           +      "{onSuccess:function(x)"
                           +        "{notificationBar.show('Workflow created.',notificationBar.OK);"
                           +         "fireEvent($(e).up('TR.validation-error-area').previous().down('INPUT'),'change');"
                           +        "}"
                           +      "}"
                           +    ")"
                           +   "\""
                           + ">"
                           + "Create it now"
                           + "</button>"
                           ;
                return FormValidation.errorWithMarkup(msg);
            }
            if (!f.canRead()) return FormValidation.warning("Workflow "+value+" is not readable.");
        }
        return FormValidation.ok();
    }
    
    static String launchWf(PrintStream log, String wfName, String parentName, Integer buildNo){
        // test wfNames are full paths, doesn't work as workflow name
        int p = wfName.lastIndexOf('/');
        if (p > -1){
            wfName = wfName.substring(p+1);
            p = wfName.lastIndexOf('.');
            if (p > -1) wfName = wfName.substring(0,p);
        }
        
        LOGGER.finer("WfUtil.launchWf: workflowName="+wfName);
        
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        RuntimeService rtSvc = JenkowEngine.getEngine().getRuntimeService();
        
        try {
            ProcessDefinition pDef = getDeployedWf(wfName);
            if (pDef == null) throw new RuntimeException("no deployed process definition for "+wfName);

            Map<String,Object> varMap = new HashMap<String,Object>();
            // TODO 9: move jenkow variables into JenkowProcessData
            varMap.put("jenkow_build_parent",parentName);
            varMap.put("jenkow_build_number",buildNo);
            varMap.put("console",new ConsoleLogger(parentName,buildNo));
            JobMD.setJobs(varMap,JobMD.newJobs());
            
            log.println(Consts.UI_PREFIX+": \""+wfName+"\" started");
long t = System.currentTimeMillis();            
System.out.println("starting process "+pDef.getId());
// TODO 9: why is this blocking????
            ProcessInstance proc = rtSvc.startProcessInstanceById(pDef.getId(),varMap);
//            String procId = startProcess(pDef.getId(),varMap);
            String procId = proc.getId();
System.out.println("process started procId="+procId+" ("+(System.currentTimeMillis()-t)+")");            
            return procId;
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }
    
    static void deployAllToEngine(){
        File repoDir = JenkowWorkflowRepository.getRepositoryDir();
        if (!repoDir.exists()){
            LOGGER.info("no workflow source repository");
            return;
        }
        
        LOGGER.info("deploying all workflow engine");
        
        RepositoryService repoSvc = JenkowEngine.getEngine().getRepositoryService();
        Map<String,Date> deplTimes = new HashMap<String,Date>();
        for (Deployment depl : repoSvc.createDeploymentQuery().list()){
            //System.out.println("  depl: id="+depl.getId()+" name="+depl.getName()+" time="+depl.getDeploymentTime());
            deplTimes.put(depl.getId(),depl.getDeploymentTime());
        }
        Map<String,Date> pDefTimes = new HashMap<String,Date>();
        for (ProcessDefinition pDef : repoSvc.createProcessDefinitionQuery().latestVersion().list()){
            //System.out.println(" pDef:"+pDef+" deplId="+pDef.getDeploymentId()+" key="+pDef.getKey());
            Date t = deplTimes.get(pDef.getDeploymentId());
            if (t != null) pDefTimes.put(pDef.getKey(),t);
        }
        
        for (Iterator it=FileUtils.iterateFiles(repoDir,new String[]{Consts.WORKFLOW_EXT},/*recursive=*/true); it.hasNext(); ){
            File wff = (File)it.next();
            String wfn = wff.getName();
            int p = wfn.lastIndexOf('.');
            if (p > -1) wfn = wfn.substring(0,p);
            Date prevDeplTime = pDefTimes.get(wfn);
            //System.out.println("  f="+wff+" wfn="+wfn+" deplTime="+prevDeplTime+" wff.lastModified="+new Date(wff.lastModified()));
            if (prevDeplTime == null || prevDeplTime.before(new Date(wff.lastModified()))){
                try {
                    WfUtil.deployToEngine(wff);
                } catch (FileNotFoundException e) {
                    LOGGER.log(Level.SEVERE,"file not found "+wff,e);
                }
            }
        }
    }
    
    static void deployToEngine(File wff) throws FileNotFoundException{
        LOGGER.info("deploying "+wff+" to workflow engine");
        
        ProcessEngine eng = JenkowEngine.getEngine();
        RuntimeService rtSvc = eng.getRuntimeService();
        RepositoryService repoSvc = eng.getRepositoryService();

        String wfn = wff+"20.xml"; // TODO 9: workaround for http://forums.activiti.org/en/viewtopic.php?f=8&t=3745&start=10
        DeploymentBuilder db = repoSvc.createDeployment().addInputStream(wfn,new FileInputStream(wff));

        // TODO 4: We should avoid redeploying here, if workflow file of a given version(?) is already deployed?
        Deployment d = db.deploy();
        ProcessDefinition pDef = repoSvc.createProcessDefinitionQuery().deploymentId(d.getId()).singleResult();
        LOGGER.fine("deployedToEngine("+wff+") --> "+pDef);
    }
    
    static ProcessDefinition getDeployedWf(String wfName){
        RepositoryService repoSvc = JenkowEngine.getEngine().getRepositoryService();
        if (LOGGER.isLoggable(Level.FINE)){
            LOGGER.fine("deployed process definitions:");
            for (ProcessDefinition pDef : repoSvc.createProcessDefinitionQuery().latestVersion().list()){
                LOGGER.fine("  pDef:"+pDef+" id="+pDef.getId()+" key="+pDef.getKey()+" name="+pDef.getName()+" resourceName="+pDef.getResourceName()+" version="+pDef.getVersion());
            }
        }
        ProcessDefinition pDef = repoSvc
                                 .createProcessDefinitionQuery()
                                 .processDefinitionKey(wfName)
                                 .latestVersion()
                                 .singleResult();
        LOGGER.fine("getDeployedWf("+quoted(wfName)+") --> "+pDef);
        return pDef;
    }
    
    static void generateWfDiagramTo(String wfName, OutputStream out) throws IOException{
        ProcessDefinition pDef = getDeployedWf(wfName);
        InputStream in = JenkowEngine.getEngine().getRepositoryService().getResourceAsStream(pDef.getDeploymentId(),pDef.getDiagramResourceName());
        // TODO kk? should use copyStreamAndClose here?
        Util.copyStream(in,out);
    }

    // experimental
//    synchronized static String startProcess(final String pDefId, final Map<String,Object> vars) throws InterruptedException{
//        ProcessEngine eng = JenkowEngine.getEngine();
//        final RuntimeService rtSvc = eng.getRuntimeService();
//        
//        System.out.println("current ids:");
//        Set<String> ids = new HashSet<String>();
//        for (ProcessInstance proc : rtSvc.createProcessInstanceQuery().list()){
//            String id = proc.getId();
//            System.out.println(" rId="+id);
//            ids.add(id);
//        }
//        for (HistoricProcessInstance proc : eng.getHistoryService().createHistoricProcessInstanceQuery().list()){
//            String id = proc.getId();
//            System.out.println(" hId="+id);
//            ids.add(id);
//        }
//        
//        System.out.println("starting process");
//        new Thread(new Runnable(){
//            @Override
//            public void run() {
//                rtSvc.startProcessInstanceById(pDefId,vars);
//            }
//        }).start();
//        System.out.println("process started");
//        
//        while (true){
//            for (ProcessInstance proc : rtSvc.createProcessInstanceQuery().list()){
//                String id = proc.getId();
//                System.out.println("  found running id="+id);
//                if (!ids.contains(id)){
//                    return id;
//                }
//            }
//            for (HistoricProcessInstance proc : eng.getHistoryService().createHistoricProcessInstanceQuery().list()){
//                String id = proc.getId();
//                System.out.println("  found historic id="+id);
//                if (!ids.contains(id)){
//                    return id;
//                }
//            }
//            Thread.sleep(100);
//        }
//    }

	/**
	 * Produces a valid workflow ID out of a given string.
	 * Replaces each invalid character with an underscore.
	 */
	static String mkWorkflowId(String s){
		if (s == null || s.length() < 1) return s;
		
		// TODO 5: figure out what's the actual definition for valid workflow IDs
		StringBuffer sb = new StringBuffer();
		
		char c = s.charAt(0);
		sb.append(Character.isJavaIdentifierStart(c)? c : '_');
		for (int i=1,n=s.length(); i<n; i++){
			c = s.charAt(i);
			sb.append(Character.isJavaIdentifierPart(c)? c : '_');
		}
				
		return sb.toString();
	}

    static String quoted(String s){
        return (s == null)? null : "\""+s+"\"";
    }
}
