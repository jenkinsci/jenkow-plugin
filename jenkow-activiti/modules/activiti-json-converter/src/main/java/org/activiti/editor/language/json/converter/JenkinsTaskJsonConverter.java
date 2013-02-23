package org.activiti.editor.language.json.converter;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ImplementationType;
//import org.activiti.bpmn.model.JenkinsTask;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.UserTask;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

public class JenkinsTaskJsonConverter extends BaseBpmnJsonConverter {
	
	public final static String JSON_JENKINSTASK_JOB_NAME = "job_name";
	public final static String JSON_JENKINSTASK_MANUAL_LAUNCH_MODE = "is_manual_job_launch_mode";
	
	public final static String XML_JENKINSTASK_DELEGATE_CLASS = "com.cisco.step.jenkins.plugins.jenkow.JenkinsTaskDelegate";
	public final static String XML_JENKINSTASK_JOB_NAME = "jobName";
	public final static String XML_JENKINSTASK_MANUAL_LAUNCH_MODE = "isManualJobLaunchMode";

	public static void fillTypes(Map<String,
			              		 Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
			                     Map<Class<? extends BaseElement>, 
			                     Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
		fillJsonTypes(convertersToBpmnMap);
		fillBpmnTypes(convertersToJsonMap);
	}

	public static void fillJsonTypes(Map<String, 
			                         Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
		convertersToBpmnMap.put(STENCIL_TASK_JENKINS, JenkinsTaskJsonConverter.class);
	}

	public static void fillBpmnTypes(Map<Class<? extends BaseElement>, 
			                         Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
		// Because the Jenkins Task is mapped to a ServiceTask, the ServiceTaskJsonConverter has to map back
	}

	@Override
	protected String getStencilId(FlowElement flowElement) {
		return STENCIL_TASK_JENKINS;
	}

	@Override
	protected void convertElementToJson(ObjectNode propertiesNode, FlowElement flowElement) {
	  	ServiceTask serviceTask = (ServiceTask)flowElement;
	  	
		for (FieldExtension extension : serviceTask.getFieldExtensions()) {
			ObjectNode propertyItemNode = objectMapper.createObjectNode();
			String fn = extension.getFieldName();
			String val = extension.getStringValue();
			
			// TODO should have a better way to map these attributes!
			if (XML_JENKINSTASK_JOB_NAME.equals(fn)){
				propertiesNode.put(JSON_JENKINSTASK_JOB_NAME,val);
			}else if (XML_JENKINSTASK_MANUAL_LAUNCH_MODE.equals(fn)){
				propertiesNode.put(JSON_JENKINSTASK_MANUAL_LAUNCH_MODE,val);
			}
		}
	}

	@Override
	protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
		ServiceTask task = new ServiceTask();
		
	    task.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
	    task.setImplementation(XML_JENKINSTASK_DELEGATE_CLASS);
	    
	    String jobName = getPropertyValueAsString(JSON_JENKINSTASK_JOB_NAME, elementNode);
		if (StringUtils.isNotEmpty(jobName)) {
			FieldExtension f1 = new FieldExtension();
            f1.setFieldName(XML_JENKINSTASK_JOB_NAME);
            f1.setStringValue(jobName);
            task.getFieldExtensions().add(f1);
	    }	    
		
		FieldExtension f2 = new FieldExtension();
        f2.setFieldName(XML_JENKINSTASK_MANUAL_LAUNCH_MODE);
        f2.setStringValue(Boolean.toString(getPropertyValueAsBoolean(JSON_JENKINSTASK_MANUAL_LAUNCH_MODE, elementNode)));
        task.getFieldExtensions().add(f2);

        convertJsonToFormProperties(elementNode, task);
		return task;
	}
}
