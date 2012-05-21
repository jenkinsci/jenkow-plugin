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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class JobMD implements Serializable{
    private static final String JOBS = "jobs";
	private static final long serialVersionUID = -5375819605680552946L;
	String lastBuildResult;
	
	JobMD() {
    }

	public JobMD setLastBuildResult(String lastBuildResult) {
    	this.lastBuildResult = lastBuildResult;
    	return this;
    }

	public String getLastBuildResult() {
    	return lastBuildResult;
    }
	
	@Override
    public String toString() {
	    return "JobMD{lastBuildResult="+lastBuildResult+"}";
    }

	static Map<String,JobMD> newJobs(){
		return new HashMap<String,JobMD>();
	}
	
	static void setJobs(Map<String,Object> varMap, Map<String,JobMD> jobs){
		varMap.put(JOBS,jobs);
	}
	
	static void setJobs(String execId, Map<String,JobMD> jobs){
		JenkowEngine.getEngine().getRuntimeService().setVariable(execId,JOBS,jobs);
	}
	
	static Map<String,JobMD> getJobs(String execId){
		Map<String,JobMD> jobs = (Map<String,JobMD>)JenkowEngine.getEngine().getRuntimeService().getVariable(execId,JOBS);
		if (jobs == null) jobs = newJobs();
		return jobs;
	}
}
