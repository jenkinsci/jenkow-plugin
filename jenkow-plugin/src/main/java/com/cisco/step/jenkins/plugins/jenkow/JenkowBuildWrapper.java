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

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.Execution;
import org.kohsuke.stapler.DataBoundConstructor;

//@Extension
public class JenkowBuildWrapper extends BuildWrapper{
	private final static Logger LOG = Logger.getLogger(JenkowBuildWrapper.class.getName());
	
	@DataBoundConstructor
	public JenkowBuildWrapper(){
	}
	
	private class EnvironmentImpl extends BuildWrapper.Environment{
		Map<String,String> varmap;
		
		EnvironmentImpl(Map<String,String> varmap) {
			this.varmap = varmap;
		}

		@Override
		public void buildEnvVars(Map<String,String> env) {
            EnvVars.resolve(varmap);
            env.putAll(varmap);
		}

		@Override
		public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
			return true;
		}
	}
	
	@Override
	public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException {
		System.out.println("JenkowBuildWrapper.setUp "+build.getFullDisplayName()+" execId="+getTaskExecId(build));
		LOG.info("JenkowBuildWrapper.setUp "+build.getFullDisplayName()+" execId="+getTaskExecId(build));
		Map<String,String> map = new HashMap<String,String>();
        
		// TODO 7: feed-in workflow context variables as job environment variables
		map.put("JENKOW_VAR","set by JenkowBuildWrapper.setUp()");

		return new EnvironmentImpl(map);
//        return new Environment() {
//            @Override
//            public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
//                System.out.println("build.getResult() -> "+build.getResult());
//                new Exception().printStackTrace();
//            	return true;
//            }
//        };
	}
	
	private static String getTaskExecId(AbstractBuild build){
		JenkowAction ja = build.getAction(JenkowAction.class);
		if (ja != null) return ja.getTaskExecId();
		return null;
	}
	
	private static DescriptorImpl descriptor;
	
	@Override
	public Descriptor<BuildWrapper> getDescriptor(){
		if (descriptor == null) descriptor = new DescriptorImpl();
		return descriptor;
	}
	
	@Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {
		
        @Override
        public String getDisplayName() {
            return "Jenkow Build Wrapper"; 
        }

        @Override
        public boolean isApplicable(AbstractProject<?,?> item) {
            return true;
        }
    }
}
