package com.cisco.step.jenkins.plugins.jenkow;

import org.jenkinsci.plugins.database.Database;
import org.jenkinsci.plugins.database.DatabaseDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author Kohsuke Kawaguchi
 */
public class H2DemoDatabase extends Database {
    @DataBoundConstructor
    public H2DemoDatabase() {
    }

    @Override
    public DataSource getDataSource() throws SQLException {
        return null;
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
