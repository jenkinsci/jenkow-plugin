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
import hudson.util.FormValidation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
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
            ProcessDefinition pDef = JenkowPlugin.getInstance().repo.getDeployedWf(wfName);
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
}
