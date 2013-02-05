package org.jenkinsci.plugins.jenkow.activiti.override;

import org.activiti.engine.ProcessEngine;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;

/**
 * @author Kohsuke Kawaguchi
 */
public class JenkinsProcessEngineFactory implements FactoryBean<ProcessEngine>, ServletContextAware {
    private ServletContext servletContext;

    @Override
    public ProcessEngine getObject() throws Exception {
        return (ProcessEngine)servletContext.getAttribute(ProcessEngine.class.getName());
    }

    @Override
    public Class<ProcessEngine> getObjectType() {
        return ProcessEngine.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
