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
package com.cisco.step.jenkins.plugins.jenkow.designer;

import org.activiti.designer.integration.servicetask.AbstractCustomServiceTask;
import org.activiti.designer.integration.servicetask.PropertyType;
import org.activiti.designer.integration.servicetask.annotation.Help;
import org.activiti.designer.integration.servicetask.annotation.Property;
import org.activiti.designer.integration.servicetask.annotation.PropertyItems;
import org.activiti.designer.integration.servicetask.annotation.Runtime;

@Runtime(delegationClass = "com.cisco.step.jenkins.plugins.jenkow.JenkinsTaskDelegate")
@Help(displayHelpShort = "Executes a Jenkins job")
public class JenkinsTask extends AbstractCustomServiceTask {

    @Property(type = PropertyType.TEXT, displayName = "Job Name")
    @Help(displayHelpShort = "Name of the Jenkins job to execute.\n"
                           + "Job must exist at the time of task activation.\n"
                           + "If job does not exist, task will complete without error.\n")
    private String jobName = "${executionContext.host}";

    @Property(type = PropertyType.BOOLEAN_CHOICE, displayName = "Manual Job Launch")
    @Help(displayHelpShort = "In manual job launch mode, the user needs to manually launch the Jenkins job.\n")
    private Boolean isManualJobLaunchMode;

    @Override
    public String contributeToPaletteDrawer() {
        return "Jenkow";
    }

    @Override
    public String getName() {
        return "Jenkins Task";
    }

    @Override
    public String getLargeIconPath() {
        return super.getLargeIconPath();
    }

    @Override
    public String getShapeIconPath() {
        return super.getShapeIconPath();
    }

    @Override
    public String getSmallIconPath() {
        return "jenkins.png";
    }
}
