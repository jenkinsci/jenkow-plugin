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
import java.util.Map;

import javax.servlet.ServletException;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.lang.StringUtils;

class WfUtil {
    
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
    
    static String launchWf(PrintStream log, String workflowName, String parentName, Integer buildNo) throws FileNotFoundException{
        System.out.println("WfUtil.launchWf: workflowName="+workflowName);
        
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        
        ProcessEngine eng = JenkowEngine.getEngine();
        RuntimeService rtSvc = eng.getRuntimeService();
        RepositoryService repoSvc = eng.getRepositoryService();
        
        try {
            File wff = JenkowPlugin.getInstance().repo.getWorkflowFile(workflowName);
            if (!wff.exists()){
                log.println("error: "+wff+" does not exist");
                return null;
            }
            String wfn = wff+"20.xml"; // TODO 9: workaround for http://forums.activiti.org/en/viewtopic.php?f=8&t=3745&start=10
            DeploymentBuilder db = repoSvc.createDeployment().addInputStream(wfn,new FileInputStream(wff));

            // TODO 7: We should avoid redeploying here, if workflow is already deployed?
            Deployment d = db.deploy();
            ProcessDefinition pDef = repoSvc.createProcessDefinitionQuery().deploymentId(d.getId()).singleResult();

            Map<String,Object> varMap = new HashMap<String,Object>();
            // TODO 9: move jenkow variables into JenkowProcessData
            varMap.put("jenkow_build_parent",parentName);
            varMap.put("jenkow_build_number",buildNo);
            varMap.put("console",new ConsoleLogger(parentName,buildNo));
            JobMD.setJobs(varMap,JobMD.newJobs());
            
            log.println(Consts.UI_PREFIX+": \""+workflowName+"\" started");
long t = System.currentTimeMillis();            
System.out.println("starting process");            
// TODO 9: why is this blocking????
            String procId = rtSvc.startProcessInstanceById(pDef.getId(),varMap).getId();
System.out.println("process started procId="+procId+" ("+(System.currentTimeMillis()-t)+")");            
            return procId;
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }

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
