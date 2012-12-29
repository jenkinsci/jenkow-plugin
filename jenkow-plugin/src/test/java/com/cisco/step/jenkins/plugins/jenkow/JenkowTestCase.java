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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.jvnet.hudson.test.HudsonTestCase;

public abstract class JenkowTestCase extends HudsonTestCase{

    @Override
    protected void setUp() throws Exception {
    	super.setUp();
    	
    	setQuietPeriod(0);
    	
    	String url = new WebClient().getContextPath();
    	System.out.println("***** accessing "+url);

    	sleep("test.pre.sleep");
    }

	@Override
	protected void tearDown() throws Exception {
		sleep("test.post.sleep");
		super.tearDown();
	}
	
	private static void sleep(String propertyName) throws Exception{
		String ts = System.getProperty(propertyName);
		if (ts != null){
			System.out.println("*** Thread.sleep("+ts+")");
			Thread.sleep(Long.valueOf(ts));
		}
	}
	
	protected String getWfName(String shortName) throws FileNotFoundException{
	    String suffix = "/"+shortName+Consts.WORKFLOW_DOT_EXT;
	    String cn = getClass().getName();
	    int p = cn.lastIndexOf('.');
	    if (p > -1) cn = cn.substring(0,p);
	    URL rsc = getClass().getResource("/"+cn.replace('.','/')+suffix);
	    // TODO 2: fallback: old location for .bpmn files, should remove once all tests are in their own directory
	    if (rsc == null) rsc = getClass().getResource("/diagrams"+suffix);
        String fn = rsc.getFile();
        WfUtil.deployToEngine(new File(fn));
        return fn;
	}
	
	protected void setNumExecutors(int n) throws IOException{
        jenkins.setNumExecutors(n);
        jenkins.setNodes(jenkins.getNodes());
	}
}
