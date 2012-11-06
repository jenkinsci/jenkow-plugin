package com.cisco.step.jenkins.plugins.jenkow;

import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.TaskListener;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.jenkinsci.plugins.asyncjob.AsyncRun;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JenkowWorkflowRun extends AsyncRun<JenkowWorkflowJob,JenkowWorkflowRun> {

    public JenkowWorkflowRun(JenkowWorkflowJob job) throws IOException {
        super(job);
    }

    public JenkowWorkflowRun(JenkowWorkflowJob job, File buildDir) throws IOException {
        super(job,buildDir);
    }

    public void run() {
        run(new Runner() {
            @Override
            public Result run(BuildListener listener) throws Exception {
                PrintStream log = listener.getLogger();
                
                String workflowName = getParent().getWorkflowName();
                System.out.println("JenkowWorkflowRun: job.workflowName="+workflowName);
                
                ClassLoader previous = Thread.currentThread().getContextClassLoader();
                
                ProcessEngine eng = JenkowEngine.getEngine();
                RuntimeService rtSvc = eng.getRuntimeService();
                RepositoryService repoSvc = eng.getRepositoryService();
                
                try {
                    File wff = JenkowPlugin.getInstance().repo.getWorkflowFile(workflowName);
                    if (!wff.exists()){
                        log.println("error: "+wff+" does not exist");
                        return Result.FAILURE;
                    }
                    String wfn = wff+"20.xml"; // TODO 9: workaround for http://forums.activiti.org/en/viewtopic.php?f=8&t=3745&start=10
                    DeploymentBuilder db = repoSvc.createDeployment().addInputStream(wfn,new FileInputStream(wff));

                    // TODO 7: We should avoid redeploying here, if workflow is already deployed?
                    Deployment d = db.deploy();
                    ProcessDefinition pDef = repoSvc.createProcessDefinitionQuery().deploymentId(d.getId()).singleResult();

                    Map<String,Object> varMap = new HashMap<String,Object>();
                    // TODO 9: move jenkow variables into JenkowProcessData
                    varMap.put("jenkow_build_parent",getParent().getName());
                    varMap.put("jenkow_build_number",Integer.valueOf(getNumber()));
                    varMap.put("console",new ConsoleLogger(getParent().getName(),Integer.valueOf(getNumber())));
                    JobMD.setJobs(varMap,JobMD.newJobs());
                    
                    log.println(Consts.UI_PREFIX+": \""+workflowName+"\" started");
                    String procId = rtSvc.startProcessInstanceById(pDef.getId(),varMap).getId();
                    System.out.println("procId="+procId);
                } finally {
                    Thread.currentThread().setContextClassLoader(previous);
                }
                
                return Result.SUCCESS;
            }

            @Override
            public void post(BuildListener listener) throws Exception {
                System.out.println("JenkowWorkflowRun.run.Runner.post");
            }

            @Override
            public void cleanUp(BuildListener listener) throws Exception {
                System.out.println("JenkowWorkflowRun.run.Runner.cleanup");
            }
        });
    }

    public HttpResponse doComplete() throws IOException {
        System.out.println("JenkowWorkflowRun.doComplete");
        markCompleted();
        return HttpResponses.redirectToDot();
    }

    public HttpResponse doWriteLog() throws IOException {
        System.out.println("JenkowWorkflowRun.doWriteLog");
        TaskListener l = createListener();
        l.getLogger().println("Current time is "+new Date());
        return HttpResponses.redirectTo("console");
    }
}
