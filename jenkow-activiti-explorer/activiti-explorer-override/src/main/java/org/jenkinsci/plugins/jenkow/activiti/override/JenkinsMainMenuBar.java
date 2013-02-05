package org.jenkinsci.plugins.jenkow.activiti.override;

import org.activiti.explorer.ui.alfresco.AlfrescoMainMenuBar;
import org.activiti.explorer.ui.mainlayout.MainMenuBar;

/**
 * Customize the main menu bar to get rid of the profile link
 *
 * @author Kohsuke Kawaguchi
 * @see AlfrescoMainMenuBar
 */
public class JenkinsMainMenuBar extends MainMenuBar {
    @Override
    protected boolean useProfile() {
        // profile is not editable
        return false;
    }
}
