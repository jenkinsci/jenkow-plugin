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

import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
import hudson.model.RootAction;
import hudson.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import jenkins.model.Jenkins;

import org.acegisecurity.Authentication;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.http.server.resolver.DefaultUploadPackFactory;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.UploadPack;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import org.jenkinsci.main.modules.sshd.SSHD;
import org.jenkinsci.plugins.gitserver.HttpGitRepository;

/**
 * @author Max Spring
 */
@Extension
public class JenkowWorkflowRepository extends HttpGitRepository implements RootAction {
    private static final Logger LOGGER = Logger.getLogger(JenkowWorkflowRepository.class.getName());
    
	@Inject
    public SSHD sshd;
	
	@Inject
	JenkowPlugin plugin;
	
    public JenkowWorkflowRepository() {
    	JenkowPlugin.getInstance().repo = this;
	}
    
	File getWorkflowFile(String wfName){
		File f = new File(wfName);
		if (!f.isAbsolute() || !f.exists()){
			String relName = mkWfPath(wfName);
			f = new File(getRepositoryDir(),relName);
		}
		return f;
    }
	
    void ensureWorkflowDefinition(String wfName) throws Exception{
        // due to lazy loading of engine:
        // the engine startup would try to deploy the file we're about to create, leading to a double deploy
        JenkowEngine.getEngine();

        String relName = mkWfPath(wfName);
		File wff = new File(getRepositoryDir(),relName);
		if (!wff.exists()){
		    LOGGER.info("generating workflow definition "+wff);
		    Map<String,String> vars = new HashMap<String,String>();
		    vars.put("WORKFLOW_ID",WfUtil.mkWorkflowId(wfName));
		    vars.put("WORKFLOW_NAME",wfName);
		    String wfd = IOUtils.toString(getClass().getResourceAsStream("/templates/new.bpmn"));
		    wfd = Util.replaceMacro(wfd,vars);
		    FileUtils.writeStringToFile(wff,wfd);
		    
		    Repository r = openRepository();
		    addAndCommit(r,relName,"added generated workflow definition "+wfName);
		    r.close();
		}
		deployToEngine(wff);
    }
    
    void deployAllToEngine(){
        File repoDir = getRepositoryDir();
        if (!repoDir.exists()){
            LOGGER.info("no workflow source repository");
            return;
        }
        
        LOGGER.info("deploying all workflow engine");
        
        RepositoryService repoSvc = JenkowEngine.getEngine().getRepositoryService();
        Map<String,Date> deplTimes = new HashMap<String,Date>();
        for (Deployment depl : repoSvc.createDeploymentQuery().list()){
            //System.out.println("  depl: id="+depl.getId()+" name="+depl.getName()+" time="+depl.getDeploymentTime());
            deplTimes.put(depl.getId(),depl.getDeploymentTime());
        }
        Map<String,Date> pDefTimes = new HashMap<String,Date>();
        for (ProcessDefinition pDef : repoSvc.createProcessDefinitionQuery().latestVersion().list()){
            //System.out.println(" pDef:"+pDef+" deplId="+pDef.getDeploymentId()+" key="+pDef.getKey());
            Date t = deplTimes.get(pDef.getDeploymentId());
            if (t != null) pDefTimes.put(pDef.getKey(),t);
        }
        
        for (Iterator it=FileUtils.iterateFiles(repoDir,new String[]{Consts.WORKFLOW_EXT},/*recursive=*/true); it.hasNext(); ){
            File wff = (File)it.next();
            String wfn = wff.getName();
            int p = wfn.lastIndexOf('.');
            if (p > -1) wfn = wfn.substring(0,p);
            Date prevDeplTime = pDefTimes.get(wfn);
            //System.out.println("  f="+wff+" wfn="+wfn+" deplTime="+prevDeplTime+" wff.lastModified="+new Date(wff.lastModified()));
            if (prevDeplTime == null || prevDeplTime.before(new Date(wff.lastModified()))){
                try {
                    deployToEngine(wff);
                } catch (FileNotFoundException e) {
                    LOGGER.log(Level.SEVERE,"file not found "+wff,e);
                }
            }
        }
    }
    
    void deployToEngine(File wff) throws FileNotFoundException{
        LOGGER.info("deploying "+wff+" to workflow engine");
        
        ProcessEngine eng = JenkowEngine.getEngine();
        RuntimeService rtSvc = eng.getRuntimeService();
        RepositoryService repoSvc = eng.getRepositoryService();

        String wfn = wff+"20.xml"; // TODO 9: workaround for http://forums.activiti.org/en/viewtopic.php?f=8&t=3745&start=10
        DeploymentBuilder db = repoSvc.createDeployment().addInputStream(wfn,new FileInputStream(wff));

        // TODO 4: We should avoid redeploying here, if workflow file of a given version(?) is already deployed?
        Deployment d = db.deploy();
        ProcessDefinition pDef = repoSvc.createProcessDefinitionQuery().deploymentId(d.getId()).singleResult();
        LOGGER.fine("deployedToEngine("+wff+") --> "+pDef);
    }
    
    ProcessDefinition getDeployedWf(String wfName){
        RepositoryService repoSvc = JenkowEngine.getEngine().getRepositoryService();
        LOGGER.fine("deployed process definitions:");
        for (ProcessDefinition pDef : repoSvc.createProcessDefinitionQuery().latestVersion().list()){
            LOGGER.fine("  pDef:"+pDef+" id="+pDef.getId()+" key="+pDef.getKey()+" name="+pDef.getName()+" resourceName="+pDef.getResourceName()+" version="+pDef.getVersion());
        }
        ProcessDefinition pDef = repoSvc
                                 .createProcessDefinitionQuery()
                                 .processDefinitionKey(wfName)
                                 .latestVersion()
                                 .singleResult();
        LOGGER.fine("getDeployedWf("+quoted(wfName)+") --> "+pDef);
        return pDef;
    }
    
    public static File getRepositoryDir(){
    	return new File(Jenkins.getInstance().root,Consts.REPO_NAME);
    }
    
    private static String mkWfPath(String wfName){
        return Consts.WF_PROJ_NAME+"/"+Consts.DIAGRAMS_SUBDIR+"/"+wfName+Consts.WORKFLOW_DOT_EXT;
    }
    
	@Override
    public Repository openRepository() throws IOException {
        File rd = getRepositoryDir();
        // TODO 7: should we cache r here?  Who will be closing r?
        FileRepository r = new FileRepositoryBuilder().setWorkTree(rd).build();

        if (!r.getObjectDatabase().exists()){
        	r.create();
        	
        	try {
				new FilePath(rd).untarFrom(JenkowWorkflowRepository.class.getResourceAsStream("/jenkow-repository-seed.tar"),FilePath.TarCompression.NONE);
			} catch (InterruptedException e1) {
                LOGGER.log(Level.WARNING, "Seeding of jenkow-repository failed",e1);
			}
        	
            addAndCommit(r,".","Initial import of the existing contents");
        }
        return r;
    }
	
	private void addAndCommit(Repository r, String filePattern, String msg) throws IOException{
        try {
            Git git = new Git(r);
            AddCommand cmd = git.add();
            cmd.addFilepattern(filePattern);
            cmd.call();

            CommitCommand co = git.commit();
            co.setAuthor("Jenkow","noreply@jenkins-ci.org");
            co.setMessage(msg);
            co.call();
        } catch (GitAPIException e) {
            LOGGER.log(Level.WARNING, "Adding seeded jenkow-repository content to Git failed",e);
        }
	}

    /**
     * Requires the admin access to be able to push
     */
    @Override
    public ReceivePack createReceivePack(HttpServletRequest context, Repository db) throws ServiceNotEnabledException, ServiceNotAuthorizedException {
        Authentication a = Jenkins.getAuthentication();
        ReceivePack rp = createReceivePack(db);
        rp.setRefLogIdent(new PersonIdent(a.getName(),a.getName()+"@"+context.getRemoteAddr()));
        return rp;
    }

    ReceivePack createReceivePack(Repository db) {
        Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
        ReceivePack rp = new ReceivePack(db);
        rp.setPostReceiveHook(new GitPostReceiveHook());
        return rp;
    }

    /**
     * But pull access is open to anyone
     */
    @Override
    public UploadPack createUploadPack(HttpServletRequest context, Repository db) throws ServiceNotEnabledException, ServiceNotAuthorizedException {
        return new DefaultUploadPackFactory().create(context,db);
    }

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return Consts.REPO_NAME+".git";
    }
    
    public static String quoted(String s){
        return (s == null)? null : "\""+s+"\"";
    }
}
