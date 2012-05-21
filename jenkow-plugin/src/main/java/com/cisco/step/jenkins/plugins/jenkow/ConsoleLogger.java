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
package com.cisco.step.jenkins.plugins.jenkow;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Locale;

public class ConsoleLogger implements Serializable{
    private static final long serialVersionUID = -2374671270393369824L;
    private String parentProject;
    private Integer parentBuildNo;
    
    ConsoleLogger(String parentProject, Integer parentBuildNo){
        this.parentProject = parentProject;
        this.parentBuildNo = parentBuildNo;
    }
    
    public void flush(){
        getPrintStream().flush();
    }

    public PrintStream printf(String format, Object... args){
        return getPrintStream().printf(format,args);
    }

    public PrintStream printf(Locale l, String format, Object... args){
        return getPrintStream().printf(l,format,args);
    }

    public void print(String s){
        getPrintStream().print(s);
    }

    public void println(){
        getPrintStream().println();
    }

    public void println(String s){
        getPrintStream().println(s);
    }

    private PrintStream getPrintStream(){
        PrintStream console = BuildLoggerMap.get(parentProject,parentBuildNo);
        if (console == null) throw new JenkowException("console for build "+parentProject+"-#"+parentBuildNo+" unavailable");
        return console;
    }
}
