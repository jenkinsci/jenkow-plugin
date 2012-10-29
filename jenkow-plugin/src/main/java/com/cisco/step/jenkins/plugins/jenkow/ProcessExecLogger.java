package com.cisco.step.jenkins.plugins.jenkow;

import java.util.logging.Logger;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

public class ProcessExecLogger  implements ExecutionListener{
    private static final Logger LOG = Logger.getLogger(TaskExecLogger.class.getName());
    private String name;
    
    ProcessExecLogger(String name) {
        this.name = name;
    }

    @Override
    public void notify(DelegateExecution exec) throws Exception {
        System.out.println("*** ProcessExecLogger: "+name+" ended");
        System.out.println("name                          = "+name);
        System.out.println("exec.getEventName()          -> "+exec.getEventName());
        System.out.println("exec.getId()                 -> "+exec.getId());
        System.out.println("exec.getProcessBusinessKey() -> "+exec.getProcessBusinessKey());
        System.out.println("exec.getProcessInstanceId()  -> "+exec.getProcessInstanceId());
    }
}
