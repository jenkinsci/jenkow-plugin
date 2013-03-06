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

import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.h2.Driver;
import org.jenkinsci.plugins.database.BasicDataSource2;
import org.jenkinsci.plugins.database.Database;
import org.jenkinsci.plugins.database.DatabaseDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Dummy {@link Database} instance that indicates we'll use
 *
 * @author Kohsuke Kawaguchi
 */
public class H2DemoDatabase extends Database {
    @DataBoundConstructor
    public H2DemoDatabase() {
    }

    @Override
    public DataSource getDataSource() throws SQLException {
        BasicDataSource2 ds = new BasicDataSource2();
        StandaloneInMemProcessEngineConfiguration master = new StandaloneInMemProcessEngineConfiguration();
        ds.setUrl(master.getJdbcUrl());
        ds.setUsername(master.getJdbcUsername());
        ds.setPassword(master.getJdbcPassword());
        ds.setDriverClass(Driver.class);
        return ds;
    }

    @Override
    public DatabaseDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    // @Extension --- not registering this globally because we only use this in JenkowBuilder
    public static class DescriptorImpl extends DatabaseDescriptor {
        @Override
        public String getDisplayName() {
            return "In-memory demo database";
        }
    }

    /*package*/ static DescriptorImpl DESCRIPTOR = new DescriptorImpl();
}
