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

import java.io.Serializable;
import java.util.logging.Logger;

public class JenkowEngineConfig implements Serializable{
    private static final long serialVersionUID = 877564473385436859L;
	private static final Logger LOG = Logger.getLogger(JenkowEngineConfig.class.getName());
    
	@Deprecated // if no engineConfig is there, internal DB is implied
	private boolean localDB;
	@Deprecated
	private String h2dbTcpPort; // why can't the port come from the dsUrl?
    private String dsDriverClass;
    private String dsUrl;
    private String dsUsername;
    private String dsPassword;

    @Deprecated
    transient private String mailServerHost;
    @Deprecated
    transient private int mailServerPort;    
    
    @Deprecated
    private String databaseSchemaUpdate;    
    @Deprecated
    private boolean jobExecutorActivate;
    @Deprecated
    private String history;

    public boolean isLocalDB() {
        return localDB;
    }

    public void setLocalDB(boolean localDB) {
        this.localDB = localDB;
    }

    public String getH2dbTcpPort() {
        return h2dbTcpPort;
    }

    public void setH2dbTcpPort(String h2dbTcpPort) {
        this.h2dbTcpPort = h2dbTcpPort;
    }

    public String getDsDriverClass() {
        return dsDriverClass;
    }

    public void setDsDriverClass(String dsDriverClass) {
        this.dsDriverClass = dsDriverClass;
    }

    public String getDsUrl() {
        return dsUrl;
    }

    public void setDsUrl(String dsUrl) {
        this.dsUrl = dsUrl;
    }

    public String getDsUsername() {
        return dsUsername;
    }

    public void setDsUsername(String dsUsername) {
        this.dsUsername = dsUsername;
    }

    public String getDsPassword() {
        return dsPassword;
    }

    public void setDsPassword(String dsPassword) {
        this.dsPassword = dsPassword;
    }

    public String getMailServerHost() {
        return mailServerHost;
    }
    
    public void setMailServerHost(String mailServerHost) {
        this.mailServerHost = mailServerHost;
    }

    public int getMailServerPort() {
        return mailServerPort;
    }

    public void setMailServerPort(int mailServerPort) {
        this.mailServerPort = mailServerPort;
    }
    
	public String getDatabaseSchemaUpdate() {
        return databaseSchemaUpdate;
    }

    public void setDatabaseSchemaUpdate(String databaseSchemaUpdate) {
        this.databaseSchemaUpdate = databaseSchemaUpdate;
    }

    public boolean isJobExecutorActivate() {
        return jobExecutorActivate;
    }

    public void setJobExecutorActivate(boolean jobExecutorActivate) {
        this.jobExecutorActivate = jobExecutorActivate;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }
    
    public void print() {
        LOG.info("\nBpmnConfig "
                + "\n[localDB=" + localDB + ",\n h2dbTcpPort=" + h2dbTcpPort
                + ",\n dsDriverClass=" + dsDriverClass + ",\n dsUrl=" + dsUrl
                + ",\n dsUsername=" + dsUsername + ",\n dsPassword=" + "****"
                + ",\n databaseSchemaUpdate=" + databaseSchemaUpdate
                + ",\n mailServerHost=" + mailServerHost
                + ",\n mailServerPort=" + mailServerPort
                + ",\n jobExecutorActivate=" + jobExecutorActivate
                + ",\n history=" + history + "]\n");
    }
    
    public static JenkowEngineConfig getDefault() {
    	JenkowEngineConfig cfg = new JenkowEngineConfig();
    	String h2Port = "9092";
    	cfg.setLocalDB(true);
    	cfg.setH2dbTcpPort("9092");
    	cfg.setDsDriverClass("org.h2.Driver");
    	cfg.setDsUrl("jdbc:h2:tcp://localhost:"+h2Port+"/activiti");
    	cfg.setDsUsername("sa");
    	cfg.setDsPassword("");
    	
    	cfg.setJobExecutorActivate(true);
    	cfg.setDatabaseSchemaUpdate("true");
    	cfg.setHistory("full");
    	
    	cfg.setMailServerHost("localhost");
    	cfg.setMailServerPort(25);
    	
    	return cfg;
    }
}
