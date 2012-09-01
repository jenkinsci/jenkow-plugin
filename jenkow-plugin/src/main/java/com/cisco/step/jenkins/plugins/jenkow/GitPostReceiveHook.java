package com.cisco.step.jenkins.plugins.jenkow;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.transport.PostReceiveHook;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;

class GitPostReceiveHook implements PostReceiveHook{
	
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
}
