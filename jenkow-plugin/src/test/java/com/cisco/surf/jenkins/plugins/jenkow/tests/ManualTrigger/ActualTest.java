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
package com.cisco.surf.jenkins.plugins.jenkow.tests.ManualTrigger;

import hudson.model.FreeStyleBuild;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;
import hudson.tasks.Shell;
import hudson.util.DescribableList;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;

import com.cisco.step.jenkins.plugins.jenkow.JenkowBuilder;
import com.cisco.step.jenkins.plugins.jenkow.JenkowTestCase;

public class ActualTest extends JenkowTestCase{
    
    public void testManualTriggerSuccess() throws Exception{
        setNumExecutors(10);
        
        FreeStyleProject one = createFreeStyleProject("one");
        one.getBuildersList().add(new Shell("echo \"one running\"; sleep 5; exit 0"));
        
        FreeStyleProject two = createFreeStyleProject("two");
        two.getBuildersList().add(new Shell("echo \"two running\"; sleep 8; exit 0"));
        
        FreeStyleProject launcher = createFreeStyleProject("launcher");
        DescribableList<Builder,Descriptor<Builder>> bl = launcher.getBuildersList();
        bl.add(new JenkowBuilder(getWfName("workflow")));
        bl.add(new Shell("echo wf.done"));
        
        Future<FreeStyleBuild> launcherBF = launcher.scheduleBuild2(0);
        
        Thread.sleep(5000);
        one.scheduleBuild2(0);
        two.scheduleBuild2(0);
        
        FreeStyleBuild build = launcherBF.get(30,TimeUnit.SECONDS);
        System.out.println(build.getDisplayName()+" completed");

        String s = FileUtils.readFileToString(build.getLogFile());
        assertTrue(s.contains("+ echo wf.done"));
    }

    public void testManualTriggerTimeout() throws Exception{
        FreeStyleProject one = createFreeStyleProject("one");
        one.getBuildersList().add(new Shell("echo \"one running\"; sleep 5; exit 0"));
        
        FreeStyleProject two = createFreeStyleProject("two");
        two.getBuildersList().add(new Shell("echo \"two running\"; sleep 8; exit 0"));
        
        FreeStyleProject launcher = createFreeStyleProject("launcher");
        DescribableList<Builder,Descriptor<Builder>> bl = launcher.getBuildersList();
        bl.add(new JenkowBuilder(getWfName("workflow")));
        bl.add(new Shell("echo wf.done"));
        
        Future<FreeStyleBuild> launcherBF = launcher.scheduleBuild2(0);
      
        Thread.sleep(5000);
        one.scheduleBuild2(0);
        // two.scheduleBuild2(0); // no triggering to force a timeout
      
        try {
            FreeStyleBuild build = launcherBF.get(30,TimeUnit.SECONDS);
            System.out.println(build.getDisplayName()+" completed");
            fail("should have timed out");
        } catch (TimeoutException e) {
            return;
        }
    }
}
