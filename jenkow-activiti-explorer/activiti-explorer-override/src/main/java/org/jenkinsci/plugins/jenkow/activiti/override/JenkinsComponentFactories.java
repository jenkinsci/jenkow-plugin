package org.jenkinsci.plugins.jenkow.activiti.override;

import org.activiti.explorer.ComponentFactories;
import org.activiti.explorer.Environments;
import org.activiti.explorer.ui.custom.ToolBar;
import org.activiti.explorer.ui.mainlayout.MainMenuBarFactory;
import org.activiti.explorer.ui.management.ManagementMenuBarFactory;

/**
 * More hook for injecting custom UI components.
 *
 * @author Kohsuke Kawaguchi
 */
public class JenkinsComponentFactories extends ComponentFactories {
    public JenkinsComponentFactories() {
        // TODO: send patch to Activiti to clean up this hack
        factories.put(MainMenuBarFactory.class, new MainMenuBarFactory(){
            @Override
            protected Class getDefaultComponentClass() {
                return JenkinsMainMenuBar.class;
            }
        });
        factories.put(ManagementMenuBarFactory.class, new ManagementMenuBarFactory() {
            @Override
            protected Class<? extends ToolBar> getDefaultComponentClass() {
                return JenkinsManagementMenuBar.class;
            }
        });
        setEnvironment(Environments.ACTIVITI);
    }
}
