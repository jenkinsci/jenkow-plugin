/*
 * The MIT License
 * 
 * Copyright (c) 2012, Cisco Systems, Inc., Max Spring
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.cisco.step.jenkins.plugins.jenkow;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

class EclipseResources {
    private static final Logger LOG = Logger.getLogger(EclipseResources.class.getName());
    static final String DIAGRAMS_SUBDIR = "src/main/resources/diagrams";
    static final String WORKFLOW_EXT = ".bpmn";
    
    void prepareProject(File dir){
        if (dir.exists()) return;
        
        LOG.info("creating workflow project at "+dir);
        new File(dir,DIAGRAMS_SUBDIR).mkdirs();

        try {
            FileUtils.writeStringToFile(new File(dir,".project")
                , "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<projectDescription>\n"
                + "	<name>jenkow-workflows</name>\n"
                + "	<comment>BPMN workflows used by Jenkins</comment>\n"
                + "	<projects>\n"
                + "	</projects>\n"
                + "	<buildSpec>\n"
                + "		<buildCommand>\n"
                + "			<name>org.eclipse.wst.common.project.facet.core.builder</name>\n"
                + "			<arguments>\n"
                + "			</arguments>\n"
                + "		</buildCommand>\n"
                + "		<buildCommand>\n"
                + "			<name>org.eclipse.jdt.core.javabuilder</name>\n"
                + "			<arguments>\n"
                + "			</arguments>\n"
                + "		</buildCommand>\n"
                + "	</buildSpec>\n"
                + "	<natures>\n"
                + "		<nature>org.eclipse.jdt.core.javanature</nature>\n"
                + "		<nature>org.eclipse.wst.common.project.facet.core.nature</nature>\n"
                + "	</natures>\n"
                + "</projectDescription>\n");

// TODO: use hudson.Util.expandMacro to expand occurrences of ${ade} to the actual value
//  move these files into resources
//            Util.expandMacro(str, Collections.singletonMap("ade",ade));
            String ade = getClass().getResource("/activiti-designer-extensions").getFile();
            FileUtils.writeStringToFile(new File(dir,".classpath")
                , "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<classpath>\n"
                + " <classpathentry excluding=\"**\" kind=\"src\" output=\"target/classes\" path=\"src/main/resources\"/>\n"
                + " <classpathentry kind=\"con\" path=\"org.eclipse.jdt.USER_LIBRARY/Activiti Designer Extensions\">\n"
                + "     <attributes>\n"
                + "         <attribute name=\"org.eclipse.jdt.launching.CLASSPATH_ATTR_LIBRARY_PATH_ENTRY\" value=\""+ade+"\"/>\n"
                + "     </attributes>\n"
                + " </classpathentry>\n"
                + " <classpathentry kind=\"output\" path=\"target/classes\"/>\n"
                + "</classpath>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    void ensureWorkflowDefinition(File dir, String wfName){
        try {
            if (new File(wfName).exists()) return;
            File f = new File(dir,mkWfPath(wfName));
            if (f.exists()) return;
            
            LOG.info("generating workflow definition "+f);
            dir.mkdirs();
            FileUtils.writeStringToFile(f
                , "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:activiti=\"http://activiti.org/bpmn\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:omgdc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:omgdi=\"http://www.omg.org/spec/DD/20100524/DI\" typeLanguage=\"http://www.w3.org/2001/XMLSchema\" expressionLanguage=\"http://www.w3.org/1999/XPath\" targetNamespace=\"http://www.activiti.org/test\">\n"
                + "  <process id=\""+wfName+"\" name=\""+wfName+"\">\n"
                + "    <startEvent id=\"startevent1\" name=\"Start\"></startEvent>\n"
                + "    <endEvent id=\"endevent1\" name=\"End\"></endEvent>\n"
                + "    <scriptTask id=\"scripttask1\" name=\"Script Task\" scriptFormat=\"groovy\">\n"
                + "      <script><![CDATA[console.println(\"Script Task says 'hello world'\");\n"
                + "]]></script>\n"
                + "    </scriptTask>\n"
                + "    <sequenceFlow id=\"flow1\" name=\"\" sourceRef=\"startevent1\" targetRef=\"scripttask1\"></sequenceFlow>\n"
                + "    <sequenceFlow id=\"flow2\" name=\"\" sourceRef=\"scripttask1\" targetRef=\"endevent1\"></sequenceFlow>\n"
                + "  </process>\n"
                + "  <bpmndi:BPMNDiagram id=\"BPMNDiagram_"+wfName+"\">\n"
                + "    <bpmndi:BPMNPlane bpmnElement=\""+wfName+"\" id=\"BPMNPlane_"+wfName+"\">\n"
                + "      <bpmndi:BPMNShape bpmnElement=\"startevent1\" id=\"BPMNShape_startevent1\">\n"
                + "        <omgdc:Bounds height=\"35\" width=\"35\" x=\"410\" y=\"60\"></omgdc:Bounds>\n"
                + "      </bpmndi:BPMNShape>\n"
                + "      <bpmndi:BPMNShape bpmnElement=\"endevent1\" id=\"BPMNShape_endevent1\">\n"
                + "        <omgdc:Bounds height=\"35\" width=\"35\" x=\"410\" y=\"240\"></omgdc:Bounds>\n"
                + "      </bpmndi:BPMNShape>\n"
                + "      <bpmndi:BPMNShape bpmnElement=\"scripttask1\" id=\"BPMNShape_scripttask1\">\n"
                + "        <omgdc:Bounds height=\"55\" width=\"105\" x=\"375\" y=\"140\"></omgdc:Bounds>\n"
                + "      </bpmndi:BPMNShape>\n"
                + "      <bpmndi:BPMNEdge bpmnElement=\"flow1\" id=\"BPMNEdge_flow1\">\n"
                + "        <omgdi:waypoint x=\"427\" y=\"95\"></omgdi:waypoint>\n"
                + "        <omgdi:waypoint x=\"427\" y=\"140\"></omgdi:waypoint>\n"
                + "      </bpmndi:BPMNEdge>\n"
                + "      <bpmndi:BPMNEdge bpmnElement=\"flow2\" id=\"BPMNEdge_flow2\">\n"
                + "        <omgdi:waypoint x=\"427\" y=\"195\"></omgdi:waypoint>\n"
                + "        <omgdi:waypoint x=\"427\" y=\"240\"></omgdi:waypoint>\n"
                + "      </bpmndi:BPMNEdge>\n"
                + "    </bpmndi:BPMNPlane>\n"
                + "  </bpmndi:BPMNDiagram>\n"
                + "</definitions>\n"
                );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String mkWfPath(String wfName){
        return DIAGRAMS_SUBDIR+"/"+wfName+WORKFLOW_EXT;
    }
}
