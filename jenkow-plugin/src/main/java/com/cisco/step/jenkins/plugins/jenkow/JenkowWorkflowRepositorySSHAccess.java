package com.cisco.step.jenkins.plugins.jenkow;

import hudson.Extension;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

import javax.inject.Inject;

import jenkins.model.Jenkins;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.transport.PostReceiveHook;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.UploadPack;
import org.jenkinsci.plugins.gitserver.RepositoryResolver;

/**
 * Exposes this repository over SSH.
 *
 * @author Max Spring
 */
@Extension
public class JenkowWorkflowRepositorySSHAccess extends RepositoryResolver {
    @Inject
    JenkowWorkflowRepository repo;

    @Override
    public ReceivePack createReceivePack(String fullRepositoryName) throws IOException, InterruptedException {
        if (isMine(fullRepositoryName)) {
            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
            ReceivePack rp = repo.createReceivePack(repo.openRepository());
            rp.setPostReceiveHook(new PostReceiveHook() {
                public void onPostReceive(ReceivePack rp, Collection<ReceiveCommand> commands) {
                    try {
                        ResetCommand cmd = new Git(rp.getRepository()).reset();
                        cmd.setMode(ResetType.HARD);
                        cmd.setRef("master");
                        cmd.call();
                    } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        rp.sendMessage("Failed to update workspace: "+sw);
                    }
                }
            });
			return rp;
        }

        return null;
    }

    @Override
    public UploadPack createUploadPack(String fullRepositoryName) throws IOException, InterruptedException {
        if (isMine(fullRepositoryName))
            return new UploadPack(repo.openRepository());
        return null;
    }

    private boolean isMine(String name) {
        if (name.startsWith("/"))
            name = name.substring(1);
        return name.equals(JenkowWorkflowRepository.REPO_NAME+".git");
    }
}
