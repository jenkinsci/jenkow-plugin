package com.cisco.surf.jenkins.plugins.jenkow.tests.DiagramGeneration;

import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.remoting.Which;
import hudson.tasks.Builder;
import hudson.tasks.Shell;
import hudson.util.DescribableList;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import javax.imageio.ImageIO;

import junitx.framework.FileAssert;

import com.cisco.step.jenkins.plugins.jenkow.JenkowBuilder;
import com.cisco.step.jenkins.plugins.jenkow.JenkowPlugin;
import com.cisco.step.jenkins.plugins.jenkow.JenkowTestCase;
import com.cisco.step.jenkins.plugins.jenkow.JenkowWorkflowRepository;

public class DiagramTest extends JenkowTestCase {
    
    public void testDiagramGeneration() throws Exception {
        
        
        JenkowWorkflowRepository repo = JenkowPlugin.getInstance().getRepo();
        repo.ensureWorkflowDefinition("wf1");
        
        FreeStyleProject launcher = createFreeStyleProject("j1");
        DescribableList<Builder,Descriptor<Builder>> bl = launcher.getBuildersList();
        bl.add(new JenkowBuilder("wf1"));
        bl.add(new Shell("echo wf.done"));
        configRoundtrip(launcher);  // work around the problem in the core of not registering our builder's project actions

        URL url = new URL(new WebClient().getContextPath()+"job/j1/jenkow/graph");
        System.out.println("url="+url);
        BufferedImage img = ImageIO.read(url);
        System.out.println("img="+img+" "+img.getWidth()+"x"+img.getHeight());
        
        File pf = new File("target/test-artifacts/"+getTestName()+".png");
        pf.getParentFile().mkdirs();
        ImageIO.write(img,"png",pf);
        
        FileAssert.assertBinaryEquals("generated diagram file discrepancy: ",new File(getResource("ref.png")),pf);
    }
}
