package com.cisco.step.jenkins.plugins.jenkow;

import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.remoting.Which;
import hudson.tasks.Builder;
import hudson.tasks.Shell;
import hudson.util.DescribableList;

import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;

public class DiagramTest extends JenkowTestCase {
    
    public void testDiagramGeneration() throws Exception {
        System.out.println("hello world");
        System.out.println(Which.jarFile(org.apache.xpath.compiler.FunctionTable.class));

        JenkowWorkflowRepository repo = JenkowPlugin.getInstance().repo;
        repo.ensureWorkflowDefinition("wf1");
        
        FreeStyleProject launcher = createFreeStyleProject("j1");
        DescribableList<Builder,Descriptor<Builder>> bl = launcher.getBuildersList();
        bl.add(new JenkowBuilder("wf1"));
        bl.add(new Shell("echo wf.done"));
        configRoundtrip(launcher);  // work around the problem in the core of not registering our builder's project actions

        URL url = new URL(new WebClient().getContextPath()+"job/j1/jenkow/graph");
        System.out.println("url="+url);
        BufferedImage img = ImageIO.read(url);
        System.out.println("img="+img);
    }
}
