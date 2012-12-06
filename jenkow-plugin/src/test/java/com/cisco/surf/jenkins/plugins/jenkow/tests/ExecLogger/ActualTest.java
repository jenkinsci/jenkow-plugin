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
package com.cisco.surf.jenkins.plugins.jenkow.tests.ExecLogger;

import hudson.model.FreeStyleBuild;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;
import hudson.tasks.Shell;
import hudson.util.DescribableList;

import org.apache.commons.io.FileUtils;

import com.cisco.step.jenkins.plugins.jenkow.Consts;
import com.cisco.step.jenkins.plugins.jenkow.JenkowBuilder;
import com.cisco.step.jenkins.plugins.jenkow.JenkowTestCase;

public class ActualTest extends JenkowTestCase{

	public void testExecLogger() throws Exception{
		FreeStyleProject launchy = createFreeStyleProject("launchy");
        launchy.getBuildersList().add(new Shell("echo \"launchy running\"; sleep 1; exit 0"));
        
		FreeStyleProject launcher = createFreeStyleProject("launcher");
        DescribableList<Builder,Descriptor<Builder>> bl = launcher.getBuildersList();
		// TODO 4: if we have a way a script task can print into the job console, we no longer need the Shell build step
        bl.add(new JenkowBuilder(getWfName("workflow")));
        
        FreeStyleBuild build = launcher.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName()+" completed");

        String s = FileUtils.readFileToString(build.getLogFile());
        System.out.println("<console>\n"+s+"</console>");
        assertTrue(s.contains(Consts.UI_PREFIX+": scripttask1 started"));
        assertTrue(s.contains(Consts.UI_PREFIX+": scripttask1 ended"));
        assertTrue(s.contains(Consts.UI_PREFIX+": servicetask1 started"));
        assertTrue(s.contains(Consts.UI_PREFIX+": servicetask1 ended"));
	}
}
