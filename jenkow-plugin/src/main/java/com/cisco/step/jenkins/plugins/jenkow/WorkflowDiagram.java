package com.cisco.step.jenkins.plugins.jenkow;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Represents a PNG image of the workflow diagram.
 *
 * @author Kohsuke Kawaguchi
 */
public class WorkflowDiagram implements HttpResponse {
    private final String workflowName;

    public WorkflowDiagram(String workflowName) {
        this.workflowName = workflowName;
    }

    @Override
    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
        // TODO: send out proper HTTP cache
        rsp.setContentType("image/png");
        WfUtil.generateWfDiagramTo(workflowName,rsp.getOutputStream());
    }
}
