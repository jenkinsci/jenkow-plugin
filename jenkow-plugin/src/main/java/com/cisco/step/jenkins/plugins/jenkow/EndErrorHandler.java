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

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

public class EndErrorHandler implements ExecutionListener{
    private static final Logger LOG = Logger.getLogger(EndErrorHandler.class.getName());
    private String name;
    
    EndErrorHandler(String name) {
        this.name = name;
    }

    @Override
    public void notify(DelegateExecution exec) throws Exception {
        if (LOG.isLoggable(Level.FINE)){
            LOG.fine("name                          = "+name);
            LOG.fine("exec.getEventName()          -> "+exec.getEventName());
            LOG.fine("exec.getId()                 -> "+exec.getId());
            LOG.fine("exec.getProcessBusinessKey() -> "+exec.getProcessBusinessKey());
            LOG.fine("exec.getProcessInstanceId()  -> "+exec.getProcessInstanceId());
            LOG.fine("jenkow_build_parent           = "+exec.getVariable("jenkow_build_parent"));
            LOG.fine("jenkow_build_number           = "+exec.getVariable("jenkow_build_number"));
        }
        
        exec.setVariable(Consts.BUILD_RESULT_NAME,Result.FAILURE);
        PrintStream log = BuildLoggerMap.get(exec.getVariable("jenkow_build_parent"),exec.getVariable("jenkow_build_number"));
        if (log != null){
            String ev = exec.getEventName();
            if (ev != null) ev += "ed";
            log.println(Consts.UI_PREFIX+": "+name+" "+ev);
        }
    }
}
