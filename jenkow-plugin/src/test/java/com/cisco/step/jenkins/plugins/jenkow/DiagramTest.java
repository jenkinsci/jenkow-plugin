package com.cisco.step.jenkins.plugins.jenkow;

import com.google.common.io.NullOutputStream;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;
import hudson.tasks.Shell;
import hudson.util.DescribableList;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

public class DiagramTest extends JenkowTestCase {
    
    public void testDiagramGeneration() throws Exception {
        JenkowWorkflowRepository repo = JenkowPlugin.getInstance().repo;
        repo.ensureWorkflowDefinition("wf1");
        
        FreeStyleProject launcher = createFreeStyleProject("j1");
        DescribableList<Builder,Descriptor<Builder>> bl = launcher.getBuildersList();
        bl.add(new JenkowBuilder("wf1"));
        bl.add(new Shell("echo wf.done"));
        configRoundtrip(launcher);  // work around the problem in the core of not registering our builder's project actions

        testImage("job/j1/jenkow/graph");
        testImage("job/j1/jenkow/graph/0");
        testImage("job/j1/jenkow/graph/wf1");

        testError("job/j1/jenkow/graph/1");
        testError("job/j1/jenkow/graph/no-such-workflow");
        testError("job/j1/jenkow/graph/WF1");
    }

    private void testError(String path) throws Exception{
        try {
            URL url = new URL(new WebClient().getContextPath()+path);
            IOUtils.copy(url.openStream(), new NullOutputStream());
            fail("Should have been 404");
        } catch (FileNotFoundException e) {
            // expected
        }
    }

    private void testImage(String path) throws IOException {
        URL url = new URL(new WebClient().getContextPath()+path);
        System.out.println("url="+url);
        BufferedImage img = ImageIO.read(url);
        System.out.println("img="+img);
    }
}
