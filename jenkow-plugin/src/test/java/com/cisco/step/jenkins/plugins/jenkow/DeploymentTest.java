package com.cisco.step.jenkins.plugins.jenkow;

import org.activiti.engine.repository.ProcessDefinition;

public class DeploymentTest extends JenkowTestCase {
    
    public void testDeployment() throws Exception{
        JenkowWorkflowRepository repo = JenkowPlugin.getInstance().repo;
        repo.ensureWorkflowDefinition("wf1");
        repo.ensureWorkflowDefinition("wf1");
        repo.ensureWorkflowDefinition("wf2");
        
        ProcessDefinition pDef = WfUtil.getDeployedWf("wf1");
        assertNotNull("process definition",pDef);
        assertEquals("process definition version",2,pDef.getVersion());
        
        ProcessDefinition pd2 = WfUtil.getDeployedWf("wf2");
        assertNotNull("process definition",pd2);
        assertEquals("process definition version",1,pd2.getVersion());

        assertNull("process definition",WfUtil.getDeployedWf("wf3"));
    }
}
