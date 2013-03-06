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
package com.cisco.step.jenkins.plugins.jenkow.git;

import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;
import hudson.tasks.Shell;
import hudson.util.DescribableList;

import java.io.File;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jenkinsci.main.modules.sshd.SSHD;

import com.cisco.step.jenkins.plugins.jenkow.JenkowTestCase;
import com.cisco.step.jenkins.plugins.jenkow.JenkowWorkflowRepository;

public class GitTest extends JenkowTestCase{
	@Inject
	SSHD sshd;
	
	String git = System.getProperty("jenkow.test.git","git");

    @Override
    protected void setUp() throws Exception {
    	super.setUp();
		jenkins.getInjector().injectMembers(this);
    }
    
    // Several git-related test cases in one Jenkins instance
    public void testGit() throws Exception{
	    // Just to see whether there's a git we can run.
	    // It'll barf if we don't.
	    runTest("version",git+" --version");
		
		String tag = "<added by unit test/>";
		runTest("clone-modify-commit-push"
			   , git+" clone ssh://localhost:"+sshd.getActualPort()+"/jenkow-repository.git\n"
			   + "cd jenkow-repository\n"
			   + "echo \""+tag+"\" >>readme\n"
			   + git+" commit -am \"changed readme\"\n"
			   + git+" push\n"
			   );
		String readme = FileUtils.readFileToString(new File(JenkowWorkflowRepository.getRepositoryDir(),"readme"));
        assertTrue(readme.contains(tag));
	}

	private void runTest(String id, String cmd) throws Exception{
        // The GIT_SSH is needed so we can do the StrictHostKeyChecking=no, 
	    // otherwise ssh would prompt for the "new" host key.
	    String script = "export GIT_SSH=$WORKSPACE/run-ssh.sh\n"
	                  + "\n"
	                  + "cat >$GIT_SSH <<EOF\n" 
	                  + "#!/bin/sh\n"
	                  + "exec ssh -oStrictHostKeyChecking=no \"\\$@\"\n" 
	                  + "EOF\n"
	                  + "chmod +x $GIT_SSH\n" 
                      + "\n" 
	                  + cmd+"\n";
		
		FreeStyleProject launcher = createFreeStyleProject(id);
        DescribableList<Builder,Descriptor<Builder>> bl = launcher.getBuildersList();
		bl.add(new Shell(script));
        
        FreeStyleBuild build = launcher.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName()+" completed");

        String s = FileUtils.readFileToString(build.getLogFile());
        System.out.println("<output>\n"+s+"</output>");
        assertTrue(build.getResult() == Result.SUCCESS);
	}
}
