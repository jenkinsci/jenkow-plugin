package com.cisco.step.jenkins.plugins.jenkow;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;

@Extension
public class JenkowUpdateSite implements UnprotectedRootAction{

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return "plugin/"+Consts.PLUGIN_NAME+"/eclipse.site";
    }
}
