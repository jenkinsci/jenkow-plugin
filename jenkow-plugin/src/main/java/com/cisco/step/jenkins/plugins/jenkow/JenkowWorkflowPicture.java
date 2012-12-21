package com.cisco.step.jenkins.plugins.jenkow;

import hudson.model.Action;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Kohsuke Kawaguchi
 */
public class JenkowWorkflowPicture implements Action, HttpResponse {
    JenkowBuilder builder;

    public JenkowWorkflowPicture(JenkowBuilder builder) {
        this.builder = builder;
    }

    public JenkowWorkflowPicture doGraph(StaplerResponse rsp) {
        return this;
    }

    private void generateTo(OutputStream os) {
        // TODO:
    }

    @Override
    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
        rsp.setContentType("image/png");
        generateTo(rsp.getOutputStream());
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return "jenkow";
    }
}
