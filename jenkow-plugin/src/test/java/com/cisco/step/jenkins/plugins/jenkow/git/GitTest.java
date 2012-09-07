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
