package com.cisco.step.jenkins.plugins.jenkow;

import java.io.Serializable;
import java.util.Map;

public class JenkowProcessData implements Serializable{
	private static final String JENKOW_DATA_NAME = "jenkow_data";
	private String parentJobName;
	private Integer buildNumber;
	
	// TODO 9: make JobMD part of this hierarchy
	
	public String getParentJobName() {
    	return parentJobName;
    }
	public void setParentJobName(String parentJobName) {
    	this.parentJobName = parentJobName;
    }
	public Integer getBuildNumber() {
    	return buildNumber;
    }
	public void setBuildNumber(Integer buildNumber) {
    	this.buildNumber = buildNumber;
    }
	
	static void saveTo(Map<String,Object> varMap, JenkowProcessData jpd){
		varMap.put(JENKOW_DATA_NAME,jpd);
	}
}
