package com.cisco.step.jenkins.plugins.jenkow.git;

import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.output.TeeOutputStream;

public class GitTest extends TestCase{
	
	public void testGitItself() throws Exception{
		CommandLine cli = CommandLine.parse("git --version");
		DefaultExecutor executor = new DefaultExecutor();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		TeeOutputStream tos = new TeeOutputStream(baos,System.out);
		
		PumpStreamHandler sh = new PumpStreamHandler(tos);
		executor.setStreamHandler(sh);
		executor.execute(cli);
		tos.close();
		
		String output = baos.toString();
	}
}
