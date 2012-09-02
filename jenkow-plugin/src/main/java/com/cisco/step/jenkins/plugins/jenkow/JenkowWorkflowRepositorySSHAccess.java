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

import java.io.IOException;

import javax.inject.Inject;

import jenkins.model.Jenkins;

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
            rp.setPostReceiveHook(new GitPostReceiveHook());
			return rp;
        }
        return null;
    }

    @Override
    public UploadPack createUploadPack(String fullRepositoryName) throws IOException, InterruptedException {
        if (isMine(fullRepositoryName)) return new UploadPack(repo.openRepository());
        return null;
    }

    private boolean isMine(String name) {
        if (name.startsWith("/")) name = name.substring(1);
        return name.equals(Consts.REPO_NAME+".git");
    }
}
