package com.cisco.step.jenkins.plugins.jenkow;

import hudson.model.AbstractBuild;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

class BuildLoggerMap {
    private final static Map<String,PrintStream> parentLoggerMap = new HashMap<String,PrintStream>();

    static PrintStream get(String jobName, int buildNumber){
    	return parentLoggerMap.get(mkKey(jobName,buildNumber));
    }
    
    static PrintStream get(Object jobName, Object buildNumber){
    	if (jobName instanceof String && buildNumber instanceof Integer){
    		return get((String)jobName,((Integer)buildNumber).intValue());
    	}
    	return null;
    }
    
    static void put(AbstractBuild build, PrintStream logger){
    	parentLoggerMap.put(mkKey(build.getParent().getName(),build.getNumber()),logger);
    }
    
    static void remove(AbstractBuild build){
    	parentLoggerMap.remove(mkKey(build.getParent().getName(),build.getNumber()));
    }
    
    private static String mkKey(String jobName, int buildNumber){
    	return jobName+"|"+buildNumber;
    }
}
