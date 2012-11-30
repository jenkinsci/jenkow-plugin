package com.cisco.surf.activiti.tests;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.io.IOUtils;

public class GenDiagTest extends TestCase{
    
    public void testGenDiag() throws Exception{
        ProcessEngine eng = ProcessEngineConfiguration
                            .createStandaloneInMemProcessEngineConfiguration()
                            .buildProcessEngine();
        
        System.out.println("engine version: "+eng.VERSION);
        
        RepositoryService repoSvc = eng.getRepositoryService();
        RuntimeService rtSvc = eng.getRuntimeService();
        
        String wfn = "/diagrams/GenDiag.bpmn";
        String wffn = wfn+"20.xml"; // workaround for http://forums.activiti.org/en/viewtopic.php?f=8&t=3745&start=10
        DeploymentBuilder db = repoSvc
                               .createDeployment()
                               .addInputStream(wffn,this.getClass().getResourceAsStream(wfn));
        
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
            throw e;
        }
    }
}
