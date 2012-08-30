package com.cisco.step.jenkins.plugins.jenkow;

import hudson.Extension;
import hudson.model.RootAction;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.http.server.resolver.DefaultReceivePackFactory;
import org.eclipse.jgit.http.server.resolver.DefaultUploadPackFactory;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PostReceiveHook;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.UploadPack;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import org.jenkinsci.main.modules.sshd.SSHD;
import org.jenkinsci.plugins.gitserver.HttpGitRepository;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

/**
 * @author Kohsuke Kawaguchi
 */
@Extension
public class JenkowWorkflowRepository extends HttpGitRepository implements RootAction {
    static final String REPO_NAME = "jenkow-workflows";
    
	@Inject
    public SSHD sshd;

    @Override
    public Repository openRepository() throws IOException {
        File userContent = new File(Jenkins.getInstance().root, REPO_NAME);
        FileRepository r = new FileRepositoryBuilder().setWorkTree(userContent).build();

        // if the repository doesn't exist, create it
        if (!r.getObjectDatabase().exists())
            r.create();
        return r;
    }

    /**
     * Requires the admin access to be able to push
     */
    @Override
    public ReceivePack createReceivePack(HttpServletRequest context, Repository db) throws ServiceNotEnabledException, ServiceNotAuthorizedException {
        Authentication a = Jenkins.getAuthentication();

        ReceivePack rp = createReceivePack(db);

        rp.setRefLogIdent(new PersonIdent(a.getName(), a.getName()+"@"+context.getRemoteAddr()));

        return rp;
    }

    ReceivePack createReceivePack(Repository db) {
        Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);

        ReceivePack rp = new ReceivePack(db);

        // update userContent after the push
        rp.setPostReceiveHook(new PostReceiveHook() {
            public void onPostReceive(ReceivePack rp, Collection<ReceiveCommand> commands) {
                try {
                    CheckoutCommand co = new Git(rp.getRepository()).checkout();
                    co.setForce(true);
                    co.setName("master");
                    co.call();
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    rp.sendMessage("Failed to update workspace: "+sw);
                }
            }
        });
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
        return REPO_NAME+".git";
    }
}
