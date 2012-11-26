package org.jenkinsci.plugins.jenkow.activiti.override;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

/**
 * Returns {@link DataSource} injected through {@link ServletContext} attribute.
 *
 * This is how the jenkow plugin passes in the {@link DataSource} to the embedded activiti explorer.
 *
 * @author Kohsuke Kawaguchi
 */
public class ServletContextDataSource implements FactoryBean, ServletContextAware {
    private ServletContext servletContext;

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public Object getObject() throws Exception {
        Object o = servletContext.getAttribute(ServletContextDataSource.class.getName());
        if (o==null)
            throw new IllegalStateException("ServletContext doesn't contain a DataSource");
        return o;
    }

    @Override
    public Class<?> getObjectType() {
        return DataSource.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
