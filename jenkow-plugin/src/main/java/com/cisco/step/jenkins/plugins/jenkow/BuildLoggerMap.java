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
import hudson.model.AbstractBuild;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import jenkins.model.Jenkins;

class BuildLoggerMap {
    private final static Map<String,PrintStream> parentLoggerMap = new HashMap<String,PrintStream>();

    static PrintStream get(String jobName, int buildNumber){
    	TopLevelItem it = Jenkins.getInstance().getItem(jobName);
    	if (it instanceof JenkowWorkflowJob){
    	    JenkowWorkflowJob job = (JenkowWorkflowJob)it;
    	    JenkowWorkflowRun build = job.getBuildByNumber(buildNumber);
    	    if (build != null){
    	        try {
                    return build.createListener().getLogger();
                } catch (IOException e) {
                    e.printStackTrace();
                }
    	    }
    	}
    	
        return parentLoggerMap.get(mkKey(jobName,buildNumber));
    }
    
    static PrintStream get(Object jobName, Object buildNumber){
    	if (jobName instanceof String && buildNumber instanceof Integer){
    		return get((String)jobName,((Integer)buildNumber).intValue());
    	}
    	return null;
    }
    
    static void put(AbstractBuild build, PrintStream logger){
    	parentLoggerMap.put(mkKey(build.getParent().getName(),build.getNumber()),logger);
    }
    
    static void remove(AbstractBuild build){
    	parentLoggerMap.remove(mkKey(build.getParent().getName(),build.getNumber()));
    }
    
    private static String mkKey(String jobName, int buildNumber){
    	return jobName+"|"+buildNumber;
    }
}
