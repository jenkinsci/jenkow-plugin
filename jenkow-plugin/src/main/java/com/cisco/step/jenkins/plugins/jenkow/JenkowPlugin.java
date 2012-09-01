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

import java.util.logging.Logger;

import javax.inject.Inject;

import com.cisco.step.jenkins.plugins.jenkow.JenkowBuilder.DescriptorImpl;

import jenkins.model.Jenkins;

import hudson.Extension;
import hudson.Plugin;

public class JenkowPlugin extends Plugin{
    private static final Logger LOG = Logger.getLogger(JenkowPlugin.class.getName());
    
    JenkowWorkflowRepository repo;
    
	@Override
    public void start() throws Exception {
		instance = this;
	    LOG.info("JenkowPlugin.start()");
		// TODO 8: need to open workflow engine automatically here,
    }

	@Override
    public void stop() throws Exception {
		LOG.info("JenkowPlugin.stop()");
		JenkowEngine.closeEngine();
    }
	
	// TODO 6: is there a better way to wire up things?
	private static JenkowPlugin instance;
	
	static JenkowPlugin getInstance(){
	    return instance;
	}
}
