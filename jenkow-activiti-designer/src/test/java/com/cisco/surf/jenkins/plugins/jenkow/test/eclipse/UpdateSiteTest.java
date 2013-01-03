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
package com.cisco.surf.jenkins.plugins.jenkow.test.eclipse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

import org.apache.commons.io.IOUtils;
import org.jvnet.hudson.test.HudsonTestCase;

public class UpdateSiteTest extends HudsonTestCase{
    private final static String PLUGIN_NAME = "jenkow-activiti-designer";
    
    public void testCompositeSite() throws Exception{
        String s = readUrl("eclipse.site/compositeContent.xml");
        assertTrue(s.contains("<child location=\"../plugin/"+PLUGIN_NAME+"/eclipse.site/\"/>"));
    }
    
    public void testUpdateSite() throws Exception{
        URL u = new URL(getURL(),"plugin/"+PLUGIN_NAME+"/eclipse.site/content.jar");
        Set<String> names = new HashSet<String>();
        JarInputStream jis = new JarInputStream(u.openStream());
        try {
            while (true){
                ZipEntry ze = jis.getNextEntry();
                if (ze == null) break;
                names.add(ze.getName());
            }
        } finally {
            IOUtils.closeQuietly(jis);
        }
        assertTrue(names.contains("content.xml"));
    }
    
    private String readUrl(String relPath) throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        URL u = new URL(getURL(),relPath);
        System.out.println("fetching "+u);
        try {
            try {
                IOUtils.copy(u.openStream(),baos);
            } finally {
                IOUtils.closeQuietly(baos);
            }
        } finally {
            IOUtils.closeQuietly(u.openStream());
        }
        String s = baos.toString();
        System.out.println("<response>\n"+s+"\n</response>");
        return s;
    }
}
