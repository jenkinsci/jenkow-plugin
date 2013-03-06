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
package com.cisco.surf.jenkins.plugins.jenkow.tests.DiagramGeneration;

import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;
import hudson.tasks.Shell;
import hudson.util.DescribableList;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import junitx.framework.FileAssert;

import org.apache.commons.io.IOUtils;

import com.cisco.step.jenkins.plugins.jenkow.JenkowBuilder;
import com.cisco.step.jenkins.plugins.jenkow.JenkowPlugin;
import com.cisco.step.jenkins.plugins.jenkow.JenkowTestCase;
import com.cisco.step.jenkins.plugins.jenkow.JenkowWorkflowRepository;
import com.google.common.io.NullOutputStream;

public class DiagramTest extends JenkowTestCase {
    
    public void testDiagramGeneration() throws Exception {
        JenkowWorkflowRepository repo = JenkowPlugin.getInstance().getRepo();
        repo.ensureWorkflowDefinition("wf1");
        
        FreeStyleProject launcher = createFreeStyleProject("j1");
        DescribableList<Builder,Descriptor<Builder>> bl = launcher.getBuildersList();
        bl.add(new JenkowBuilder("wf1"));
        bl.add(new Shell("echo wf.done"));
        configRoundtrip(launcher);  // work around the problem in the core of not registering our builder's project actions

        testImage("job/j1/jenkow/graph","default");
        testImage("job/j1/jenkow/graph/0","0");
        testImage("job/j1/jenkow/graph/wf1","wf1");

        testError("job/j1/jenkow/graph/1");
        testError("job/j1/jenkow/graph/no-such-workflow");
        testError("job/j1/jenkow/graph/WF1");
    }

    private void testImage(String path, String suffix) throws IOException {
        URL url = new URL(new WebClient().getContextPath()+path);
        System.out.println("url="+url);
        
        BufferedImage img = ImageIO.read(url);
        File pf = new File("target/test-artifacts/"+getTestName()+"-"+suffix+".png");
        pf.getParentFile().mkdirs();
        ImageIO.write(img,"png",pf);
        
        // TODO 5: maybe I can find a more robust way to diff the images
        // Different platforms produce slightly different PNGs
        // So for now I'm just comparing the geometries :-(
        BufferedImage ref = ImageIO.read(new File(getResource("ref.png")));
        assertEquals("wrong geometry",getGeo(ref),getGeo(img));
//        FileAssert.assertBinaryEquals("generated diagram file discrepancy: ",new File(getResource("ref.png")),pf);
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
    
    private static String getGeo(BufferedImage img){
        return img.getWidth()+"x"+img.getHeight();
    }
}
