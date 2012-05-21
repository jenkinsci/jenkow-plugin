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

import hudson.model.FreeStyleProject;

import org.activiti.engine.RuntimeService;

public class WaitsForeverTest extends JenkowTestCase{

	public void testAbortJob() throws Exception{
		FreeStyleProject launcher = createFreeStyleProject("launcher");
        launcher.getBuildersList().add(new JenkowBuilder(getWfName("WaitsForever")));
        
        launcher.scheduleBuild2(0);
        
        RuntimeService rtSvc = JenkowEngine.getEngine().getRuntimeService();
        while (launcher.getLastBuild() == null || rtSvc.createExecutionQuery().count() < 1){
        	Thread.sleep(1000);
        }

		System.out.println("nofProcs="+rtSvc.createExecutionQuery().count());
		Thread.sleep(1000);
    	System.out.println("interrupting");
    	launcher.getLastBuild().getExecutor().interrupt();
    	
    	Thread.sleep(1000);
    	assertTrue("process still exists",(rtSvc.createExecutionQuery().count() < 1));
	}
}
