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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import jenkins.model.Jenkins;

import org.acegisecurity.Authentication;
import org.apache.commons.io.FileUtils;
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
	
    void ensureWorkflowDefinition(String wfName){
    	String relName = mkWfPath(wfName);
		File wff = new File(getRepositoryDir(),relName);
		if (wff.exists()) return;
		
		LOGGER.info("generating workflow definition "+wff);
		
		try {
			Map<String,String> vars = new HashMap<String,String>();
			vars.put("WORKFLOW_ID",WfUtil.mkWorkflowId(wfName));
			vars.put("WORKFLOW_NAME",wfName);
			String wfd = IOUtils.toString(getClass().getResourceAsStream("/templates/new.bpmn"));
			wfd = Util.replaceMacro(wfd,vars);
			FileUtils.writeStringToFile(wff,wfd);
			
			Repository r = openRepository();
			addAndCommit(r,relName,"added generated workflow definition "+wfName);
			r.close();
		} catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
		}
    }
    
    File getRepositoryDir(){
    	return new File(Jenkins.getInstance().root,Consts.REPO_NAME);
    }
    
    private static String mkWfPath(String wfName){
        return Consts.WF_PROJ_NAME+"/"+Consts.DIAGRAMS_SUBDIR+"/"+wfName+Consts.WORKFLOW_EXT;
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
}
