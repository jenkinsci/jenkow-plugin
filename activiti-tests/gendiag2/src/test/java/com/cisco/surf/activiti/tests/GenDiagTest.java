package com.cisco.surf.activiti.tests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import junitx.framework.FileAssert;

import org.activiti.engine.RepositoryService;
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
    public void tstGenDiag() throws Exception {
        String wfd = "diagrams";
        String wfn = "GenDiag";
        String wfbn = "/"+wfd+"/"+wfn;
        String wffn = wfbn+".bpmn20.xml";
        
        RepositoryService repoSvc = activitiRule.getRepositoryService();
        DeploymentBuilder db = repoSvc
                               .createDeployment()
                               .addInputStream(wffn,this.getClass().getResourceAsStream(wffn));
        Deployment d = db.deploy();
        ProcessDefinition pDef = repoSvc
                                 .createProcessDefinitionQuery()
                                 .deploymentId(d.getId())
                                 .singleResult();
        
        String pn = wfn+"."+pDef.getKey()+".png";
        File tpf = new File("target/"+pn);
        
        InputStream ds = repoSvc.getResourceAsStream(d.getId(),"/"+wfd+"/"+pn);
        OutputStream os = new FileOutputStream(tpf);
        IOUtils.copy(ds,os);
        os.close();
        
        FileAssert.assertBinaryEquals(new File("src/test/resources/baseline/"+pn),tpf);
    }
}
