package com.cisco.surf.activiti.tests;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.test.ActivitiRule;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;

public class GenDiagTest{
    
    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();
    
    @Test
    public void tstGenDiag() {
        String wffn = "/diagrams/GenDiag.bpmn20.xml";
        RepositoryService repoSvc = activitiRule.getRepositoryService();
        DeploymentBuilder db = repoSvc
                               .createDeployment()
                               .addInputStream(wffn,this.getClass().getResourceAsStream(wffn));
        Deployment d = db.deploy();
        ProcessDefinition pDef = repoSvc
                                 .createProcessDefinitionQuery()
                                 .deploymentId(d.getId())
                                 .singleResult();
        try {
            InputStream ds = ProcessDiagramGenerator.generatePngDiagram((ProcessDefinitionEntity)pDef);
            OutputStream os = new FileOutputStream("target/diag.png");
            IOUtils.copy(ds,os);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
