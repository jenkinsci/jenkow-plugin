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

import java.util.List;

import org.activiti.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmEvent;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.util.xml.Element;

class JenkowBpmnParseListener extends AbstractBpmnParseListener{
	// TODO 3: add logger for other types of workflow elements?
	
	@Override
	public void parseUserTask(Element el, ScopeImpl scope, ActivityImpl activity) {
		addLogger(el,activity);
	}
	
	@Override
	public void parseTask(Element el, ScopeImpl scope, ActivityImpl activity) {
		addLogger(el,activity);
	}
	
	@Override
	public void parseServiceTask(Element el, ScopeImpl scope, ActivityImpl activity) {
		addLogger(el,activity);
	}
	
	@Override
	public void parseSendTask(Element el, ScopeImpl scope, ActivityImpl activity) {
		addLogger(el,activity);
	}
	
	@Override
	public void parseScriptTask(Element el, ScopeImpl scope, ActivityImpl activity) {
		addLogger(el,activity);
	}
	
	@Override
	public void parseManualTask(Element el, ScopeImpl scope, ActivityImpl activity) {
		addLogger(el,activity);
	}
	
	@Override
	public void parseBusinessRuleTask(Element el, ScopeImpl scope, ActivityImpl activity) {
		addLogger(el,activity);
	}
	
	@Override
    public void parseReceiveTask(Element el, ScopeImpl scope, ActivityImpl activity) {
		addLogger(el,activity);
    }
	
	@Override
    public void parseEndEvent(Element el, ScopeImpl scope, ActivityImpl activity) {
	    if (el.element("errorEventDefinition") != null){
	        activity.addExecutionListener(PvmEvent.EVENTNAME_END,new EndErrorHandler(el.attribute("id")));
	    }
    }

    @Override
    public void parseProcess(Element el, ProcessDefinitionEntity processDefinition) {
	    processDefinition.addExecutionListener(PvmEvent.EVENTNAME_END,new ProcessExecLogger(el.attribute("id")));
    }

    private void addLogger(Element el, ActivityImpl activity){
        TaskExecLogger logger = new TaskExecLogger(el.attribute("id"));
		activity.addExecutionListener(PvmEvent.EVENTNAME_START,logger);
        activity.addExecutionListener(PvmEvent.EVENTNAME_END,logger);
	}
}
