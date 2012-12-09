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
