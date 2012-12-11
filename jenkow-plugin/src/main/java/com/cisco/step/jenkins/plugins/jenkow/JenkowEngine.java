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

import hudson.tasks.Mailer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.bpmn.parser.BpmnParseListener;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.util.xml.Element;
import org.activiti.engine.impl.variable.VariableDeclaration;
import org.jenkinsci.plugins.database.Database;

public class JenkowEngine {
    private static final Logger LOG = Logger.getLogger(JenkowEngine.class.getName());
	private static ProcessEngine engine;
	
	public static ProcessEngine getEngine(){
		if (engine == null){
			Database ec = JenkowBuilder.descriptor().getDatabase();
			LOG.info("engineConfig="+ec);

			ProcessEngineConfiguration cfg;
			// context for *all* processes. available in exppressions and scripts in the process.
			Map<Object,Object> ctxBeans = new HashMap<Object,Object>();
			ctxBeans.put("log",LOG);
			
			if (ec instanceof H2DemoDatabase) {
				cfg = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
                // we will be sharing this database with Activiti Explorer, so don't force re-creation of the whole DB
                // and honor what's already there
                cfg.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
			} else {
				
				cfg = ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration();
				
				Mailer.DescriptorImpl md = Mailer.descriptor();
	         	System.out.println("mailer config");
				System.out.println("md.getSmtpServer()     -> "+md.getSmtpServer());
				System.out.println("md.getDefaultSuffix()  -> "+md.getDefaultSuffix());
				System.out.println("md.getReplyToAddress() -> "+md.getReplyToAddress());
				System.out.println("md.getSmtpPort()       -> "+md.getSmtpPort());
				
				// set database config
                try {
                    cfg.setDataSource(ec.getDataSource());
                } catch (SQLException e) {
                    throw new Error(e); // TODO: what's the error handling strategy in this method?
                }

                // set other engine config
				cfg.setDatabaseSchemaUpdate("true");
				cfg.setHistory("full");
			}
			cfg.setJobExecutorActivate(true);
			
			ClassLoader peCL = JenkowEngine.class.getClassLoader();
			Thread.currentThread().setContextClassLoader(peCL);
			// set common cfg here.
			ProcessEngineConfigurationImpl peCfg = (ProcessEngineConfigurationImpl)cfg;
			peCfg.setBeans(ctxBeans);
			List<BpmnParseListener> preParseListeners = peCfg.getPreParseListeners();
			if (preParseListeners == null){
				preParseListeners = new ArrayList<BpmnParseListener>();
				peCfg.setPreParseListeners(preParseListeners);
			}
			preParseListeners.add(new JenkowBpmnParseListener());
			cfg.setClassLoader(peCL);
			// build engine
			engine = cfg.buildProcessEngine();			
		}
		
		return engine;
	}
	
	static void closeEngine(){
		LOG.info("closing engine");
		if (engine != null) engine.close();
		engine = null;
	}
}
