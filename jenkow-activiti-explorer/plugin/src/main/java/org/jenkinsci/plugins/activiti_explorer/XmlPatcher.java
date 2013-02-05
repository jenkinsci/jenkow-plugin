package org.jenkinsci.plugins.activiti_explorer;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
abstract class XmlPatcher {
    private final File xml;
    protected Document dom;

    public XmlPatcher(File xml) throws IOException, DocumentException {
        this.xml = xml;
        run();
    }

    protected Element findBean(String id) {
        Element bean = (Element)dom.selectSingleNode(String.format("/*/*[@id='%s' or @name='%s']", id,id));
        if (bean==null)
            throw new IllegalStateException("Can't find the "+id+" bean in "+xml);
        return bean;
    }

    protected void overrideBeanTo(String id, String className) {
        Element pe = findBean(id);
        pe.elements().clear();
        pe.attributes().clear();
        pe.addAttribute("id", id);
        pe.addAttribute("class", className);
    }

    protected void swapClass(String id, String className) {
        Element pe = findBean(id);
        pe.addAttribute("class", className);
    }

    protected void removeBean(String id) {
        findBean(id).detach();
    }

    public abstract void patch();

    public void run() throws DocumentException, IOException {
        dom = new SAXReader().read(xml);

        patch();

        FileOutputStream out = new FileOutputStream(xml);
        try {
            new XMLWriter(out, OutputFormat.createPrettyPrint()).write(dom);
        } finally {
            out.close();
        }

    }
}
