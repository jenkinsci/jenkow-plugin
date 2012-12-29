package com.cisco.step.jenkins.plugins.jenkow;

import hudson.model.FreeStyleBuild;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;
import hudson.tasks.Shell;
import hudson.util.DescribableList;

import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;

public class DiagramTest extends JenkowTestCase {
    
    public void testDiagramGeneration() throws Exception{
        System.out.println("hello world");
        
        JenkowWorkflowRepository repo = JenkowPlugin.getInstance().repo;
        repo.ensureWorkflowDefinition("wf1");
        
        FreeStyleProject launcher = createFreeStyleProject("j1");
        DescribableList<Builder,Descriptor<Builder>> bl = launcher.getBuildersList();
        bl.add(new JenkowBuilder("wf1"));
        bl.add(new Shell("echo wf.done"));
        FreeStyleBuild build = launcher.scheduleBuild2(0).get();
        
        try {
            // TODO kk? why does this give a valid stream only *after* I manually visit&save the job's configure page?
            URL url = new URL(new WebClient().getContextPath()+"job/j1/jenkow/graph");
            System.out.println("url="+url);
            BufferedImage img = ImageIO.read(url);
            System.out.println("img="+img);
        } catch (Exception e) {
            // eat the exception
            e.printStackTrace();
        }
    }
}
