package com.cisco.step.jenkins.plugins.jenkow.git;

import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;
import hudson.tasks.Shell;
import hudson.util.DescribableList;

import java.io.ByteArrayOutputStream;

import javax.inject.Inject;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.jenkinsci.main.modules.sshd.SSHD;

import com.cisco.step.jenkins.plugins.jenkow.JenkowTestCase;

public class GitTest extends JenkowTestCase{
	@Inject
	SSHD sshd;
	
	String git = System.getProperty("jenkow.test.git","git");

    @Override
    protected void setUp() throws Exception {
    	super.setUp();
		jenkins.getInjector().injectMembers(this);
    }

	public void testGitVersion() throws Exception{
		CommandLine cli = CommandLine.parse(git+" --version");
		DefaultExecutor executor = new DefaultExecutor();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		TeeOutputStream tos = new TeeOutputStream(baos,System.out);
		
		PumpStreamHandler sh = new PumpStreamHandler(tos);
		executor.setStreamHandler(sh);
		executor.execute(cli);
		tos.close();
		
		String output = baos.toString();
        System.out.println("<output>\n"+output+"</output>");
	}

	public void testGitClone() throws Exception{
		int port = sshd.getActualPort();
		System.out.println("***** sshd.getActualPort = "+port);
		
		String script 
		= "export GIT_SSH=$WORKSPACE/run-ssh.sh\n"
        + "\n"
        + "cat >$GIT_SSH <<EOF\n"
        + "#!/bin/sh\n"
        + "exec ssh -oStrictHostKeyChecking=no \"\\$@\"\n"
        + "EOF\n"
        + "chmod +x $GIT_SSH\n"
        + "\n"
        + "git clone ssh://localhost:"+port+"/jenkow-repository.git\n"
		;
		
		FreeStyleProject launcher = createFreeStyleProject("git-clone");
        DescribableList<Builder,Descriptor<Builder>> bl = launcher.getBuildersList();
		bl.add(new Shell(script));
        
        FreeStyleBuild build = launcher.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName()+" completed");

        String s = FileUtils.readFileToString(build.getLogFile());
        System.out.println("<output>\n"+s+"</output>");
        assertTrue(build.getResult() == Result.SUCCESS);
	}
}
