package com.cisco.surf.activiti.tests;

import java.util.concurrent.TimeoutException;

import junit.framework.TestCase;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;

public class WfSleepTest extends TestCase{
	
	public void testWfSleep() throws Exception{
		ProcessEngine eng = ProcessEngineConfiguration
							.createStandaloneInMemProcessEngineConfiguration()
							.setJobExecutorActivate(true)
							.buildProcessEngine();
		
		System.out.println("engine version: "+eng.VERSION);
		
		RepositoryService repoSvc = eng.getRepositoryService();
		RuntimeService rtSvc = eng.getRuntimeService();
		
		String wfn = "/diagrams/WfSleep.bpmn";
		String wffn = wfn+"20.xml"; // workaround for http://forums.activiti.org/en/viewtopic.php?f=8&t=3745&start=10
        DeploymentBuilder db = repoSvc
							   .createDeployment()
							   .addInputStream(wffn,this.getClass().getResourceAsStream(wfn));
		
        Deployment d = db.deploy();
        ProcessDefinition pDef = repoSvc
                                 .createProcessDefinitionQuery()
                                 .deploymentId(d.getId())
                                 .singleResult();

        ProcessInstance proc = rtSvc.startProcessInstanceById(pDef.getId());
        
        String procId = proc.getId();
        System.out.println("procId="+procId);
        
        long timeout = System.currentTimeMillis()+15*1000;
        HistoryService hstSvc = eng.getHistoryService();
        while (true){
            if (System.currentTimeMillis() > timeout) throw new TimeoutException();
            HistoricProcessInstance hProcInst = hstSvc.createHistoricProcessInstanceQuery().processInstanceId(procId).singleResult();
            if (hProcInst.getEndTime() != null){
                System.out.println(wfn+" ended");
                break;
            }
            Thread.sleep(1000);
        }
	}
}
