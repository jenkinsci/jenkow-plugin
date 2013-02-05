package org.jenkinsci.plugins.jenkow.activiti.override;

import org.activiti.explorer.ui.alfresco.AlfrescoManagementMenuBar;
import org.activiti.explorer.ui.management.ManagementMenuBar;

/**
 * Get rid of the users and groups UI because we won't let those things edited in Activiti
 *
 * @author Kohsuke Kawaguchi
 * @see AlfrescoManagementMenuBar
 */
public class JenkinsManagementMenuBar extends ManagementMenuBar {
    protected void addUsersToolbarEntry() {
    }

    protected void addGroupToolbarEntry() {
    }
}
